package com.thirard.pim.internal.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Headers;

import java.io.IOException;

public class RequestRes {
    private static final ObjectMapper mapper = new ObjectMapper();

    private int code;
    byte[] body;
    Headers headers;

    public RequestRes() {
        this.code = 0;
        this.body = null;
    }

    public RequestRes(int code, byte[] bodyString, Headers headers) {
        this.code = code;
        this.body = bodyString;
        this.headers = headers;
    }

    public JsonNode getBodyAsNode() {
        try {
            return mapper.readTree(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean tokenNeedToBeRefresh() {
        if(code >= 200 && code < 300) return false;

        JsonNode body = getBodyAsNode();
        String message = body.path("message").asText();

        return message.equals("The access token provided has expired.") || message.equals("The access token provided is invalid.");
    }

    public boolean isSuccessful() {
        return this.code >= 200 && this.code < 300;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "RequestRes{" +
                "code=" + code +
                ", body=" + getBodyAsNode().asText() +
                '}';
    }
}
