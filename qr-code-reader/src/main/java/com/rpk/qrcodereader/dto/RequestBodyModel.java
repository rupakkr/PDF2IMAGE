package com.rpk.qrcodereader.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class RequestBodyModel implements Serializable {
    private Long requestId;
    private String filePath;
    private String fileType;
    private int pageIndex;
}
