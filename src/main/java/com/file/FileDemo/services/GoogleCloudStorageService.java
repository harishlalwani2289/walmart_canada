package com.file.FileDemo.services;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class GoogleCloudStorageService {

    @Autowired
    private Storage storage;

    public String uploadFilesToGoogleCloud(MultipartFile file) throws IOException {
        String bucketName = "file-demo-uploads";
        File filetoUpload = convertMultipartFileToFile(file);
        Blob blob = storage.create(
                BlobInfo.newBuilder(bucketName, getFileName(Objects.requireNonNull(file.getOriginalFilename())))
                        .setContentType("image/jpeg")
                        .setAcl(new ArrayList<>(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
                        .build(), Files.readAllBytes(Path.of(filetoUpload.getPath()))
        );
        return blob.getMediaLink();
    }

    private String getFileName(String originalFilename) {
        return originalFilename.substring(0, originalFilename.lastIndexOf(".") + 1)
                + new SimpleDateFormat("dd-MMM-yyyy-hhmmss").format(new Date())
                + originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(convertedFile);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.close();
        return convertedFile;

    }

}
