package com.gross.cloudstorage;

import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.minio.MinioClientHelper;
import com.gross.cloudstorage.service.StorageProtectionService;
import com.gross.cloudstorage.service.minio.MinioUploadService;
import com.gross.cloudstorage.service.minio.MinioValidationService;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class MinioUploadServiceTest {

    @Mock
    private StorageProtectionService storageProtectionService;

    @Mock
    private MinioClientHelper minioClientHelper;

    @Mock
    private MinioValidationService minioValidationService;

    @InjectMocks
    private MinioUploadService minioUploadService;

    @BeforeEach
    void setUp() {
        reset(storageProtectionService, minioClientHelper, minioValidationService);
        ReflectionTestUtils.setField(minioUploadService, "maxFileSize", DataSize.ofGigabytes(5));
    }
    @Test
    void shouldFreeReservedBytesOnUploadSuccess() {
        long contentLength = 2000L;
        List<MultipartFile> files = List.of(createMockFile("test.txt", 1000L));

        minioUploadService.uploadResource(1L, "user-1-files/test/", files, contentLength);

        verify(storageProtectionService).removeReservedSpace(contentLength * 2);
    }

    @Test
    void shouldFreeReservedBytesOnUploadException() throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        long contentLength = 2000L;
        List<MultipartFile> files = List.of(createMockFile("test.txt", 1000L));

        doThrow(new MinioServiceException("Ошибка MinIO"))
                .when(minioClientHelper).uploadFile(nullable(String.class), anyString(), any());

        assertThrows(MinioServiceException.class, () ->
                minioUploadService.uploadResource(1, "test/", files, contentLength));

        verify(storageProtectionService).removeReservedSpace(contentLength * 2);
    }

    private MultipartFile createMockFile(String filename, long size) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/plain",
                new byte[(int) size]
        );
    }
}
