package me.lkp111138.mysupercutebot.api.handlers;

public class HttpResponse {
    private String response = "";
    private int rcode = 500;
    private String content_type = null;

    public int getRcode() {
        return rcode;
    }

    public HttpResponse setRcode(int rcode) {
        this.rcode = rcode;
        return this;
    }

    public String getResponse() {
        return response;
    }

    public HttpResponse() {
    }

    public HttpResponse setResponse(String response) {
        this.response = response;
        return this;
    }

    public String getContenType() {
        return content_type;
    }

    public HttpResponse setContentType(String content_type) {
        this.content_type = content_type;
        return this;
    }
}
