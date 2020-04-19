package me.lkp111138.mysupercutebot.api.discord.commands;

import me.lkp111138.mysupercutebot.api.discord.message.PaginatedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

import static me.lkp111138.mysupercutebot.helpers.Functions.http_get;

public class XpLogCommand implements CommandHandler {
    @Override
    public PaginatedMessage process(MessageReceivedEvent evt, String cmd, String rest, PaginatedMessage pmsg) {
        String _data = http_get("http://localhost:6080/v1/xpgain");
        JSONObject data = new JSONObject(_data);
        JSONArray guilds = data.getJSONArray("guilds");
        StringBuilder sb = new StringBuilder("```ml\n");
        int longest = guilds.toList().stream().map(x -> (Map) x).mapToInt(x -> x.get("name").toString().length()).max().getAsInt();
        sb.append("  # | Guild");
        for (int i = 5; i < longest; i++) {
            sb.append(" ");
        }
        sb.append(" | Lvl | Gained\n");
        for (int i = -21; i < longest; i++) {
            sb.append("-");
        }
        sb.append("\n");
        for (int i = 0; i < 20; i++) {
            JSONObject guild = guilds.getJSONObject(i);
            sb.append(String.format("% 3d | %" + longest + "s | % 3d | %s\n", i + 1, guild.getString("name"), guild.getInt("level"), formatNumber(guild.getLong("gained"))));
        }
        sb.append("```");
        System.out.println(sb);
        evt.getMessage().getChannel().sendMessage(sb.toString()).submit();
        return null;
    }

    private static String formatNumber(Long n) {
        BigDecimal bd = new BigDecimal(n);
        n = bd.round(new MathContext(4)).longValue();
        if (n >= 1000000000) {
            return String.format("%f", n / 1000000000.0).substring(0, 5) + "B";
        }
        if (n >= 1000000) {
            return String.format("%f", n / 1000000.0).substring(0, 5) + "M";
        }
        if (n >= 1000) {
            return String.format("%f", n / 1000.0).substring(0, 5) + "K";
        }
        return n.toString();
    }
}
