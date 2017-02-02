package fr.ambox.p2p.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Loader {
	private HashMap<String, Resource> store;

	public Loader() {
		this.store = new HashMap<String, Resource>();
		
		ArrayList<String> resourcesToLoad = new ArrayList<String>();
		resourcesToLoad.add("index.html");
		resourcesToLoad.add("main.js");
		resourcesToLoad.add("jquery.js");
		resourcesToLoad.add("bootstrap/css/bootstrap-responsive.css");
		resourcesToLoad.add("bootstrap/css/bootstrap.css");
		resourcesToLoad.add("bootstrap/img/glyphicons-halflings-white.png");
		resourcesToLoad.add("bootstrap/img/glyphicons-halflings.png");
		resourcesToLoad.add("bootstrap/js/bootstrap.js");

		for (String s : resourcesToLoad) {
			try {
				System.out.println("loading "+s);
				InputStream in = ClassLoader.getSystemResourceAsStream(s);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				in.close();
				out.close();
				Resource r = new Resource(out.toByteArray());
				this.store.put(s, r);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
	}
	
	public Resource getResource(String name) {
		return this.store.get(name);
	}
	
	public static void main (String[] args) {
		new Loader();
	}
}
