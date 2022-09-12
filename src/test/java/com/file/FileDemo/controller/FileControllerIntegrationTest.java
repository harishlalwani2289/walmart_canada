package com.file.FileDemo.controller;


import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"file.path=testUploadFiles"})
class FileControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Value("${file.path}")
    String pathToUploadFolder;

    MockMultipartFile[] multipartFiles;

    @BeforeEach
    public void prepareMultipartFiles() {
        multipartFiles =
                new MockMultipartFile[]{new MockMultipartFile("files", "testFile1.png", "image/png", "Some bytes".getBytes()),
                        new MockMultipartFile("files", "testFile2.png", "image/png", "Some more bytes".getBytes())};
    }

    @AfterEach
    public void deleteFilesInTestUploadDir() throws IOException {

        File file = new File(pathToUploadFolder);
        FileUtils.cleanDirectory(file);
    }

    @Test
    void test_uploadFiles_WithSuccess_AndErrorAfterRepeating() throws Exception {


        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/files/upload");
        // Testing file upload
        mockMvc.perform(multipartRequest.file(multipartFiles[0]).file(multipartFiles[1]))
//                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.ACCEPTED.value()));
                .andExpect(status().isAccepted())
                .andExpect(content()
                        .json("{\"fileNames\":\"[testFile1.png, testFile2.png]\",\"message\":\" Files are successfully uploaded\"}"));

        // checking the file is actually uploaded
        File file = new File(pathToUploadFolder);
        File[] files = file.listFiles();
        assert files != null;
        List<String> fileNames = Arrays.stream(files).map(File::getName).collect(Collectors.toList());
        Assertions.assertTrue(fileNames.contains("testFile1.png"));
        Assertions.assertTrue(fileNames.contains("testFile2.png"));
        Assertions.assertEquals("Some bytes",
                FileUtils.readFileToString(Arrays.stream(files)
                        .filter(f -> f.getName().equals("testFile1.png"))
                        .collect(Collectors.toList()).get(0), "utf-8"));


        // If we test this again we should get 500 internal server error
        mockMvc.perform(multipartRequest.file(multipartFiles[0]).file(multipartFiles[1]))
//                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.ACCEPTED.value()));
                .andExpect(status().is5xxServerError())
                .andExpect(content().json("{\"fileNames\":\"testFile1.png\",\"message\":\"File with provided name already uploaded\"}"));
    }

//    @Test
//    public void test_uploadFiles_FileSizeExceedException() throws Exception {
//
//
//        MockMultipartHttpServletRequestBuilder multipartRequest =
//                MockMvcRequestBuilders.multipart("/files/upload");
//        // Testing file upload
//
//        System.out.println(multipartFiles[0].getSize());
//        byte[] bytes = new byte[100000];
//        Arrays.fill(bytes, 0,bytes.length-1, (byte)110);
//        System.out.println(bytes);
//        MockMultipartFile multipartFile = new MockMultipartFile("files", "test", "image/jpeg", bytes);
//        mockMvc.perform(multipartRequest.file(multipartFile))
////                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.ACCEPTED.value()));
//                .andExpect(status().is5xxServerError());
//    }

    @Test
    public void test_deleteFiles() throws Exception {
        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/files/upload");
        // Testing file upload
        mockMvc.perform(multipartRequest.file(multipartFiles[0]).file(multipartFiles[1]))
//                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.ACCEPTED.value()));
                .andExpect(status().isAccepted());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.delete("/files");
        mockMvc.perform(mockHttpServletRequestBuilder
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("[\"testFile2.png\", \"testFile1.png\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"fileName\":\"testFile2.png\",\"status\":\"DELETED\",\"reason\":\"User Request.\"},{\"fileName\":\"testFile1.png\"," +
                        "\"status\":\"DELETED\",\"reason\":\"User Request.\"}]"));
    }

    @Test
    public void test_deleteFiles_WithNonExistingFile() throws Exception {
        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/files/upload");
        // Testing file upload
        mockMvc.perform(multipartRequest.file(multipartFiles[0]).file(multipartFiles[1]))
//                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.ACCEPTED.value()));
                .andExpect(status().isAccepted());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.delete("/files");
        mockMvc.perform(mockHttpServletRequestBuilder
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("[\"testFile2.png\", \"testFile3.png\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"fileName\":\"testFile2.png\",\"status\":\"DELETED\",\"reason\":\"User Request.\"}," +
                        "{\"fileName\":\"testFile3.png\",\"status\":\"NOT-DELETED\",\"reason\":\"File does not exists.\"}]"));
    }

    @Test
    public void test_ListFiles() throws Exception {


        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/files/upload");
        // Testing file upload
        mockMvc.perform(multipartRequest.file(multipartFiles[0]).file(multipartFiles[1]))
//                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.ACCEPTED.value()));
                .andExpect(status().isAccepted());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get("/files");
        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk())
                .andExpect(content().json("[{\"fileName\":\"testFile2.png\",\"url\":\"http://localhost/files/testFile2.png\"}," +
                        "{\"fileName\":\"testFile1.png\",\"url\":\"http://localhost/files/testFile1.png\"}]"));
    }

    @Test
    public void testDownloadFile() throws Exception {

        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/files/upload");
        // Testing file upload
        mockMvc.perform(multipartRequest.file(multipartFiles[0]).file(multipartFiles[1]))
//                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.ACCEPTED.value()));
                .andExpect(status().isAccepted());
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get("/files/testFile1.png");
        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.ALL))
                .andExpect(content().bytes("Some bytes".getBytes()));
    }

    @Test
    public void testDownloadFile_NotExisting() throws Exception {

        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/files/upload");
        // Testing file upload
        mockMvc.perform(multipartRequest.file(multipartFiles[0]).file(multipartFiles[1]))
//                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.ACCEPTED.value()));
                .andExpect(status().isAccepted());
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get("/files/testFile3.png");
        mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("{\"fileNames\":\"testFile3.png\",\"message\":\"File does not exists.\"}"));
    }
}