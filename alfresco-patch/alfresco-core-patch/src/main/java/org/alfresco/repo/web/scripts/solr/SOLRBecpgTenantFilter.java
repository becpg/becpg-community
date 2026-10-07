package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;

/**
 * Fix #2143
 * 03061477 domain mismatch: expected = demo.becpg.fr, actual = agro
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SOLRBecpgTenantFilter implements DependencyInjectedFilter {

	/** {@inheritDoc} */
	@Override
	public void doFilter(ServletContext context, ServletRequest request,
			ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{

		if(AuthenticationUtil.isMtEnabled()){
			
			//beCPG in SOLR authentication is NONE security context should be empty, but *sometimes* is not
			AuthenticationUtil.clearCurrentSecurityContext();
			
			TenantUtil.runAsDefaultTenant(new TenantRunAsWork<Void>() {
				@Override
				public Void doWork() throws Exception {
					
					chain.doFilter(request, response);
					return null;
				}
			});
		}	else {
			chain.doFilter(request, response);
		}

	}

}
