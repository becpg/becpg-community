package fr.becpg.olap.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;



public class GetMondrianSchemaCommand  extends AbstractHttpCommand {


	private static String COMMAND_URL_TEMPLATE = "/becpg/olap/schema?instance=%s";

	
	public GetMondrianSchemaCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
	
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, params );
	}

	public String getSchema(HttpClient httpClient,Object... params) throws IOException {
		HttpEntity entity = getEntity(httpClient,params);
	
		return EntityUtils.toString(entity);
	}

	
}
