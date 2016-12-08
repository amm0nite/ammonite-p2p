package fr.ambox.f2f.chat;

import fr.ambox.f2f.connexion.PDU;

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
