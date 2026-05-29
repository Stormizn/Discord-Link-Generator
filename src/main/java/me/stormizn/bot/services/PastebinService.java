package me.stormizn.bot.services;

import me.stormizn.bot.DiscordBot;
import okhttp3.*;

import java.io.IOException;

public class PastebinService implements PasteService {

    // pastebin api
    private static final String API_URL = "https://pastebin.com/api/api_post.php";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String upload(String content, String filename) throws Exception {
        String name = filename != null ? filename : "upload.txt";

        RequestBody body = new FormBody.Builder()
                .add("api_dev_key", DiscordBot.PASTEBIN_API_KEY)
                .add("api_option", "paste")
                .add("api_paste_code", content)
                .add("api_paste_name", name)
                .add("api_paste_private", "1")       // 0=public, 1=unlisted, 2=private
                .add("api_paste_expire_date", "N")    // N=never, 10M=10min, 1H=1hour…
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String url = response.body().string();
            if (url.startsWith("https://pastebin.com/")) {
                return url;
            }
            throw new IOException("Pastebin error: " + url);
        }
    }
}
