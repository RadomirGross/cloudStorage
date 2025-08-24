package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.*;
import com.gross.cloudstorage.mapper.MinioMapper;
import com.gross.cloudstorage.minio.MinioClientHelper;
import com.gross.cloudstorage.utils.PathUtils;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    @PostConstruct
    public void init() {
        createBucket(bucketName);
    }

    public List<MinioObjectResponseDto> getUserFolder(long userId, String path) {
        return getDirectory(PathUtils.addUserPrefix(userId, path), userId);
    }

    public void createBucket(String bucketName) {
        try {
            if (minioClientHelper.createBucket(bucketName)) {
                logger.info("создался bucket - {}", bucketName);
            } else logger.info("bucket {} уже существует, создание отменено", bucketName);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("ошибка при создании bucket - {}", bucketName, e);
            throw new MinioServiceException("ошибка при создании bucket " + bucketName);
        }
    }


    public boolean isDirectoryExists(String path) {
        try {
            return minioClientHelper.folderExists(bucketName, path);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при проверке существования папки {}:", path, e);
            throw new MinioServiceException("Ошибка при проверке существования папки " + path + e.getMessage());
        }
    }

    public boolean isFileExists(String path) {
        try {
            return minioClientHelper.fileExists(bucketName, path);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при проверке существования файла {}:", path, e);
            throw new MinioServiceException("Ошибка при проверке существования файла " + path + e.getMessage());
        }
    }

    public void validateParentFoldersExist(String path, boolean createNonExistentFolders) {
        String[] parts = path.split("/");
        String name = parts[parts.length - 1];
        StringBuilder pathForValidation = new StringBuilder();
        pathForValidation.append(parts[0]).append("/");

        for (int i = 1; i < parts.length - 1; i++) {
            pathForValidation.append(parts[i]).append("/");
            if (!isDirectoryExists(pathForValidation.toString())) {
                if (createNonExistentFolders) {
                    try {
                        minioClientHelper.createFolder(bucketName, pathForValidation.toString());
                    } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
                        throw new MinioServiceException("ошибка при создании папки " + path + e.getMessage());
                    }
                } else throw new MissingParentFolderException("Не существует родительская директория");
            }
        }
        if (isDirectoryExists(pathForValidation.append(name).append("/").toString())) {
            throw new DirectoryAlreadyExistsException("Директория по этому пути уже существует");
        }

    }

    public MinioObjectResponseDto createDirectory(long userId, String path, boolean isRootFolder) {
        String fullPath = PathUtils.addUserPrefix(userId, path);
        try {
            if (fullPath.split("/").length <= 1 && !isRootFolder) {
                throw new PathValidationException("Недопустимый путь для новой директории");
            }
            PathUtils.validatePath(fullPath, true);
            validateParentFoldersExist(fullPath, false);
            minioClientHelper.createFolder(bucketName, fullPath);
            return MinioMapper.toDtoMinioObjectJustForDirectory(fullPath, userId);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при создании папки {}:", path, e);
            throw new MinioServiceException("ошибка при создании папки " + path + e.getMessage());
        }
    }

    public List<MinioObjectResponseDto> getDirectory(String path, long userId) {
        Iterable<Result<Item>> folder = minioClientHelper.getFolder(bucketName, path);
        List<Item> filtered = findAndDeleteDirectoryEmptyFile(folder, path);

        return MinioMapper.toListDtoMinioObject(filtered, userId);

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
        PathUtils.validatePathToDeleteResource(fullPath);
        try {
            if (isDirectory) {
                if (isDirectoryExists(fullPath)) {
                    minioClientHelper.deleteFolder(bucketName, fullPath);
                } else throw new ResourceNotFoundException("Нельзя удалить несуществующую директорию");
            } else {
                if (isFileExists(fullPath)) {
                    minioClientHelper.deleteFile(bucketName, fullPath);
                } else throw new ResourceNotFoundException("Нельзя удалить несуществующий файл");
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при удалении ресурса {}:", path, e);
            throw new MinioServiceException("Ошибка при удалении ресурса");
        }
    }

    public MinioObjectResponseDto getResourceInformation(long userId, String path) {
        boolean isDirectory = path.endsWith("/");
        PathUtils.validatePath(path, isDirectory);
        if (isDirectory) {
            if (!isDirectoryExists(path)) {
                throw new ResourceNotFoundException("Ресурс не найден");
            }
        } else if (!isFileExists(path)) {
            throw new ResourceNotFoundException("Ресурс не найден");
        }
        try {
            return MinioMapper.toDtoMinioObjectFromStat(minioClientHelper.getStatObjectResponse(bucketName, path), userId);
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
                System.out.println("uploadResource!!! " + fullPath + object.getOriginalFilename());
                validateParentFoldersExist(fullPath + object.getOriginalFilename(), true);
                return MinioMapper.toDtoMinioObject(fullPath + object.getOriginalFilename(), object.getSize(), userId);
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
    }

    public InputStream downloadResource(long userId, String path) {
        String fullPath = PathUtils.addUserPrefix(userId, path);
        System.out.println("downloadResource!!! " + fullPath);
        if(path.endsWith("/")) {
            if (isDirectoryExists(fullPath))
            {return downloadDirectory(fullPath);}
            else throw new ResourceNotFoundException("Директории для скачивания по пути "+fullPath+" не существует");
        }else  {
            if (isFileExists(fullPath))
            {return downloadFile(fullPath);}
            else throw new ResourceNotFoundException("Файла для скачивания по пути "+fullPath+" не существует");
        }
    }

    public InputStream downloadFile(String path) {
        System.out.println("downloadFile!!! " + path);
        PathUtils.validatePath(path, false);
        try {
            return minioClientHelper.getObject(bucketName, path);}
         catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при скачивании файла ", e);
            throw new MinioServiceException("Ошибка при скачивании файла");
        }
    }

    public InputStream downloadDirectory(String path) {
        System.out.println("downloadDirectory!!! " + path);
        PathUtils.validatePath(path, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)){
        Iterable<Result<Item>> results = minioClientHelper.getListObjects(bucketName, path, true);
            System.out.println("ResultSize ");
            for (Result<Item> result : results) {
                System.out.println(result.get().objectName());
            }
            for (Result<Item> result : results) {
                Item item = result.get();
                if (isDirectoryEmptyFile(item, item.objectName())) {
                    continue;
                }
                String objectName = item.objectName();

                try(InputStream inputStream = minioClientHelper.getObject(bucketName, objectName))
                {
                zipOutputStream.putNextEntry(new ZipEntry(objectName.substring(path.length())));
                    System.out.println("downloadDirectory objectName "+objectName.substring(path.length()));
                inputStream.transferTo(zipOutputStream);
                zipOutputStream.closeEntry();}
            }
            zipOutputStream.finish();
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при скачивании директории ", e);
            throw new MinioServiceException("Ошибка при скачивании директории");
        }
    }


}

