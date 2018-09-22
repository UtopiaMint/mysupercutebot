package me.lkp111138.mysupercutebot.helpers;

import me.lkp111138.mysupercutebot.objects.War;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static me.lkp111138.mysupercutebot.helpers.Functions.http_get;

public class Looper extends Thread {
    private JSONObject warlog_cache;
    private JSONObject terrlog_cache;
    private int last_xp_log = -1;
    private List<String> war_servers = new ArrayList<>();

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        for (;;) {
            long last_run = System.currentTimeMillis();
            Date date = new Date(last_run);
            SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss.SSS");
            System.out.printf("%s / %d]\n", sdf.format(date), date.getTime());
            // player war log
            warlog();
            // terr log
            terrlog();
            // xp log
            xplog();
            try {
                sleep(Math.max(0, last_run + 20000 - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void xplog() {
        String _xp = null;
        try {
            _xp = http_get("https://api.wynncraft.com/public_api.php?action=statsLeaderboard&type=guild&timeframe=alltime");
            JSONObject xp = new JSONObject(_xp);
            xp_log(xp);
        } catch (Exception e) {
            System.out.println(_xp);
            e.printStackTrace();
        }
    }

    private void warlog() {
        String _online = null;
        try {
            _online = http_get("https://api.wynncraft.com/public_api.php?action=onlinePlayers");
            JSONObject online = new JSONObject(_online);
            warlog_log(warlog_cache, online);
            warlog_cache = online;
        } catch (Exception e) {
            System.out.println(_online);
            e.printStackTrace();
        }
    }

    private void terrlog() {
        String _terr_list = null;
        try {
            _terr_list = http_get("https://api.wynncraft.com/public_api.php?action=territoryList");
            JSONObject terr_list = new JSONObject(_terr_list);
            terrlog_log(terrlog_cache, terr_list);
            terrlog_cache = terr_list;
        } catch (Exception e) {
            System.out.println(_terr_list);
            e.printStackTrace();
        }
    }

    private void warlog_log(JSONObject last, JSONObject now) {
        if (now == null || last == null) return;
        // normal -> war
        List<String> removed = new ArrayList<>();
        for (String server : war_servers) {
            if (!now.has(server)) {
                // war server closed
                try {
                    War.byServer(server).close(now.getJSONObject("request").getInt("timestamp")); // cuz the war object may close itself
                } catch (Exception ignored) {
                }
                removed.add(server);
            }
        }
        war_servers.removeAll(removed);
        for (String i : now.keySet()) {
            if (!i.startsWith("WAR")) continue;
            if (!war_servers.contains(i)) {
                new War(i, now.getJSONObject("request").getInt("timestamp"));
                war_servers.add(i);
            }
            JSONArray players = now.getJSONArray(i);
            System.out.printf("%s: %s\n", i, players.toString());
            War war = War.byServer(i);
            if (war == null) continue;
            for (int j = 0; j < players.length(); ++j) {
                String name = players.getString(j);
                if (war.getGuild() == null) {
                    try {
                        war.setGuild(Functions.playerInfo(name).getJSONObject("guild").getString("name"));
                    } catch (Exception ignored){
                    }
                }
            }
            war.putPlayerList(players, now.getJSONObject("request").getInt("timestamp"));
        }
        System.out.print("\n");
    }

    private void terrlog_log(JSONObject last, JSONObject now) {
        if (now == null || last == null) return;
        for (String i : last.getJSONObject("territories").keySet()) {
            JSONObject terr0 = last.getJSONObject("territories").getJSONObject(i);
            JSONObject terr1 = now.getJSONObject("territories").getJSONObject(i);
            if (!terr0.getString("guild").equals(terr1.getString("guild"))) {
                long acquired = date2long(terr1.getString("acquired"));
                long ms = acquired - date2long(terr0.getString("acquired"));
                DatabaseHelper.terr_log(i, terr0.getString("guild"), terr1.getString("guild"), (int) (acquired / 1000), (int) (ms / 1000));
            }
        }
    }

    private void xp_log(JSONObject data) {
        int ts = data.getJSONObject("request").getInt("timestamp");
        if (ts > last_xp_log + 1800) {
            last_xp_log = ts;
            try {
                Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement("insert into xp_log (timestamp, payload) values (?, ?)");
                JSONArray payload = data.getJSONArray("data");
                stmt.setInt(1, ts);
                stmt.setString(2, payload.toString());
                stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private long date2long(String data) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date date;
        try {
            date = formatter.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        TimeZone tz = Calendar.getInstance().getTimeZone();
        return date.getTime() + tz.getRawOffset() + 14400000; // wynn is at gmt-4
    }

    public static String tag2name(String tag) {
        Connection conn = DatabaseHelper.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("select guild from guild_tag where tag=?");
            stmt.setString(1, tag);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }
}
