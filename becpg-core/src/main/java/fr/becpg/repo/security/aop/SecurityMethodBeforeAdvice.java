/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.security.aop;

import java.lang.reflect.Method;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.MethodBeforeAdvice;

import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.security.BeCPGAccessDeniedException;
import fr.becpg.repo.security.SecurityService;

/**
 * <p>SecurityMethodBeforeAdvice class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SecurityMethodBeforeAdvice implements MethodBeforeAdvice {

	private SecurityService securityService;

	private NamespaceService namespaceService;

	private static final Log logger = LogFactory.getLog(SecurityMethodBeforeAdvice.class);

	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/** {@inheritDoc} */
	@Override
	public void before(Method m, Object[] args, Object target) throws Throwable {

		if (m.isAnnotationPresent(AlfQname.class)) {
			String methodQName = m.getAnnotation(AlfQname.class).qname();
			if (methodQName != null && target!=null &&  target.getClass().isAnnotationPresent(AlfQname.class)) {
				String classQName = target.getClass().getAnnotation(AlfQname.class).qname();
				if (classQName != null) {
					if (securityService.computeAccessMode(null, QName.createQName(classQName, namespaceService), methodQName) != SecurityService.WRITE_ACCESS) {
						if(logger.isInfoEnabled()){
							logger.info("Access denied to field " + methodQName + " for " + AuthenticationUtil.getFullyAuthenticatedUser());
						}
						throw new BeCPGAccessDeniedException(methodQName);
					}
				}

			}
		}

	}

}
