package me.lkp111138.mysupercutebot.api.discord.commands;

import me.lkp111138.mysupercutebot.api.discord.message.PaginatedMessage;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static me.lkp111138.mysupercutebot.helpers.Functions.guildInfo;

public class TerrTrackCommand implements CommandHandler {
    @Override
    public PaginatedMessage process(MessageReceivedEvent evt, String cmd, String rest, PaginatedMessage pmsg) {
        Connection conn = DatabaseHelper.getConnection();
        if (rest == null) {
            evt.getChannel().sendMessage("Provide a guild to track.").submit();
            return null;
        }
        String track;
        try {
            JSONObject guild = guildInfo(rest);
            track = guild.getString("name");
        } catch (JSONException e) {
            evt.getChannel().sendMessage("The guild `").append(rest).append("` either doesn't exist or have spelling/capitalization errors.").submit();
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement("replace into discord_terr_log (channel_id, guild) VALUES (?, ?)")) {
            stmt.setString(2, track);
            stmt.setLong(1, evt.getChannel().getIdLong());
            stmt.execute();
            evt.getChannel().sendMessage("Successfully set up territory track for guild `").append(track).append("`").submit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
