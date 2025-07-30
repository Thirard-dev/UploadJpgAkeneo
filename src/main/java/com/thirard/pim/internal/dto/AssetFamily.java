package com.thirard.pim.internal.dto;

public class AssetFamily {
    String name;
    String url;

    public AssetFamily(String code, String endpointUrl) {
        this.name = code;
        this.url = endpointUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "AssetFamily{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
