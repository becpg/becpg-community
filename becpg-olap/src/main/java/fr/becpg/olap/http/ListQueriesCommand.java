package fr.becpg.olap.http;



public class ListQueriesCommand  extends AbstractHttpCommand {


	private static String COMMAND_URL_TEMPLATE = "/becpg/olap/chart";

	
	public ListQueriesCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, encodeParams(params) );
	}

	
}
