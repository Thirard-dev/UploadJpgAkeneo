package com.thirard.pim.internal.database;

import com.thirard.logger.QuickLogger;
import com.thirard.pim.internal.dto.Asset;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    public static Connection connection;
    public static Statement statement;


    public static void Connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:akeneo.db");
            statement = connection.createStatement();
        } catch (SQLException e) {
            QuickLogger.error("Erreur lors de la connection à la base de données.\n" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void Init() {
        try {
            statement.execute("CREATE TABLE IF NOT EXISTS ASSET_FAMILY (code TEXT PRIMARY KEY, endpoint_url TEXT NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS ASSET (code TEXT PRIMARY KEY, asset_family TEXT NOT NULL, jpg_data TEXT, pdf_data TEXT NOT NULL, pdf_more_than_one_page INTEGER NOT NULL);");
        } catch (SQLException e) {
            QuickLogger.error("Erreur lors de l'initialisation de la base de données.\n" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void MergeAsset(Asset asset) {
        asset.insertOrReplaceSql();
    }
}
