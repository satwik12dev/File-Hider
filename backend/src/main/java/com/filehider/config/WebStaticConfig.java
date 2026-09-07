package com.filehider.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebStaticConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve frontend directory relative to project root
        String frontendPath = Paths.get("../frontend").toAbsolutePath().normalize().toUri().toString();
        if (!new File(Paths.get("../frontend").toAbsolutePath().normalize().toString()).exists()) {
            frontendPath = Paths.get("frontend").toAbsolutePath().normalize().toUri().toString();
        }

        registry.addResourceHandler("/**")
                .addResourceLocations(frontendPath + "/", "classpath:/static/", "classpath:/public/")
                .setCachePeriod(0);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
