package com.gross.cloudstorage.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gross.cloudstorage.service.StorageProtectionService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.util.Map;


@Order(Ordered.HIGHEST_PRECEDENCE)
public class EarlySizeCheckFilter implements Filter {

    private final DataSize maxRequestSize;
    private final StorageProtectionService storageProtectionService;
    private static final Logger logger = LoggerFactory.getLogger(EarlySizeCheckFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int DISK_USAGE_MULTIPLIER_TMP_AND_STORAGE = 2;

    public EarlySizeCheckFilter(StorageProtectionService storageProtectionService, DataSize maxRequestSize) {
        this.storageProtectionService = storageProtectionService;
        this.maxRequestSize = maxRequestSize;
    }

    @Override
    public void doFilter(ServletRequest httpRequest, ServletResponse httpResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) httpRequest;
        HttpServletResponse response = (HttpServletResponse) httpResponse;

        if (!request.getMethod().equalsIgnoreCase("POST") ||
                !request.getRequestURI().equals("/api/resource")) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        long contentLength = request.getContentLengthLong();

        if (contentLength == -1) {
            logger.warn("Запрос без Content-Length заголовка отклонен");
            sendErrorResponse(response, "Заголовок Content-Length обязателен для загрузки файлов");
            return;
        }

        if (contentLength > maxRequestSize.toBytes()) {
            logger.warn("Размер запроса {} превысил допустимый размер - {}",
                    contentLength, maxRequestSize);
            sendErrorResponse(response, "Превышен максимальный размер запроса -" + maxRequestSize);
            return;
        }

        if (contentLength > 0) {
            boolean reserved = false;
            try {
                storageProtectionService.assertEnoughSpace(contentLength * DISK_USAGE_MULTIPLIER_TMP_AND_STORAGE);
                storageProtectionService.addReservedSpace(contentLength * DISK_USAGE_MULTIPLIER_TMP_AND_STORAGE);
                reserved = true;
                logger.info("Фильтр. Зарезервировано {} байт для загрузки", contentLength * DISK_USAGE_MULTIPLIER_TMP_AND_STORAGE);
            } catch (Exception e) {
                if (reserved) {
                    storageProtectionService.removeReservedSpace(contentLength * DISK_USAGE_MULTIPLIER_TMP_AND_STORAGE);
                    logger.info("Фильтр.Освобожден резерв {} байт из-за ошибки при резервировании", contentLength * DISK_USAGE_MULTIPLIER_TMP_AND_STORAGE);
                }
                return;
            }
        }

        try {
            request.setAttribute("contentLength", contentLength);
            chain.doFilter(httpRequest, httpResponse);
        } catch (Exception e) {
            if (contentLength > 0) {
                storageProtectionService.removeReservedSpace(contentLength * DISK_USAGE_MULTIPLIER_TMP_AND_STORAGE);
                logger.info("Фильтр. Освобожден резерв {} байт из-за ошибки в цепочке фильтров", contentLength);
            }
            throw e;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.CONFLICT.value());
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> errorResponse = Map.of("message", message);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }


}

