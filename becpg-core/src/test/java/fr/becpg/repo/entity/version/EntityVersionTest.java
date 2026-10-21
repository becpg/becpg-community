package fr.becpg.repo.entity.version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

/**
 * Unit tests of {@link EntityVersion#isUnversionedEntity()}.
 */
public class EntityVersionTest {

	private static final NodeRef ENTITY = new NodeRef("workspace://SpacesStore/entity");

	private static final NodeRef FROZEN_STATE = new NodeRef("workspace://version2Store/frozen-state");

	private static final NodeRef ENTITY_VERSION = new NodeRef("workspace://SpacesStore/entity-version");

	@Test
	public void entityWithoutVersionHistoryIsUnversioned() {
		assertTrue(createVersion(ENTITY, ENTITY).isUnversionedEntity());
	}

	@Test
	public void versionFromTheVersionHistoryIsNotUnversioned() {
		assertFalse(createVersion(FROZEN_STATE, ENTITY_VERSION).isUnversionedEntity());
	}

	private static EntityVersion createVersion(NodeRef frozenState, NodeRef entityVersion) {
		Map<String, Serializable> versionProperties = new HashMap<>();
		return new EntityVersion(new VersionImpl(versionProperties, frozenState), ENTITY, entityVersion, null);
	}
}
