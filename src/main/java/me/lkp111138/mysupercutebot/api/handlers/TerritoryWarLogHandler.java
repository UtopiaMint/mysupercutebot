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

import static me.lkp111138.mysupercutebot.helpers.Looper.tag2name;

public class TerritoryWarLogHandler extends AbstractHandler {
    @Override
    public HttpResponse realhandle(HttpExchange exchange) throws Exception {
        // get by guild
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
        String terr = exchange.getRequestURI().getPath().substring(17);
//        System.out.println(terr);
        try (Connection conn = DatabaseHelper.getConnection()) {
            if (terr.length() == 3) {
                // is a tag
                terr = tag2name(terr);
            }
            String _offset = _GET.get("offset");
            List<Integer> war_ids = new ArrayList<>();
            JSONArray array = new JSONArray();
            JSONObject resp = new JSONObject();
            try (PreparedStatement stmt = conn.prepareStatement("select w.server, w.guild, w.start_time, w.end_time, t.defender, t.terr_name, t.acquired, w.id from war_log w left join terr_log t on w.terr_entry=t.id where t.terr_name=? order by w.start_time desc limit 5 offset ?;")) {
                stmt.setString(1, terr);
                if (_offset != null) {
                    stmt.setInt(2, Integer.parseInt(_offset));
                } else {
                    stmt.setInt(2, 0);
                }
                ResultSet rs = stmt.executeQuery();
                resp.put("success", "true");
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
                    data.put("verdict", rs.getString(6) != null || (rs.getInt(4) > 0 && rs.getInt(4) + 120 <= now));
                    data.put("players", new JSONArray());
                    war_ids.add(rs.getInt(8));
                    array.put(data);
                }
            }
            if (war_ids.size() > 0) {
                List<String> q_marks = new ArrayList<>();
                for (int ignored : war_ids) {
                    q_marks.add("?");
                }
                try (PreparedStatement stmt = conn.prepareStatement("select war_id, ign, survived, won from player_war_log where war_id in (" + String.join(",", q_marks) + ")")) {
                    for (int i = 0; i < war_ids.size(); ++i) {
                        stmt.setInt(i + 1, war_ids.get(i));
                    }
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        array.getJSONObject(war_ids.indexOf(rs.getInt(1))).getJSONArray("players").put(new JSONObject().put("player", rs.getString(2)).put("survived", rs.getInt(3)).put("won", rs.getInt(4)));
                    }
                }
            }
            resp.put("wars", array);
            return new HttpResponse().setRcode(200).setResponse(resp.toString());
        }
    }
}
