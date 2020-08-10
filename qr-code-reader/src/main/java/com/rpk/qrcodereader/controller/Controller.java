package com.rpk.qrcodereader.controller;


import com.rpk.qrcodereader.dto.FileConversionRequestModel;
import com.rpk.qrcodereader.dto.RequestBodyModel;
import com.rpk.qrcodereader.dto.ResponseBodyModel;
import com.rpk.qrcodereader.services.QRCodeProcessingService;
import com.google.zxing.*;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
public class Controller {
    Logger logger = LoggerFactory.getLogger(Controller.class);
    @Value("${charset}")
    private String CHARSET;
    @Autowired
    private QRCodeProcessingService qrCodeProcessingService;

    @RequestMapping(method = RequestMethod.POST, value = "/readQRCode")
    public @ResponseBody
    ResponseEntity<ResponseBodyModel> readQRCode(@RequestBody RequestBodyModel requestBody) {
        logger.info("=======Read QR-Code request Received========");
        logger.info("========== Request Parameters ================");
        logger.info("Request Id : "+requestBody.getRequestId());
        logger.info("File Type in the Request : "+ requestBody.getFileType());
        logger.info("File Path in the Request : "+ requestBody.getFilePath());
        logger.info("===============================================");

        String targetFilePath = null;
        String resultString = null;
        ResponseBodyModel responseDto = new ResponseBodyModel();
        responseDto.setRequestId(requestBody.getRequestId());
        try {

            Map<EncodeHintType,
                    ErrorCorrectionLevel>
                    hintMap
                    = new HashMap<EncodeHintType,
                    ErrorCorrectionLevel>();

            hintMap.put(
                    EncodeHintType.ERROR_CORRECTION,
                    ErrorCorrectionLevel.L);
            //"/Users/rupak/Desktop/test1.pdf"
            if(requestBody == null || ("PDF".equalsIgnoreCase(requestBody.getFileType()) && StringUtils.isBlank(requestBody.getFilePath()))) {

                responseDto.setResponseCode("F");
                responseDto.setResponseMsg("Failed : Request Body is empty or File path is null");
                return ResponseEntity.ok(responseDto);
            }
            if("PDF".equalsIgnoreCase(requestBody.getFileType())){
                logger.info("========File Type in Request is PDF, Converting it into png=======");
                targetFilePath = qrCodeProcessingService.convertPDFtoImage(requestBody.getFilePath(),requestBody.getPageIndex());
                logger.info("Converted Target File Path : "+targetFilePath);
                logger.info("===================================================================");
            } else if("IMAGE".equalsIgnoreCase(requestBody.getFileType())) {
                logger.info("========File Type in Request is IMAGE(png), Conversion not required=======");
                targetFilePath = requestBody.getFilePath();
                logger.info("Target File Path : "+targetFilePath);
                logger.info("===================================================================");
            }

            if (StringUtils.isBlank(targetFilePath)) {
                responseDto.setResponseCode("F");
                responseDto.setResponseMsg("Failed : PDF file conversion into Image file failed or Provided image file in the request not found");
                logger.info("============= Response Body ==================");
                logger.info(responseDto.toString());
                logger.info("============= QR Code Request Completed========");
                return ResponseEntity.ok(responseDto);
            } else {
                resultString = qrCodeProcessingService.readQR(targetFilePath, CHARSET, hintMap);
                if (StringUtils.isBlank(resultString)) {
                    // TODO : Write a Utility method to remove the PDF file from the Disc to remove the garbage data.
                    // TODO : Write a Utility method to remove the created Image file from the disc, to free up the garbage data.
                    responseDto.setConvertedImageFilePath(targetFilePath);
                    responseDto.setResponseCode("F");
                    responseDto.setResponseMsg("Failed : Invalid QR code, no data found in the QR Code");
                } else {
                    logger.info("QR Reading Output : " + resultString);
                    responseDto.setConvertedImageFilePath(targetFilePath);
                    responseDto.setQRCodeData(resultString);
                    responseDto.setResponseCode("S");
                    responseDto.setResponseMsg("Success : QR Code scanning successful");
                }

                logger.info("============= Response Body ==================");
                logger.info(responseDto.toString());
                logger.info("============= QR Code Request Completed========");
                return ResponseEntity.ok(responseDto);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception Raised while Reading the QR code : " + e.getMessage());
        }
        return ResponseEntity.notFound().build();
    }





    @RequestMapping(method = RequestMethod.POST, value = "/convertPdfToImage")
    public @ResponseBody
    ResponseEntity<String>  convertPdfToImage(@RequestBody FileConversionRequestModel fileConversionRequestModel) {


        String targetFilePath = null;

        try {

            //"/Users/rupak/Desktop/test1.pdf"

                logger.info("========File Type in Request is PDF, Converting it into png=======");
                targetFilePath = qrCodeProcessingService.convertPDFtoImage(fileConversionRequestModel.getFilePath(), fileConversionRequestModel.getPageIndex());
                logger.info("Converted Target File Path : "+targetFilePath);
                logger.info("===================================================================");


        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Exception Raised while Reading the QR code : " + e.getMessage());
        }
        return ResponseEntity.ok(targetFilePath);
    }

}
