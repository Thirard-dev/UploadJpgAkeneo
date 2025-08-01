package com.thirard.pim.internal.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String BASE_URL = dotenv.get("BASE_URL");

    private static final String CONFIG_FILE_PATH = dotenv.get("CONFIG_FILE_PATH");

    private static final String TEMP_FOLDER = dotenv.get("TEMP_FOLDER");

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static void LoadLastExec() {
        File file = new File(CONFIG_FILE_PATH);

        try {
            JsonNode node = yamlMapper.readTree(file);
            LastExec.LoadFromNode(node.get("exec"));
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    public static void SaveExec() {
        File file = new File(CONFIG_FILE_PATH);

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("last_asset_family_code_updated", LastExec.getAssetFamilyCode());
        node.put("last_asset_code_updated", LastExec.getAssetCode());

        ObjectNode exec = JsonNodeFactory.instance.objectNode();
        exec.set("exec", node);

        try {
            yamlMapper.writeValue(file, exec);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String GetConfigFilePath() { return CONFIG_FILE_PATH; }

    public static String GetTempFolder() { return TEMP_FOLDER; }
}
