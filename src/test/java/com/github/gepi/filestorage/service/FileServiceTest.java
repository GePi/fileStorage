package com.github.gepi.filestorage.service;

import com.github.gepi.filestorage.dto.FileInfo;
import com.github.gepi.filestorage.exception.DirectoryIsNotEmpty;
import com.github.gepi.filestorage.exception.FileIsEmpty;
import com.github.gepi.filestorage.exception.PathNotExist;
import com.github.gepi.filestorage.exception.ViolationBoundariesRootDirectory;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileServiceTest {

    @Test
    public void testSave_filePresentInFS() throws IOException {
        String destinationFilePath = "PATHPART1/PATHPART2";
        String destinationFileName = "1.txt";
        String targetFilePath = "/PATHPART1/PATHPART2/1.txt";
        String fileContent = "Content0";
        String fileRewritingContent1 = "Content1";

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            FileService fileService = new FileServiceImpl(fileSystem.getPath("/"));
            MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", fileContent.getBytes());
            fileService.save(multipartFile, destinationFilePath, destinationFileName);

            assertTrue(Files.exists(fileSystem.getPath(targetFilePath)));
            assertEquals(fileContent, Files.readString(fileSystem.getPath(targetFilePath)));

            multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", fileRewritingContent1.getBytes());
            fileService.save(multipartFile, destinationFilePath, destinationFileName);
            assertEquals(fileRewritingContent1, Files.readString(fileSystem.getPath(targetFilePath)));
        }
    }

    @Test
    public void testSave_thrownFileIsEmpty() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            FileService fileService = new FileServiceImpl(fileSystem.getPath("/"));
            MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "".getBytes());
            assertThrows(FileIsEmpty.class, () -> fileService.save(multipartFile, "PATHPART1/PATHPART2", "1.txt"));
        }
    }

    @Test
    public void testGetAbsoluteResolvedWithRoot_thrownViolationBoundariesRootDirectory() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Files.createDirectories(fileSystem.getPath("/composite/storage/path"));
            FileServiceImpl fileService = new FileServiceImpl(fileSystem.getPath("/composite/storage/path"));
            assertThrows(ViolationBoundariesRootDirectory.class, () -> fileService.getAbsoluteResolvedWithRoot("../../"));
        }
    }

    @Test
    public void testGetFileList_returnFileInfoList() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            prepareFiles(fileSystem);

            FileService fileService = new FileServiceImpl(fileSystem.getPath("/"));
            List<FileInfo> fileList = fileService.getFileList("/", true);

            assertEquals(6, fileList.size());
            assertEquals(6,
                    fileList.stream().filter(fileInfo ->
                            (fileInfo.getFileName().equals("testroot.txt") &&
                                    fileInfo.getFullFileName().equals("testroot.txt") &&
                                    fileInfo.getSize() == "testroot.txt".getBytes().length) ||

                                    (fileInfo.getFileName().equals("test1.txt") &&
                                            fileInfo.getFullFileName().equals("dir1/test1.txt") &&
                                            fileInfo.getSize() == "test1.txt".getBytes().length) ||

                                    (fileInfo.getFileName().equals("test2.txt") &&
                                            fileInfo.getFullFileName().equals("dir1/dir2/test2.txt") &&
                                            fileInfo.getSize() == "test2.txt".getBytes().length) ||

                                    (fileInfo.getFileName().equals("test22.txt") &&
                                            fileInfo.getFullFileName().equals("dir1/dir2/test22.txt") &&
                                            fileInfo.getSize() == "test22.txt".getBytes().length) ||

                                    (fileInfo.getFileName().equals("test3.txt") &&
                                            fileInfo.getFullFileName().equals("dir3/test3.txt") &&
                                            fileInfo.getSize() == "test3.txt".getBytes().length) ||

                                    (fileInfo.getFileName().equals("test8.txt") &&
                                            fileInfo.getFullFileName().equals("dir7/dir8/test8.txt") &&
                                            fileInfo.getSize() == "test8.txt".getBytes().length)
                    ).count());
        }
    }

    @Test
    public void testDelete_resultFileDeleted() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            prepareFiles(fileSystem);

            FileService fileService = new FileServiceImpl(fileSystem.getPath("/"));

            fileService.delete("/dir3/test3.txt");
            assertFalse(Files.exists(fileSystem.getPath("/dir3/test3.txt")));
        }
    }

    @Test
    public void testDelete_resultDirectoryDeleted() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            prepareFiles(fileSystem);

            FileService fileService = new FileServiceImpl(fileSystem.getPath("/"));

            assertTrue(Files.exists(fileSystem.getPath("/dir5/dir6")));
            fileService.delete("/dir5");
            assertFalse(Files.exists(fileSystem.getPath("/dir5")));

            fileService.delete("/dir7/dir8/test8.txt");
            assertTrue(Files.exists(fileSystem.getPath("/dir7/dir8")));
            fileService.delete("/dir7/dir8");
            assertFalse(Files.exists(fileSystem.getPath("/dir7/dir8")));
            assertTrue(Files.exists(fileSystem.getPath("/dir7")));
        }
    }

    @Test
    public void testDelete_thrownDirectoryNotEmpty() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            prepareFiles(fileSystem);
            FileService fileService = new FileServiceImpl(fileSystem.getPath("/"));

            assertThrows(DirectoryIsNotEmpty.class, () -> fileService.delete("/dir3"));
            assertThrows(DirectoryIsNotEmpty.class, () -> fileService.delete("/dir1/dir2"));
            assertThrows(DirectoryIsNotEmpty.class, () -> fileService.delete("/dir7"));
        }
    }

    @Test
    public void testDelete_thrownPathNotExist() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            prepareFiles(fileSystem);

            FileService fileService = new FileServiceImpl(fileSystem.getPath("/"));

            fileService.delete("/dir3/test3.txt");
            assertThrows(PathNotExist.class, () -> fileService.delete("/dir3/test3.txt"));
            assertThrows(PathNotExist.class, () -> fileService.delete("/dir100500"));
        }
    }

    private static void prepareFiles(FileSystem fileSystem) throws IOException {
        Files.createDirectories(fileSystem.getPath("/dir1/dir2"));
        Files.createDirectory(fileSystem.getPath("/dir3"));
        Files.createDirectory(fileSystem.getPath("/dir4"));
        Files.createDirectories(fileSystem.getPath("/dir5/dir6")); //empty
        Files.createDirectories(fileSystem.getPath("/dir7/dir8"));

        Files.write(fileSystem.getPath("/testroot.txt"), "testroot.txt".getBytes());
        Files.write(fileSystem.getPath("/dir1/test1.txt"), "test1.txt".getBytes());
        Files.write(fileSystem.getPath("/dir1/dir2/test2.txt"), "test2.txt".getBytes());
        Files.write(fileSystem.getPath("/dir1/dir2/test22.txt"), "test22.txt".getBytes());
        Files.write(fileSystem.getPath("/dir3/test3.txt"), "test3.txt".getBytes());
        Files.write(fileSystem.getPath("/dir7/dir8/test8.txt"), "test8.txt".getBytes());

    }
}

