package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;

public class XpGainLeaderboardHandler extends AbstractHandler {
    @Override
    public HttpResponse realhandle(HttpExchange exchange) throws Exception {
        Connection conn = DatabaseHelper.getConnection();
        try (PreparedStatement stmt1 = conn.prepareStatement("select * from xp_log order by abs(unix_timestamp()-timestamp-86400) asc limit 1")) {
            try (PreparedStatement stmt2 = conn.prepareStatement("select * from xp_log order by timestamp desc limit 1")) {
                ResultSet rs1 = stmt1.executeQuery();
                ResultSet rs2 = stmt2.executeQuery();
                rs1.next();
                rs2.next();
                JSONArray now = new JSONArray(rs2.getString(2));
                JSONArray old = new JSONArray(rs1.getString(2));
                HashMap<String, XpLogInfo> xp_log = new HashMap<>();
                for (int i = 0, ii = old.length(); i < ii; ++i) {
                    String tag = old.getJSONObject(i).getString("prefix");
                    XpLogInfo info;
                    if (!xp_log.containsKey(tag)) {
                        info = new XpLogInfo();
                        xp_log.put(tag, info);
                    } else {
                        info = xp_log.get(tag);
                    }
                    info.tag = tag;
                    info.name = old.getJSONObject(i).getString("name");
                    info.old_lvl = old.getJSONObject(i).getInt("level");
                    info.old_xp = old.getJSONObject(i).getLong("xp");
                }
                for (int i = 0, ii = now.length(); i < ii; ++i) {
                    XpLogInfo info;
                    String tag = now.getJSONObject(i).getString("prefix");
                    if (!xp_log.containsKey(tag)) {
                        info = new XpLogInfo();
                        xp_log.put(tag, info);
                    } else {
                        info = xp_log.get(tag);
                    }
                    info.tag = tag;
                    info.name = now.getJSONObject(i).getString("name");
                    info.now_lvl = now.getJSONObject(i).getInt("level");
                    info.now_xp = now.getJSONObject(i).getLong("xp");
                }
                XpLogInfo[] xp_log2 = xp_log.values().toArray(new XpLogInfo[0]);
                Arrays.sort(xp_log2, (xpLogInfo, t1) -> (int) Math.signum(t1.gained() - xpLogInfo.gained()));
                JSONObject resp = new JSONObject().put("success", true);
                JSONArray data = new JSONArray();
                resp.put("guilds", data);
                for (XpLogInfo info : xp_log2) {
                    JSONObject guild = new JSONObject();
                    guild.put("name", info.name);
                    guild.put("level", info.now_lvl);
                    guild.put("xp", info.now_xp);
                    guild.put("gained", info.gained());
                    data.put(guild);
                }
                resp.put("start", rs1.getInt(1));
                resp.put("end", rs2.getInt(1));
                return new HttpResponse().setResponse(resp.toString()).setRcode(200);
            }
        }
    }

    private class XpLogInfo {
        private String name;
        private String tag;
        private int now_lvl = 0;
        private int old_lvl = 0;
        private long now_xp = 0;
        private long old_xp = 666666666666L;
        private long gained() {
            return Math.max(0, now_xp - old_xp);
        }
    }
}
