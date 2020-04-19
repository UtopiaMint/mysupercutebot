package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TerritoriesHandler extends AbstractHandler {
    @Override
    public HttpResponse realhandle(HttpExchange exchange) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("select attacker, terr_name, acquired from terr_log where id in(select max(id) from terr_log where hold_time>0 group by terr_name)")) {
                ResultSet rs = stmt.executeQuery();
                JSONObject resp = new JSONObject();
                JSONObject terrs = new JSONObject();
                while (rs.next()) {
                    JSONObject terr = new JSONObject();
                    terr.put("territory", rs.getString(2));
                    terr.put("guild", rs.getString(1));
                    terr.put("acquired", rs.getInt(3));
                    terrs.put(rs.getString(2), terr);
                }
                resp.put("territories", terrs);
                return new HttpResponse().setRcode(200).setResponse(resp.toString());
            }
        }
    }
}
