package com.file.filedemo.services;

import com.file.filedemo.domain.FileDeleteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
@Service
public class FileServiceImpl implements FileService {

    Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String uploadFile(String path, MultipartFile file) throws IOException {

        //File name
        String fileName = file.getOriginalFilename();
        logger.info("Got the file with name {}", fileName);

        /* Make the path till file -- Full Path */
        String filePath = path + File.separator + fileName;
        logger.info("File Path generated for current file is : {},", filePath);

        // Create folder if not already present
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
            logger.info("Created the directory {} as it is non-existing ", path);
        }
        // Copy the file to the directory created above or existing directory
        try {
            Files.copy(file.getInputStream(), Paths.get(filePath));
        } catch (FileAlreadyExistsException faee) {
            logger.error("File {} already exist", filePath);
            throw faee;
        }
        logger.info("File uploaded successfully");
        return fileName;
    }

    @Override
    public InputStream downloadFile(String path, String fileName) throws FileNotFoundException {
        logger.info("File {} is being downloaded", fileName);
        String fullPath = path + File.separator + fileName;
        InputStream inputStream = new FileInputStream(fullPath);
        logger.info("Successfully created the inout stream {} for the file", inputStream);
        return inputStream;
    }

    @Override
    public String[] listFiles(String path) {
//        Stream<Path> pathStream = Files.walk(Paths.get(path), 1).map(name -> Paths.get(path).relativize(name));
//        return pathStream.map(path1 -> path1.getFileName()).collect(Collectors.toList());
        File file = new File(path);
        return file.list();
    }

    @Override
    public List<FileDeleteResponse> deleteFiles(String path, String[] fileNames) {
        List<FileDeleteResponse> fileDeleteResponses = new ArrayList<>();
        for (String fileName : fileNames) {
            File fileToDelte = new File(path + File.separator + fileName);
            boolean success = fileToDelte.delete();
            if(success)
                fileDeleteResponses.add(new FileDeleteResponse(fileName, "DELETED", "User Request."));
            else
                fileDeleteResponses.add(new FileDeleteResponse(fileName, "NOT-DELETED", "File does not exists."));
        }
        return fileDeleteResponses;
    }
}
