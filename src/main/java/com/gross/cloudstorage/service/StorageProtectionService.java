package com.gross.cloudstorage.service;

import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.StorageQuotaExceededException;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.messages.info.Disk;
import io.minio.admin.messages.info.Message;
import io.minio.admin.messages.info.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class StorageProtectionService {

    private final AtomicLong reservedSpace = new AtomicLong(0);
    private final MinioAdminClient minioAdminClient;
    Logger logger = LoggerFactory.getLogger(StorageProtectionService.class);

    public StorageProtectionService(MinioAdminClient minioAdminClient, @Value("${storage.reserved-bytes}") long reservedBytes) {
        this.minioAdminClient = minioAdminClient;
        this.reservedSpace.set(reservedBytes);
    }

    public void addReservedSpace(long size) {
        reservedSpace.addAndGet(size);
        logger.info("Добавлено резервное место на {} bytes. Всего зарезервировано: {} bytes. Всего доступно на сервере: {} MB" , size, reservedSpace.get(),availableSpace()/1024/1024);
    }

    public void removeReservedSpace(long size) {
        reservedSpace.addAndGet(-size);
        logger.info("Удалено резервное место на {} bytes. Всего зарезервировано: {} bytes. Всего доступно на сервере: {} MB", size, reservedSpace.get(),availableSpace()/1024/1024);
    }

    public boolean hasEnoughSpace(long objectSize) {
        long available = availableSpace();
        long needed = reservedSpace.get() + objectSize;
        return available >= needed;
    }

    public void assertEnoughSpace(long objectSize) {
        if (!hasEnoughSpace(objectSize)) {
            throw new StorageQuotaExceededException(
                    String.format("Недостаточно места: требуется %d МБ, доступно %d МБ (резерв %d МБ).",
                            objectSize/1024/1024, (availableSpace()-reservedSpace.get())/1024/1024, reservedSpace.get()/1024/1024)
            );
        }
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
