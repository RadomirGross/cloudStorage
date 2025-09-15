package com.gross.cloudstorage.interceptor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Component
public class ReactRouterInterceptor implements HandlerInterceptor {
    @Value("${web.interceptor.excluded-paths}")
    private List<String> excludedPaths;

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean isExcluded = excludedPaths.stream().anyMatch(path::startsWith);
        boolean hasExtension = path.contains(".");

        if (!isExcluded && !hasExtension) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return false;
        }
        return true;
    }
}
