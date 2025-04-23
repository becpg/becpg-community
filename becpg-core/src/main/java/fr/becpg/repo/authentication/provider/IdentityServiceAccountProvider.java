package fr.becpg.repo.authentication.provider;

import java.io.IOException;
import java.util.ArrayList;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceException;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
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

	private static final String GET_USER_ID_ERROR = "Could not find userId from identity service for user: ";

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
		if (logger.isDebugEnabled()) {
        	logger.debug("registerAccount in IDS for username: " + userAccount.getUserName());
        }
		if (getUserId(userAccount.getUserName()) != null) {
			if(logger.isDebugEnabled()){
				logger.debug("user already exists in IDS: " + userAccount.getUserName());
			}
			return false;
		}
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
					String response = EntityUtils.toString(createResp.getEntity());
					if (!HttpStatus.valueOf(createResp.getStatusLine().getStatusCode()).is2xxSuccessful()) {
						throw new IdentityServiceException(response);
					} else if(logger.isDebugEnabled()){
						logger.debug(response);
					}
				}

			}
		} catch (IOException e) {
			logger.error(e, e);
            throw new IdentityServiceException("Could not register user in IDS", e);
		}
		return true;
	}
	
	public void deleteAccount(String username) {
		if (logger.isDebugEnabled()) {
        	logger.debug("deleteAccount in IDS for username: " + username);
        }
		String userId = getUserId(username);
		if (userId == null) {
			if(logger.isDebugEnabled()){
				logger.debug("user already deleted in IDS: " + username);
			}
			return;
		}
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpDelete request = new HttpDelete(authServerUrl + "/admin/realms/" + realm + "/users/" + userId);
			request.setHeader("Authorization", "Bearer " + getAdminAccessToken());
			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
					throw new IdentityServiceException("Failed to delete user: " + username);
				}
			}
		} catch (IOException e) {
			logger.error(e, e);
			throw new IdentityServiceException("Could not delete user in IDS", e);
		}
	}


	public boolean updateUser(BeCPGUserAccount userAccount) {
		if (logger.isDebugEnabled()) {
			logger.debug("updateUser in IDS for username: " + userAccount.getUserName());
		}
		String userId = getUserId(userAccount.getUserName());
		if (userId == null) {
			throw new IllegalStateException(GET_USER_ID_ERROR + userAccount.getUserName());
		}
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpPut request = new HttpPut(authServerUrl + "/admin/realms/" + realm + "/users/" + userId);
			request.setHeader("Content-Type", "application/json;charset=UTF-8");
			request.setHeader("Authorization", "Bearer " + getAdminAccessToken());

			JSONObject userRepresentation = new JSONObject();
			if (userAccount.getEmail() != null && !userAccount.getEmail().isBlank()) {
				userRepresentation.put("email", userAccount.getEmail());
			}
			if (userAccount.getFirstName() != null && !userAccount.getFirstName().isBlank()) {
				userRepresentation.put("firstName", userAccount.getFirstName());
			}
			if (userAccount.getLastName() != null && !userAccount.getLastName().isBlank()) {
				userRepresentation.put("lastName", userAccount.getLastName());
			}
			
			request.setEntity(new StringEntity(userRepresentation.toString(), "UTF-8"));
			try (CloseableHttpResponse response = httpClient.execute(request)) {
				if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
					String reponseBody = "";
					if (response.getEntity() != null) {
						reponseBody = EntityUtils.toString(response.getEntity());
					}
					throw new IdentityServiceException("Failed to update user: " + userAccount.getUserName() + ", reponseBody: " + reponseBody);
				}
			}
		} catch (IOException e) {
			logger.error(e, e);
			throw new IdentityServiceException("Error while updating user in IDS", e);
		}
		return true;
	}
	
	public void updatePassword(String username, String newPassword) {
		if (logger.isDebugEnabled()) {
        	logger.debug("generatePassword in IDS for username: " + username);
        }
	    try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
	        String userId = getUserId(username);
	        if (userId == null) {
	            throw new IllegalStateException(GET_USER_ID_ERROR + username);
	        }
			HttpPut request = new HttpPut(authServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password");
	        request.setHeader("Content-Type", "application/json;charset=UTF-8");
	        request.setHeader("Authorization", "Bearer " + getAdminAccessToken());
	
	        JSONObject credential = new JSONObject();
	        credential.put("type", "password");
	        credential.put("value", newPassword);
	        credential.put("temporary", true);
	
	        request.setEntity(new StringEntity(credential.toString(), "UTF-8"));
	        try (CloseableHttpResponse response = httpClient.execute(request)) {
	            if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
	            	String responseBody = EntityUtils.toString(response.getEntity());
	            	throw new IdentityServiceException("Error while updating password in IDS for user: " + username + ", responseBody: " + responseBody);
	            }
	        }
	    } catch (IOException e) {
	        logger.error(e, e);
	        throw new IdentityServiceException("Error while updating password in IDS for user: " + username, e);
	    }
	}


	private String getUserId(String username) {
		if (logger.isDebugEnabled()) {
        	logger.debug("getUserId in IDS for username: " + username);
        }
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(authServerUrl + "/admin/realms/" + realm + "/users?username=" + username);
            request.setHeader("Content-Type", "application/json;charset=UTF-8");
            request.setHeader("Authorization", "Bearer " + getAdminAccessToken());

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String reponseBody = EntityUtils.toString(response.getEntity());
                if (logger.isDebugEnabled()) {
                	logger.debug("reponseBody from getUserId: " + reponseBody);
                }
				if (HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                    JSONArray users = new JSONArray(reponseBody);
                    for (int i = 0 ; i < users.length(); i ++) {
                    	JSONObject user = users.getJSONObject(i);
						if (user.getString("username").equals(username)) {
                    		return user.getString("id");
                    	}
                    }
                    return null;
                } else {
                	throw new IdentityServiceException("Error while fetching userId from identity service: " + reponseBody + " for user: " + username);
                }
            }
        } catch (IOException e) {
            logger.error(e, e);
            throw new IdentityServiceException("Error while fetching userId from identity service for user: " + username, e);
        }
	}
	
	private String getAdminAccessToken() throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token");
            ArrayList<BasicNameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("client_id", clientId));
            if (clientSecret != null && !clientSecret.isBlank()) {
            	parameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
            	parameters.add(new BasicNameValuePair("client_secret", clientSecret));
            } else {
            	parameters.add(new BasicNameValuePair("grant_type", "password"));
            	parameters.add(new BasicNameValuePair("username", identityServiceUserName));
            	parameters.add(new BasicNameValuePair("password", identityServicePassword));
            }
            request.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject auth = new JSONObject(EntityUtils.toString(response.getEntity()));
                    if (auth.has("access_token")) {
                    	return auth.getString("access_token");
                    } else {
						logger.error(auth.toString());
						throw new IllegalStateException("Error while fetching access_token from identityService");
					}
                }
            }
        }
        throw new IOException("Error while fetching access_token from identityService");
    }

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "IdentityServiceAccountProvider [enabled=" + enabled + ", identityServiceUserName=" + identityServiceUserName
				+ ", identityServicePassword=" + identityServicePassword + ", realm=" + realm + ", clientId=" + clientId + ", clientSecret="
				+ clientSecret + ", authServerUrl=" + authServerUrl + "]";
	}

}
