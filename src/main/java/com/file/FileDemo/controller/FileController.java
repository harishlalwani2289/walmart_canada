package com.file.FileDemo.controller;

import com.file.FileDemo.domain.FileDeleteResponse;
import com.file.FileDemo.domain.FileResponse;
import com.file.FileDemo.domain.FileUploadResponse;
import com.file.FileDemo.domain.ListFileResponse;
import com.file.FileDemo.services.FileService;
import com.file.FileDemo.services.GoogleCloudStorageService;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(value = "/files")
@CrossOrigin(methods = {GET, POST, PATCH, PUT})
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private GoogleCloudStorageService googleCloudStorageService;

    @Autowired
    private Storage storage;

    @Value("${file.path}")
    private String path;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload files.")
    public ResponseEntity<List<FileUploadResponse>> uploadFiles(
            @RequestParam("files") @ApiParam(value = "Select files to upload", required = true, name = "files") List<MultipartFile> files)
            throws IOException {
        log.info("Request received to upload files [{}]", files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList()));
        List<FileUploadResponse> fileUploadResponseList = new ArrayList<>();
        for (MultipartFile file : files) {
            String mediaLinkForFile;
            try {
                fileService.uploadFile(path, file);
                mediaLinkForFile = googleCloudStorageService.uploadFilesToGoogleCloud(file);
                fileUploadResponseList.add(FileUploadResponse.builder()
                        .fileName(file.getOriginalFilename())
                        .mediaLink(mediaLinkForFile)
                        .uploadStatus("UPLOADED")
                        .reason("User Request")
                        .build());
            } catch (FileAlreadyExistsException fileAlreadyExistsException) {
                log.error("File already exists in the path");
                fileUploadResponseList.add(FileUploadResponse.builder()
                                .fileName(file.getOriginalFilename())
                                .mediaLink("")
                                .uploadStatus("NOT-UPLOADED")
                                .reason("File Already Exist.")
                        .build());
            } catch (IOException e) {
                fileUploadResponseList.add(FileUploadResponse.builder()
                        .fileName(file.getOriginalFilename())
                        .mediaLink("")
                        .uploadStatus("NOT-UPLOADED")
                        .reason("Server Error")
                        .build());
            }
        }
        // After uploading it to local storage we will try to upload it inot google cloud storage
        for (MultipartFile file : files) {
            String mediaLinkForFile = googleCloudStorageService.uploadFilesToGoogleCloud(file);
            log.info("Media link for file :{}", mediaLinkForFile);
        }
        return new ResponseEntity<>(fileUploadResponseList,
                HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/gcp/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload files.")
    public String uploadFilesToGoogleCloud() throws IOException {
        BlobId blobId = BlobId.of("file-demo-uploads", "sample1.txt");
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        File fileToRead = new File("/Users/h0l07o1/Desktop", "sample.txt");
        byte[] bytes = Files.readAllBytes(Paths.get(fileToRead.toURI()));
        storage.create(blobInfo, bytes);
        return new String(bytes);
    }

    // Method to download file
    @GetMapping(value = "/{fileName}")
    @Operation(summary = "Download/View a file.")
    public ResponseEntity<FileResponse> downloadFile(@PathVariable("fileName") @NonNull String fileName, HttpServletResponse response) {
        try {
            InputStream inputStream = fileService.downloadFile(path, fileName);
/*
        Check for the extension of file.
            if(fileName.substring(fileName.lastIndexOf(".") +1).equalsIgnoreCase("PDF")){
                response.setContentType(String.valueOf(MediaType.APPLICATION_PDF));
            } else if(fileName.substring(fileName.lastIndexOf(".") +1).equalsIgnoreCase("JPG") ||
                    fileName.substring(fileName.lastIndexOf(".") +1).equalsIgnoreCase("PNG")) {
                response.setContentType(String.valueOf(MediaType.IMAGE_JPEG));
            }
*/
            response.setContentType(String.valueOf(MediaType.ALL));
            StreamUtils.copy(inputStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "File not found");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            return new ResponseEntity<>(new FileResponse(fileName, "File does not exists."), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Method to download file
    @GetMapping(value = "/gcp/{fileName}")
    @Operation(summary = "Download/View a file.")
    public String downloadFileFromGoogleCloud(@PathVariable("fileName") @NonNull String fileName, HttpServletResponse response) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();

        try (ReadChannel channel = storage.reader("file-demo-uploads", "sample.txt")) {
            ByteBuffer bytes = ByteBuffer.allocate(10 * 1024);
            while (channel.read(bytes) > 0) {
                bytes.flip();
                String data = new String(bytes.array(), 0, bytes.limit());
                stringBuilder.append(data);
                bytes.clear();
            }
        }
        return stringBuilder.toString();
    }

    @GetMapping("")
    @Operation(summary = "List files.")
    public ResponseEntity<List<ListFileResponse>> listFiles() throws IOException {
        String[] fileNames = fileService.listFiles(path);
        List<ListFileResponse> listFileResponses = Arrays.stream(fileService.listFiles(path)).map(fileName -> new ListFileResponse(fileName,
                MvcUriComponentsBuilder
                        .fromMethodName(FileController.class, "downloadFile", fileNames[0], HttpServletResponse.class)
                        .build().toString())).collect(Collectors.toList());

        return new ResponseEntity<>(listFileResponses, HttpStatus.OK);
    }

    @DeleteMapping("")
    @Operation(summary = "Delete the files.")
    public ResponseEntity<List<FileDeleteResponse>> deleteFiles(
            @RequestBody @NonNull String[] fileNames,
            HttpServletResponse response)
            throws IOException {
        if (fileNames.length == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File list is Empty");
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(fileService.deleteFiles(path, fileNames), HttpStatus.OK);
    }
}
