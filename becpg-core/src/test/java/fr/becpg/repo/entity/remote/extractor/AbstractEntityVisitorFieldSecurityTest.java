package fr.becpg.repo.entity.remote.extractor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.Set;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.security.SecurityService;

/**
 * Unit tests of the opt-in of the field level security rules in the remote export (#37820).
 */
public class AbstractEntityVisitorFieldSecurityTest {

	private static final QName TYPE_FINISHED_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "finishedProduct");

	private static final QName PROP_COST = QName.createQName(BeCPGModel.BECPG_URI, "unitTotalCost");

	private final NodeRef product = new NodeRef("workspace://SpacesStore/product");

	private final SecurityService securityService = mock(SecurityService.class);

	private StubEntityVisitor visitor;

	private static class StubEntityVisitor extends AbstractEntityVisitor {

		StubEntityVisitor(RemoteServiceRegisty remoteServiceRegisty) {
			super(remoteServiceRegisty);
		}

		@Override
		public void visit(NodeRef entityNodeRef, OutputStream result) {
			// Only the field check of the base class is under test
		}

		@Override
		public void visit(PagingResults<NodeRef> entities, OutputStream result) {
			// Only the field check of the base class is under test
		}

		@Override
		public void visitData(NodeRef entityNodeRef, OutputStream result) {
			// Only the field check of the base class is under test
		}
	}

	@Before
	public void setUp() {
		NodeService nodeService = mock(NodeService.class);
		when(nodeService.getType(product)).thenReturn(TYPE_FINISHED_PRODUCT);

		NamespaceService namespaceService = mock(NamespaceService.class);
		when(namespaceService.getNamespaceURI(BeCPGModel.BECPG_PREFIX)).thenReturn(BeCPGModel.BECPG_URI);

		RemoteServiceRegisty registry = mock(RemoteServiceRegisty.class);
		when(registry.unsecuredNodeService()).thenReturn(nodeService);
		when(registry.namespaceService()).thenReturn(namespaceService);
		when(registry.securityService()).thenReturn(securityService);

		when(securityService.getFieldsWithRules(any(), any())).thenReturn(Set.of("bcpg:unitTotalCost"));
		when(securityService.computeAccessMode(null, TYPE_FINISHED_PRODUCT, PROP_COST)).thenReturn(SecurityService.NONE_ACCESS);

		visitor = new StubEntityVisitor(registry);
	}

	@Test
	public void exportsEveryFieldByDefault() {
		visitor.setParams(new RemoteParams(RemoteEntityFormat.json));

		assertTrue(visitor.canReadField(product, PROP_COST));
		verify(securityService, never()).computeAccessMode(any(), any(), any(QName.class));
	}

	@Test
	public void hidesFieldWhenFieldSecurityIsEnforced() {
		RemoteParams params = new RemoteParams(RemoteEntityFormat.json);
		params.setEnforceFieldSecurity(true);
		visitor.setParams(params);

		assertFalse(visitor.canReadField(product, PROP_COST));
		assertFalse(visitor.canReadField(TYPE_FINISHED_PRODUCT, PROP_COST));
	}

	@Test
	public void exportsSerializedNodeOfUnknownType() {
		RemoteParams params = new RemoteParams(RemoteEntityFormat.json);
		params.setEnforceFieldSecurity(true);
		visitor.setParams(params);

		assertTrue(visitor.canReadField((QName) null, PROP_COST));
	}
}
