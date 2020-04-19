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

import static me.lkp111138.mysupercutebot.helpers.Looper.tag2name;

public class GuildPlayerWarLeaderboardHandler extends AbstractHandler {
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
        String guild = exchange.getRequestURI().getPath().substring(30);
        try (Connection conn = DatabaseHelper.getConnection()) {
            JSONArray players = new JSONArray();
            if (guild.length() == 3) {
                // is a tag
                guild = tag2name(guild);
            }
            int page = Integer.parseInt(_page);
            JSONObject resp = new JSONObject().put("success", true);
            HashMap<String, String> names = new HashMap<>();
            ArrayList<String> q_marks = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement("select p.uuid, p.count_total, p.count_won, p.count_survived, p.ign from player_war_total p where p.guild=? order by p.count_total desc, p.count_won desc limit 10 offset ?")) {
                stmt.setInt(2, page * 10);
                stmt.setString(1, guild);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    players.put(new JSONObject().put("uuid", rs.getString(1)).put("won", rs.getInt(3)).put("total", rs.getInt(2)).put("survived", rs.getInt(4)).put("player", rs.getString(5)));
                    names.put(rs.getString(1), "");
                    q_marks.add("?");
                }
                // get the ign
            }
            resp.put("players", players);
            return new HttpResponse().setRcode(200).setResponse(resp.toString());
        }
    }
}
