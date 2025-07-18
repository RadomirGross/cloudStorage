package com.gross.cloudstorage.config;

import com.gross.cloudstorage.interceptor.ReactRouterInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

private final ReactRouterInterceptor reactRouterInterceptor;

public WebConfig(ReactRouterInterceptor reactRouterInterceptor) {
    this.reactRouterInterceptor = reactRouterInterceptor;
}


  /*  @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(reactRouterInterceptor);
    }*/
}