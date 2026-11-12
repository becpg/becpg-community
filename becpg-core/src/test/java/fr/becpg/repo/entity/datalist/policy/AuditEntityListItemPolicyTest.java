package fr.becpg.repo.entity.datalist.policy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.entity.catalog.EntityCatalogService;

/**
 * Unit tests of the audited fields update {@link AuditEntityListItemPolicy} performs before commit on
 * the entities whose variants changed.
 */
public class AuditEntityListItemPolicyTest {

	private static final String VARIANT_ENTITIES = "AuditEntityListItemPolicy.VariantEntities";

	private static final NodeRef DELETED_ENTITY = new NodeRef("workspace://SpacesStore/deleted-entity");

	private static final NodeRef EXISTING_ENTITY = new NodeRef("workspace://SpacesStore/existing-entity");

	private final NodeService nodeService = mock(NodeService.class);

	private final LockService lockService = mock(LockService.class);

	private final BehaviourFilter policyBehaviourFilter = mock(BehaviourFilter.class);

	private final EntityCatalogService entityCatalogService = mock(EntityCatalogService.class);

	private final AuditEntityListItemPolicy policy = new AuditEntityListItemPolicy();

	/**
	 * Wires the policy on mocks: the deleted entity behaves like an Alfresco node deleted in the
	 * current transaction, the existing entity is an unlocked plain entity.
	 */
	@Before
	public void setUp() {
		policy.setNodeService(nodeService);
		policy.setLockService(lockService);
		policy.setPolicyBehaviourFilter(policyBehaviourFilter);
		policy.setEntityCatalogService(entityCatalogService);

		when(nodeService.exists(DELETED_ENTITY)).thenReturn(false);
		when(nodeService.hasAspect(eq(DELETED_ENTITY), any(QName.class))).thenThrow(new InvalidNodeRefException(DELETED_ENTITY));

		when(nodeService.exists(EXISTING_ENTITY)).thenReturn(true);
		when(lockService.getLockStatus(EXISTING_ENTITY)).thenReturn(LockStatus.NO_LOCK);
	}

	/**
	 * An entity deleted with its variants in the same transaction is skipped, while the other
	 * entities still get their audited fields updated.
	 */
	@Test
	public void shouldSkipDeletedEntitiesWhenUpdatingVariantEntities() {
		Set<NodeRef> pendingEntities = new LinkedHashSet<>();
		pendingEntities.add(DELETED_ENTITY);
		pendingEntities.add(EXISTING_ENTITY);

		try {
			policy.doBeforeCommit(VARIANT_ENTITIES, pendingEntities);
		} catch (InvalidNodeRefException e) {
			Assert.fail("A deleted entity must be skipped, not read: " + e.getMessage());
		}

		verify(entityCatalogService).updateAuditedField(eq(EXISTING_ENTITY), isNull(), isNull());
		verify(entityCatalogService, never()).updateAuditedField(eq(DELETED_ENTITY), any(), any());
	}
}
