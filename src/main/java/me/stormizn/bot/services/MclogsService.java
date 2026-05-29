package me.stormizn.bot.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;

public class MclogsService implements PasteService {

    //mclogs api
    private static final String API_URL = "https://api.mclo.gs/1/log";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String upload(String content, String filename) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("content", content);
        body.addProperty("source", "DiscordBot");

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            if (json.get("success").getAsBoolean()) {
                return json.get("url").getAsString();
            }
            throw new IOException(json.has("error") ? json.get("error").getAsString() :
                    "Unknown mclo.gs error!");
        }
    }
}
