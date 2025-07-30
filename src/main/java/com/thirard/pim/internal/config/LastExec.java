package com.thirard.pim.internal.config;

import com.fasterxml.jackson.databind.JsonNode;

public class LastExec {
    static String assetFamilyCode;
    static String assetCode;

    public static void LoadFromNode(JsonNode node) {
        assetFamilyCode = node.path("last_asset_family_code_updated").asText();
        assetCode = node.path("last_asset_code_updated").asText();
    }

    public static String getAssetFamilyCode() {
        return assetFamilyCode;
    }

    public static String getAssetCode() {
        return assetCode;
    }

    public static void setAssetFamilyCode(String assetFamilyCode) {
        LastExec.assetFamilyCode = assetFamilyCode;
    }

    public static void setAssetCode(String assetCode) {
        LastExec.assetCode = assetCode;
    }
}
