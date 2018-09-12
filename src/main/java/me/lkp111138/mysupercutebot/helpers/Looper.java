package me.lkp111138.mysupercutebot.helpers;

import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static me.lkp111138.mysupercutebot.helpers.Functions.ign2uuid;
import static me.lkp111138.mysupercutebot.helpers.Functions.playerInfo;

public class Looper extends Thread {
    private long last_run = 0;
    private JSONObject warlog_cache;
    private JSONObject terrlog_cache;

    @Override
    public void run() {
        for (;;) {
            last_run = System.currentTimeMillis();
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
        for (String i : now.keySet()) {
            if (!i.startsWith("WAR")) continue;
            for (int j = 0; j < now.getJSONArray(i).length(); ++j) {
                String name = now.getJSONArray(i).getString(j);
                for (String server : last.keySet()) {
                    if (server.equals("request")) continue;
                    if (last.getJSONArray(server).toList().indexOf(name) > 0) {
                        if (!server.equals(i)) {
                            System.out.printf("%s: %s -> %s\n", name, server, i);
                            String guild = "???";
                            try {
                                guild = playerInfo(name).getJSONObject("guild").getString("name");
                            } catch (Exception ignored) {
                            }
                            DatabaseHelper.insert_warlog(ign2uuid(name), name, guild, now.getJSONObject("request").getInt("timestamp"), server, i);
                        }
                        break;
                    }
                }
            }
        }
        // war -> normal
        // TODO: detect war servers appearing as a sign of a new war; give verdict when war ends
        List<String> winners = new ArrayList<>();
        for (String i : now.keySet()) {
            if (!i.startsWith("WC")) continue;
            System.out.printf("processing server %s\n", i);
            for (int j = 0; j < now.getJSONArray(i).length(); ++j) {
                String name = now.getJSONArray(i).getString(j);
                for (String server : last.keySet()) {
                    if (server.equals("request") || server.startsWith("WC") || server.startsWith("lobby")) continue; // why should we care about people switch servers for csst?
                    if (last.getJSONArray(server).toList().indexOf(name) > 0) {
                        if (!server.equals(i)) {
                            System.out.printf("%s: %s -> %s\n", name, server, i);
//                            DatabaseHelper.insert_warlog(ign2uuid(name), playerInfo(name).getJSONObject("guild").getString("name"), server, i);
                            // now we determine the verdict
                            // if the war server isn't empty yet, you're dead
                            if (now.has(server) && now.getJSONArray(server).length() > 0) {
                                DatabaseHelper.war_verdict(ign2uuid(name), server, false, null);
                            } else {
                                // so the server is empty, you probably survived, lets check for a recent terr gain event of your guild
                                String guild = "???";
                                try {
                                    guild = playerInfo(name).getJSONObject("guild").getString("name");
                                } catch (Exception ignored) {
                                }
                                boolean won = winners.contains(guild);
                                ResultSet rs = won ? null : DatabaseHelper.query("select count(*) from territory_log where attacker=? and acquired_ts<?", new String[]{guild, String.valueOf(System.currentTimeMillis() / 1000 - 75)});
                                try {
                                    if (won || rs != null && rs.next() && rs.getInt(1) == 1) {
                                        // congrats for winning
                                        DatabaseHelper.war_verdict(ign2uuid(name), server, true, true);
                                    } else {
                                        DatabaseHelper.war_verdict(ign2uuid(name), server, false, false);
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        break;
                    }
                }
            }
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
