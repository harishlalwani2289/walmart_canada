package com.file.FileDemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class FileDeleteResponse {
    private String fileName;
    private String status;
    private String reason;
}
