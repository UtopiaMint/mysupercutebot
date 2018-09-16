package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class GuildWarLogHandler extends AbstractHandler {
    @Override
    public HttpResponse realhandle(HttpExchange exchange) throws Exception {
        // get by guild
        String query = exchange.getRequestURI().getQuery();
        query = query == null ? "" : query;
        String[] params = query.split("&");
        HashMap<String, String> _POST = new HashMap<>();
        for (String param : params) {
            String[] _param = param.split("=");
            switch (_param.length) {
                case 1:
                    _POST.put(decodeURIComponent(_param[0]), "");
                    break;
                case 2:
                    _POST.put(decodeURIComponent(_param[0]), decodeURIComponent(_param[1]));
                    break;
            }
        }
        String guild = exchange.getRequestURI().getPath().substring(18);
        String _before = _POST.get("before");
        if (_before == null) {
            _before = "2147483647";
        }
        int before = Integer.parseInt(_before);
        Connection conn = DatabaseHelper.getConnection();
        PreparedStatement stmt = conn.prepareStatement("select w.server, w.guild, w.start_time, w.end_time, t.defender, t.terr_name, t.acquired from war_log w left join terr_log t on w.terr_entry=t.id where w.guild=? and w.start_time<? order by w.start_time desc limit 5;");
        stmt.setString(1, guild);
        stmt.setInt(2, before);
        ResultSet rs = stmt.executeQuery();
        JSONObject resp = new JSONObject();
        resp.put("success", "true");
        JSONArray array = new JSONArray();
        int now = (int) (System.currentTimeMillis() / 1000);
        while (rs.next()) {
            JSONObject data = new JSONObject();
            data.put("server", rs.getString(1));
            data.put("guild", rs.getString(2));
            data.put("start_time", rs.getInt(3));
            data.put("end_time", rs.getInt(4));
            data.put("defender", rs.getString(5));
            data.put("terr_name", rs.getString(6));
            data.put("acquired", rs.getInt(7));
            data.put("verdict", rs.getString(6) != null || rs.getInt(4) + 120 >= now);
            array.put(data);
        }
        resp.put("data", array);
        return new HttpResponse().setRcode(200).setResponse(resp.toString());
    }
}
