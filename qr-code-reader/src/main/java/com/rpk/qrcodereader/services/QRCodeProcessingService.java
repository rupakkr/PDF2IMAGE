package com.rpk.qrcodereader.services;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@Service
public class QRCodeProcessingService {
    static Logger logger = LoggerFactory.getLogger(QRCodeProcessingService.class);
    @Value("${destinationDir}")
    private String destinationDir;// converted images from pdf document are saved here

    /**
     * This method takes PDF file path as an argument and convert this file into image.
     * Return the path of converted Image File path.
     *
     * @return String as Converted File path.
     * @throws FileNotFoundException
     * @throws IOException
     * @Maintainer Rupak kumar
     * @date 01-Aug-2020
     */
    public String convertPDFtoImage(String pdfPath, int pageIndex) throws FileNotFoundException, IOException {

        String sourceDir = pdfPath; // Pdf files are read from this folder
        String targetFilePath = null;
        File sourceFile = new File(sourceDir);
        File destinationFile = new File(destinationDir);
        if (!destinationFile.exists()) {
            destinationFile.mkdir();
            logger.info("Folder Created -> " + destinationFile.getAbsolutePath());
        }
        if (sourceFile.exists()) {
            System.out.println("Images copied to Folder Location: " + destinationFile.getAbsolutePath());
            PDDocument document = PDDocument.load(sourceFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            int numberOfPages = document.getNumberOfPages();
            logger.info("Total files to be converting -> " + numberOfPages);

            String fileName = sourceFile.getName().replace(".pdf", "");
            String fileExtension = "png";

            /*
             * 600 dpi give good image clarity but size of each image is 2x times of 300 dpi.
             */
            int dpi = 150;// use less dpi for to save more space in harddisk. For professional usage you can use more than 300dpi


//                for (int i = 0; i < numberOfPages; ++i) {
//                    File outPutFile = new File(destinationDir + fileName + "_" + (i + 1) + "." + fileExtension);
//                    BufferedImage bImage = pdfRenderer.renderImageWithDPI(i, dpi, ImageType.RGB);
//                    ImageIO.write(bImage, fileExtension, outPutFile);
//                }

            // Assumptions :
            //  1. QR code will be available on the first page of the PDF.
            File outPutFile = new File(destinationDir + fileName + "_QR"+pageIndex + "." + fileExtension);
            BufferedImage bImage = pdfRenderer.renderImageWithDPI(pageIndex, dpi, ImageType.GRAY);
            ImageIO.write(bImage, fileExtension, outPutFile);

            document.close();
            logger.info("Converted Images are saved at -> " + destinationFile.getAbsolutePath());
            targetFilePath = outPutFile.getAbsolutePath();
        } else {
            logger.info(sourceFile.getName() + " File not exists");
        }

        return targetFilePath;
    }

    /**
     * Function to read the QR file
     *
     * @param path
     * @param charset
     * @param hashMap
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NotFoundException
     */
    public String readQR(
            String path,
            String charset,
            Map hashMap)
            throws FileNotFoundException,
            IOException,
            NotFoundException {
        BinaryBitmap binaryBitmap
                = new BinaryBitmap(
                new HybridBinarizer(
                        new BufferedImageLuminanceSource(
                                ImageIO.read(
                                        new FileInputStream(path)))));



        Result result
                = new MultiFormatReader()
                .decode(binaryBitmap);

        return result.getText();
    }
}
