package com.rpk.qrcodereader.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileConversionRequestModel implements Serializable {
    private String filePath;
    private int pageIndex;
}
