package com.gross.cloudstorage.service.minio;

import com.gross.cloudstorage.exception.*;
import com.gross.cloudstorage.minio.MinioClientHelper;
import com.gross.cloudstorage.utils.PathUtils;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MinioDownloadService {
    private final MinioClientHelper minioClientHelper;
    private final MinioValidationService minioValidationService;
    private final String bucketName;
    private final Logger logger;

    private MinioDownloadService(MinioClientHelper minioClientHelper, MinioValidationService minioValidationService, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClientHelper = minioClientHelper;
        this.minioValidationService = minioValidationService;
        this.bucketName = bucketName;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public InputStream downloadResource(long userId, String path) {
        String fullPath = PathUtils.addUserPrefix(userId, path);
        boolean isDirectory=path.endsWith("/");
        PathUtils.validatePath(fullPath, isDirectory);

        if(minioValidationService.isResourceExists(path,isDirectory))
        {throw new ResourceNotFoundException("По пути -"+PathUtils.stripUserPrefix(userId, path)+"не найден"+
                (isDirectory?"а директория":"файл")+"для скачивания");}

        if(isDirectory){
            return downloadDirectory(fullPath);
        }else return downloadFile(fullPath);
    }

    private InputStream downloadFile(String path) {
        PathUtils.validatePath(path, false);
        try {
            return minioClientHelper.getObject(bucketName, path);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при скачивании файла ", e);
            throw new MinioServiceException("Ошибка при скачивании файла");
        }
    }

    private InputStream downloadDirectory(String path) {
        PathUtils.validatePath(path, true);

        try {
            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            Thread thread = new Thread(() -> createZipArchive(path, pipedOutputStream), "zip-thread");
            thread.start();
            return pipedInputStream;
        } catch (IOException e) {
            logger.error("Ошибка при подготовке скачивания архива", e);
            throw new MinioServiceException("Ошибка при подготовке скачивания архива");
        }
    }

    private void createZipArchive(String path, PipedOutputStream pipedOutputStream) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(pipedOutputStream)) {
            Iterable<Result<Item>> results = minioClientHelper.getListObjects(bucketName, path, true);
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                if (minioValidationService.isDirectoryEmptyFile(item, objectName)) {
                    continue;
                }

                try (InputStream inputStream = minioClientHelper.getObject(bucketName, objectName)) {
                    zipOutputStream.putNextEntry(new ZipEntry(objectName.substring(path.length())));
                    inputStream.transferTo(zipOutputStream);
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    logger.error("Ошибка при добавлении файла в zip архив", e);
                    throw new MinioServiceException("Ошибка при добавлении файла в zip архив");
                }
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            safeClose(pipedOutputStream);
            logger.error("Ошибка при архивации", e);
            throw new MinioServiceException("Ошибка при архивации");
        }
    }

    private void safeClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            logger.error("Ошибка при закрытии потока: ", e);
            throw new MinioServiceException("Ошибка при закрытии потока.");
        }

    }
}

