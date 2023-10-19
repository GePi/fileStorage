package com.github.gepi.filestorage.service;

import com.github.gepi.filestorage.dto.FileInfo;
import com.github.gepi.filestorage.exception.DirectoryIsNotEmpty;
import com.github.gepi.filestorage.exception.PathNotExist;
import com.github.gepi.filestorage.exception.FileIsEmpty;
import com.github.gepi.filestorage.exception.ViolationBoundariesRootDirectory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


@Slf4j
public class FileServiceImpl implements FileService {
    private final Path root;

    public FileServiceImpl(String rootAbsolutePathString) {
        this(Path.of(Objects.requireNonNull(rootAbsolutePathString)));
    }

    public FileServiceImpl(Path rootAbsolutePath) {
        if (!Files.exists(rootAbsolutePath)) {
            throw new IllegalArgumentException("The root path does not exist");
        }
        this.root = rootAbsolutePath;
    }

    @Override
    public List<FileInfo> getFileList(String pathString, Boolean deepDive) {
        log.debug("getFileList {}, {}", pathString, deepDive);

        List<FileInfo> fileInfos = new ArrayList<>();
        Path path = getAbsoluteResolvedWithRoot(removeFirstSlash(pathString));

        if (!Files.exists(path)) {
            return fileInfos;
        }
        try (Stream<Path> pathStream = Files.walk(path, deepDive ? Integer.MAX_VALUE : 0)) {
            fileInfos = pathStream.filter(Files::isRegularFile)
                    .map(path1 -> new FileInfo(path.relativize(path1), fileSizeOrException(path1)))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileInfos;
    }

    @Override
    public Path save(MultipartFile uploadedFile, String destinationFilePath, String destinationFileName) throws IOException {
        log.debug("upload {}, {}, {}, {}", uploadedFile.getOriginalFilename(), uploadedFile.getSize(), destinationFileName, destinationFileName);

        if (uploadedFile.isEmpty()) {
            throw new FileIsEmpty();
        }

        Path targetPath = getAbsoluteResolvedWithRoot(removeFirstSlash(destinationFilePath));

        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        targetPath = targetPath.resolve(destinationFileName);

        try (var uploadedFileInputStream = uploadedFile.getInputStream()) {
            Files.copy(uploadedFileInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return targetPath;
    }

    @Override
    public void delete(String requestPath) throws IOException {
        log.debug("delete {}", requestPath);

        Path path = getAbsoluteResolvedWithRoot(removeFirstSlash(requestPath));
        if (Files.isDirectory(path) && hasFiles(path)) {
            throw new DirectoryIsNotEmpty();
        }
        if (Files.notExists(path)) {
            throw new PathNotExist();
        }
        FileSystemUtils.deleteRecursively(path);
    }

    @Override
    public void hardDelete(String requestPath) throws IOException {
        log.debug("hardDelete {}", requestPath);

        Path path = getAbsoluteResolvedWithRoot(removeFirstSlash(requestPath));
        FileSystemUtils.deleteRecursively(path);
    }

    protected Path getAbsoluteResolvedWithRoot(String relativePathString) {
        Path path = root.resolve(relativePathString).normalize().toAbsolutePath();

        if (!path.startsWith(root)) {
            throw new ViolationBoundariesRootDirectory();
        }
        return path;
    }

    private boolean hasFiles(Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new RuntimeException("Path is not directory");
        }

        try (Stream<Path> fileStream = Files.walk(path)) {
            return fileStream.anyMatch(Files::isRegularFile);
        }
    }

    private String removeFirstSlash(String pathString) {
        return pathString.startsWith("/") ? pathString.substring(1) : pathString;
    }

    private long fileSizeOrException(Path path1) {
        long size;
        try {
            size = Files.size(path1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return size;
    }
}
