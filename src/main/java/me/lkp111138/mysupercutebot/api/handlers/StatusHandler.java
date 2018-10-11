package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import me.lkp111138.mysupercutebot.Main;
import org.json.JSONObject;

public class StatusHandler extends AbstractHandler {
    @Override
    public HttpResponse realhandle(HttpExchange exchange) throws Exception {
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        return new HttpResponse().setRcode(200).setResponse(new JSONObject().put("heap", new long[]{heapSize - heapFreeSize, heapSize, heapMaxSize}).put("uptime", System.currentTimeMillis() - Main.getStarted()).toString());
    }
}
