package fr.becpg.olap.http;



public class RetrieveUserCommand  extends AbstractHttpCommand {


	private static String COMMAND_URL_TEMPLATE = "/api/people/%s?groups=true";

	
	public RetrieveUserCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, params );
	}

	
}
