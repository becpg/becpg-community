package fr.becpg.tools.http;

public class GetDocumentCommand extends AbstractHttpCommand {

	private static final String COMMAND_URL_TEMPLATE = "/api/-default-/public/alfresco/versions/1/nodes/%s/content";
	
	public GetDocumentCommand(String serverUrl) {
		super(serverUrl.replace("/service", "") );
	}
	
	@Override
	public String getHttpUrl(Object... params) {
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, encodeParams(params) );
	}

}
