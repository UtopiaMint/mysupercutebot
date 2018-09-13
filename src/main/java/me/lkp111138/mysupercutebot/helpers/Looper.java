package me.lkp111138.mysupercutebot.helpers;

import me.lkp111138.mysupercutebot.objects.War;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Looper extends Thread {
    private JSONObject warlog_cache;
    private JSONObject terrlog_cache;
//    private List<String> war_servers = new ArrayList<>();
    private List<String> war_servers = new ArrayList<>();

    @Override
    public void run() {
        for (;;) {
            long last_run = System.currentTimeMillis();
            // player war log
            warlog();
            // terr log
            terrlog();
            try {
                sleep(last_run + 20000 - System.currentTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void warlog() {
        JSONObject online = new JSONObject(Functions.http_get("https://api.wynncraft.com/public_api.php?action=onlinePlayers"));
        warlog_log(warlog_cache, online);
        warlog_cache = online;
    }

    private void terrlog() {
        JSONObject terr_list = new JSONObject(Functions.http_get("https://api.wynncraft.com/public_api.php?action=territoryList"));
        terrlog_log(terrlog_cache, terr_list);
        terrlog_cache = terr_list;
    }

    private void warlog_log(JSONObject last, JSONObject now) {
        if (now == null || last == null) return;
        // normal -> war
        for (String server : war_servers) {
            if (!now.has(server)) {
                // war server closed
                War.byServer(server).close();
            }
        }
        for (String i : now.keySet()) {
            if (!i.startsWith("WAR")) continue;
            if (!war_servers.contains(i)) {
                new War(i);
                war_servers.add(i);
            }
            JSONArray players = now.getJSONArray(i);
            War war = War.byServer(i);
            for (int j = 0; j < players.length(); ++j) {
                String name = players.getString(j);
                if (war.getGuild() == null) {
                    try {
                        war.setGuild(Functions.playerInfo(name).getJSONObject("guild").getString("name"));
                    } catch (Exception ignored){
                    }
                }
            }
            war.putPlayerList(players);
        }
    }

    private void terrlog_log(JSONObject last, JSONObject now) {
        if (now == null || last == null) return;
        for (String i : last.getJSONObject("territories").keySet()) {
            JSONObject terr0 = last.getJSONObject("territories").getJSONObject(i);
            JSONObject terr1 = now.getJSONObject("territories").getJSONObject(i);
            if (!terr0.getString("guild").equals(terr1.getString("guild"))) {
                long acquired = date2long(terr1.getString("acquired"));
                long ms = acquired - date2long(terr0.getString("acquired"));
//                console.log(`${i}: ${terr0.guild} -> ${terr1.guild}\nAcquired: ${terr1.acquired}\nHeld for: ${ms / 1000}`);
//                System.out.printf("%s: %s -> %s\nAcquired: %s\nHeld for %d secs\n", i, terr0.getString("guild"), terr1.getString("guild"), terr1.getString("acquired"), ms / 1000);
                DatabaseHelper.terr_log(i, terr0.getString("guild"), terr1.getString("guild"), (int) (acquired / 1000), (int) (ms / 1000));
            }
        }
    }

    private long date2long(String data) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date date = null;
        try {
            date = formatter.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        TimeZone tz = Calendar.getInstance().getTimeZone();
        return date.getTime() + tz.getRawOffset() + 14400000; // wynn is at gmt-4
    }
}
