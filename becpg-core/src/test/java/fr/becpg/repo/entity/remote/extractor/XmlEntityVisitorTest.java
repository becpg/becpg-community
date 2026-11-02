package fr.becpg.repo.entity.remote.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.security.SecurityService;

/**
 * Unit tests of the node header written by the XML and Excel remote exports (#37820): the code
 * hidden by a security rule, and a node whose folder the user cannot read.
 */
public class XmlEntityVisitorTest {

	private static final QName TYPE_FINISHED_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "finishedProduct");

	private static final String PRODUCT_CODE = "PF37820";

	private static final String PARENT_PATH = "/app:company_home/cm:Products";

	private final NodeRef product = new NodeRef("workspace://SpacesStore/product");

	private final NodeRef folder = new NodeRef("workspace://SpacesStore/folder");

	private final NodeService nodeService = mock(NodeService.class);

	private final SecurityService securityService = mock(SecurityService.class);

	private RemoteServiceRegisty registry;

	@Before
	public void setUp() {
		NamespaceService namespaceService = mock(NamespaceService.class);
		when(namespaceService.getNamespaceURI(BeCPGModel.BECPG_PREFIX)).thenReturn(BeCPGModel.BECPG_URI);
		when(namespaceService.getPrefixes(BeCPGModel.BECPG_URI)).thenReturn(List.of(BeCPGModel.BECPG_PREFIX));

		when(nodeService.getType(product)).thenReturn(TYPE_FINISHED_PRODUCT);
		when(nodeService.getProperty(product, ContentModel.PROP_NAME)).thenReturn("Product");
		when(nodeService.getProperty(product, BeCPGModel.PROP_CODE)).thenReturn(PRODUCT_CODE);
		when(nodeService.hasAspect(product, BeCPGModel.ASPECT_CODE)).thenReturn(true);
		when(nodeService.getPrimaryParent(product)).thenReturn(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, folder, null, product));
		Path path = mock(Path.class);
		when(path.toPrefixString(namespaceService)).thenReturn(PARENT_PATH);
		when(nodeService.getPath(folder)).thenReturn(path);

		when(securityService.getFieldsWithRules(any(), any())).thenReturn(Set.of("bcpg:code"));
		when(securityService.computeAccessMode(null, TYPE_FINISHED_PRODUCT, BeCPGModel.PROP_CODE)).thenReturn(SecurityService.READ_ACCESS);

		registry = mock(RemoteServiceRegisty.class);
		when(registry.nodeService()).thenReturn(nodeService);
		when(registry.unsecuredNodeService()).thenReturn(nodeService);
		when(registry.namespaceService()).thenReturn(namespaceService);
		when(registry.entityDictionaryService()).thenReturn(mock(EntityDictionaryService.class));
		when(registry.securityService()).thenReturn(securityService);
		when(registry.contentService()).thenReturn(mock(ContentService.class));
	}

	@Test
	public void writesPathAndCodeOfReadableNode() throws Exception {
		Element root = exportData(new XmlEntityVisitor(registry), RemoteEntityFormat.xml);

		assertEquals(PARENT_PATH, root.getAttribute(RemoteEntityService.ATTR_PATH));
		assertEquals(PRODUCT_CODE, root.getAttribute(RemoteEntityService.ATTR_CODE));
	}

	@Test
	public void leavesPathOutWhenParentIsNotReadable() throws Exception {
		when(nodeService.getPrimaryParent(product)).thenThrow(new AccessDeniedException("denied"));

		Element root = exportData(new XmlEntityVisitor(registry), RemoteEntityFormat.xml);

		assertFalse(root.hasAttribute(RemoteEntityService.ATTR_PATH));
		assertEquals(product.toString(), root.getAttribute(RemoteEntityService.ATTR_NODEREF));
	}

	@Test
	public void leavesPathOutWhenParentPathIsNotReadable() throws Exception {
		when(nodeService.getPath(folder)).thenThrow(new AccessDeniedException("denied"));

		Element root = exportData(new XmlEntityVisitor(registry), RemoteEntityFormat.xml);

		assertFalse(root.hasAttribute(RemoteEntityService.ATTR_PATH));
		assertEquals(PRODUCT_CODE, root.getAttribute(RemoteEntityService.ATTR_CODE));
	}

	@Test
	public void excelLeavesPathOutWhenParentIsNotReadable() throws Exception {
		when(nodeService.getPrimaryParent(product)).thenThrow(new AccessDeniedException("denied"));

		Element root = exportData(new ExcelXmlEntityVisitor(registry), RemoteEntityFormat.xml_excel);

		assertFalse(root.hasAttribute(RemoteEntityService.ATTR_PATH));
		assertEquals(product.toString(), root.getAttribute(RemoteEntityService.ATTR_NODEREF));
	}

	@Test
	public void excelWritesCodeOfReadableField() throws Exception {
		Element root = exportData(new ExcelXmlEntityVisitor(registry), RemoteEntityFormat.xml_excel);

		assertEquals(PARENT_PATH, root.getAttribute(RemoteEntityService.ATTR_PATH));
		assertEquals(PRODUCT_CODE, root.getAttribute(RemoteEntityService.ATTR_CODE));
	}

	@Test
	public void excelHidesCodeHiddenBySecurityRule() throws Exception {
		when(securityService.computeAccessMode(null, TYPE_FINISHED_PRODUCT, BeCPGModel.PROP_CODE)).thenReturn(SecurityService.NONE_ACCESS);

		Element root = exportData(new ExcelXmlEntityVisitor(registry), RemoteEntityFormat.xml_excel);

		assertFalse(root.hasAttribute(RemoteEntityService.ATTR_CODE));
		assertTrue(root.hasAttribute(RemoteEntityService.ATTR_NODEREF));
	}

	private Element exportData(AbstractEntityVisitor visitor, RemoteEntityFormat format) throws Exception {
		RemoteParams params = new RemoteParams(format);
		params.setEnforceFieldSecurity(true);
		visitor.setParams(params);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		visitor.visitData(product, out);

		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()))
				.getDocumentElement();
	}
}
