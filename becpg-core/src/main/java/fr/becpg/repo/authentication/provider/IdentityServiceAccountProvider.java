package fr.becpg.repo.authentication.provider;

import java.io.IOException;
import java.util.ArrayList;

import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import fr.becpg.repo.authentication.BeCPGUserAccount;

/**
 * <p>IdentityServiceAccountProvider class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class IdentityServiceAccountProvider {

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

	/**
	 * <p>isEnabled.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean isEnabled() {
		return enabled;
	}
	

	/**
	 * <p>getZoneId.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getZoneId() {
		return AuthorityService.ZONE_AUTH_EXT_PREFIX+realm.toUpperCase();
	}


	/**
	 * <p>registerAccount.</p>
	 *
	 * @param userAccount a {@link fr.becpg.repo.authentication.BeCPGUserAccount} object
	 * @return a boolean
	 */
	public boolean registerAccount(BeCPGUserAccount userAccount) {
		try {
			HttpClientBuilder builder = HttpClientBuilder.create();

			try (CloseableHttpClient httpClient = builder.build()) {

				HttpPost request = new HttpPost(authServerUrl + "/admin/realms/" + realm + "/users");
				request.setHeader("Content-Type", "application/json;charset=UTF-8");
				request.setHeader("Authorization", "Bearer " + getAdminAccessToken());

				JSONObject userRepresentation = new JSONObject();
				userRepresentation.put("enabled", true);
				userRepresentation.put("username", userAccount.getUserName());
				userRepresentation.put("firstName", userAccount.getFirstName());
				userRepresentation.put("lastName", userAccount.getLastName());
				userRepresentation.put("email", userAccount.getEmail());

				StringEntity params = new StringEntity(userRepresentation.toString(), "UTF-8");
				request.setEntity(params);
				if(logger.isDebugEnabled()) {
					logger.debug("Create user:"+userRepresentation.toString());
				}
				try (CloseableHttpResponse createResp = httpClient.execute(request)) {
					if (EntityUtils.toString(createResp.getEntity()).toLowerCase().contains("user exists")) {
						if(logger.isDebugEnabled()){
							logger.debug(EntityUtils.toString(createResp.getEntity()));
						}
						return false;
					}
					if (!HttpStatus.valueOf(createResp.getStatusLine().getStatusCode()).is2xxSuccessful()) {
						throw new IllegalStateException(EntityUtils.toString(createResp.getEntity()));
					} else  if(logger.isDebugEnabled()){
						logger.debug(EntityUtils.toString(createResp.getEntity()));
					}
				}

			}
		} catch (IOException e) {
			logger.error(e, e);
			return false;
		}
		return true;
	}
	
	private String getUserId(String username) {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(authServerUrl + "/admin/realms/" + realm + "/users?username=" + username);
            request.setHeader("Content-Type", "application/json;charset=UTF-8");
            request.setHeader("Authorization", "Bearer " + getAdminAccessToken());

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                    JSONArray users = new JSONArray(EntityUtils.toString(response.getEntity()));
                    
                    for (int i = 0 ; i < users.length(); i ++) {
                    	JSONObject user = users.getJSONObject(i);
						if (user.getString("username").equals(username)) {
                    		return user.getString("id");
                    	}
                    }
                } else {
                	throw new IllegalStateException("Cannot get userId from identity service");
                }
            }
        } catch (IOException e) {
            logger.error(e, e);
        }
		throw new IllegalStateException("Cannot get userId from identity service");
	}
	
	public boolean generatePassword(String username, String newPassword) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPut request = new HttpPut(authServerUrl + "/admin/realms/" + realm + "/users/" + getUserId(username) + "/reset-password");
            request.setHeader("Content-Type", "application/json;charset=UTF-8");
            request.setHeader("Authorization", "Bearer " + getAdminAccessToken());

            JSONObject credential = new JSONObject();
            credential.put("type", "password");
            credential.put("value", newPassword);
            credential.put("temporary", true);

            request.setEntity(new StringEntity(credential.toString(), "UTF-8"));
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful();
            }
        } catch (IOException e) {
            logger.error(e, e);
            return false;
        }
    }
	
	 private String getAdminAccessToken() throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token");
            ArrayList<BasicNameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("client_id", clientId));
            parameters.add(new BasicNameValuePair("grant_type", "password"));
            parameters.add(new BasicNameValuePair("username", identityServiceUserName));
            parameters.add(new BasicNameValuePair("password", identityServicePassword));
            request.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject auth = new JSONObject(EntityUtils.toString(response.getEntity()));
                    if (auth.has("access_token")) {
                    	return auth.getString("access_token");
                    } else {
						logger.error(auth.toString());
						throw new IllegalStateException("Cannot get access_token from identityService");
					}
                }
            }
        }
        throw new IOException("Failed to obtain admin access token");
    }

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "IdentityServiceAccountProvider [enabled=" + enabled + ", identityServiceUserName=" + identityServiceUserName
				+ ", identityServicePassword=" + identityServicePassword + ", realm=" + realm + ", clientId=" + clientId + ", clientSecret="
				+ clientSecret + ", authServerUrl=" + authServerUrl + "]";
	}

}
