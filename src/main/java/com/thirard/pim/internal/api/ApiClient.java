package com.thirard.pim.internal.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thirard.pim.internal.config.Config;
import okhttp3.*;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(0, TimeUnit.MILLISECONDS).build();

    private static int requestCounter = 0;
    private static String accessToken;
    private static String refreshToken;

    private static Date accessTokenDate = new Date();

    public static void UpdateAccessToken() {
        if (null != accessToken && !accessToken.isBlank()) {
            if (accessTokenDate.after(DateUtils.addHours(accessTokenDate, 1))) {
                RefreshAccessToken();
            }
        } else {
            GetNewAccessToken();
        }
    }

    static void GetNewAccessToken() {
        //System.out.println("\nGet new access token");

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = new FormBody.Builder()
                .add("username", Config.getUSER())
                .add("password", Config.getPASSWORD())
                .add("grant_type", "password")
                .build();

        Request request = new Request.Builder()
                .url(Config.getURl_AUTH())
                .addHeader("Authorization", Config.getAuthAuthorization())
                .method("POST", body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            incrementRequestCounter();
            String responseString = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(responseString);

            if (response.isSuccessful()) {
                accessToken = node.get("access_token").asText();
                refreshToken = node.get("refresh_token").asText();
                accessTokenDate = new Date();
            } else {
                System.err.println("1. Erreur token\n" + responseString);
            }
        } catch (IOException e) {
            System.err.println("2. Erreur token");
        }
    }

    static void RefreshAccessToken() {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = new FormBody.Builder()
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .build();

        Request request = new Request.Builder()
                .url(Config.getURl_AUTH())
                .addHeader("Authorization", Config.getAuthAuthorization())
                .method("POST", body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            incrementRequestCounter();

            String responseString = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(responseString);

            if (response.isSuccessful()) {
                accessToken = node.get("access_token").asText();
                refreshToken = node.get("refresh_token").asText();
                accessTokenDate = new Date();
            } else {
                System.err.println("1. Erreur token\n" + responseString);
            }
        } catch (IOException e) {
            System.err.println("2. Erreur token");
        }
    }

    static void incrementRequestCounter() {
        requestCounter++;
    }

    private static boolean TokenHasToBeRefresh() {
        if(null == accessToken || accessToken.isBlank())
            return true;

        return accessTokenDate.after(DateUtils.addHours(accessTokenDate, 1));
    }

    public static RequestRes Get(String url) {

        if(TokenHasToBeRefresh())
            UpdateAccessToken();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try(Response response = client.newCall(request).execute()) {
            incrementRequestCounter();

            return new RequestRes(response.code(), response.body().bytes(), response.headers());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RequestRes Post(String url, RequestBody body) {

        if(TokenHasToBeRefresh())
            UpdateAccessToken();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        try(Response response = client.newCall(request).execute()) {
            incrementRequestCounter();

            return new RequestRes(response.code(), response.body().bytes(), response.headers());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RequestRes Patch(String url, RequestBody body) throws IOException {
        if(TokenHasToBeRefresh())
            UpdateAccessToken();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .patch(body)
                .build();

        Response response = client.newCall(request).execute();
        incrementRequestCounter();

        RequestRes res = new RequestRes(response.code(), response.body().bytes(), response.headers());

        response.close();

        return res;
    }

    public static int getRequestCounter() {
        return requestCounter;
    }
}
