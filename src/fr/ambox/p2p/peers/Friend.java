package fr.ambox.p2p.peers;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.simple.JSONObject;

import fr.ambox.p2p.connexion.Frame;

public class Friend {
	private PeerId peerId;
	private Socket socket;
	private HostAndPort hostAndPort;
	private ObjectOutputStream oos;

	public Friend(HostAndPort hp, PeerId peerId) {
		this.peerId = peerId;
		this.socket = null;
		this.hostAndPort = hp;
	}

	public void send(Frame fm) throws FriendComException {
		this.send(fm, false);
	}
	
	public void send(Frame fm, boolean retry) throws FriendComException {
		try {
			if (this.socket == null) {
				this.socket = new Socket(this.hostAndPort.getHost(), this.hostAndPort.getPort());
				this.oos = new ObjectOutputStream(this.socket.getOutputStream());
			}

			try {
				this.oos.writeObject(fm);
				this.oos.flush();
			} catch (IOException e) {
				this.socket = null;
				this.oos = null;
				if (!retry) {
					this.send(fm, true);
				}
				else {
					throw new FriendComException("failed to send");
				}
			}
		} catch (UnknownHostException e) {
			throw new FriendComException(this.toString()+" is unknown");
		} catch (IOException e) {
			throw new FriendComException("connection to "+this.toString()+" failed");
		}
	}

	public PeerId getPeerId() {
		return this.peerId;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jsonObject = new JSONObject();
		
		if (this.peerId != null) {
			jsonObject.put("id", this.peerId.getId());
			jsonObject.put("nickname", this.peerId.getNickname());
		}
		else {
			jsonObject.put("id", null);
			jsonObject.put("nickname", null);
		}
		
		jsonObject.put("host", this.hostAndPort.getHost());
		jsonObject.put("port", this.hostAndPort.getPort());
		return jsonObject;
	}
	
	public String toString() {
		return this.hostAndPort.toString();
	}

	public HostAndPort getHostAndPort() {
		return this.hostAndPort;
	}

	public void updateNickname(String nickname) {
		if (nickname != null && !nickname.isEmpty()) {
				this.peerId.setNickname(nickname);
		}
	}
}
