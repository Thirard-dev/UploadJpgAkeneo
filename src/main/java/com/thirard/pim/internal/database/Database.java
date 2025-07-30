package com.thirard.pim.internal.database;

import com.thirard.pim.internal.dto.Asset;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    public static Connection connection;
    public static Statement statement;


    public static void Connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:akeneo.db");
        statement = connection.createStatement();
    }

    public static void Init() {
        try {
            statement.execute("CREATE TABLE IF NOT EXISTS ASSET_FAMILY (code TEXT PRIMARY KEY, endpoint_url TEXT NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS ASSET (code TEXT PRIMARY KEY, asset_family TEXT NOT NULL, jpg_data TEXT, pdf_data TEXT NOT NULL, pdf_more_than_one_page INTEGER NOT NULL);");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void MergeAsset(Asset asset) {
        asset.insertOrReplaceSql();
//        try {
//            //statement.execute("DROP TABLE ASSET_FAMILY");
//            String sql = "INSERT OR REPLACE INTO ASSET ("
//                    + "\"" + asset.code + "\""
//                    + ", \"" + asset.assetFamilyCode + "\""
//                    + asset.mediaJpg != null && asset.mediaJpg.data != null ? ", \"" + asset.mediaJpg.data + "\"" : null
//                    + asset.media != null && asset.media.data != null ? ", \"" + asset.media.data + "\"" : null
//                    +
//            statement.execute("INSERT OR REPLACE INTO ASSET (\"" + asset.code + "\", \"" + asset.assetFamilyCode + "\", \"" + asset.mediaJpg.data + "\", \"" + asset.media.data + "\", " + asset.pdfhasMoreThanOnePage() + ");");
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }
}
