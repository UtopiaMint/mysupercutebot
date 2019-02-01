package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class GuildWarLeaderboardHandler extends AbstractHandler {
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
        try (PreparedStatement stmt = conn.prepareStatement("select guild, total, won from war_log_aggregated order by total desc limit 10 offset ?")) {
            stmt.setInt(1, page * 10);
            ResultSet rs = stmt.executeQuery();
            JSONObject resp = new JSONObject().put("success", true);
            JSONArray guilds = new JSONArray();
            while (rs.next()) {
                guilds.put(new JSONObject().put("guild", rs.getString(1)).put("won", rs.getInt(3)).put("total", rs.getInt(2)));
            }
            resp.put("guilds", guilds);
            return new HttpResponse().setRcode(200).setResponse(resp.toString());
        }
    }
}
