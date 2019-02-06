package me.lkp111138.mysupercutebot.api.discord.commands;

import me.lkp111138.mysupercutebot.api.discord.message.PaginatedMessage;
import me.lkp111138.mysupercutebot.objects.War;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class WhosWarringCommand implements CommandHandler {
    @Override
    public PaginatedMessage process(MessageReceivedEvent evt, String cmd, String rest, PaginatedMessage pmsg) {
        evt.getChannel().sendMessage("```ml\n").append(War.export()).append("```").complete();
        return null;
    }
}
