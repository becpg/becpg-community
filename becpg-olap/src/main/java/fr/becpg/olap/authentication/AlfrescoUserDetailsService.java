package fr.becpg.olap.authentication;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import fr.becpg.olap.InstanceManager;
import fr.becpg.olap.InstanceManager.Instance;
import fr.becpg.olap.helper.UserNameHelper;
import fr.becpg.olap.http.RetrieveUserCommand;

/**
 * 
 * @author matthieu
 * 
 */
public class AlfrescoUserDetailsService implements UserDetailsService {

	private static Log logger = LogFactory.getLog(AlfrescoUserDetailsService.class);

	InstanceManager instanceManager;

	public void setInstanceManager(InstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {

		try {
			Instance instance = instanceManager.findInstanceByUserName(username);

			HttpClient client = instanceManager.createInstanceSession(instance);

			RetrieveUserCommand retrieveUserCommand = new RetrieveUserCommand(instance.getInstanceUrl());
			try {

				String presentedLogin = UserNameHelper.extractLogin(username);

				List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				try (InputStream in = retrieveUserCommand.runCommand(client, presentedLogin)) {

					JsonFactory jsonFactory = new JsonFactory();
					JsonParser jp = jsonFactory.createJsonParser(in);

					ObjectMapper mapper = new ObjectMapper();

					JsonNode rootNode = mapper.readTree(jp);

					for (JsonNode node : rootNode.path("groups")) {
						authorities.add(new GrantedAuthorityImpl(node.path("itemName").getTextValue()));
					}

					return new AlfrescoUserDetails(username, "no-password", rootNode.path("enabled").asBoolean(), authorities, instance);

				}

			} finally {
				client.getConnectionManager().shutdown();
			}

		} catch (Exception e) {
			logger.error(e, e);
			throw new UsernameNotFoundException(e.getMessage());
		}

	}

}
