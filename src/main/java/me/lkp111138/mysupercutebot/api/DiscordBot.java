package me.lkp111138.mysupercutebot.api;

import me.lkp111138.mysupercutebot.Constants;
import me.lkp111138.mysupercutebot.api.discord.commands.CommandHandler;
import me.lkp111138.mysupercutebot.api.discord.commands.PlayerWarStatsCommand;
import me.lkp111138.mysupercutebot.api.discord.commands.WarStatsCommand;
import me.lkp111138.mysupercutebot.api.discord.commands.WhosWarringCommand;
import me.lkp111138.mysupercutebot.api.discord.message.PaginatedMessage;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.RestAction;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

public class DiscordBot extends ListenerAdapter {
    private static final HashMap<String, CommandHandler> commands = new HashMap<>();
    private static final String prefix = "~";

    public static void init() throws LoginException {
        JDA jda = new JDABuilder(Constants.DISCORD_TOKEN).build();
        jda.addEventListener(new DiscordBot());
        commands.put("ws", new WarStatsCommand());
        commands.put("pws", new PlayerWarStatsCommand());
        commands.put("ww", new WhosWarringCommand());
        commands.put("warstats", new WarStatsCommand());
        commands.put("playerwarstats", new PlayerWarStatsCommand());
        commands.put("whoswarring", new WhosWarringCommand());
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
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(9194).toString()).complete(); // 1st page
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(11013).toString()).complete(); // priv page
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(10145).toString()).complete(); // next page
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(9193).toString()).complete(); // last page
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(128260).toString()).complete(); // reload
                        pmsg.getMsg().addReaction(new StringBuffer().appendCodePoint(10060).toString()).complete(); // cancel
                    }
                }
            }
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Bot ready");
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        RestAction<Void> action = event.getReaction().removeReaction(event.getUser());
        action.submit();
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
}
