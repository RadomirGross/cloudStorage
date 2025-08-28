package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.MinioDto;
import com.gross.cloudstorage.service.minio.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
public class CloudStorageService {
    private final MinioDirectoryService minioDirectoryService;
    private final MinioDownloadService minioDownloadService;
    private final MinioUploadService minioUploadService;
    private final MinioResourceService minioResourceService;
    private final MinioValidationService minioValidationService;

    public CloudStorageService(MinioDirectoryService minioDirectoryService,
                               MinioDownloadService minioDownloadService,
                               MinioUploadService minioUploadService,
                               MinioResourceService minioResourceService,
                               MinioValidationService minioValidationService) {
        this.minioDirectoryService = minioDirectoryService;
        this.minioDownloadService = minioDownloadService;
        this.minioUploadService = minioUploadService;
        this.minioResourceService = minioResourceService;
        this.minioValidationService = minioValidationService;
    }

    public List<MinioDto> getUserDirectory(long userId, String path){
        return minioDirectoryService.getUserDirectory(userId, path);
    }

    public MinioDto createDirectory(long userId, String path,boolean isRootDirectory){
        return minioDirectoryService.createDirectory(userId,path,isRootDirectory);
    }

    public InputStream downloadResource(long userId, String path){
        return minioDownloadService.downloadResource(userId, path);
    }

    public MinioDto getResourceInformation(long userId, String path){
        return minioResourceService.getResourceInformation(userId, path);
    }

    public List<MinioDto> uploadResource(long userId, String path, List<MultipartFile> objects){
        return minioUploadService.uploadResource(userId, path, objects);
    }

    public void deleteResource(long userId, String path){
        minioResourceService.deleteResource(userId, path);
    }

    public MinioDto moveResource(long userId, String from, String to){
        return minioResourceService.moveResource(userId, from, to);
    }

    public List<MinioDto> searchResources(long userId, String query){
        return minioResourceService.searchResources(userId, query);
    }
}
