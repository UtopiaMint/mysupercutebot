package me.lkp111138.mysupercutebot.helpers;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Functions {
    private static Map<String, JSONObject> guild_cache = new HashMap<>();
    private static Map<String, JSONObject> player_cache = new HashMap<>();
    private static Map<String, JSONObject> uuid_cache = new HashMap<>();


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
            if (ts < System.currentTimeMillis() / 1000 - 20) {
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
            if (ts < System.currentTimeMillis() / 1000 - 600) {
                uuid_cache.remove(ign);
                return ign2uuid(ign);
            } else {
                return player.getString("id");
            }
        }
    }

    public static String http_get(String url) {
        URL warlog_url;
        HttpsURLConnection conn;
        try {
            warlog_url = new URL(url);
            conn = (HttpsURLConnection) warlog_url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } catch (Exception e) {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder input = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                input.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return input.toString();
    }
}
