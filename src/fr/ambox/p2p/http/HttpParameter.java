package fr.ambox.p2p.http;

public class HttpParameter {

	private String key;
	private String value;

	public HttpParameter(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String toString() {
		return this.key+"="+this.value+"\n";
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
}
