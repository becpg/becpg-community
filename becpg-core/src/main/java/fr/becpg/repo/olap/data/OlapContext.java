package fr.becpg.repo.olap.data;

import java.util.UUID;

import org.apache.http.client.HttpClient;

public class OlapContext {

	HttpClient session;
	
	String currentUser;
	
	String uuid; 

	public HttpClient getSession() {
		return session;
	}


	public String getCurrentUser() {
		return currentUser;
	}


	public String getUuid() {
		return uuid;
	}


	public OlapContext(HttpClient session, String currentUser) {
		super();
		this.session = session;
		this.currentUser = currentUser;
		this.uuid = UUID.randomUUID().toString();
	}

}
