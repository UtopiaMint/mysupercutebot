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
        private long[] lvl_xp = new long[]{0,2200L,6864L,-1,30976L,56428L,97994L,163300L,264844L,419560L,602448L,852324L,1274800L,1741568L,2393896L,3178888L,4395776L,5822952L,7680496L,10094700L,12576960L,15271408L,18176328L,21296324L,-1,28540928L,34120944L,40238272L,48132760L,56424720L,65569680L,76150072L,94969036L,106260696L,-1,140582440L,-1,-1,-1,250716864L,302589056L,372900328L,439287340L,528448652L,635079244L,762509284L,914665840L,1074729260L,1262236800L,1481808020L,1738858000L,2039683800L,2391634960L,2803273880L,3284594280L,3847231300L,4504758720L,5272982220L,6170314920L,7218203960L,-1,9030089860L,-1,11282040000L,12601600000L,14085500000L,15725160000L,17570861220L,-1,19616419680L,24424400000L,27261351700L};

        private long gained() {
            long gained = now_xp - old_xp;
            for (int i = old_lvl; i < now_lvl; ++i) {
                try {
                    long _lvl_xp = lvl_xp[i];
                    if (_lvl_xp < 0) {
                        _lvl_xp = (long) Math.sqrt(lvl_xp[i - 1] * lvl_xp[i + 1]);
                    }
                    gained += _lvl_xp;
                } catch (ArrayIndexOutOfBoundsException e) {
                    return 0;
                }
            }
            return gained;
        }
    }
}
