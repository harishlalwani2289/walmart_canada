package com.file.FileDemo.services;

import com.file.FileDemo.domain.FileDeleteResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public interface FileService {

     String uploadFile(String path, MultipartFile file) throws IOException;
     InputStream downloadFile(String path, String fileName) throws FileNotFoundException;
     String[] listFiles(String path) throws IOException;
     List<FileDeleteResponse> deleteFiles(String path, String[] fileNames);
}
