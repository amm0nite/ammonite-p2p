package fr.ambox.f2f.chat;

import fr.ambox.f2f.connexion.ReceptionData;

public class ChatLineData {
	public ReceptionData receptionData;
	
	public ChatPDURange range;
	public String text;
	public long time;

	public boolean local;
}
