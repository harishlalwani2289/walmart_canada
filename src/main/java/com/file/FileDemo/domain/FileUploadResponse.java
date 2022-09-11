package com.file.FileDemo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class FileUploadResponse {

    private String fileName;
    private String uploadStatus;
    private String reason;
    private String mediaLink;
}
