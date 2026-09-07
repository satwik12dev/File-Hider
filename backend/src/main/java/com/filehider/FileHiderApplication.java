package com.filehider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileHiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileHiderApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  CYPHERVAULT SPRING BOOT BACKEND STARTED");
        System.out.println("  Port          : 8080");
        System.out.println("  API Endpoint  : http://localhost:8080/api");
        System.out.println("  Health Status : http://localhost:8080/api/health");
        System.out.println("=================================================");
    }
}
