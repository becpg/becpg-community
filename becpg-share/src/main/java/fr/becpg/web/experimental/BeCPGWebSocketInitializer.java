package fr.becpg.web.experimental;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.ServletContextAware;

import jakarta.servlet.ServletContext;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Automatically discovers and registers WebSocket server endpoints in the ServerContainer.
 *
 * @author beCPG
 */
public class BeCPGWebSocketInitializer implements ServletContextAware {

	private static final Log logger = LogFactory.getLog(BeCPGWebSocketInitializer.class);

	private static final String SERVER_CONTAINER_ATTRIBUTE = "jakarta.websocket.server.ServerContainer";
	private static final String BASE_PACKAGE = "fr.becpg";

	/** {@inheritDoc} */
	@Override
	public void setServletContext(ServletContext servletContext) {
		registerEndpoints(servletContext);
	}

	/**
	 * Discovers and registers WebSocket endpoints in the ServerContainer.
	 *
	 * @param servletContext the current ServletContext
	 */
	private void registerEndpoints(ServletContext servletContext) {
		Object attr = servletContext.getAttribute(SERVER_CONTAINER_ATTRIBUTE);
		if (attr instanceof ServerContainer serverContainer) {
			Set<BeanDefinition> candidates = scanWebSocketEndpoints();
			for (BeanDefinition candidate : candidates) {
				registerCandidate(serverContainer, candidate.getBeanClassName());
			}
		} else if (logger.isWarnEnabled()) {
			logger.warn("ServerContainer attribute not found in ServletContext, WebSocket endpoints not registered.");
		}
	}

	/**
	 * Scans the base package for classes annotated with @ServerEndpoint.
	 *
	 * @return set of matching bean definitions
	 */
	private Set<BeanDefinition> scanWebSocketEndpoints() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(ServerEndpoint.class));
		return scanner.findCandidateComponents(BASE_PACKAGE);
	}

	/**
	 * Registers a single WebSocket endpoint class by name.
	 *
	 * @param serverContainer the WebSocket ServerContainer
	 * @param className the fully-qualified class name
	 */
	private void registerCandidate(ServerContainer serverContainer, String className) {
		try {
			Class<?> endpointClass = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
			serverContainer.addEndpoint(endpointClass);
			if (logger.isInfoEnabled()) {
				logger.info("Successfully registered WebSocket endpoint: " + className);
			}
		} catch (ClassNotFoundException | LinkageError e) {
			logger.error("Could not load WebSocket endpoint class: " + className, e);
		} catch (DeploymentException e) {
			logger.error("Failed to register WebSocket endpoint: " + className, e);
		}
	}
}
