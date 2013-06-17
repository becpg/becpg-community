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

public class SecurityMethodBeforeAdvice implements MethodBeforeAdvice {

	private SecurityService securityService;

	private NamespaceService namespaceService;

	private static Log logger = LogFactory.getLog(SecurityMethodBeforeAdvice.class);

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public void before(Method m, Object[] args, Object target) throws Throwable {

		if (m.isAnnotationPresent(AlfQname.class)) {
			String methodQName = m.getAnnotation(AlfQname.class).qname();
			if (methodQName != null) {
				String classQName = target.getClass().getAnnotation(AlfQname.class).qname();
				if (classQName != null) {
					if (securityService.computeAccessMode(QName.createQName(classQName, namespaceService), methodQName) != SecurityService.WRITE_ACCESS) {
						logger.info("Access denied to field " + methodQName + " for " + AuthenticationUtil.getFullyAuthenticatedUser());
						throw new BeCPGAccessDeniedException(methodQName);
					}
				}

			}
		}

	}

}
