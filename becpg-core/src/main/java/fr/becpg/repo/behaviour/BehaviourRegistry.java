package fr.becpg.repo.behaviour;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * <p>BehaviourRegistry class.</p>
 *
 * @author matthieu
 */
public class BehaviourRegistry {
	
	private static final List<ActivityBehaviour> activityBehaviours = new ArrayList<>();
	
	private static final List<AuditBehaviour> auditBehaviours = new ArrayList<>();
	
	private BehaviourRegistry() {
		
	}
	
	/**
	 * <p>shouldIgnoreAuditField.</p>
	 *
	 * @param field a {@link org.alfresco.service.namespace.QName} object
	 * @return a boolean
	 */
	public static boolean shouldIgnoreAuditField(QName field) {
		for (AuditBehaviour behaviour : auditBehaviours) {
			if (behaviour.shouldIgnoreField(field)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <p>registerAuditBehaviour.</p>
	 *
	 * @param fieldBehaviour a {@link fr.becpg.repo.behaviour.BehaviourRegistry.AuditBehaviour} object
	 */
	public static void registerAuditBehaviour(AuditBehaviour fieldBehaviour) {
		auditBehaviours.add(fieldBehaviour);
	}
	
	
	/**
	 * <p>registerActivityBehaviour.</p>
	 *
	 * @param fieldBehaviour a {@link fr.becpg.repo.behaviour.BehaviourRegistry.ActivityBehaviour} object
	 */
	public static void registerActivityBehaviour(ActivityBehaviour fieldBehaviour) {
		activityBehaviours.add(fieldBehaviour);
	}
	
	/**
	 * <p>shouldIgnoreActivityField.</p>
	 *
	 * @param field a {@link org.alfresco.service.namespace.QName} object
	 * @return a boolean
	 */
	public static boolean shouldIgnoreActivityField(QName field) {
		for (ActivityBehaviour behaviour : activityBehaviours) {
			if (behaviour.shouldIgnoreField(field)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <p>shouldIgnoreActivity.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 * @param fields a {@link java.util.Map} object
	 * @return a boolean
	 */
	public static boolean shouldIgnoreActivity(NodeRef nodeRef, QName type, Map<QName, Pair<Serializable, Serializable>> fields) {
		for (ActivityBehaviour behaviour : activityBehaviours) {
			if (behaviour.shouldIgnoreActivity(nodeRef, type, fields)) {
				return true;
			}
		}
		return false;
	}
	
	public static class AuditBehaviour {
		private List<QName> ignoredFields = List.of();
		public AuditBehaviour(QName... ignoredFields) {
			this.ignoredFields = Arrays.asList(ignoredFields);
		}
		public boolean shouldIgnoreField(QName field) {
			return ignoredFields.contains(field);
		}
	}
	
	public static class ActivityBehaviour {
		
		private List<QName> ignoredFields;
		
		public ActivityBehaviour() {
			this.ignoredFields = List.of();
		}
		
		public ActivityBehaviour(QName... ignoredFields) {
			this.ignoredFields = Arrays.asList(ignoredFields);
		}
		
		public boolean shouldIgnoreField(QName field) {
			return ignoredFields.contains(field);
		}
		
		public boolean shouldIgnoreActivity(NodeRef nodeRef, QName type, Map<QName, Pair<Serializable, Serializable>> updatedFields) {
			return false;
		}
	}

}
