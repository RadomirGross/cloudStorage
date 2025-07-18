package com.gross.cloudstorage.interceptor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Component
public class ReactRouterInterceptor implements HandlerInterceptor {
    private static final List<String> EXCLUDED_PATHS =
            List.of( "/api", "/static", "/favicon.ico", "/config.js"
            );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws ServletException, IOException {
        String path = request.getRequestURI();
        System.out.println("Работает интерсептор"+path);
        boolean isExcluded = EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
        boolean hasExtension = path.contains(".");

        if (!isExcluded && !hasExtension) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return false;
        }
        return true;
    }
}
