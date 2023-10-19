package com.github.gepi.filestorage.controller;

import com.github.gepi.filestorage.dto.ErrorInfo;
import com.github.gepi.filestorage.dto.FileInfo;
import com.github.gepi.filestorage.exception.FileServiceException;
import com.github.gepi.filestorage.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/")
@Slf4j
public class FileOperationsController {
    private final FileService fileService;

    public FileOperationsController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/list/{*path}")
    public ResponseEntity<List<FileInfo>> getFiles(@PathVariable String path) {
        return ResponseEntity.ok().body(fileService.getFileList(path, true));
    }

    @PostMapping("/upload")
    @SneakyThrows
    public ResponseEntity<Void> uploadFile(@RequestParam MultipartFile file,
                                           @RequestParam String destinationFilePath,
                                           @RequestParam String destinationFileName) {
        fileService.save(file, destinationFilePath, destinationFileName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/delete/{*path}")
    @SneakyThrows
    public ResponseEntity<Void> delete(@PathVariable String path) {
        fileService.delete(path);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hardDelete/{*path}")
    @SneakyThrows
    public ResponseEntity<Void> hardDelete(@PathVariable String path) {
        fileService.hardDelete(path);
        return ResponseEntity.ok().build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    private ErrorInfo handleException(HttpServletRequest req, Exception ex) {
        log.error("General file processing error", ex);
        return new ErrorInfo(req.getRequestURL().toString(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FileServiceException.class)
    @ResponseBody
    private ErrorInfo handleApiException(HttpServletRequest req, Exception ex) {
        log.error("API processing error", ex);
        return new ErrorInfo(req.getRequestURL().toString(), ex.getMessage());
    }
}
