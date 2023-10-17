package com.github.gepi.filestorage.controller;

import com.github.gepi.filestorage.dto.ErrorInfo;
import com.github.gepi.filestorage.dto.FileInfo;
import com.github.gepi.filestorage.exception.FileServiceException;
import com.github.gepi.filestorage.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/")
@Slf4j
public class FileOperationsController {
    private final FileService fileService;

    public FileOperationsController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/files/{*path}")
    public ResponseEntity<List<FileInfo>> getFiles(@PathVariable String path) {
        return ResponseEntity.ok().body(fileService.getFileList(path, true));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file,
                                             @RequestParam String destinationFilePath,
                                             @RequestParam String destinationFileName) {
        try {
            return ResponseEntity.ok().body(fileService.upload(file, destinationFilePath, destinationFileName));
        } catch (IOException e) {
            throw new FileServiceException(e);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    private ErrorInfo handleException(HttpServletRequest req, Exception ex) {
        log.error("General file processing error", ex);
        return new ErrorInfo(req.getRequestURL().toString(), ex.getMessage());
    }
}
