package me.lkp111138.mysupercutebot.helpers;

import java.util.HashMap;

public class RateLimiter {
    private int tokens = 60000;
    private long last_req;

    private static HashMap<Integer, RateLimiter> limiters = new HashMap<>();

    private RateLimiter(int key) {
        last_req = System.currentTimeMillis();
        limiters.put(key, this);
    }

    public int consume() {
        long now = System.currentTimeMillis();
        tokens = (int) Math.min(60000, tokens + now - last_req);
        last_req = now;
        if (tokens >= 2000) {
            tokens -= 2000;
            return tokens;
        } else {
            return tokens - 2000;
        }
    }

    public static RateLimiter get(int ip) {
        return limiters.computeIfAbsent(ip, RateLimiter::new);
    }

    public static int ip2int(String ip) {
        String[] segments = ip.split("\\.");
        int _ip = 0;
        for (int i = 0; i < 4; ++i) {
            _ip += Integer.parseInt(segments[i]) << (24 - 8 * i);
        }
        return _ip;
    }
}
