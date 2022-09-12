package com.file.FileDemo.controller;

import com.file.FileDemo.domain.FileDeleteResponse;
import com.file.FileDemo.domain.FileResponse;
import com.file.FileDemo.domain.ListFileResponse;
import com.file.FileDemo.services.FileService;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
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

    @Value("${file.path}")
    private String path;

    @Value("${spring.servlet.multipart.max-file-size}")
    String maxPermittedSize;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload files.")
    public ResponseEntity<FileResponse> uploadFiles(
            @RequestParam("files") @ApiParam(value = "Select files to upload", required = true, name = "files") List<MultipartFile> files) {
        log.info("Request received to upload files [{}]", files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList()));
        try {
            for (MultipartFile file : files) {
                fileService.uploadFile(path, file);
            }
        } catch (FileAlreadyExistsException fileAlreadyExistsException) {
            log.error("File already exists in the path");
            return new ResponseEntity<>(new FileResponse(fileAlreadyExistsException.getMessage()
                    .substring(fileAlreadyExistsException.getMessage().indexOf("/") + 1),
                    "File with provided name already uploaded"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            return new ResponseEntity<>(new FileResponse(null, "File is not uploaded due to some server error"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(new FileResponse(files.stream().map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList()).toString(), " Files are successfully uploaded"),
                HttpStatus.ACCEPTED);
    }

    // Method to download file
    @GetMapping(value = "/{fileName}")
    @Operation(summary = "Download/View a file.")
    public ResponseEntity<FileResponse> downloadFile(@PathVariable("fileName") @NonNull String fileName, HttpServletResponse response) {
        log.info("Request received to download file {}", fileName);
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

    @GetMapping("")
    @Operation(summary = "List files.")
    public ResponseEntity<List<ListFileResponse>> listFiles() throws IOException {
        String[] fileNames = fileService.listFiles(path);
        List<ListFileResponse> listFileResponses = Arrays.stream(fileService.listFiles(path)).map(fileName -> new ListFileResponse(fileName,
                MvcUriComponentsBuilder
                        .fromMethodName(FileController.class, "downloadFile", fileName, HttpServletResponse.class)
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
