package me.lkp111138.mysupercutebot.api.discord.message;

import me.lkp111138.mysupercutebot.api.discord.commands.CommandHandler;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PaginatedMessage {
    private static final HashMap<Long, PaginatedMessage> paginated_msgs = new HashMap<>();
    private static final ScheduledExecutorService cancels = Executors.newScheduledThreadPool(4);

    private final MessageReceivedEvent evt;
    private final String cmd;
    private final String rest;
    private final long msg_id;
    private final int page_count;
    private final Message msg;
    private CommandHandler handler;
    private boolean command_set = false;
    private int page_current;
    private ScheduledFuture cancel_future;

    public PaginatedMessage(MessageReceivedEvent evt, String cmd, String rest, Message msg, int page_count) {
        this.evt = evt;
        this.cmd = cmd;
        this.rest = rest;
        this.msg_id = msg.getIdLong();
        this.msg = msg;
        this.page_count = page_count; // 0-based
        paginated_msgs.put(msg_id, this);
        this.cancel_future = cancels.schedule(this::cancel, 600000, TimeUnit.MILLISECONDS);
    }

    public static PaginatedMessage fromId(long id) {
        return paginated_msgs.get(id);
    }

    public long getMsgId() {
        return msg_id;
    }

    public int getCurrentPage() {
        return page_current;
    }

    public void setHandler(CommandHandler handler) {
        if (!command_set) {
            this.handler = handler;
            command_set = true;
        }
    }

    public Message getMsg() {
        return msg;
    }

    public void nextPage() {
        if (page_current + 1 < page_count) {
            ++page_current;
            cancel_future.cancel(false);
            this.cancel_future = cancels.schedule(this::cancel, 600000, TimeUnit.MILLISECONDS);
            handler.process(evt, cmd, rest, this);
        }
    }

    public void privPage() {
        if (page_current > 0) {
            --page_current;
            cancel_future.cancel(false);
            this.cancel_future = cancels.schedule(this::cancel, 600000, TimeUnit.MILLISECONDS);
            handler.process(evt, cmd, rest, this);
        }
    }

    public void firstPage() {
        if (page_current != 0) {
            page_current = 0;
            cancel_future.cancel(false);
            this.cancel_future = cancels.schedule(this::cancel, 600000, TimeUnit.MILLISECONDS);
            handler.process(evt, cmd, rest, this);
        }
    }

    public void lastPage() {
        if (page_current + 1 != page_count) {
            page_current = page_count - 1;
            cancel_future.cancel(false);
            this.cancel_future = cancels.schedule(this::cancel, 600000, TimeUnit.MILLISECONDS);
            handler.process(evt, cmd, rest, this);
        }
    }

    public void reload() {
        cancel_future.cancel(false);
        this.cancel_future = cancels.schedule(this::cancel, 600000, TimeUnit.MILLISECONDS);
        handler.process(evt, cmd, rest, this);
    }

    public void cancel() {
        paginated_msgs.remove(msg_id);
        msg.clearReactions().complete();
    }
}
