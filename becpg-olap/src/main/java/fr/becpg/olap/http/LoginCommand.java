package fr.becpg.olap.http;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class LoginCommand  extends AbstractHttpCommand {


	private static String COMMAND_URL_TEMPLATE = "/api/login?u=%s&pw=%s";

	
	public LoginCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, encodeParams(params) );
	}
	
	public String getAlfTicket(String login, String password) throws UsernameNotFoundException{
		DefaultHttpClient httpclient = new DefaultHttpClient();

		
		try(InputStream in  = runCommand(httpclient,login, password )){
			
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(in);
	
			NodeList nodes = (NodeList) doc.getElementsByTagName("ticket");
			if(nodes!=null && nodes.getLength()>0){
				return nodes.item(0).getTextContent();
			}
			
		} catch (Exception e) {
			throw new UsernameNotFoundException(e.getMessage());
		}
		return null;
		
	}
	
	
}
