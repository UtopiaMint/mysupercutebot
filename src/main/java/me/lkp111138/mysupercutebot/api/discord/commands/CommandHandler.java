package me.lkp111138.mysupercutebot.api.discord.commands;

import me.lkp111138.mysupercutebot.api.discord.message.PaginatedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface CommandHandler {
    /**
     * a new command is received
     * @param evt the new message event
     * @param cmd the command to run
     * @param rest the message excluding the command part
     * @param pmsg
     * @return PaginatedMessage if the message needs pagination, null otherwise
     */
    PaginatedMessage process(MessageReceivedEvent evt, String cmd, String rest, PaginatedMessage pmsg);
}
