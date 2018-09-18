package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.lkp111138.mysupercutebot.helpers.RateLimiter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public abstract class AbstractHandler implements HttpHandler {
    /*
    function ip(s) {
        var t=s.split("/");
        var mask=1<<(32-parseInt(t[1]));
        var octets=t[0].split(".").map(function(a){return parseInt(a)})
        return [((octets[0]<<24)+(octets[1]<<16)+(octets[2]<<8)+octets[3])|(mask-1) ,mask-1];
    }
     */
    private static final int[][] masks = new int[][]{{-1,-1},{1729492991,1023},{1729547263,1023},{1730086911,1023},{1746927615,1048575},{1822621695,16383},{-2097132545,1023},{-1922727937,16383},{-1566572545,131071},{-1404567553,524287},{-1376436225,4095},{-1133350913,4095},{-1101135873,4095},{-974457857,1023},{-970326017,32767}};

    public final void handle(HttpExchange exchange) {
        long start = System.currentTimeMillis();
        // verify ip
        int addr = 0;
        int i = 0;
        for (byte b : exchange.getRemoteAddress().getAddress().getAddress()) {
            addr |= (b & 0xff) << (24 - 8 * i);
            ++i;
        }
        boolean permit = false;
        for (int[] mask : masks) {
            if ((addr | mask[1]) == mask[0]) {
                permit = true;
                break;
            }
        }
        if (!permit) {
            fuck(exchange, new HttpResponse().setResponse("").setRcode(403));
            return;
        }
        int tokens = RateLimiter.get(addr).consume();
        exchange.getResponseHeaders().add("X-Ratelimit-Remaining", String.valueOf(tokens / 1000));
//        System.out.printf("[%.3f] Starting handler\n", 0.001 * (System.currentTimeMillis() - start));
        int length = -1;
        try {
            HttpResponse response = new HttpResponse().setResponse("{\"success\": false}");
            try {
                response = realhandle(exchange);
            } catch (Exception e) {
                e.printStackTrace();
            }
            length = fuck(exchange, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //date method path status length useragent time token
//        try {
        System.out.println(String.format("%s \"%s %s %s\" %d %d %s %dms", getRemoteAddress(exchange), exchange.getRequestMethod(), exchange.getRequestURI().toString(), exchange.getProtocol(), exchange.getResponseCode(), length, exchange.getRequestHeaders().get("User-Agent"), System.currentTimeMillis() - start));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        exchange.close();
    }

    public abstract HttpResponse realhandle(HttpExchange exchange) throws Exception;

    private int fuck(HttpExchange exchange, HttpResponse _response) {
//        System.out.printf("[%.3f] Starting outputting response\n", 0.001 * (System.currentTimeMillis() - start));
        int rcode = _response.getRcode();
        String response = _response.getResponse();
        //System.out.println(response);
        String content_type = _response.getContenType();
        if (content_type == null) {
            content_type = "application/json";
        }
        exchange.getResponseHeaders().add("Content-type", content_type + "; charset=utf-8");
        if (response != null) {
            try {
                exchange.sendResponseHeaders(rcode, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
//        System.out.printf("[%.3f] Finished outputting response\n", 0.001 * (System.currentTimeMillis() - start));
            return response.getBytes().length;
        } else {
            try {
                exchange.sendResponseHeaders(rcode, -1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    final public String decodeURIComponent(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        } catch (RuntimeException e) {
            System.out.println(s);
            return s;
        }
    }

    private static String getRemoteAddress(HttpExchange exchange) {
        String ip = exchange.getRequestHeaders().getFirst("CF-Connecting-IP");
        if (ip == null || ip.length() == 0) {
            return exchange.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }
}
