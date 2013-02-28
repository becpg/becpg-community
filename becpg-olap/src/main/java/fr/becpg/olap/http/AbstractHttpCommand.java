package fr.becpg.olap.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public abstract class AbstractHttpCommand {

	private String serverUrl;
	
	private static Log logger = LogFactory.getLog(AbstractHttpCommand.class);
	

	
	public AbstractHttpCommand(String serverUrl)  {
		super();
		this.serverUrl = serverUrl;
	}



	public InputStream runCommand(HttpClient client , Object... params) throws IOException{

		HttpEntity entity = getEntity(client, params);
				
		return entity.getContent();

	}

	
	protected HttpEntity getEntity(HttpClient client , Object... params) throws IOException{
		
		
		for(int i=0;i< params.length;i++){
			if(params[i] instanceof String){
				params[i] = URLEncoder.encode((String)params[i],"UTF-8");
			}
		}
		
		
		String url  = getHttpUrl( params);
		if(logger.isDebugEnabled()){
		 logger.debug("Run http command:"+url);
		}

		HttpGet httpGet = new HttpGet(url);
		
		HttpResponse response = client.execute(httpGet);

		return response.getEntity();

	}
	
	
	public abstract String getHttpUrl(Object... params);

	public String getServerUrl() {
		return serverUrl;
	}



	
	
}
