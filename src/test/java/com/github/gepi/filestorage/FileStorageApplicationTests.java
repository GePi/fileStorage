package com.github.gepi.filestorage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gepi.filestorage.dto.FileInfo;
import com.github.gepi.filestorage.service.FileService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
class FileStorageApplicationTests {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private FileService fileService;

    @Test
    void contextLoads() {
    }

    @Test
    void callUpload() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Some file".getBytes());
        Map<String, String> params = new HashMap<>();
        params.put("destinationFilePath", "/111/112/");
        params.put("destinationFileName", "uploaded.txt");

        mvc.perform(
                        multipart("/upload")
                                .file(multipartFile)
                                .param((String) params.keySet().toArray()[0], (String) params.values().toArray()[0])
                                .param((String) params.keySet().toArray()[1], (String) params.values().toArray()[1]))
                .andExpect(status().isOk());

        then(this.fileService).should().save(multipartFile, (String) params.values().toArray()[1], (String) params.values().toArray()[0]);
    }

    @Test
    void callList() throws Exception {
        List<FileInfo> fileInfos = List.of(
                new FileInfo("/path/to/file.txt", "file.txt", 111),
                new FileInfo("/path/to/other/file.txt", "file.txt", 222));

        when(fileService.getFileList(anyString(), anyBoolean())).thenReturn(fileInfos);

        mvc.perform(get("/list/path"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(content().json(new ObjectMapper().writeValueAsString(fileInfos)));

        then(this.fileService).should().getFileList("/path", true);
    }

    @Test
    void callDelete() throws Exception {
        mvc.perform(get("/delete/path"))
                .andExpect(status().isOk());
        then(fileService).should().delete("/path");
    }

    @Test
    void callHardDelete() throws Exception {
        mvc.perform(get("/hardDelete/"))
                .andExpect(status().isOk());
        then(fileService).should().hardDelete("/");
    }
}