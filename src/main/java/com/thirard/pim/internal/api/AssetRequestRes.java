package com.thirard.pim.internal.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.thirard.pim.internal.dto.Asset;

import java.util.ArrayList;
import java.util.List;

public class AssetRequestRes {
    public String nextUrl;
    public List<Asset> assets;

    public AssetRequestRes() {
        nextUrl = "";
        assets = new ArrayList<>();
    }

    public void fromJsonResponse(String assetFamilyCode, JsonNode json) {
        JsonNode nodeNextUrl = json.get("_links").path("next").get("href");
        if(null != nodeNextUrl) this.nextUrl = nodeNextUrl.asText();

        JsonNode items = json.path("_embedded").path("items");

        for (JsonNode item : items) {
            Asset asset = Asset.fromJson(assetFamilyCode, item);

            this.assets.add(asset);
        }
    }
}
