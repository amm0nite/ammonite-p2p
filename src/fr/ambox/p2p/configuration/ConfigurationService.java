package fr.ambox.p2p.configuration;

import java.util.HashMap;

import fr.ambox.p2p.UserService;
import fr.ambox.p2p.connexion.PDU;
import fr.ambox.p2p.connexion.ReceptionData;
import fr.ambox.p2p.http.HttpResponse;

public class ConfigurationService extends UserService {
	private HashMap<String, String> configuration;

	public ConfigurationService() {
		this.configuration = new HashMap<String, String>();
		this.configuration.put("routerPort", String.valueOf(8080));
		this.configuration.put("httpPort", String.valueOf(80));
	}
	
	@Override
	public void run() {
		// read config file?
	}

	public String getOption(String name) {
		return this.configuration.get(name);
	}

	public void setOption(String name, String value) {
		this.configuration.put(name, value);
	}
	
	@Override
	public void handle(PDU pdu, ReceptionData receptionData) {
		
	}

	@Override
	public HttpResponse apiGET(String[] elements, HashMap<String, String> params) {
		return HttpResponse.fail();
	}

	@Override
	public HttpResponse apiPOST(String[] elements, HashMap<String, String> params) {
		return HttpResponse.fail();
	}

	@Override
	public HttpResponse apiDELETE(String[] elements, HashMap<String, String> params) {
		return HttpResponse.fail();
	}
}
