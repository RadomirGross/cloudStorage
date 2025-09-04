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
import java.util.concurrent.atomic.AtomicLong;


@Service
public class StorageProtectionService {
    @Value("${storage.reserved-bytes}")
    private long reservedBytes;
    private final AtomicLong reservedSpace = new AtomicLong(reservedBytes);
    private final MinioAdminClient minioAdminClient;

    public StorageProtectionService(MinioAdminClient minioAdminClient) {
        this.minioAdminClient = minioAdminClient;
    }

    public void addReservedSpace(long size) {
        reservedSpace.addAndGet(size);
    }

    public void removeReservedSpace(long size) {
        reservedSpace.addAndGet(-size);
    }

    public boolean hasEnoughSpace(long objectSize) {
        long availableSpace = availableSpace();
        if (availableSpace - objectSize < reservedSpace.get()) {
            throw new StorageQuotaExceededException("Недостаточно места на сервере. Попробуйте позже.");
        }
        return true;
    }

    public long availableSpace() {
        try {
            Message serverMessage = minioAdminClient.getServerInfo();
            List<ServerProperties> servers = serverMessage.servers();
            ServerProperties server = servers.get(0);
            List<Disk> allDisks = server.disks();
            Disk mydisk = allDisks.get(0);

            return mydisk.availspace().longValueExact();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new MinioServiceException("Ошибка при получении информации о хранилище.", e);
        }
    }


}
