package fr.becpg.repo.audit.plugin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import fr.becpg.repo.audit.model.AuditDataType;

/**
 * <p>Abstract AbstractAuditPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractAuditPlugin implements AuditPlugin, InitializingBean {
	
	/** Constant <code>KEY_MAP</code> */
	protected static final Map<String, AuditDataType> KEY_MAP = new HashMap<>();

	private boolean databaseEnable = false;
	
	private boolean stopWatchEnable = false;
	
	private boolean tracerEnable = false;
	
	private String auditParameters;
	
	/**
	 * <p>Setter for the field <code>auditParameters</code>.</p>
	 *
	 * @param auditParameters a {@link java.lang.String} object
	 */
	protected void setAuditParameters(String auditParameters) {
		this.auditParameters = auditParameters;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isDatabaseEnable() {
		return databaseEnable;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isStopWatchEnable() {
		return stopWatchEnable;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isTracerEnable() {
		return tracerEnable;
	}
	
	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (auditParameters != null && !auditParameters.isBlank()) {
			String[] parameters = auditParameters.split(",");
			
			for (String parameter : parameters) {
				if ("stopwatch".equals(parameter)) {
					stopWatchEnable = true;
				} else if ("audit".equals(parameter)) {
					databaseEnable = true;
				} else if ("tracer".equals(parameter)) {
					tracerEnable = true;
				}
			}
		}
	}

	/**
	 * <p>getKeyMap.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<String, AuditDataType> getKeyMap() {
		return KEY_MAP;
	}
	
}
