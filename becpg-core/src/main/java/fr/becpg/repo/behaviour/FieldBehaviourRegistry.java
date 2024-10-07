package fr.becpg.repo.behaviour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class FieldBehaviourRegistry {
	
	private static final List<FieldBehaviour> behaviours = new ArrayList<>();
	
	public static boolean shouldIgnoreActivity(NodeRef nodeRef, QName type, QName field) {
		for (FieldBehaviour behaviour : behaviours) {
			if (behaviour.shouldIgnoreActivity(nodeRef, type, field)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean shouldIgnoreAudit(NodeRef nodeRef, QName field) {
		for (FieldBehaviour behaviour : behaviours) {
			if (behaviour.shouldIgnoreAudit(nodeRef, field)) {
				return true;
			}
		}
		return false;
	}
	
	public static void registerFieldBehaviour(FieldBehaviour fieldBehaviour) {
		behaviours.add(fieldBehaviour);
	}
	
	public static void registerIgnoredActivityFields(QName... fields) {
		List<QName> fieldsList = Arrays.asList(fields);
		registerFieldBehaviour(new FieldBehaviour() {
			@Override
			public boolean shouldIgnoreActivity(NodeRef nodeRef, QName type, QName field) {
				return fieldsList.contains(field);
			}
		});
	}
	
	public static void registerIgnoredAuditFields(QName... fields) {
		List<QName> fieldsList = Arrays.asList(fields);
		registerFieldBehaviour(new FieldBehaviour() {
			@Override
			public boolean shouldIgnoreAudit(NodeRef nodeRef, QName field) {
				return fieldsList.contains(field);
			}
		});
	}
	
	public interface FieldBehaviour {
		default boolean shouldIgnoreActivity(NodeRef nodeRef, QName type, QName field) {
			return false;
		}
		default boolean shouldIgnoreAudit(NodeRef nodeRef, QName field) {
			return false;
		}
	}
	
}
