package fr.becpg.olap.http;



public class GetEntityCommand  extends AbstractHttpCommand {


	private static String COMMAND_URL_TEMPLATE = "/becpg/remote/entity?nodeRef=%s";

	
	public GetEntityCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, encodeParams(params) );
	}


	
}
