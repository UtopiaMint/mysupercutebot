package me.lkp111138.mysupercutebot.api.discord;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class Functions {
    @NotNull
    public static String getString(int id, JSONObject war, SimpleDateFormat sdf) {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(". ");
        int margin = sb.length();
        sb.append(war.getString("server")).append(" : ");
        if (war.getBoolean("verdict")) {
            sb.append(war.optString("terr_name", "(Lost)"));
        } else {
            sb.append("Ongoing...");
        }
        sb.append("\n");
        if (war.optString("terr_name", null) != null) {
            for (int i = 0; i < margin; i++) {
                sb.append(' ');
            }
            sb.append(war.getString("defender")).append(" -> ").append(war.getString("guild"));
            sb.append("\n");
        }
        for (int i = 0; i < margin; i++) {
            sb.append(' ');
        }
        sb.append("Time: ").append(sdf.format(war.getInt("start_time") * 1000L)).append(" - ").append(sdf.format(war.getInt("end_time") * 1000L));
        sb.append("\n");
        for (int i = 0; i < margin; i++) {
            sb.append(' ');
        }
        sb.append("Players: ");
        JSONArray players = war.getJSONArray("players");
        for (int i = 0, ii = players.length(); i < ii; i++) {
            JSONObject p = players.getJSONObject(i);
            if (p.getInt("survived") == 1 || !war.getBoolean("verdict")) {
                sb.append(p.getString("player"));
            } else {
                sb.append("(").append(p.getString("player")).append(")");
            }
            if (i < ii - 1) {
                sb.append(", ");
            }
        }
        return sb.append("\n\n").toString();
    }
}
