package com.rpk.qrcodereader.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class ResponseBodyModel implements Serializable {
    private Long requestId;
    private String convertedImageFilePath;
    private String QRCodeData;
    private String responseCode;
    private String responseMsg;
}
