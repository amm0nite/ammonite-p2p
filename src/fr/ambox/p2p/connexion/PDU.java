package fr.ambox.p2p.connexion;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class PDU implements Serializable {
	protected String service;

	public String getService() {
		return this.service;
	}
}
