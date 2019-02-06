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

public class PlayerWarLeaderboardHandler extends AbstractHandler {
    @Override
    public HttpResponse realhandle(HttpExchange exchange) throws Exception {
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
        String _offset = _GET.get("offset");
        if (_offset == null) {
            _offset = "0";
        }
        int offset = Integer.parseInt(_offset);
        Connection conn = DatabaseHelper.getConnection();
        JSONObject resp = new JSONObject().put("success", true);
        JSONArray players = new JSONArray();
        try (PreparedStatement stmt = conn.prepareStatement("select p.uuid, sum(p.count_total), sum(p.count_won), sum(p.count_survived), p.ign from player_war_total p group by uuid order by sum(p.count_total) desc, sum(p.count_won) desc limit 10 offset ?")) {
            stmt.setInt(1, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                players.put(new JSONObject().put("uuid", rs.getString(1)).put("won", rs.getInt(3)).put("total", rs.getInt(2)).put("survived", rs.getInt(4)).put("player", rs.getString(5)));
            }
            // get the ign
        }
        resp.put("players", players);
        return new HttpResponse().setRcode(200).setResponse(resp.toString());
    }
}
