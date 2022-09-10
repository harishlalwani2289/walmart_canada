package com.file.filedemo.domain;


import lombok.Data;

@Data
public class ListFileResponse {

    String fileName;
    String url;

    public ListFileResponse(String fileName, String url) {
        this.fileName = fileName;
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
