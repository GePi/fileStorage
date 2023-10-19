package com.github.gepi.filestorage;

import com.github.gepi.filestorage.service.FileService;
import com.github.gepi.filestorage.service.FileServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FileStorageApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileStorageApplication.class, args);
    }

    @Bean
    public FileService createFileServiceBean(@Value("${fileService.rootAbsolutePath}") String rootAbsolutePathString) {
        return new FileServiceImpl(rootAbsolutePathString);
    }
}
