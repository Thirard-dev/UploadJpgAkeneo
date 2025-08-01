package com.thirard.pim.internal.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static String BASE_URL;

    private static String CONFIG_FILE_PATH;

    private static String TEMP_FOLDER;

    private static String URl_AUTH;

    private static String AUTH_AUTHORIZATION;

    private static String USER;

    private static String PASSWORD;

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    static {
        try {
            Path envFilePath = Path.of(System.getProperty("user.dir"), String.format(".env.%s", System.getenv("ENV")));

            if(!Files.exists(envFilePath)) {
                throw new FileNotFoundException(String.format("Le fichier de configuration .env.%s", System.getenv("ENV")));
            }

            final Dotenv dotenv = Dotenv.configure()
                    .filename(String.format(".env.%s", System.getenv("ENV")))
                    .load();

            String urlAuth = dotenv.get("URL_AUTH");
            String authAuthorization = dotenv.get("AUTH_AUTHORIZATION");
            String user = dotenv.get("USER");
            String password = dotenv.get("PASSWORD");
            String baseUrl = dotenv.get("BASE_URL");
            String configFilePath = dotenv.get("CONFIG_FILE_PATH");
            String tempFolder = dotenv.get("TEMP_FOLDER");

            if(null == urlAuth || urlAuth.isBlank()) throw new IllegalArgumentException("URL_AUTH est requis dans le fichier .env");
            if(null == authAuthorization || authAuthorization.isBlank()) throw new IllegalArgumentException("AUTH_AUTHORIZATION est requis dans le fichier .env");
            if(null == user || user.isBlank()) throw new IllegalArgumentException("USER est requis dans le fichier .env");
            if(null == password || password.isBlank()) throw new IllegalArgumentException("PASSWORD est requis dans le fichier .env");

            if(null == baseUrl || baseUrl.isBlank()) throw new IllegalArgumentException("BASE_URL est requis dans le fichier .env");
            if(null == configFilePath || configFilePath.isBlank()) throw new IllegalArgumentException("CONFIG_FILE_PATH est requis dans le fichier .env");
            if(null == tempFolder || tempFolder.isBlank()) throw new IllegalArgumentException("TEMP_FOLDER est requis dans le fichier .env");

            URl_AUTH = urlAuth;
            AUTH_AUTHORIZATION = authAuthorization;
            USER = user;
            PASSWORD = password;
            BASE_URL = baseUrl;
            CONFIG_FILE_PATH = configFilePath;
            TEMP_FOLDER = tempFolder;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

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

    public static String getConfigFilePath() {
        return CONFIG_FILE_PATH;
    }

    public static String getTempFolder() {
        return TEMP_FOLDER;
    }

    public static String getURl_AUTH() {
        return URl_AUTH;
    }

    public static String getAuthAuthorization() {
        return AUTH_AUTHORIZATION;
    }

    public static String getUSER() {
        return USER;
    }

    public static String getPASSWORD() {
        return PASSWORD;
    }
}
