package me.lkp111138.mysupercutebot.helpers;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static me.lkp111138.mysupercutebot.helpers.Looper.tag2name;

public class Functions {
    private static Map<String, JSONObject> guild_cache = new HashMap<>();
    private static Map<String, JSONObject> player_cache = new HashMap<>();
    private static Map<String, JSONObject> uuid_cache = new HashMap<>();
    private static OkHttpClient client = new OkHttpClient.Builder().readTimeout(15, TimeUnit.SECONDS).build();


    public static JSONObject guildInfo(String name) {
        if (name.length() == 3) {
            return guildInfo(tag2name(name));
        }
        JSONObject guild = guild_cache.get(name);
        if (guild == null) {
            // have to claim to cute to make up for trailing spaces smh
            JSONObject obj = new JSONObject(http_get("https://api.wynncraft.com/public_api.php?action=guildStats&command=" + name + "&cute=true"));
            if (obj.has("request")) {
                guild_cache.put(name, obj);
            }
            return obj;
        } else {
            int ts = 0;
            try {
                ts = guild.getJSONObject("request").getInt("timestamp");
            } catch (JSONException ignored) {
            }
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
            String uuid = ign2uuid(name); // fuck you wynn for not issuing that damn update statement on legitmate joins
            uuid = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
            System.out.println(uuid);
            JSONObject obj = new JSONObject(http_get("https://api.wynncraft.com/v2/player/" + uuid + "/stats"));
            player_cache.put(name, obj);
            return obj;
        } else {
            long ts = 0;
            try {
                ts = player.getLong("timestamp");
            } catch (JSONException ignored) {
            }
            if (ts < System.currentTimeMillis() - 120000) { // cache player info for 2mins
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

    public static String http_get(String url) {
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
            System.out.printf("%d http_get fail %s\n", System.currentTimeMillis(), url);
            e.printStackTrace();
            return "{}";
        }
    }
}
