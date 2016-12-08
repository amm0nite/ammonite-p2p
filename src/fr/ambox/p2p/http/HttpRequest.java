package fr.ambox.f2f.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpRequest {

	private ArrayList<HttpHeader> headers;
	private HttpCommand command;
	private ArrayList<HttpParameter> parameters;

	public HttpRequest(HttpCommand command) throws HttpParsingException {
		this.command = command;
		this.headers = new ArrayList<HttpHeader>();
		
		this.parameters = new ArrayList<HttpParameter>();
		for (HttpParameter p : this.command.getQuery().getParameters()) {
			this.parameters.add(p);
		}
	}

	public int getContentLength() {
		for (HttpHeader h : this.headers) {
			if (h.getName().equalsIgnoreCase("Content-Length")) {
				return h.getIntValue();
			}
		}
		return -1;
	}

	public void setBody(byte[] data) {
		if (this.command.getMethod() == HttpMethod.POST) {
			try {
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = reader.readLine();
				reader.close();
				in.close();
				if (line != null) {
					HttpQuery query = HttpParser.parseParametersString(line);
					for (HttpParameter p : query.getParameters()) {
						this.parameters.add(p);
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addHeader(HttpHeader header) {
		this.headers.add(header);
	}

	public HttpPath getPath() {
		return this.command.getPath();
	}

	public HttpMethod getMethod() {
		return this.command.getMethod();
	}

	public HashMap<String, String> getParams() {
		HashMap<String, String> res = new HashMap<String, String>();
		for (HttpParameter p : this.parameters) {
			res.put(p.getKey(), p.getValue());
		}
		return res;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.command);
		for (HttpHeader h : this.headers) {
			sb.append(h);
		}
		for (HttpParameter p : this.parameters) {
			sb.append(p);
		}
		return sb.toString();
	}
}
