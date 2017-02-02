package fr.ambox.p2p.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import fr.ambox.p2p.Service;
import fr.ambox.p2p.UserService;

public class HttpHandler extends Service {
	private Socket socket;
	
	public HttpHandler(Socket clientSocket) {
		this.socket = clientSocket;
	}

	@Override
	public void run() {
		try {
			HttpRequest req = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null && !line.isEmpty()) {
				if (req == null) {
					req = new HttpRequest(HttpParser.parseCommandLine(line));
				}
				else {
					req.addHeader(HttpParser.parseHeaderLine(line));
				}
			}

			if (req.getContentLength() > 0) {
				// read body
				BufferedInputStream bytein = new BufferedInputStream(this.socket.getInputStream());
				byte[] data = new byte[req.getContentLength()];
				bytein.read(data);
				req.setBody(data);
			}

			//System.out.println(req);
			HttpResponse rep = this.handle(req);
			rep.addHeader("Connection", "close");
			this.send(rep);
			this.socket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (HttpParsingException e) {
			e.printStackTrace();
		}
	}

	private void send(HttpResponse rep) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(this.socket.getOutputStream());
		out.write(rep.toBytes());
		out.flush();
		out.close();
	}

	private HttpResponse handle(HttpRequest req) {
		if (req.getPath().hasFirst() && req.getPath().hasSecond()) {
			if (req.getPath().getFirst().equalsIgnoreCase("resource")) {
				return this.handleResource(req);

			}
			else if (req.getPath().getFirst().equalsIgnoreCase("service")) {
				return this.handleService(req);
			}
		}
		//return HttpResponse.itworks("path is "+req.getPath());
		return this.handleIndex();
	}

	private HttpResponse handleIndex() {
		HttpReceiver receiver = (HttpReceiver) this.getService("receiver");
		Loader loader = receiver.getLoader();
		Resource r = loader.getResource("index.html");
		HttpResponse rep = new HttpResponse(200);
		rep.addHeader("Content-Type", "text/html");
		rep.addHeader("Content-Length", String.valueOf(r.getData().length));
		rep.setBody(r.getData());
		return rep;
	}

	private HttpResponse handleService(HttpRequest req) {
		String service = req.getPath().getSecond();
		Service s = this.getService(service);
		if (s != null && s instanceof UserService) {
			UserService us = (UserService) s;
			if (req.getMethod() == HttpMethod.GET) {
				return us.apiGET(req.getPath().getElements(2), req.getParams());
			}
			else if (req.getMethod() == HttpMethod.POST) {
				return us.apiPOST(req.getPath().getElements(2), req.getParams());
			}
			else if (req.getMethod() == HttpMethod.DELETE) {
				return us.apiDELETE(req.getPath().getElements(2), req.getParams());
			}
		}
		return HttpResponse.notFound(service+" is not available");
	}

	private HttpResponse handleResource(HttpRequest req) {
		String resource = req.getPath().getSecond();
		for (String e : req.getPath().getElements(2)) {
			resource += "/"+e;
		}
		
		HttpReceiver receiver = (HttpReceiver) this.getService("receiver");
		Loader loader = receiver.getLoader();
		Resource r = loader.getResource(resource);
		if (r != null) {
			HttpResponse rep = new HttpResponse(200);
			rep.addHeader("Content-Type", HttpParser.parseMime(resource));
			rep.addHeader("Content-Length", String.valueOf(r.getData().length));
			rep.setBody(r.getData());
			return rep;
		}
		else {
			return HttpResponse.notFound(resource+" does not exist");
		}
	}
}
