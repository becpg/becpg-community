package fr.becpg.repo.authentication.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.repo.authentication.BeCPGUserAccount;

@Service
public class IdentityServiceAccountProvider {

	private static final Pattern PROHIBITED_CHARS = Pattern.compile(
        "[<>&\"$%!#?§;*~/\\\\|^=\\[\\]{}()\\p{Cntrl}]"
    );

	private static Log logger = LogFactory.getLog(IdentityServiceAccountProvider.class);

	@Value("${identity-service.create-user.enabled:false}")
	private Boolean enabled;

	@Value("${identity-service.client-admin.username:#{null}}")
	private String identityServiceUserName;

	@Value("${identity-service.client-admin.password:#{null}}")
	private String identityServicePassword;

	@Value("${identity-service.realm}")
	private String realm;
	
	@Value("${identity-service.resource}")
	private String clientId;

	@Value("${identity-service.credentials.secret:#{null}}")
	private String clientSecret;

	@Value("${identity-service.auth-server-url}")
	private String authServerUrl;

	public Boolean isEnabled() {
		return enabled;
	}
	

	public String getZoneId() {
		return AuthorityService.ZONE_AUTH_EXT_PREFIX+realm.toUpperCase();
	}


	public boolean registerAccount(BeCPGUserAccount userAccount) {
		sanitizeAccount(userAccount);
		try {
			HttpClientBuilder builder = new HttpClientBuilder();

			try (CloseableHttpClient httpClient = (CloseableHttpClient) builder.build()) {

				HttpPost request = new HttpPost(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token");

				ArrayList<BasicNameValuePair> parameters = new ArrayList<>();
				parameters.add(new BasicNameValuePair("grant_type", OAuth2Constants.PASSWORD));
				parameters.add(new BasicNameValuePair("client_id", clientId));
				parameters.add(new BasicNameValuePair("username", identityServiceUserName));
				parameters.add(new BasicNameValuePair("password", identityServicePassword));
				if (clientSecret != null && !clientSecret.isEmpty()) {
					parameters.add(new BasicNameValuePair("client_secret", clientSecret));
				}
				request.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));

				try (CloseableHttpResponse response = httpClient.execute(request)) {
					if (response.getStatusLine().getStatusCode() == 200) {
						JSONObject auth = new JSONObject(EntityUtils.toString(response.getEntity()));

						if (auth.has("access_token")) {

							request = new HttpPost(authServerUrl + "/admin/realms/" + realm + "/users");

							request.setHeader("Content-Type", "application/json;charset=UTF-8");
							request.setHeader("Authorization", "Bearer " + auth.getString("access_token"));

							JSONObject userRepresentation = new JSONObject();
							userRepresentation.put("enabled", true);
							userRepresentation.put("username", userAccount.getUserName());
							userRepresentation.put("firstName", userAccount.getFirstName());
							userRepresentation.put("lastName", userAccount.getLastName());
							userRepresentation.put("email", userAccount.getEmail());
							
							if( userAccount.getPassword() !=null && !  userAccount.getPassword().isEmpty()) {

								JSONObject credentialrepresentation = new JSONObject();
	
								credentialrepresentation.put("type", OAuth2Constants.PASSWORD);
								credentialrepresentation.put("value", userAccount.getPassword());
								credentialrepresentation.put("temporary", true);
								
								JSONArray credentials = new JSONArray();
								credentials.put(credentialrepresentation);

								userRepresentation.put("credentials", credentials);
							}
							

							StringEntity params = new StringEntity(userRepresentation.toString(), "UTF-8");
							request.setEntity(params);
							if(logger.isDebugEnabled()) {
								logger.debug("Create user:"+userRepresentation.toString());
							}
							try (CloseableHttpResponse createResp = httpClient.execute(request)) {
								if (response.getStatusLine().getStatusCode() != 200) {
									throw new IllegalStateException(EntityUtils.toString(createResp.getEntity()));
								} else  if(logger.isDebugEnabled()){
									logger.debug(EntityUtils.toString(createResp.getEntity()));
								}
							}
						} else {
							logger.error(auth.toString());
							throw new IllegalStateException("Cannot get access_token from identityService");
						}

					} else {
						logger.error(EntityUtils.toString(response.getEntity()));
						throw new IllegalStateException("Cannot get access_token from identityService");
					}
				}

			}
		} catch (IOException e) {
			logger.error(e, e);
		}
		return true;
	}

	private void sanitizeAccount(BeCPGUserAccount userAccount) {
		userAccount.setFirstName(sanitize(userAccount.getFirstName()));
		userAccount.setLastName(sanitize(userAccount.getLastName()));
	}
	
	private String sanitize(String input) {
        if (input == null) {
        	return null;
        }
        return PROHIBITED_CHARS.matcher(input).replaceAll("");
    }
	
	@Override
	public String toString() {
		return "IdentityServiceAccountProvider [enabled=" + enabled + ", identityServiceUserName=" + identityServiceUserName
				+ ", identityServicePassword=" + identityServicePassword + ", realm=" + realm + ", clientId=" + clientId + ", clientSecret="
				+ clientSecret + ", authServerUrl=" + authServerUrl + "]";
	}

}
