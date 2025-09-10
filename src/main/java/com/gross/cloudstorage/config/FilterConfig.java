package com.gross.cloudstorage.config;

import com.gross.cloudstorage.filter.EarlySizeCheckFilter;
import com.gross.cloudstorage.service.StorageProtectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.unit.DataSize;

@Configuration
public class FilterConfig {


    @Bean
    public FilterRegistrationBean<EarlySizeCheckFilter> earlySizeCheckFilter(
            StorageProtectionService storageProtectionService,
            @Value("${spring.servlet.multipart.max-request-size}") DataSize maxRequestSize) {

        EarlySizeCheckFilter filter = new EarlySizeCheckFilter(storageProtectionService, maxRequestSize);

        FilterRegistrationBean<EarlySizeCheckFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/resource");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
