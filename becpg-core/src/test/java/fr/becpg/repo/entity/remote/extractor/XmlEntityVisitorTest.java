package fr.becpg.repo.entity.remote.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.DocumentBuilderFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.security.SecurityService;

/**
 * Unit tests of the XML and Excel remote exports (#37820): the code hidden by a security rule, a
 * node whose folder the user cannot read, a referenced node the user cannot read, and the depth
 * <code>xml_all</code> follows associations and references to.
 */
public class XmlEntityVisitorTest {

	private static final QName TYPE_FINISHED_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "finishedProduct");

	private static final String PRODUCT_CODE = "PF37820";

	private static final String PARENT_PATH = "/app:company_home/cm:Products";

	private static final QName PROP_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "rclSourcesV2");

	private static final QName PROP_SOURCE = QName.createQName(BeCPGModel.BECPG_URI, "rclSource");

	private static final QName TYPE_REQ_CTRL_LIST = QName.createQName(BeCPGModel.BECPG_URI, "reqCtrlList");

	private static final QName ASSOC_REQ_CTRL_ID = QName.createQName(BeCPGModel.BECPG_URI, "rclCharactRef");

	private static final String READABLE_NAME = "Readable source";

	private static final String HIDDEN_NAME = "Hidden source";

	private static final String HIDDEN_CODE = "PF-HIDDEN";

	private static final QName ASSOC_COMPO_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "compoListProduct");

	private static final String ELEM_FINISHED_PRODUCT = "bcpg:finishedProduct";

	private static final int LONG_CHAIN_LENGTH = 2000;

	private static final long SMALL_STACK_SIZE = 256L * 1024L;

	private final NodeRef component = new NodeRef("workspace://SpacesStore/component");

	private final NodeRef subComponent = new NodeRef("workspace://SpacesStore/subComponent");

	private final NodeRef product = new NodeRef("workspace://SpacesStore/product");

	private final NodeRef folder = new NodeRef("workspace://SpacesStore/folder");

	private final NodeRef readableSource = new NodeRef("workspace://SpacesStore/readableSource");

	private final NodeRef hiddenSource = new NodeRef("workspace://SpacesStore/hiddenSource");

	private final NodeRef deletedSource = new NodeRef("workspace://SpacesStore/deletedSource");

	private final EntityDictionaryService entityDictionaryService = mock(EntityDictionaryService.class);

	private final PermissionService permissionService = mock(PermissionService.class);

	private final AssociationService associationService = mock(AssociationService.class);

	private final NodeService nodeService = mock(NodeService.class);

	private final SecurityService securityService = mock(SecurityService.class);

	private RemoteServiceRegisty registry;

	@Before
	public void setUp() {
		NamespaceService namespaceService = mock(NamespaceService.class);
		when(namespaceService.getNamespaceURI(BeCPGModel.BECPG_PREFIX)).thenReturn(BeCPGModel.BECPG_URI);
		when(namespaceService.getPrefixes(BeCPGModel.BECPG_URI)).thenReturn(List.of(BeCPGModel.BECPG_PREFIX));
		when(namespaceService.getPrefixes(NamespaceService.DICTIONARY_MODEL_1_0_URI)).thenReturn(List.of(NamespaceService.DICTIONARY_MODEL_PREFIX));

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
		when(registry.entityDictionaryService()).thenReturn(entityDictionaryService);
		when(registry.securityService()).thenReturn(securityService);
		when(registry.contentService()).thenReturn(mock(ContentService.class));
		when(registry.permissionService()).thenReturn(permissionService);
		when(registry.associationService()).thenReturn(associationService);

		mockSources();
	}

	private void mockSources() {
		mockSource(readableSource, READABLE_NAME, "PF-READABLE");
		when(permissionService.hasPermission(readableSource, PermissionService.READ)).thenReturn(AccessStatus.ALLOWED);

		mockSource(hiddenSource, HIDDEN_NAME, HIDDEN_CODE);
		when(permissionService.hasPermission(hiddenSource, PermissionService.READ)).thenReturn(AccessStatus.DENIED);
		when(nodeService.getProperty(hiddenSource, ContentModel.PROP_NAME)).thenThrow(new AccessDeniedException("denied"));
		when(nodeService.getProperty(hiddenSource, BeCPGModel.PROP_CODE)).thenThrow(new AccessDeniedException("denied"));
		when(nodeService.getPrimaryParent(hiddenSource)).thenThrow(new AccessDeniedException("denied"));

		PropertyDefinition sourcesDefinition = mock(PropertyDefinition.class);
		DataTypeDefinition nodeRefType = mock(DataTypeDefinition.class);
		when(nodeRefType.getName()).thenReturn(DataTypeDefinition.NODE_REF);
		when(sourcesDefinition.getDataType()).thenReturn(nodeRefType);
		when(sourcesDefinition.getName()).thenReturn(PROP_SOURCES);
		when(entityDictionaryService.getProperty(PROP_SOURCES)).thenReturn(sourcesDefinition);
		when(entityDictionaryService.getProperty(PROP_SOURCE)).thenReturn(sourcesDefinition);
		when(entityDictionaryService.toPrefixString(DataTypeDefinition.NODE_REF)).thenReturn("d:noderef");
	}

	private void mockSource(NodeRef source, String name, String code) {
		when(nodeService.exists(source)).thenReturn(true);
		when(nodeService.getType(source)).thenReturn(TYPE_FINISHED_PRODUCT);
		when(nodeService.getProperty(source, ContentModel.PROP_NAME)).thenReturn(name);
		when(nodeService.getProperty(source, BeCPGModel.PROP_CODE)).thenReturn(code);
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

	@Test
	public void leavesUnreadableReferenceOutOfMultiValuedProperty() throws Exception {
		givenProductProperty(PROP_SOURCES, new ArrayList<>(List.of(readableSource, hiddenSource, deletedSource)));

		String xml = export(new XmlEntityVisitor(registry), RemoteEntityFormat.xml_all);
		Element sources = firstElement(parse(xml), "bcpg:rclSourcesV2");

		assertEquals(1, sources.getElementsByTagName(RemoteEntityService.ELEM_LIST_VALUE).getLength());
		assertEquals(readableSource.toString(), firstElement(sources, "bcpg:finishedProduct").getAttribute(RemoteEntityService.ATTR_NODEREF));
		assertNothingDisclosed(xml);
	}

	@Test
	public void leavesUnreadableReferenceOutOfSingleValuedProperty() throws Exception {
		givenProductProperty(PROP_SOURCE, hiddenSource);

		String xml = export(new XmlEntityVisitor(registry), RemoteEntityFormat.xml);
		Element source = firstElement(parse(xml), "bcpg:rclSource");

		assertEquals(0, source.getChildNodes().getLength());
		assertNothingDisclosed(xml);
	}

	@Test
	public void excelLeavesUnreadableReferenceOutOfMultiValuedProperty() throws Exception {
		givenProductProperty(PROP_SOURCES, new ArrayList<>(List.of(readableSource, hiddenSource)));
		when(entityDictionaryService.getType(TYPE_FINISHED_PRODUCT)).thenReturn(mock(TypeDefinition.class));

		String xml = export(new ExcelXmlEntityVisitor(registry), RemoteEntityFormat.xml_excel);
		Element sources = firstElement(parse(xml), "bcpg_rclSourcesV2");

		assertEquals(1, sources.getElementsByTagName(RemoteEntityService.ELEM_LIST_VALUE).getLength());
		assertEquals(READABLE_NAME, firstElement(sources, "bcpg_finishedProduct").getAttribute(RemoteEntityService.ATTR_NAME));
		assertNothingDisclosed(xml);
	}

	@Test
	public void lightExportWritesOwnHeaderWhenPivotIsNotReadable() throws Exception {
		NodeRef listItem = new NodeRef("workspace://SpacesStore/listItem");
		when(nodeService.getType(listItem)).thenReturn(TYPE_REQ_CTRL_LIST);
		when(nodeService.getProperty(listItem, ContentModel.PROP_NAME)).thenReturn("Requirement");
		when(entityDictionaryService.isSubClass(TYPE_REQ_CTRL_LIST, BeCPGModel.TYPE_ENTITYLIST_ITEM)).thenReturn(true);
		when(entityDictionaryService.getDefaultPivotAssoc(TYPE_REQ_CTRL_LIST)).thenReturn(ASSOC_REQ_CTRL_ID);
		when(associationService.getTargetAssoc(listItem, ASSOC_REQ_CTRL_ID)).thenReturn(hiddenSource);

		String xml = export(new XmlEntityVisitor(registry), RemoteEntityFormat.xml_light, listItem);
		Element root = parse(xml);

		assertEquals(listItem.toString(), root.getAttribute(RemoteEntityService.ATTR_NODEREF));
		assertNull(root.getAttributeNode(RemoteEntityService.CHARACT_ATTR_NODEREF));
		assertNothingDisclosed(xml);
	}

	@Test
	public void allExportDetailsDirectAssociationTarget() throws Exception {
		givenAssociations(product, component);

		Element root = parse(export(new XmlEntityVisitor(registry), RemoteEntityFormat.xml_all));

		assertTrue(hasChildElement(nodeElements(root, component).get(0)));
	}

	@Test
	public void allExportWritesTargetOfTargetAsHeaderOnly() throws Exception {
		givenAssociations(product, component);
		givenAssociations(component, subComponent);

		Element root = parse(export(new XmlEntityVisitor(registry), RemoteEntityFormat.xml_all));
		Element subComponentElement = nodeElements(root, subComponent).get(0);

		assertFalse(hasChildElement(subComponentElement));
		assertEquals(subComponent.toString(), subComponentElement.getAttribute(RemoteEntityService.ATTR_NODEREF));
	}

	@Test
	public void allExportDetailsDirectTargetFirstMetBeyondBound() throws Exception {
		givenAssociations(product, component, subComponent);
		givenAssociations(component, subComponent);

		List<Element> subComponentElements = nodeElements(parse(export(new XmlEntityVisitor(registry), RemoteEntityFormat.xml_all)), subComponent);

		assertEquals(2, subComponentElements.size());
		assertFalse(hasChildElement(subComponentElements.get(0)));
		assertTrue(hasChildElement(subComponentElements.get(1)));
	}

	@Test
	public void allExportWritesReferencedNodeAsHeaderOnly() throws Exception {
		givenAssociations(product);
		givenProductProperty(PROP_SOURCES, new ArrayList<>(List.of(readableSource)));

		Element sourceElement = nodeElements(parse(export(new XmlEntityVisitor(registry), RemoteEntityFormat.xml_all)), readableSource).get(0);

		assertFalse(hasChildElement(sourceElement));
		assertEquals(READABLE_NAME, sourceElement.getAttribute(RemoteEntityService.ATTR_NAME));
	}

	@Test
	public void allExportCompletesOnLongAssociationChain() throws Exception {
		NodeRef previous = product;
		for (int i = 0; i < LONG_CHAIN_LENGTH; i++) {
			NodeRef next = new NodeRef("workspace://SpacesStore/chain" + i);
			givenAssociations(previous, next);
			previous = next;
		}

		String xml = exportOnSmallStack(RemoteEntityFormat.xml_all);

		assertEquals(2, parse(xml).getElementsByTagName(ELEM_FINISHED_PRODUCT).getLength());
	}

	private void givenAssociations(NodeRef source, NodeRef... targets) {
		AssociationDefinition compoProduct = mock(AssociationDefinition.class);
		when(compoProduct.getName()).thenReturn(ASSOC_COMPO_PRODUCT);
		TypeDefinition productType = mock(TypeDefinition.class);
		when(productType.getAssociations()).thenReturn(Map.of(ASSOC_COMPO_PRODUCT, compoProduct));
		when(entityDictionaryService.getType(TYPE_FINISHED_PRODUCT)).thenReturn(productType);

		List<AssociationRef> assocRefs = new ArrayList<>();
		for (NodeRef target : targets) {
			when(nodeService.getType(target)).thenReturn(TYPE_FINISHED_PRODUCT);
			assocRefs.add(new AssociationRef(source, ASSOC_COMPO_PRODUCT, target));
		}
		when(nodeService.getTargetAssocs(source, ASSOC_COMPO_PRODUCT)).thenReturn(assocRefs);
	}

	private String exportOnSmallStack(RemoteEntityFormat format) throws Exception {
		AtomicReference<String> xml = new AtomicReference<>();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread exporter = new Thread(null, () -> {
			try {
				xml.set(export(new XmlEntityVisitor(registry), format));
			} catch (Throwable e) {
				failure.set(e);
			}
		}, "xml-export", SMALL_STACK_SIZE);
		exporter.start();
		exporter.join();
		assertNull(failure.get());
		return xml.get();
	}

	private static List<Element> nodeElements(Element root, NodeRef nodeRef) {
		List<Element> elements = new ArrayList<>();
		NodeList candidates = root.getElementsByTagName(ELEM_FINISHED_PRODUCT);
		for (int i = 0; i < candidates.getLength(); i++) {
			Element candidate = (Element) candidates.item(i);
			if (nodeRef.toString().equals(candidate.getAttribute(RemoteEntityService.ATTR_NODEREF))) {
				elements.add(candidate);
			}
		}
		return elements;
	}

	private static boolean hasChildElement(Element element) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) {
				return true;
			}
		}
		return false;
	}

	private void givenProductProperty(QName propName, Serializable value) {
		when(nodeService.getProperties(product)).thenReturn(Map.of(propName, value));
	}

	private void assertNothingDisclosed(String xml) {
		assertFalse(xml.contains(hiddenSource.getId()));
		assertFalse(xml.contains(HIDDEN_NAME));
		assertFalse(xml.contains(HIDDEN_CODE));
	}

	private static Element firstElement(Element parent, String tagName) {
		NodeList elements = parent.getElementsByTagName(tagName);
		assertEquals(1, elements.getLength());
		return (Element) elements.item(0);
	}

	private String export(AbstractEntityVisitor visitor, RemoteEntityFormat format) throws Exception {
		return export(visitor, format, product);
	}

	private String export(AbstractEntityVisitor visitor, RemoteEntityFormat format, NodeRef nodeRef) throws Exception {
		RemoteParams params = new RemoteParams(format);
		params.setEnforceFieldSecurity(true);
		visitor.setParams(params);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		visitor.visit(nodeRef, out);
		return out.toString(StandardCharsets.UTF_8);
	}

	private static Element parse(String xml) throws Exception {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
				.getDocumentElement();
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
