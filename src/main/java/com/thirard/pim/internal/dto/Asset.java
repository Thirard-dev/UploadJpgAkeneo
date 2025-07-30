package com.thirard.pim.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thirard.pim.internal.api.ApiClient;
import com.thirard.pim.internal.config.Config;
import com.thirard.pim.internal.database.Database;
import com.thirard.pim.internal.api.RequestRes;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Asset {
    private static final ObjectMapper mapper= new ObjectMapper();


    public String assetFamilyCode;

    @JsonProperty("code")
    public String code;

    public Resource media;
    public Resource mediaJpg;

    public File pdf;
    public File jpg;

    public void buildMedia(JsonNode media) {
        if(null == media) return;
        this.media = new Resource(media);
    }

    public void buildMediaJpg(JsonNode mediaJpg) {
        if(null == mediaJpg) return;
        this.mediaJpg = new Resource(mediaJpg);
    }

    public static Asset fromJson(String assetFamilyCode, JsonNode json) {
        Asset asset = new Asset();
        asset.assetFamilyCode = assetFamilyCode;
        asset.code = json.path("code").asText();

        JsonNode mediaNode = json.path("values").path("media").get(0);
        if (mediaNode != null) {
            asset.buildMedia(mediaNode);
        }

        JsonNode mediaJpgNode = json.path("values").path("media_jpg").get(0);
        if (mediaJpgNode != null) {
            asset.buildMediaJpg(mediaJpgNode);
        }

        return asset;
    }

    public void updateJpg() {
        try {
            this.downloadPdf();
            this.transformPdfToJpg();
            this.uploadJpg();
            this.associateJpgToAsset();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void downloadPdf() throws IOException {

        String outputPath = Paths.get(Config.GetTempFolder(), code).toString();

        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            Files.createDirectories(outputDir.toPath());
        }

        File outputFile = new File(outputDir,  code + ".pdf");

        if(outputFile.exists()) {
            System.out.println("Fichier pdf déjà téléchargé : " + code);
            pdf = outputFile;
            return;
        }

        RequestRes response = ApiClient.Get(media.url);

        InputStream inputStream = new ByteArrayInputStream(response.getBody());
        OutputStream outputStream = new FileOutputStream(outputFile);

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        System.out.println("PDF téléchargé avec succès : " + outputPath);

        pdf = outputFile;

        inputStream.close();
        outputStream.close();
    }

    private void transformPdfToJpg() throws IOException {
        if(null == pdf || !pdf.exists()) {
            return;
        }

        String outputPath = Paths.get(Config.GetTempFolder(), code).toString();

        File outputFile = new File(outputPath,  code + ".jpg");

        PDDocument pdfDoc = Loader.loadPDF(pdf);
        if(pdfDoc.getNumberOfPages() == 1) {
            PDFRenderer renderer = new PDFRenderer(pdfDoc);
            BufferedImage image = renderer.renderImage(0);
            ImageIO.write(image, "JPEG", outputFile);
            jpg = outputFile;
        }

        pdfDoc.close();
    }

    private void uploadJpg() {

        if(null == jpg) {
            return;
        }

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", jpg.getName(), RequestBody.create(jpg, MediaType.parse("application/octet-stream")))
                .build();

        RequestRes res = ApiClient.Post("https://thirard.cloud.akeneo.com/api/rest/v1/asset-media-files", body);
        if(res.isSuccessful()) {
            mediaJpg = new Resource();
            mediaJpg.data = res.getHeaders().get("asset-media-file-code");
        }
    }

    private void associateJpgToAsset() throws IOException {

        if(null == jpg || null == mediaJpg) {
            return;
        }

        Map<String, Object> mediaEntry = new HashMap<>();
        mediaEntry.put("locale", null);
        mediaEntry.put("channel", null);
        mediaEntry.put("data", mediaJpg.data);

        Map<String, Object> values = new HashMap<>();
        values.put("media_jpg", List.of(mediaEntry));

        Map<String, Object> bodyData = new HashMap<>();
        bodyData.put("code", code);
        bodyData.put("values", values);


        String json = mapper.writeValueAsString(bodyData);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        RequestRes res = ApiClient.Patch("https://thirard.cloud.akeneo.com/api/rest/v1/asset-families/" + assetFamilyCode + "/assets/" + code, body);
        if(res.isSuccessful())
            mediaJpg.url = "https://thirard.cloud.akeneo.com/api/rest/v1/asset-media-files/" + mediaJpg.data;
    }

    public void deleteTempFolder() {
        Path path = Paths.get(Config.GetTempFolder(), this.code);

        if(!Files.exists(path)) return;

        try {
            FileUtils.deleteDirectory(new File(path.toString()));
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression du dossier temporaire : " + path);
            System.err.println(e);
        }
    }

    public boolean hasMediaJpg() {
        return mediaJpg != null;
    }

    public boolean doesntHaveMediaJpg() {
        return !hasMediaJpg();
    }

    public boolean jpgHasBeenCreated() {
        return null != jpg;
    }

    public boolean pdfHasChanged() {
        try {
            ResultSet res = Database.statement.executeQuery("SELECT * FROM ASSET WHERE code = \"" + code + "\"");
            return res.getString("code") == null || !res.getString("pdf_data").equals(media.data);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasNoPdfOrPdfHasOnePage() {
        try {
            ResultSet res = Database.statement.executeQuery("SELECT * FROM ASSET WHERE code = \"" + code + "\"");
            return res.getString("code") == null || !res.getBoolean("pdf_more_than_one_page");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean pdfHasMoreThanOnePage() {
        if(null == pdf)
            return false;

        try( PDDocument pdfDoc = Loader.loadPDF(pdf) ) {
            return pdfDoc.getNumberOfPages() > 1;
        } catch (IOException e) {
            System.err.println(e);
        }

        return false;
    }

    public boolean hasPdf() {
        return null != media && null != media.data && !media.data.isBlank();
    }

    public boolean hasJpg() {
        return null != mediaJpg && null != mediaJpg.data && !mediaJpg.data.isBlank();
    }

    public void insertOrReplaceSql() {
        String sql = getSqlStringParameters();

        try {
            PreparedStatement pStatement = Database.connection.prepareStatement(sql);
            pStatement.setString(1, code);
            pStatement.setString(2, assetFamilyCode);
            pStatement.setString(3, media.data);
            pStatement.setBoolean(4, pdfHasMoreThanOnePage());

            if(null != mediaJpg && null != mediaJpg.data && !mediaJpg.data.isBlank()) {
                pStatement.setString(5, mediaJpg.data);
            }

            int x = pStatement.executeUpdate();

            System.out.println(x + " row inserted or updated");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private String getSqlStringParameters() {
        String sql = ""; //"INSERT OR REPLACE INTO ASSET (code, asset_family, pdf_data, pdf_more_than_one_page) VALUES (?, ?, ?, ?);";

        if(null != mediaJpg && null != mediaJpg.data && !mediaJpg.data.isBlank()) {
            sql += "INSERT OR REPLACE INTO ASSET (code, asset_family, pdf_data, pdf_more_than_one_page, jpg_data) VALUES (?, ?, ?, ?, ?);";
        } else {
            sql += "INSERT OR REPLACE INTO ASSET (code, asset_family, pdf_data, pdf_more_than_one_page) VALUES (?, ?, ?, ?);";
        }
        return sql;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "assetFamilyCode='" + assetFamilyCode + '\'' +
                ", code='" + code + '\'' +
                ", media=" + media +
                ", mediaJpg=" + mediaJpg +
                ", pdf=" + pdf +
                ", jpg=" + jpg +
                '}';
    }
}
