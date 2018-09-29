package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.lkp111138.mysupercutebot.helpers.Functions;
import me.lkp111138.mysupercutebot.helpers.Looper;

public class GuildInfoHandler extends AbstractHandler {
    @Override
    public HttpResponse realhandle(HttpExchange exchange) throws Exception {
        String guild = exchange.getRequestURI().getPath().substring(14);
        if (guild.length() == 3) {
            guild = Looper.tag2name(guild);
        }
        return new HttpResponse().setRcode(200).setResponse(Functions.guildInfo(guild).toString());
    }
}
