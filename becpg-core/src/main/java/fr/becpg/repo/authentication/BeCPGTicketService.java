package fr.becpg.repo.authentication;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BeCPGTicketService {



	@Value("${becpg.instance.name}")
	private String instanceName;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private TenantService tenantService;
	
	public String getCurrentAuthToken() {
		String currentUserName = getCurrentBeCPGUserName();
		currentUserName += "#" + authenticationService.getCurrentTicket();

		return java.util.Base64.getEncoder().encodeToString(currentUserName.getBytes());
	}

	public String getCurrentBeCPGUserName() {
		String currentUserName = (instanceName != null ? instanceName : "default") + "$" + authenticationService.getCurrentUserName();
		if (!currentUserName.contains("@") || !tenantService.isEnabled()) {
			currentUserName += "@default";
		}
		return currentUserName;
	}
	
	
}
