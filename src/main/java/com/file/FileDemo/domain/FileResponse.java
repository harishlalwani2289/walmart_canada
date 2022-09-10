package com.file.FileDemo.domain;

public class FileResponse {
    String fileNames;
    String message;

    public FileResponse(String fileName, String message) {
        this.fileNames = fileName;
        this.message = message;
    }

    public String getFileNames() {
        return fileNames;
    }

    public void setFileNames(String fileNames) {
        this.fileNames = fileNames;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
