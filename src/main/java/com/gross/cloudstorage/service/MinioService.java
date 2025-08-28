package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.MinioDto;
import com.gross.cloudstorage.exception.*;
import com.gross.cloudstorage.mapper.MinioMapper;
import com.gross.cloudstorage.minio.MinioClientHelper;
import com.gross.cloudstorage.utils.PathUtils;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {
    private final MinioClientHelper minioClientHelper;
    private final Logger logger;
    String bucketName;


    public MinioService(MinioClientHelper minioClientHelper, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClientHelper = minioClientHelper;
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.bucketName = bucketName;

    }

   /* @PostConstruct
    public void init() {
        createBucket(bucketName);
    }*/


   /* public List<MinioObjectResponseDto> getUserDirectory(long userId, String path) {
        String fullPath = PathUtils.addUserPrefix(userId, path);
        PathUtils.validatePath(fullPath, true);
        if (isResourceExists(fullPath, true)) {
            return getDirectory(fullPath, userId);
        } else throw new ResourceNotFoundException("Директория по пути " + path + " не найдена");
    }*/

   /* public void createBucket(String bucketName) {
        try {
            if (minioClientHelper.createBucket(bucketName)) {
                logger.info("создался bucket - {}", bucketName);
            } else logger.info("bucket {} уже существует, создание отменено", bucketName);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("ошибка при создании bucket - {}", bucketName, e);
            throw new MinioServiceException("ошибка при создании bucket " + bucketName);
        }
    }*/




    public boolean isResourceExists(String path, boolean isDirectory) {
        try {
            return minioClientHelper.resourceExists(bucketName, path, isDirectory);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при проверке существования ресурса {}:", path, e);
            throw new MinioServiceException("Ошибка при проверке существования ресурса ");
        }
    }



    public void validateParentFoldersExist(String path, boolean createNonExistentFolders) {
        String[] parts = path.split("/");
        String name = parts[parts.length - 1];
        StringBuilder pathForValidation = new StringBuilder();
        pathForValidation.append(parts[0]).append("/");

        for (int i = 1; i < parts.length - 1; i++) {
            pathForValidation.append(parts[i]).append("/");
            if (!isResourceExists(pathForValidation.toString(),true)) {
                if (createNonExistentFolders) {
                    try {
                        minioClientHelper.createFolder(bucketName, pathForValidation.toString());
                    } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
                        throw new MinioServiceException("Ошибка при создании директории. ");
                    }
                } else throw new MissingParentFolderException("Не существует родительская директория");
            }
        }
        if (isResourceExists(pathForValidation.append(name).append("/").toString(),true)) {
            throw new DirectoryAlreadyExistsException("Директория по этому пути уже существует");
        }
    }

    public MinioDto createDirectory(long userId, String path, boolean isRootFolder) {
        String fullPath = PathUtils.addUserPrefix(userId, path);

        if (fullPath.split("/").length <= 1 && !isRootFolder) {
            throw new PathValidationException("Недопустимый путь для новой директории");
        }
        PathUtils.validatePath(fullPath, true);
        validateParentFoldersExist(fullPath, false);
        try {
            minioClientHelper.createFolder(bucketName, fullPath);
            return MinioMapper.toDtoJustForDirectory(fullPath, userId);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при создании папки {}:", path, e);
            throw new MinioServiceException("ошибка при создании папки " + path + e.getMessage());
        }
    }

    public List<MinioDto> getDirectory(String path, long userId) {
        Iterable<Result<Item>> folder = minioClientHelper.getFolder(bucketName, path);
        List<Item> filtered = findAndDeleteDirectoryEmptyFile(folder, path);

        return MinioMapper.toListDto(filtered, userId);

    }

    public boolean isDirectoryEmptyFile(Item item, String path) {
        return item.objectName().equals(path) && item.size() == 0 && item.objectName().endsWith("/");

    }

    public List<Item> findAndDeleteDirectoryEmptyFile(Iterable<Result<Item>> folder, String path) {
        List<Item> filtered = new ArrayList<>();
        try {
            for (Result<Item> result : folder) {
                Item item = result.get();
                if (isDirectoryEmptyFile(item, path)) {
                    continue;
                }
                filtered.add(item);
            }
            return filtered;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при фильтрации папки {}:", path, e);
            throw new MinioServiceException("Ошибка при фильтрации папки");
        }
    }


    public void deleteResource(long userId, String path) {
        String fullPath = PathUtils.addUserPrefix(userId, path);
        boolean isDirectory = path.endsWith("/");
        PathUtils.validatePath(fullPath,isDirectory);
        try {
            if (isResourceExists(path, isDirectory)) {
                throw new ResourceNotFoundException("По пути -" + PathUtils.stripUserPrefix(userId, path) + "не найден" +
                        (isDirectory ? "а директория" : "файл") + "для удаления");
            }

            if (isDirectory) {
                minioClientHelper.deleteDirectory(bucketName, fullPath);
            } else {
                minioClientHelper.deleteFile(bucketName, fullPath);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при удалении ресурса {}:", path, e);
            throw new MinioServiceException("Ошибка при удалении ресурса");
        }
    }

    /*public MinioObjectResponseDto getResourceInformation(long userId, String path) {
        boolean isDirectory = path.endsWith("/");
        PathUtils.validatePath(path, isDirectory);

        if (isResourceExists(path, isDirectory)) {
            throw new ResourceNotFoundException("По пути -" + PathUtils.stripUserPrefix(userId, path) + "не найден" +
                    (isDirectory ? "а директория" : "файл"));
        }

        try {
            return MinioMapper.toDtoFromStat(minioClientHelper.getStatObjectResponse(bucketName, path), userId);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при получении информации о ресурсе {}:", path, e);
            throw new MinioServiceException("Ошибка при получении информации о ресурсе");
        }
    }

    public MinioObjectResponseDto uploadResource(long userId, String path, MultipartFile object) {
        try {
            String fullPath = PathUtils.addUserPrefix(userId, path);
            PathUtils.validatePath(fullPath, true);
            if (!minioClientHelper.fileExists(bucketName, fullPath + object.getOriginalFilename())) {
                minioClientHelper.uploadFile(bucketName, fullPath, object);
                validateParentFoldersExist(fullPath + object.getOriginalFilename(), true);
                return MinioMapper.toDto(fullPath + object.getOriginalFilename(), object.getSize(), userId);
            } else throw new ResourceAlreadyExistsException("Файл с таким именем уже существует");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при загрузке ресурса на сервер ", e);
            throw new MinioServiceException("Ошибка при загрузке ресурса на сервер");
        }
    }

    public List<MinioObjectResponseDto> uploadResources(long userId, String path,
                                                        List<MultipartFile> objects) {
        List<MinioObjectResponseDto> uploaded = new ArrayList<>();
        for (MultipartFile object : objects) {
            uploaded.add(uploadResource(userId, path, object));
        }
        return uploaded;
    }*/

   /* public InputStream downloadResource(long userId, String path) {
        String fullPath = PathUtils.addUserPrefix(userId, path);
        boolean isDirectory = path.endsWith("/");
        PathUtils.validatePath(fullPath, isDirectory);

        if (isResourceExists(path, isDirectory)) {
            throw new ResourceNotFoundException("По пути -" + PathUtils.stripUserPrefix(userId, path) + "не найден" +
                    (isDirectory ? "а директория" : "файл") + "для скачивания");
        }

        if (isDirectory) {
            return downloadDirectory(path);
        } else return downloadFile(path);
    }

    public InputStream downloadFile(String path) {
        PathUtils.validatePath(path, false);
        try {
            return minioClientHelper.getObject(bucketName, path);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при скачивании файла ", e);
            throw new MinioServiceException("Ошибка при скачивании файла");
        }
    }

    public InputStream downloadDirectory(String path) {
        PathUtils.validatePath(path, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            Iterable<Result<Item>> results = minioClientHelper.getListObjects(bucketName, path, true);

            for (Result<Item> result : results) {
                Item item = result.get();
                if (isDirectoryEmptyFile(item, item.objectName())) {
                    continue;
                }
                String objectName = item.objectName();

                try (InputStream inputStream = minioClientHelper.getObject(bucketName, objectName)) {
                    zipOutputStream.putNextEntry(new ZipEntry(objectName.substring(path.length())));
                    inputStream.transferTo(zipOutputStream);
                    zipOutputStream.closeEntry();
                }
            }
            zipOutputStream.finish();
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при скачивании директории ", e);
            throw new MinioServiceException("Ошибка при скачивании директории");
        }
    }

*/
    /*public List<MinioObjectResponseDto> searchResources(long userId, String query) {
        query = query.trim();
        PathUtils.validateSearchRequest(query);
        List<MinioObjectResponseDto> searched = new ArrayList<>();
        Iterable<Result<Item>> results = minioClientHelper.getListObjects(bucketName, PathUtils.addUserPrefix(userId, ""), true);
        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                if (PathUtils.extractName(item.objectName()).toLowerCase()
                        .contains(query.toLowerCase())) {
                    searched.add(MinioMapper.toDto(item, userId));
                }
            }
            return searched;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при поисковом запросе ", e);
            throw new MinioServiceException("Ошибка при поисковом запросе");
        }
    }

    public MinioObjectResponseDto moveResource(long userId, String from, String to) {
        boolean isDirectory = from.endsWith("/");

        String fullFrom = PathUtils.addUserPrefix(userId, from);
        String fullTo = PathUtils.addUserPrefix(userId, to);

        PathUtils.validatePath(from, isDirectory);
        PathUtils.validatePath(to, isDirectory);

        if (!isResourceExists(fullFrom,isDirectory)) {
            throw new ResourceNotFoundException("Копируемый ресурс не найден");
        }
        if (isResourceExists(fullTo,isDirectory)) {
            throw new ResourceAlreadyExistsException("Ресурс по пути " + PathUtils.stripUserPrefix(userId, fullTo) + " уже существует. Операция невозможна.");
        }

        try {
            minioClientHelper.copyResource(bucketName, fullFrom, fullTo, isDirectory);
            deleteResource(userId, from);
            return getResourceInformation(userId, fullTo);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при перемещении или переименовании  ", e);
            throw new MinioServiceException("Ошибка при перемещении или переименовании");
        }
    }*/

}

