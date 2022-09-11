package com.file.filedemo.services;

import com.file.filedemo.domain.FileDeleteResponse;
import com.file.filedemo.entities.UserEntity;
import com.file.filedemo.repositories.UserRepository;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(properties = "file.path=testUploadFiles")
@SpringBootTest
class FileServiceTest {

    @InjectMocks
    FileServiceImpl fileService;

    @Value("${file.path}")
    String pathToUploadFolder;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    public void deleteFilesInTestUploadDir() throws IOException {

        File file = new File(pathToUploadFolder);
        FileUtils.cleanDirectory(file);
    }

    @Test
    public void test_uploadFiles() throws IOException {
        MockMultipartFile[] multipartFiles =
                new MockMultipartFile[]{new MockMultipartFile("files", "testFile1.png", "image/png", "Some bytes".getBytes()),
                        new MockMultipartFile("files", "testFile2.png", "image/png", "Some more bytes".getBytes())};
        fileService.uploadFile(pathToUploadFolder, multipartFiles[0]);
        fileService.uploadFile(pathToUploadFolder, multipartFiles[1]);
        File file = new File(pathToUploadFolder);
        File[] files = file.listFiles();
        List<String> fileNames = Arrays.stream(Objects.requireNonNull(files)).map(File::getName).collect(Collectors.toList());
        assertTrue(fileNames.contains("testFile1.png"));
        assertTrue(fileNames.contains("testFile2.png"));
        assertEquals("Some bytes",
                FileUtils.readFileToString(Arrays.stream(files)
                        .filter(f -> f.getName().equals("testFile1.png"))
                        .collect(Collectors.toList()).get(0), "utf-8"));

        userRepository.save(new UserEntity(123L, "Harish"));

    }

    @Test
    public void test_uploadFiles_DirectoryDoesNotExist() throws IOException {
        File directory = new File(pathToUploadFolder);
        final boolean delete = directory.delete();
        MockMultipartFile[] multipartFiles =
                new MockMultipartFile[]{new MockMultipartFile("files", "testFile1.png", "image/png", "Some bytes".getBytes()),
                        new MockMultipartFile("files", "testFile2.png", "image/png", "Some more bytes".getBytes())};
        fileService.uploadFile(pathToUploadFolder, multipartFiles[0]);
        fileService.uploadFile(pathToUploadFolder, multipartFiles[1]);
        File file = new File(pathToUploadFolder);
        File[] files = file.listFiles();
        List<String> fileNames = Arrays.stream(Objects.requireNonNull(files)).map(File::getName).collect(Collectors.toList());
        assertTrue(fileNames.contains("testFile1.png"));
        assertTrue(fileNames.contains("testFile2.png"));
        assertEquals("Some bytes",
                FileUtils.readFileToString(Arrays.stream(files)
                        .filter(f -> f.getName().equals("testFile1.png"))
                        .collect(Collectors.toList()).get(0), "utf-8"));
    }

    @Test
    public void test_uploadFiles_FileAlreadyExistException() throws IOException {
        MockMultipartFile[] multipartFiles =
                new MockMultipartFile[]{new MockMultipartFile("files", "testFile1.png", "image/png", "Some bytes".getBytes()),
                        new MockMultipartFile("files", "testFile2.png", "image/png", "Some more bytes".getBytes())};
        fileService.uploadFile(pathToUploadFolder, multipartFiles[0]);

        FileAlreadyExistsException fileAlreadyExistsException = Assertions.assertThrows(FileAlreadyExistsException.class,
                () -> fileService.uploadFile(pathToUploadFolder, multipartFiles[0]));

        assertEquals("testUploadFiles/testFile1.png", fileAlreadyExistsException.getLocalizedMessage());
    }

    @Test
    public void test_downloadFile() throws IOException {
        MockMultipartFile[] multipartFiles =
                new MockMultipartFile[]{new MockMultipartFile("files", "testFile1.png", "image/png", "Some bytes".getBytes()),
                        new MockMultipartFile("files", "testFile2.png", "image/png", "Some more bytes".getBytes())};
        fileService.uploadFile(pathToUploadFolder, multipartFiles[0]);
        fileService.uploadFile(pathToUploadFolder, multipartFiles[1]);
        assertEquals(FileInputStream.class,fileService.downloadFile(pathToUploadFolder, "testFile1.png").getClass());

    }

    @Test
    public void test_downloadFile_FileNotFoundException() throws IOException {
        MockMultipartFile[] multipartFiles =
                new MockMultipartFile[]{new MockMultipartFile("files", "testFile1.png", "image/png", "Some bytes".getBytes()),
                        new MockMultipartFile("files", "testFile2.png", "image/png", "Some more bytes".getBytes())};
        fileService.uploadFile(pathToUploadFolder, multipartFiles[0]);
        fileService.uploadFile(pathToUploadFolder, multipartFiles[1]);
        Assertions.assertThrows(FileNotFoundException.class, () -> fileService.downloadFile(pathToUploadFolder, "test"));

    }

    @Test
    public void test_ListFiles() throws IOException {
        MockMultipartFile[] multipartFiles =
                new MockMultipartFile[]{new MockMultipartFile("files", "testFile1.png", "image/png", "Some bytes".getBytes()),
                        new MockMultipartFile("files", "testFile2.png", "image/png", "Some more bytes".getBytes())};
        fileService.uploadFile(pathToUploadFolder, multipartFiles[0]);
        fileService.uploadFile(pathToUploadFolder, multipartFiles[1]);

        String[] strings = fileService.listFiles(pathToUploadFolder);
        assertTrue(Arrays.asList(strings).contains("testFile1.png"));

    }

    @Test
    public void test_DeleteFiles() throws IOException {
        MockMultipartFile[] multipartFiles =
                new MockMultipartFile[]{new MockMultipartFile("files", "testFile1.png", "image/png", "Some bytes".getBytes()),
                        new MockMultipartFile("files", "testFile2.png", "image/png", "Some more bytes".getBytes())};
        fileService.uploadFile(pathToUploadFolder, multipartFiles[0]);
        fileService.uploadFile(pathToUploadFolder, multipartFiles[1]);

        List<FileDeleteResponse> fileDeleteResponses = fileService.deleteFiles(pathToUploadFolder, new String[]{"testFile1.png", "testFile3.png"});
        assertTrue(fileDeleteResponses.stream().anyMatch(fileDeleteResponse -> fileDeleteResponse.getReason().equals("User Request.")));
        assertTrue(fileDeleteResponses.stream().anyMatch(fileDeleteResponse -> fileDeleteResponse.getReason().equals("File does not exists.")));

        File file = new File(pathToUploadFolder);
        File[] files = file.listFiles();

        assertEquals(1, (files != null ? files.length : 0));

    }
}