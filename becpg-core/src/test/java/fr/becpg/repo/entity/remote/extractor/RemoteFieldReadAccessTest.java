package fr.becpg.repo.entity.remote.extractor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.security.SecurityService;

/**
 * Unit tests of the field level read access applied by the remote export (#37820).
 */
public class RemoteFieldReadAccessTest {

	private static final QName TYPE_FINISHED_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "finishedProduct");

	private static final QName PROP_COST = QName.createQName(BeCPGModel.BECPG_URI, "unitTotalCost");

	private static final String RULED_COST = "bcpg:unitTotalCost";

	private final NodeRef firstProduct = new NodeRef("workspace://SpacesStore/product1");

	private final NodeRef secondProduct = new NodeRef("workspace://SpacesStore/product2");

	private final SecurityService securityService = mock(SecurityService.class);

	private final NodeService nodeService = mock(NodeService.class);

	private RemoteFieldReadAccess fieldReadAccess;

	@Before
	public void setUp() {
		NamespaceService namespaceService = mock(NamespaceService.class);
		when(namespaceService.getNamespaceURI(BeCPGModel.BECPG_PREFIX)).thenReturn(BeCPGModel.BECPG_URI);
		fieldReadAccess = new RemoteFieldReadAccess(securityService, nodeService, namespaceService);

		when(nodeService.getType(any(NodeRef.class))).thenReturn(TYPE_FINISHED_PRODUCT);
		when(securityService.getFieldsWithRules(any(), any())).thenReturn(Set.of(RULED_COST, "View-documents"));
	}

	@Test
	public void hidesRuledFieldWithoutReadAccess() {
		when(securityService.computeAccessMode(null, TYPE_FINISHED_PRODUCT, PROP_COST)).thenReturn(SecurityService.NONE_ACCESS);

		assertFalse(fieldReadAccess.canRead(firstProduct, PROP_COST));
	}

	@Test
	public void exportsReadOnlyField() {
		when(securityService.computeAccessMode(null, TYPE_FINISHED_PRODUCT, PROP_COST)).thenReturn(SecurityService.READ_ACCESS);

		assertTrue(fieldReadAccess.canRead(firstProduct, PROP_COST));
	}

	@Test
	public void exportsFieldWithoutRuleWithoutEvaluatingIt() {
		assertTrue(fieldReadAccess.canRead(firstProduct, BeCPGModel.PROP_ERP_CODE));

		verify(securityService, never()).computeAccessMode(any(), any(), any(QName.class));
	}

	@Test
	public void readsRuledFieldsOncePerType() {
		when(securityService.getFieldsWithRules(any(), any())).thenReturn(Set.of());

		fieldReadAccess.canRead(firstProduct, PROP_COST);
		fieldReadAccess.canRead(secondProduct, BeCPGModel.PROP_ERP_CODE);
		fieldReadAccess.canReadOnType(TYPE_FINISHED_PRODUCT, PROP_COST);

		verify(securityService, times(1)).getFieldsWithRules(null, TYPE_FINISHED_PRODUCT);
		verify(securityService, never()).computeAccessMode(any(), any(), any(QName.class));
	}

	@Test
	public void evaluatesRuledFieldOncePerType() {
		when(securityService.computeAccessMode(null, TYPE_FINISHED_PRODUCT, PROP_COST)).thenReturn(SecurityService.NONE_ACCESS);

		fieldReadAccess.canRead(firstProduct, PROP_COST);
		fieldReadAccess.canRead(firstProduct, PROP_COST);
		fieldReadAccess.canRead(secondProduct, PROP_COST);
		fieldReadAccess.canReadOnType(TYPE_FINISHED_PRODUCT, PROP_COST);

		verify(securityService, times(1)).computeAccessMode(any(), any(), any(QName.class));
	}

	@Test
	public void readsTypeAndAspectsOncePerNode() {
		fieldReadAccess.canRead(firstProduct, PROP_COST);
		fieldReadAccess.canRead(firstProduct, BeCPGModel.PROP_ERP_CODE);

		verify(nodeService, times(1)).getType(firstProduct);
		verify(nodeService, times(1)).hasAspect(firstProduct, SecurityModel.ASPECT_SECURITY);
	}

	@Test
	public void evaluatesLocalRuleOnTheNodeCarryingIt() {
		when(nodeService.hasAspect(firstProduct, SecurityModel.ASPECT_SECURITY)).thenReturn(true);
		when(securityService.computeAccessMode(firstProduct, TYPE_FINISHED_PRODUCT, PROP_COST)).thenReturn(SecurityService.NONE_ACCESS);
		when(securityService.computeAccessMode(null, TYPE_FINISHED_PRODUCT, PROP_COST)).thenReturn(SecurityService.WRITE_ACCESS);

		assertFalse(fieldReadAccess.canRead(firstProduct, PROP_COST));
		assertTrue(fieldReadAccess.canRead(secondProduct, PROP_COST));
	}

	@Test
	public void evaluatesTemplateApart() {
		when(nodeService.hasAspect(firstProduct, BeCPGModel.ASPECT_ENTITY_TPL)).thenReturn(true);
		when(securityService.computeAccessMode(firstProduct, TYPE_FINISHED_PRODUCT, PROP_COST)).thenReturn(SecurityService.WRITE_ACCESS);
		when(securityService.computeAccessMode(null, TYPE_FINISHED_PRODUCT, PROP_COST)).thenReturn(SecurityService.NONE_ACCESS);

		assertTrue(fieldReadAccess.canRead(firstProduct, PROP_COST));
		assertFalse(fieldReadAccess.canRead(secondProduct, PROP_COST));
	}
}
