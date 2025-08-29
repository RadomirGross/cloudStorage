package com.gross.cloudstorage.service;

import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.StorageQuotaExceededException;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.messages.DataUsageInfo;
import io.minio.admin.messages.info.Disk;
import io.minio.admin.messages.info.Message;
import io.minio.admin.messages.info.ServerProperties;
import org.apache.catalina.util.ServerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


@Service
public class StorageProtectionService {
    @Value("${storage.reserved-bytes}")
    private long reservedBytes;
    private final MinioAdminClient minioAdminClient;

    public StorageProtectionService(MinioAdminClient minioAdminClient) {
        this.minioAdminClient = minioAdminClient;
    }

    public void validateUploadSpace(long size) {
        try {
            Message serverMessage = minioAdminClient.getServerInfo();
            List<ServerProperties> servers = serverMessage.servers();
            ServerProperties server = servers.get(0);
            List<Disk> allDisks = server.disks();
            Disk mydisk = allDisks.get(0);

            long availableSpace = mydisk.availspace().longValueExact();
            if (availableSpace - size < reservedBytes) {
                throw new StorageQuotaExceededException("Недостаточно места на сервере. Попробуйте позже.");
            }

        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new MinioServiceException("Ошибка при получении информации о хранилище.",e);
        }
    }


}
