package fr.ambox.f2f.http;

import java.util.ArrayList;

public class HttpQuery {

	private ArrayList<HttpParameter> parameters;

	public HttpQuery(ArrayList<HttpParameter> parameters) {
		this.parameters = parameters;
	}

	public HttpParameter[] getParameters() {
		return this.parameters.toArray(new HttpParameter[0]);
	}
}
