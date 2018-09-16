package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerWarLogHandler extends AbstractHandler {
    @Override
    public HttpResponse realhandle(HttpExchange exchange) throws Exception {
        // get by player
        String query = exchange.getRequestURI().getQuery();
        query = query == null ? "" : query;
        String[] params = query.split("&");
        HashMap<String, String> _GET = new HashMap<>();
        for (String param : params) {
            String[] _param = param.split("=");
            switch (_param.length) {
                case 1:
                    _GET.put(decodeURIComponent(_param[0]), "");
                    break;
                case 2:
                    _GET.put(decodeURIComponent(_param[0]), decodeURIComponent(_param[1]));
                    break;
            }
        }
        String player = exchange.getRequestURI().getPath().substring(19);
        String _before = _GET.get("before");
        if (_before == null) {
            _before = "2147483647";
        }
        int before = Integer.parseInt(_before);
        Connection conn = DatabaseHelper.getConnection();
        PreparedStatement stmt = conn.prepareStatement("select w.id, p.ign, p.guild, p.survived, p.won, w.server, w.start_time, w.end_time, t.defender, t.terr_name, t.acquired from player_war_log p, war_log w, terr_log t where uuid=(select uuid from player_war_log where ign=? order by id desc limit 1) and p.war_id=w.id and w.terr_entry=t.id and w.start_time<? order by w.id desc limit 5;");
        stmt.setString(1, player);
        stmt.setInt(2, before);
        ResultSet rs = stmt.executeQuery();
        JSONObject resp = new JSONObject();
        resp.put("success", "true");
        JSONArray array = new JSONArray();
        int now = (int) (System.currentTimeMillis() / 1000);
        List<Integer> war_ids = new ArrayList<>();
        while (rs.next()) {
            JSONObject data = new JSONObject();
            war_ids.add(rs.getInt(1));
            data.put("player", rs.getString(2));
            data.put("guild", rs.getString(3));
            data.put("survived", rs.getInt(4));
            data.put("won", rs.getInt(5));
            data.put("server", rs.getString(6));
            data.put("start_time", rs.getInt(7));
            data.put("end_time", rs.getInt(8));
            data.put("defender", rs.getString(9));
            data.put("terr_name", rs.getString(10));
            data.put("acquired", rs.getInt(11));
            data.put("verdict", rs.getString(10) != null || rs.getInt(8) + 120 >= now);
            data.put("players", new JSONArray());
            array.put(data);
        }
        if (war_ids.size() > 0) {
            List<String> q_marks = new ArrayList<>();
            for (int ignored : war_ids) {
                q_marks.add("?");
            }
            stmt.close();
            System.out.println("select war_id, ign, survived, won from player_war_log where war_id in (" + String.join(",", q_marks) + ")");
            stmt = conn.prepareStatement("select war_id, ign, survived, won from player_war_log where war_id in (" + String.join(",", q_marks) + ")");
            for (int i = 0; i < war_ids.size(); ++i) {
                stmt.setInt(i + 1, war_ids.get(i));
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                array.getJSONObject(war_ids.indexOf(rs.getInt(1))).getJSONArray("players").put(new JSONObject().put("player", rs.getString(2)).put("survived", rs.getInt(3)).put("won", rs.getInt(4)));
            }
        }
        resp.put("data", array);
        return new HttpResponse().setRcode(200).setResponse(resp.toString());
    }
}
