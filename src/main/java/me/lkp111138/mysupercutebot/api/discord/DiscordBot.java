package me.lkp111138.mysupercutebot.api.discord;

import me.lkp111138.mysupercutebot.Constants;
import me.lkp111138.mysupercutebot.api.discord.commands.*;
import me.lkp111138.mysupercutebot.api.discord.message.PaginatedMessage;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.RequestFuture;
import net.dv8tion.jda.core.requests.RestAction;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class DiscordBot extends ListenerAdapter {
    private static final HashMap<String, CommandHandler> commands = new HashMap<>();
    private static final String prefix = "~";
    private static JDA jda;
    private static boolean ready = false;

    public static void init() throws LoginException {
        jda = new JDABuilder(Constants.DISCORD_TOKEN).build();
        _init();
    }

    public static void init(String token) throws LoginException {
        jda = new JDABuilder(token).build();
        _init();
    }

    private static void _init() {
        jda.addEventListener(new DiscordBot());
        commands.put("ws", new WarStatsCommand());
        commands.put("pws", new PlayerWarStatsCommand());
        commands.put("ww", new WhosWarringCommand());
        commands.put("wt", new WarTrackCommand());
        commands.put("tt", new TerrTrackCommand());
        commands.put("warstats", new WarStatsCommand());
        commands.put("playerwarstats", new PlayerWarStatsCommand());
        commands.put("whoswarring", new WhosWarringCommand());
        commands.put("wartrack", new WarTrackCommand());
        commands.put("terrtrack", new TerrTrackCommand());
        commands.put("xp", new XpLogCommand());
    }

    public static RequestFuture<Message> send(long channel, String msg) {
        if (jda != null) {
            return jda.getTextChannelById(channel).sendMessage(msg).submit();
        } else {
            return null;
        }
    }

    public static boolean isReady() {
        return ready;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();
        // ignore bots
        if (!event.getMember().getUser().isBot()) {
            if (event.isFromType(ChannelType.PRIVATE)) {
                System.out.printf("[PM] %s: %s\n", event.getAuthor().getName(), msg);
            } else {
                System.out.printf("[%s][%s] %s#%s {%s}: %s\n", event.getGuild().getName(),
                        event.getTextChannel().getName(), event.getMember().getUser().getName(), event.getMember().getUser().getDiscriminator(), event.getMember().getEffectiveName(), msg);
            }
            String[] _msg = msg.split(" ", 2);
            if (_msg[0].startsWith(prefix)) {
                _msg[0] = _msg[0].substring(prefix.length());
                CommandHandler handler = commands.get(_msg[0]);
                if (handler != null) {
                    PaginatedMessage pmsg;
                    if (_msg.length == 2) {
                        pmsg = handler.process(event, _msg[0], _msg[1], null);
                    } else {
                        pmsg = handler.process(event, _msg[0], null, null);
                    }
                    if (pmsg != null) {
                        pmsg.setHandler(handler);
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(9194).toString()).submit(); // 1st page
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(11013).toString()).submit(); // priv page
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(10145).toString()).submit(); // next page
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(9193).toString()).submit(); // last page
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(128260).toString()).submit(); // reload
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(10060).toString()).submit(); // cancel
                    }
                }
            }
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Bot ready");
        ready = true;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        int emoji = event.getReaction().getReactionEmote().getName().codePointAt(0);
        System.out.println("reaction " + emoji);
//        System.out.println("reaction " + event);
        System.out.println("on message " + event.getMessageId()); // 339008
        // left = 11013
        // ist page = 9194
        // right = 10145
        // last page = 9193
        // reload = 128260
        // cross = 10060
        PaginatedMessage pmsg = PaginatedMessage.fromId(event.getMessageIdLong());
        if (pmsg == null) {
            return;
        }
        RestAction<Void> action = event.getReaction().removeReaction(event.getUser());
        action.submit();
        switch (emoji) {
            case 11013:
                pmsg.privPage();
                break;
            case 10145:
                pmsg.nextPage();
                break;
            case 9194:
                pmsg.firstPage();
                break;
            case 9193:
                pmsg.lastPage();
                break;
            case 128260:
                pmsg.reload();
                break;
            case 10060:
                pmsg.cancel();
        }
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        // delete any associated terr/war tracks
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("delete from discord_war_log where channel_id=?")) {
                stmt.setLong(1, event.getChannel().getIdLong());
                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
