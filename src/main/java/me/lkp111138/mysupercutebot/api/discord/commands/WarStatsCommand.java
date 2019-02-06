package me.lkp111138.mysupercutebot.api.discord.commands;

import me.lkp111138.mysupercutebot.api.discord.Functions;
import me.lkp111138.mysupercutebot.api.discord.message.PaginatedMessage;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static me.lkp111138.mysupercutebot.helpers.Functions.http_get;

public class WarStatsCommand implements CommandHandler {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public PaginatedMessage process(MessageReceivedEvent evt, String cmd, String rest, PaginatedMessage pmsg) {
        // rest = player name
        int page = pmsg == null ? 0 : pmsg.getCurrentPage();
        String _data = http_get("https://api.tddbot.ml/v1/warlogs/guild/" + rest + "?offset=" + page * 5);
        JSONObject data = new JSONObject(_data);
        MessageAction action;
        StringBuilder sb;
        if (data.has("wars") && data.getJSONArray("wars").length() > 0) {
            int won = data.getJSONObject("aggregated").getInt("won");
            int total = data.getJSONObject("aggregated").getInt("total");
            int num_pages = (int) Math.ceil(total / 5.0);
            sb = new StringBuilder("```ml\n----- War Log -----\n\nGuild: ").append(data.getJSONArray("wars").getJSONObject(0).getString("guild"));
            sb.append("\nSuccess Wars: ").append(won).append(" / ").append(total);
            sb.append(" (").append(String.format("%.2f", 100.0 * won / total)).append("%)");
            sb.append("\n\n");
            JSONArray wars = data.getJSONArray("wars");
            for (int i = 0, ii = wars.length(); i < ii; i++) {
                sb.append(format_war(i + 1 + 5 * page, wars.getJSONObject(i)));
            }
            sb.append("\n```");
            sb.append("\nUpdated as of ").append(sdf.format(System.currentTimeMillis()));
            sb.append("\nTimezone offset: ").append(new SimpleDateFormat("XXX").format(new Date()));
            if (pmsg != null) {
                evt.getChannel().editMessageById(pmsg.getMsgId(), sb.toString()).complete();
                return null;
            } else {
                Message message = evt.getChannel().sendMessage(sb.toString()).complete();
                return new PaginatedMessage(evt, cmd, rest, message, num_pages);
            }
        } else {
            action = evt.getChannel().sendMessage("There are no war logs recorded for this guild.");
            action.complete();
            return null;
        }
    }

    private String format_war(int id, JSONObject war) {
        return Functions.getString(id, war, sdf);
    }
}
