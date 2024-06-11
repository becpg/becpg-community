package fr.becpg.repo.authentication;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <p>BeCPGTicketService class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class BeCPGTicketService {



	@Value("${becpg.instance.name}")
	private String instanceName;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private TenantService tenantService;
	
	/**
	 * <p>getCurrentAuthToken.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getCurrentAuthToken() {
		String currentUserName = getCurrentBeCPGUserName();
		currentUserName += "#" + authenticationService.getCurrentTicket();

		return java.util.Base64.getEncoder().encodeToString(currentUserName.getBytes());
	}

	/**
	 * <p>getCurrentBeCPGUserName.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getCurrentBeCPGUserName() {
		String currentUserName = (instanceName != null ? instanceName : "default") + "$" + authenticationService.getCurrentUserName();
		if (!currentUserName.contains("@") || !tenantService.isEnabled()) {
			currentUserName += "@default";
		}
		return currentUserName;
	}
	
	
}
