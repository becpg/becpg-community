package fr.becpg.olap.authentication;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import fr.becpg.olap.InstanceManager.Instance;
/**
 * 
 * @author matthieu
 *
 */
public class AlfrescoUserDetails extends User implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3854416878397851938L;
	private Instance instance;
	
	public AlfrescoUserDetails(String username, String password, boolean enabled,
			Collection<? extends GrantedAuthority> authorities, Instance instance) {
		super(username, password, enabled, true,true,true, authorities);
		this.instance = instance;
	}

	public Instance getInstance() {
		return instance;
	}

	
}
