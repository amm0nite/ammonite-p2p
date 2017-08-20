package fr.ambox.p2p;

import java.util.Collection;
import java.util.HashMap;

import fr.ambox.p2p.utils.Logger;

public abstract class Service extends Thread {
    private HashMap<String, Service> services;

    public abstract void run();

    public Service() {
        super();
        this.services = new HashMap<String, Service>();
    }

    public void bindService(String name, Service service) {
        this.services.put(name, service);
    }

    public Service getService(String name) {
        Service service = this.services.get(name);
        if (service == null) {
            for (String srvi: this.services.keySet()) {
                System.out.println(srvi);
            }
            throw new RuntimeException("no such service : " + name);
        }
        return service;
    }

    protected Collection<Service> getServices() {
        return this.services.values();
    }

    protected void log(String msg) {
        Logger logger = (Logger) this.getService("logger");
        logger.log(msg);
    }
}
