package fr.becpg.tools.http;

public class PutEntityCommand  extends AbstractHttpCommand {

	private static final String COMMAND_URL_TEMPLATE = "/becpg/remote/entity?callback=%s&callbackUser=%s&callbackPassword=%s"; 

	
    public PutEntityCommand(String serverUrl) {
		super(serverUrl);
		this.setHttpMethod(HttpCommandMethod.METHOD_PUT);
	}

	@Override
	public String getHttpUrl(Object... params) {
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, encodeParams(params) );
	}


}
