package me.lkp111138.mysupercutebot.helpers;

import java.util.HashMap;

public class RateLimiter {
    private int tokens = 120000;
    private long last_req;

    private static HashMap<Integer, RateLimiter> limiters = new HashMap<>();

    private RateLimiter(int key) {
        last_req = System.currentTimeMillis();
        limiters.put(key, this);
    }

    public int consume() {
        long now = System.currentTimeMillis();
        tokens = (int) Math.min(120000, tokens + (now - last_req) * 2);
        if (tokens >= 1000) {
            tokens -= 1000;
            return tokens;
        } else {
            return tokens - 1000;
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
