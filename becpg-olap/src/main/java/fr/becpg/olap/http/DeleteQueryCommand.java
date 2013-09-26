package fr.becpg.olap.http;



public class DeleteQueryCommand  extends AbstractHttpCommand {


	private static String COMMAND_URL_TEMPLATE = "/api/node/%s";

	
	public DeleteQueryCommand(String serverUrl) {
		super(serverUrl);
		setHttpMethod(HttpCommandMethod.METHOD_DELETE);
	}
	
	

	@Override
	public String getHttpUrl(Object... params) {
	
		String nodeRef =((String)params[0]).replace(":/", "");
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, nodeRef );
	}
	
	
	
	
}
