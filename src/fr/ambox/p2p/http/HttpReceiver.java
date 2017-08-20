package fr.ambox.p2p.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import fr.ambox.p2p.Service;
import fr.ambox.p2p.configuration.ConfigurationService;

public class HttpReceiver extends Service {
    private boolean running;
    private Loader loader;

    public HttpReceiver() {
        this.running = true;
        this.loader = new Loader();
    }

    public void run() {
        try {
            ConfigurationService configuration = (ConfigurationService) this.getService("configuration");
            ServerSocket serverSocket = new ServerSocket(Integer.valueOf(configuration.getOption("httpPort")));

            while (this.running) {
                Socket clientSocket = serverSocket.accept();
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Loader getLoader() {
        return this.loader;
    }
}
