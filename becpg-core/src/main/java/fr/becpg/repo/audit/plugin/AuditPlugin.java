package fr.becpg.repo.audit.plugin;

import fr.becpg.repo.audit.model.AuditType;

/**
 * <p>AuditPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface AuditPlugin {

	/** Constant <code>ID="id"</code> */
	public static final String ID = "id";
	/** Constant <code>STARTED_AT="startedAt"</code> */
	public static final String STARTED_AT = "startedAt";
	/** Constant <code>COMPLETED_AT="completedAt"</code> */
	public static final String COMPLETED_AT = "completedAt";
	/** Constant <code>DURATION="duration"</code> */
	public static final String DURATION = "duration";

	/**
	 * <p>applyTo.</p>
	 *
	 * @param type a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @return a boolean
	 */
	boolean applyTo(AuditType type);

	/**
	 * <p>isDatabaseEnable.</p>
	 *
	 * @return a boolean
	 */
	boolean isDatabaseEnable();
	
	/**
	 * <p>isStopWatchEnable.</p>
	 *
	 * @return a boolean
	 */
	boolean isStopWatchEnable();
	
	/**
	 * <p>isTracerEnable.</p>
	 *
	 * @return a boolean
	 */
	boolean isTracerEnable();

	/**
	 * <p>getAuditedClass.</p>
	 *
	 * @return a {@link java.lang.Class} object
	 */
	Class<?> getAuditedClass();

}
