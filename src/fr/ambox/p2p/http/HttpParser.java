package fr.ambox.f2f.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpParser {
	public static HttpHeader parseHeaderLine(String line) throws HttpParsingException {
		String[] tab1 = line.split(":");
		if (tab1.length < 2) {
			throw new HttpParsingException();
		}
		String name = tab1[0].trim();
		String valueString = line.substring(tab1[0].length()+1);		
		String[] tab2 = valueString.split(",");
		String[] values = new String[tab2.length];
		for (int i=0; i<tab2.length; i++) {
			values[i] = tab2[i].trim();
		}
		return new HttpHeader(name, values);
	}

	public static HttpCommand parseCommandLine(String line) throws HttpParsingException {
		String[] tab = line.split(" ");
		if (tab.length != 3) {
			throw new HttpParsingException();
		}
		
		HttpMethod method = HttpParser.parseMethodString(tab[0]);
		HttpPath path = HttpParser.parsePathString(tab[1]);
		HttpQuery query = HttpParser.parseQueryString(tab[1]);
		return new HttpCommand(method, path, query);
	}

	private static HttpQuery parseQueryString(String string) throws HttpParsingException {
		try {
			URI uri = new URI(string);
			String query = uri.getQuery();
			return HttpParser.parseParametersString(query);
		} catch (URISyntaxException e) {
			throw new HttpParsingException();
		}
	}

	public static HttpQuery parseParametersString(String query) {
		ArrayList<HttpParameter> parameters = new ArrayList<HttpParameter>();
		if (query != null) {
			String[] tab1 = query.split("&");
			for (String keyvalue : tab1) {
				String[] tab2 = keyvalue.split("=");
				String key = tab2[0];
				String value = "";
				if (tab2.length == 2) {
					value = tab2[1];
				}
				parameters.add(new HttpParameter(key, value));
			}
		}
		return new HttpQuery(parameters);
	}

	private static HttpMethod parseMethodString(String string) throws HttpParsingException {
		if (string.equalsIgnoreCase("get")) {
			return HttpMethod.GET;
		}
		else if (string.equalsIgnoreCase("post")) {
			return HttpMethod.POST;
		}
		else if (string.equalsIgnoreCase("delete")) {
			return HttpMethod.DELETE;
		}
		else {
			throw new HttpParsingException();
		}
	}

	private static HttpPath parsePathString(String string) throws HttpParsingException {
		try {
			URI uri = new URI(string);
			String path = uri.getPath();
			if (path.charAt(0) == '/') { path = path.substring(1); }
			String[] tab = path.split("/");
			return new HttpPath(tab);
		} catch (URISyntaxException e) {
			throw new HttpParsingException();
		}
	}

	public static String parseMime(String filename) {
		HashMap<String,String> association = new HashMap<String,String>();
		association.put("gif", "image/gif");
		association.put("jpg", "image/jpeg");
		association.put("png", "image/png");
		association.put("html", "text/html");
		association.put("txt", "text/plain");
		association.put("js", "application/javascript");
		association.put("xml", "text/xml");
		association.put("css", "text/css");
		association.put("zip", "application/zip");
		String res = association.get(filename.substring(filename.lastIndexOf('.')+1));
		if (res == null) {
			res = "application/octet-stream";
		}
		return res;
	}
}
