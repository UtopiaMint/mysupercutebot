package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import me.lkp111138.mysupercutebot.helpers.Functions;
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
        String _after = _GET.get("after");
        String terr = _GET.get("terr");
        Connection conn = DatabaseHelper.getConnection();
        String uuid = Functions.ign2uuid(player);
        String player_guild = Functions.playerInfo(player).getJSONObject("guild").getString("name");
        PreparedStatement stmt;
        String clauses = "";
        if (terr != null) {
            clauses += " and t.terr_name=?";
        }
        if (_before != null) {
            clauses += " and w.start_time<?";
        }
        if (_after != null) {
            clauses += " and w.start_time>?";
        }
        stmt = conn.prepareStatement("SELECT w.id, p.ign, p.guild, p.survived, p.won, w.server, w.start_time, w.end_time, t.defender, t.terr_name, t.acquired FROM player_war_log p left join war_log w on p.war_id=w.id left join terr_log t on w.terr_entry=t.id WHERE UUID=? and w.guild=?" + clauses + " ORDER BY w.id DESC LIMIT 5;");
        stmt.setString(1, uuid);
        stmt.setString(2, player_guild);
        int arg = 3;
        if (terr != null) {
            stmt.setString(arg++, terr);
        }
        if (_before != null) {
            stmt.setInt(arg++, Integer.parseInt(_before));
        }
        if (_after != null) {
            stmt.setInt(arg, Integer.parseInt(_after));
        }
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
            data.put("verdict", rs.getString(10) != null || (rs.getInt(8) > 0 && rs.getInt(8) + 120 <= now));
            data.put("players", new JSONArray());
            array.put(data);
        }
        stmt.close();
        if (war_ids.size() > 0) {
            List<String> q_marks = new ArrayList<>();
            for (int ignored : war_ids) {
                q_marks.add("?");
            }
            stmt = conn.prepareStatement("select war_id, ign, survived, won from player_war_log where war_id in (" + String.join(",", q_marks) + ")");
            for (int i = 0; i < war_ids.size(); ++i) {
                stmt.setInt(i + 1, war_ids.get(i));
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                array.getJSONObject(war_ids.indexOf(rs.getInt(1))).getJSONArray("players").put(new JSONObject().put("player", rs.getString(2)).put("survived", rs.getInt(3)).put("won", rs.getInt(4)));
            }
        }
        resp.put("wars", array);
        stmt = conn.prepareStatement("select total, won, survived from player_war_log_aggregated where uuid=? and guild=?");
        stmt.setString(1, uuid);
        stmt.setString(2, player_guild);
        rs = stmt.executeQuery();
        if (rs.next()) {
            resp.put("aggregated", new JSONObject().put("total", rs.getInt(1)).put("won", rs.getInt(2)).put("survived", rs.getInt(3)).put("guild", player_guild));
        } else  {
            // no row
            resp.put("aggregated", JSONObject.NULL);
        }
        return new HttpResponse().setRcode(200).setResponse(resp.toString());
    }
}
