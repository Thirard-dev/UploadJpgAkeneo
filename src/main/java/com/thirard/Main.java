package com.thirard;

import com.thirard.logger.QuickLogger;
import com.thirard.logger.ThirardLogger;

import com.thirard.pim.internal.api.ApiClient;
import com.thirard.pim.internal.api.AssetRequestRes;
import com.thirard.pim.internal.api.RequestRes;
import com.thirard.pim.internal.config.Config;
import com.thirard.pim.internal.config.LastExec;
import com.thirard.pim.internal.database.Database;
import com.thirard.pim.internal.dto.Asset;
import com.thirard.pim.internal.dto.AssetFamily;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws SQLException {

        ThirardLogger.Init("UPLOAD_JPG_AKENEO", ".");

        Database.Connect();
        Database.Init();

        Config.LoadLastExec();

        ApiClient.UpdateAccessToken();

        List<AssetFamily> assetFamilies = GetAssetFamilies();

        boolean found = null == LastExec.getAssetFamilyCode() || LastExec.getAssetFamilyCode().isBlank();
        for(AssetFamily assetFamily : assetFamilies) {

            String url = Config.getBaseUrl() + assetFamily.getUrl() + "/assets";

            if(!found && Objects.equals(assetFamily.getName(), LastExec.getAssetFamilyCode())) {
                found = true;

                if(null != LastExec.getAssetCode() && !LastExec.getAssetCode().isBlank()) {
                    url += "?search_after=" + LastExec.getAssetCode();
                }
            }

            if(!found)
                continue;

            LastExec.setAssetFamilyCode(assetFamily.getName());
            LastExec.setAssetCode("");

            AssetRequestRes assetRes = GetAssets(assetFamily.getName(), url);

            if(assetRes.assets.isEmpty())
                continue;

            int pageNumber = 0;
            do {

                if(ApiClient.getRequestCounter() > 3980) {
                    break;
                }

                pageNumber++;
                System.out.println("Page numéro : " + pageNumber);

                if(pageNumber > 1) {
                    assetRes = GetAssets(assetFamily.getName(), assetRes.nextUrl);
                }

                List<Asset> assets = assetRes.assets;

                //Filter on the assets without jpg
                List<Asset> assetsWithoutJpg = assets.stream().filter(Asset::doesntHaveMediaJpg).collect(Collectors.toList());
                //Filter on the asset with jpg
                List<Asset> assetsWithJpg = assets.stream().filter(Asset::hasMediaJpg).collect(Collectors.toList());

                List<Asset> assetsToUpdate = new ArrayList<>();
                assetsToUpdate.addAll(assetsWithJpg.stream().filter(Asset::pdfHasChanged).collect(Collectors.toList()));
                assetsToUpdate.addAll(assetsWithoutJpg.stream().filter(Asset::hasNoPdfOrPdfHasOnePage).collect(Collectors.toList()));

                for(Asset asset: assetsToUpdate) {

                    if(ApiClient.getRequestCounter() > 3980) {
                        break;
                    }

                    asset.updateJpg();

                    Database.MergeAsset(asset);

                    LastExec.setAssetCode(asset.code);
                    Config.SaveExec();

                    asset.deleteTempFolder();
                }

            } while(null != assetRes.nextUrl && !assetRes.nextUrl.isBlank());
        }

        LastExec.setAssetFamilyCode("");
        LastExec.setAssetCode("");

        QuickLogger.info("Tous les assets ont été mis à jour !", true);
    }

    static List<AssetFamily> GetAssetFamilies() {

        List<AssetFamily> assetFamilies = new ArrayList<>();

        try {
            ResultSet res = Database.statement.executeQuery("SELECT * FROM ASSET_FAMILY;");

            while(res.next()) {
                assetFamilies.add(new AssetFamily(res.getString("code"), res.getString("endpoint_url")));
            }
        } catch (SQLException e) {
            QuickLogger.error("Erreur lors de la récupération des famille d'asset.\n" + e.getMessage());
        }

        return assetFamilies;
    }


    static AssetRequestRes GetAssets(String assetFamilyCode, String url) {
        AssetRequestRes res = new AssetRequestRes();

        RequestRes response = ApiClient.Get(url);

        if (response.isSuccessful()) {
            res.fromJsonResponse(assetFamilyCode, response.getBodyAsNode());
        } else {
            System.err.println("1. Erreur requete\n" + response.getBody());
        }

        return res;
    }
}

