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
        String _page = _GET.get("page");
        if (_page == null) {
            _page = "0";
        }
        int page = Integer.parseInt(_page);
        Connection conn = DatabaseHelper.getConnection();
        PreparedStatement stmt = conn.prepareStatement("select p.uuid, p.total, p.won, p.survived from player_war_stats p order by p.total desc, p.won desc limit 10 offset ?");
        stmt.setInt(1, page * 10);
        ResultSet rs = stmt.executeQuery();
        JSONObject resp = new JSONObject().put("success", true);
        JSONArray players = new JSONArray();
        HashMap<String, String> names = new HashMap<>();
        ArrayList<String> q_marks = new ArrayList<>();
        while (rs.next()) {
            players.put(new JSONObject().put("uuid", rs.getString(1)).put("won", rs.getInt(3)).put("total", rs.getInt(2)).put("survived", rs.getInt(4)));
            names.put(rs.getString(1), "");
            q_marks.add("?");
        }
        // get the ign
        stmt = conn.prepareStatement("select uuid, ign from player_war_log where uuid in (" + String.join(",", q_marks) + ")");
        int i = 1;
        for (String uuid : names.keySet()) {
            stmt.setString(i++, uuid);
        }
        rs = stmt.executeQuery();
        while (rs.next()) {
            names.put(rs.getString(1), rs.getString(2));
        }
        for (i = 0; i < players.length(); ++i) {
            JSONObject player = players.getJSONObject(i);
            player.put("player", names.get(player.getString("uuid")));
        }
        resp.put("players", players);
        return new HttpResponse().setRcode(200).setResponse(resp.toString());
    }
}
