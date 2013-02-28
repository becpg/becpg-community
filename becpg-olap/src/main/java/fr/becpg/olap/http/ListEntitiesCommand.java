package fr.becpg.olap.http;

public class ListEntitiesCommand extends AbstractHttpCommand {


	private static String COMMAND_URL_TEMPLATE = "/becpg/remote/entity/list?query=%s";

	
	public ListEntitiesCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
	
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, params );
	}
	
	
}
