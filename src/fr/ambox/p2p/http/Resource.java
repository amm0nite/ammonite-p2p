package fr.ambox.p2p.http;

public class Resource {

	private byte[] data;

	public Resource(byte[] byteArray) {
		this.data = byteArray;
	}

	public byte[] getData() {
		return this.data;
	}

}
