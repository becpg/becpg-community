package fr.becpg.olap.http;

public class ListEntitiesCommand extends AbstractHttpCommand {


	private static String COMMAND_URL_TEMPLATE = "/becpg/remote/entity/list?query=%s";

	
	public ListEntitiesCommand(String serverUrl, String remoteUser, char[] remotePwd) {
		super(serverUrl, remoteUser, remotePwd);
	}

	@Override
	public String getHttpUrl(String param) {
	
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, param );
	}
	
	
}
