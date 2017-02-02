package fr.ambox.p2p.http;

import java.util.Arrays;

public class HttpPath {

	private String[] elements;

	public HttpPath(String[] tab) {
		this.elements = tab;
	}

	public boolean hasFirst() {
		return (elements.length >= 1);
	}

	public String getFirst() {
		return this.elements[0];
	}

	public boolean hasSecond() {
		return (elements.length >= 2);
	}

	public String getSecond() {
		return this.elements[1];
	}

	public String[] getElements(int offset) {
		return Arrays.copyOfRange(this.elements, offset, this.elements.length);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String e : this.elements) {
			sb.append(e);
			sb.append(System.getProperty("file.separator"));
		}
		return sb.toString();
	}
}
