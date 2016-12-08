package fr.ambox.f2f.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import fr.ambox.f2f.Service;
import fr.ambox.f2f.configuration.ConfigurationService;

public class HttpReceiver extends Service {
	private ServerSocket serverSocket;
	private Loader loader;

	public HttpReceiver() {
		this.loader = new Loader();
	}
	
	public void run() {
		try {
			ConfigurationService configuration = (ConfigurationService) this.getService("configuration");
			this.serverSocket = new ServerSocket(Integer.valueOf(configuration.getOption("httpPort")));
			
			while (true) {
				Socket clientSocket = this.serverSocket.accept();
				HttpHandler handler = new HttpHandler(clientSocket);
				
				handler.bindService("logger", this.getService("logger"));
				handler.bindService("identity", this.getService("identity"));
				handler.bindService("configuration", this.getService("configuration"));
				handler.bindService("friendship", this.getService("friendship"));
				handler.bindService("peers", this.getService("peers"));
				handler.bindService("chat", this.getService("chat"));
				handler.bindService("receiver", this);
				
				handler.start();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public Loader getLoader() {
		return this.loader;
	}
	
	public static void main(String[] args) {
		ConfigurationService configuration = new ConfigurationService();
		HttpReceiver r = new HttpReceiver();
		r.bindService("configuration", configuration);
		r.start();
	}
}
