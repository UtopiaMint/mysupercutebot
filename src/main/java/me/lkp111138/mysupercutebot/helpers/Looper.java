package me.lkp111138.mysupercutebot.helpers;

import me.lkp111138.mysupercutebot.objects.War;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.lkp111138.mysupercutebot.helpers.Functions.guildInfo;
import static me.lkp111138.mysupercutebot.helpers.Functions.http_get;

public class Looper extends Thread {
    private JSONObject warlog_cache;
    private static JSONObject terrlog_cache;
    private int last_xp_log = -1;
    private List<String> war_servers = new ArrayList<>();
    private ExecutorService[] executor = new ExecutorService[]{Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor()};

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        for (;;) {
            long last_run = System.currentTimeMillis();
            Date date = new Date(last_run);
            SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss.SSS");
            System.out.printf("%s / %d]\n", sdf.format(date), date.getTime());
            // player war log
            executor[0].submit(this::warlog);
            // terr log
            executor[1].submit(this::terrlog);
            // xp log
            executor[2].submit(this::xplog);
            try {
                sleep(Math.max(0, last_run + 20000 - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void newguilds() {
        Connection conn = DatabaseHelper.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("select tag, guild from guild_tag")) {
            ResultSet rs = stmt.executeQuery();
            HashMap<String, String> guilds = new HashMap<>();
            while (rs.next()) {
                guilds.put(rs.getString(2), rs.getString(1));
            }
            JSONArray guild_arr = new JSONObject(http_get("https://api.wynncraft.com/public_api.php?action=guildList")).getJSONArray("guilds");
            Iterator<Object> it = guild_arr.iterator();
            while (it.hasNext()) {
                String g = (String) it.next();
                if (guilds.containsKey(g)) {
                    guilds.remove(g);
                    it.remove();
                }
            }
            // remaining in guilds are deleted guilds
            for (String g : guilds.keySet()) {
                try (PreparedStatement stmt1 = conn.prepareStatement("delete from guild_tag where guild=?")) {
                    stmt1.setString(1, g);
                    stmt1.execute();
                }
            }
            // remaining in guild_arr are new guilds
            int size = Math.min(guild_arr.length(), 20);
            for (int i = 0; i < size; i++) {
                String g = guild_arr.getString(i);
                System.out.println("\"" + g + "\"");
                JSONObject guild = guildInfo(g);
                try (PreparedStatement stmt1 = conn.prepareStatement("insert into guild_tag (guild, tag) values (?, ?)")) {
                    stmt1.setString(1, g);
                    try {
                        stmt1.setString(2, guild.getString("prefix"));
                        stmt1.execute();
                    } catch (JSONException e) {
                        System.out.println("\"" + g + "\"");
                        System.out.println(guild);
                    }
                }
            }
            System.out.printf("new guild count: %d, deleted guild count: %d\n", guild_arr.length(), guilds.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void xplog() {
        String _xp = null;
        try {
            xp_log();
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
        String _terr_list;
        try {
            _terr_list = http_get("https://api.wynncraft.com/public_api.php?action=territoryList");
            JSONObject terr_list = new JSONObject(_terr_list);
            if (terr_list.has("territories")) {
                JSONObject cache = terrlog_cache;
                terrlog_cache = terr_list;
                terrlog_log(cache, terr_list);
            } else {
                System.out.println(terr_list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void warlog_log(JSONObject last, JSONObject now) {
        if (now == null || last == null) {
            return;
        }
        // normal -> war
        List<String> removed = new ArrayList<>();
        int ts = now.getJSONObject("request").getInt("timestamp");
        for (String server : war_servers) {
            if (!now.has(server)) {
                // war server closed
                try {
                    War.byServer(server).close(ts); // cuz the war object may close itself
                } catch (Exception ignored) {
                }
                removed.add(server);
            }
        }
        war_servers.removeAll(removed);
        for (String i : now.keySet()) {
            if (!i.startsWith("WAR")) continue;
            if (!war_servers.contains(i)) {
                new War(i, ts);
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
                        war.setGuild(Functions.playerInfo(name).getJSONObject("guild").getString("name"), ts);
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

    private void xp_log() {
        int _ts = (int) (System.currentTimeMillis() / 1000);
        if (_ts > last_xp_log + 1800) {
            System.out.printf("now is %d %d and last xp log is %d, refreshing\n", System.currentTimeMillis(), _ts, last_xp_log);
            String _data = http_get("https://api.wynncraft.com/public_api.php?action=statsLeaderboard&type=guild&timeframe=alltime");
            JSONObject data = new JSONObject(_data);
            int ts = data.getJSONObject("request").getInt("timestamp");
            last_xp_log = ts;
            Connection conn = DatabaseHelper.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement("insert into xp_log (timestamp, payload) values (?, ?)")) {
                JSONArray payload = data.getJSONArray("data");
                for (int i = 0; i < payload.length(); ++i) {
                    JSONObject guild = payload.getJSONObject(i);
                    guild.remove("created");
                    guild.remove("banner");
                    guild.remove("membersCount");
                }
                stmt.setInt(1, ts);
                stmt.setString(2, payload.toString());
                stmt.execute();
                // look for new guilds
                long start = System.currentTimeMillis();
                newguilds();
                System.out.println(System.currentTimeMillis() - start);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.printf("now is %d %d and last xp log is %d, not refreshing\n", System.currentTimeMillis(), _ts, last_xp_log);
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
        return date.getTime() + tz.getRawOffset() + 14400000; // wynn is at gmt-5 now smh // edit: -4 now
    }

    public static String tag2name(String tag) {
        Connection conn = DatabaseHelper.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("select guild from guild_tag where tag=?")) {
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

    static int terr_count(String guild) {
        int count = 0;
        for (String i : terrlog_cache.getJSONObject("territories").keySet()) {
            JSONObject terr0 = terrlog_cache.getJSONObject("territories").getJSONObject(i);
            if (terr0.getString("guild").equals(guild)) {
                ++count;
            }
        }
        return count;
    }
}
