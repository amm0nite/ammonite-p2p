package fr.ambox.p2p.http;

public class HttpCommand {

	private HttpMethod method;
	private HttpPath path;
	private HttpQuery query;
	
	public HttpCommand(HttpMethod method, HttpPath path, HttpQuery query) {
		this.method = method;
		this.path = path;
		this.query = query;
	}

	public HttpPath getPath() {
		return this.path;
	}

	public HttpMethod getMethod() {
		return this.method;
	}

	public HttpQuery getQuery() {
		return this.query;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.method);
		sb.append(' ');
		sb.append(this.path);
		sb.append(' ');
		sb.append(this.query);
		sb.append('\n');
		return sb.toString();
	}
}
