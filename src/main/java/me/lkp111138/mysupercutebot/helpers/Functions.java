package me.lkp111138.mysupercutebot.helpers;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Functions {
    private static Map<String, JSONObject> guild_cache = new HashMap<>();
    private static Map<String, JSONObject> player_cache = new HashMap<>();
    private static Map<String, JSONObject> uuid_cache = new HashMap<>();
    private static OkHttpClient client = new OkHttpClient();


    public static JSONObject guildInfo(String name) {
        // TODO: detect tags and fetch from db
        JSONObject guild = guild_cache.get(name);
        if (guild == null) {
            JSONObject obj = new JSONObject(http_get("https://api.wynncraft.com/public_api.php?action=guildStats&command=" + name));
            guild_cache.put(name, obj);
            return obj;
        } else {
            int ts = guild.getJSONObject("request").getInt("timestamp");
            if (ts < System.currentTimeMillis() / 1000 - 20) {
                guild_cache.remove(name);
                return guildInfo(name);
            } else {
                return guild;
            }
        }
    }

    public static JSONObject playerInfo(String name) {
        JSONObject player = player_cache.get(name);
        if (player == null) {
            JSONObject obj = new JSONObject(http_get("https://api.wynncraft.com/public_api.php?action=playerStats&command=" + name));
            player_cache.put(name, obj);
            return obj;
        } else {
            int ts = player.getJSONObject("request").getInt("timestamp");
            if (ts < System.currentTimeMillis() / 1000 - 120) { // cache player info for 2mins
                player_cache.remove(name);
                return playerInfo(name);
            } else {
                return player;
            }
        }
    }

    public static String ign2uuid(String ign) {
        JSONObject player = uuid_cache.get(ign);
        if (player == null) {
            JSONObject obj = new JSONObject(http_get("https://api.mojang.com/users/profiles/minecraft/" + ign));
            obj.put("ts", System.currentTimeMillis() / 1000);
            uuid_cache.put(ign, obj);
            return obj.getString("id");
        } else {
            int ts = player.getInt("ts");
            if (ts < System.currentTimeMillis() / 1000 - 600) { // cache player uuid for 10mins
                uuid_cache.remove(ign);
                return ign2uuid(ign);
            } else {
                return player.getString("id");
            }
        }
    }

    static String http_get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                return response.body().string();
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
