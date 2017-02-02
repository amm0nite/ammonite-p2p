package fr.ambox.p2p.chat;

import fr.ambox.p2p.connexion.PDU;

@SuppressWarnings("serial")
public class ChatPDU extends PDU {
	private String text;
	private long time;
	private ChatPDURange range;
	
	public ChatPDU(String text) {
		this.service = "chat";
		this.text = text;
		this.time = System.currentTimeMillis();
		this.range = ChatPDURange.PUBLIC;
	}
	
	public void setRange(ChatPDURange range) {
		this.range = range;
	}
	
	public String getText() {
		return this.text;
	}
	
	public long getTime() {
		return this.time;
	}

	public ChatPDURange getRange() {
		return this.range;
	}
}
