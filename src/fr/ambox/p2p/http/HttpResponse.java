package fr.ambox.p2p.http;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpResponse {
    public static final String CRLF = "\r\n";

    private ArrayList<HttpHeader> headers;
    private int status;
    private byte[] body;

    public HttpResponse(int i) {
        this.status = i;
        this.headers = new ArrayList<HttpHeader>();
    }

    public HttpResponse(int code, String response) {
        this.status = code;
        this.headers = new ArrayList<HttpHeader>();
        try {
            this.setBody(response.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            this.setBody(response.getBytes());
        }
    }

    public void addHeader(String key, String value) {
        this.headers.add(new HttpHeader(key, value));
    }

    public void setBody(byte[] data) {
        this.body = data;
    }

    public byte[] getBody() {
        return this.body;
    }

    public String getBodyAsString() {
        try {
            return new String(this.body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new String(this.body);
        }
    }

    public byte[] toBytes() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1");
            sb.append(' ');
            sb.append(this.status);
            sb.append(' ');

            HashMap<Integer, String> statuses = new HashMap<Integer, String>();
            statuses.put(200, "OK");
            statuses.put(404, "NOT FOUND");
            statuses.put(302, "FOUND");
            String status = statuses.get(this.status);
            if (status == null) {
                status = "UNKNOWN";
            }

            sb.append(status);
            sb.append(CRLF);

            for (HttpHeader p : this.headers) {
                sb.append(p.getName());
                sb.append(':');
                sb.append(' ');
                sb.append(p.getStringValue());
                sb.append(CRLF);
            }

            sb.append(CRLF);
            String head = sb.toString();
            byte[] headBytes = head.getBytes("UTF-8");

            if (this.body != null) {
                byte[] repbytes = new byte[headBytes.length + this.body.length];
                for (int i = 0; i < repbytes.length; i++) {
                    if (i < headBytes.length) {
                        repbytes[i] = headBytes[i];
                    } else {
                        repbytes[i] = this.body[i - headBytes.length];
                    }
                }
                return repbytes;
            } else {
                return headBytes;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static HttpResponse notFound(String message) {
        return new HttpResponse(404, "<html><h1>NOT FOUND</h1><h2>" + message + "</h2></html>");
    }

    public static HttpResponse itworks(String message) {
        return new HttpResponse(200, "<html><h1>IT WORKS</h1><h2>" + message + "</h2></html>");
    }

    public static HttpResponse error(String message) {
        return new HttpResponse(500, "<html><h1>INTERNAL ERROR</h1><h2>" + message + "</h2></html>");
    }

    public static HttpResponse success() {
        return new HttpResponse(200, "action succeded");
    }

    public static HttpResponse fail() {
        return new HttpResponse(400, "action failed");
    }

    public static HttpResponse fail(Exception e) {
        return new HttpResponse(400, e.getMessage());
    }
}
