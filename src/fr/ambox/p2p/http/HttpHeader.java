package fr.ambox.p2p.http;

public class HttpHeader {

	private String name;
	private String[] values;

	public HttpHeader(String name, String[] values) {
		this.name = name;
		this.values = values;
	}

	public HttpHeader(String name, String value) {
		this.name = name;
		String[] values = new String[1];
		values[0] = value;
		this.values = values;
	}

	public String getName() {
		return this.name;
	}

	public int getIntValue() {
		return Integer.valueOf(this.values[0]);
	}

	public Object getStringValue() {
		StringBuilder sb = new StringBuilder();
		for (String v : this.values) {
			sb.append(v);
			sb.append(',');
		}
		String res = sb.toString();
		return res.substring(0, res.length()-1);
	}

	public String toString() {
		return this.name+": "+this.getStringValue()+"\n";
	}
}
