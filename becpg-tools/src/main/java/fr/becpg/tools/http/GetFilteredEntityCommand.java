package fr.becpg.tools.http;

public class GetFilteredEntityCommand extends GetEntityCommand {
	
	private static final String COMMAND_URL_TEMPLATE = "/becpg/remote/entity?nodeRef=%s&lists=%s&fields=%s";

	public GetFilteredEntityCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, encodeParams(params) );
	}
	
}
