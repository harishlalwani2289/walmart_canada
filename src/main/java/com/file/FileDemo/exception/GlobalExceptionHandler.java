package com.file.FileDemo.exception;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-request-size}")
    String maxPermittedRequestSize;

    @Value(("${spring.servlet.multipart.max-file-size}"))
    String maxPermittedFileSize;

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<ExceptionMessage> handleFileSizeLimitExceededException(FileSizeLimitExceededException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return new ResponseEntity<>(new ExceptionMessage(" File " + e.getFileName() +
                " size exceeds the permitted value " + maxPermittedFileSize), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionMessage> handleMaxUploadException(MaxUploadSizeExceededException e, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return new ResponseEntity<>(new ExceptionMessage(" Total File " +
                " size exceeds the permitted value " + maxPermittedRequestSize), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
