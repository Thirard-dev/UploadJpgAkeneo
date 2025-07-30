package com.thirard.pim.internal.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class Resource {
    public String data;
    public String url;

    public Resource() {
        data = "";
        url = "";
    }

    public Resource(JsonNode node) {
        this.data = node.path("data").asText();
        this.url = node.path("_links").path("download").path("href").asText();
    }

    @Override
    public String toString() {
        return "Resource{" +
                "data='" + data + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
