package fr.ambox.f2f.peers;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HostAndPort implements Serializable {
	private int port;
	private String host;

	public HostAndPort(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public HostAndPort withDefaultPort(int port) {
		if (this.port == 0) {
			this.port = port;
		}
		return this;
	}
	
	public static HostAndPort fromString(String s) {
		String host = "";
		int port = 0;
		String[] parts = s.split(":");
		host = parts[0].toLowerCase().trim();
		if (parts.length > 1) {
			try {
				port = Integer.parseInt(parts[1].trim());
			} catch (NumberFormatException e) {
				port = 0;
			}
		}
		return new HostAndPort(host, port);
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}
	
	public String toString() {
		return this.host+":"+this.port;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HostAndPort other = (HostAndPort) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
}
