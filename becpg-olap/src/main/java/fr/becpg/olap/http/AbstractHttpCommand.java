package fr.becpg.olap.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractHttpCommand {

	private String remoteUser;
	
	private char[] remotePwd;
	
	private String serverUrl;
	
	private static Log logger = LogFactory.getLog(AbstractHttpCommand.class);
	
	
	public AbstractHttpCommand(String serverUrl, String remoteUser, char[] remotePwd) {
		super();
		this.remoteUser = remoteUser;
		this.remotePwd = remotePwd;
		this.serverUrl = serverUrl;
	}

	private void doAuthenticate(){
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(remoteUser, remotePwd);
			}
		});
		
	}
	
	public InputStream runCommand(String param) throws IOException{
		doAuthenticate();
		String url  = getHttpUrl( URLEncoder.encode(param,"UTF-8"));
		if(logger.isDebugEnabled()){
		 logger.debug("Run http command:"+url);
		}
		
		URL commandUrl = new URL(url);
		return commandUrl.openStream();

	}

	public abstract String getHttpUrl(String param);

	public String getServerUrl() {
		return serverUrl;
	}
	
	
}
