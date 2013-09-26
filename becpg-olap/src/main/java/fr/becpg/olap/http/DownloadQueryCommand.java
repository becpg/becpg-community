package fr.becpg.olap.http;



public class DownloadQueryCommand  extends AbstractHttpCommand {

	private static String COMMAND_URL_TEMPLATE = "/api/node/content/%s/%s";

	
	public DownloadQueryCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
		String nodeRef =((String)params[0]).replace(":/", "");
		
		String fileName = (String)encodeParams(params)[1];
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, nodeRef, fileName );
	}
	
	
}
