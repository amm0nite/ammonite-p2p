package fr.ambox.p2p.chat;

import fr.ambox.p2p.connexion.ReceptionData;

public class ChatLineData {
	public ReceptionData receptionData;
	
	public ChatPDURange range;
	public String text;
	public long time;

	public boolean local;
}
