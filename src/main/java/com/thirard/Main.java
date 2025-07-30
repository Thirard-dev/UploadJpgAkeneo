package com.thirard;

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

            System.out.println("------------------- " + assetFamily.getName().toUpperCase() + " -------------------");
            int pageNumber = 0;
            do {
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
                    System.out.println(asset.code + " is being updated.");

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
    }

    static List<AssetFamily> GetAssetFamilies() {

        List<AssetFamily> assetFamilies = new ArrayList<>();

        try {
            ResultSet res = Database.statement.executeQuery("SELECT * FROM ASSET_FAMILY;");

            while(res.next()) {
                assetFamilies.add(new AssetFamily(res.getString("code"), res.getString("endpoint_url")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

