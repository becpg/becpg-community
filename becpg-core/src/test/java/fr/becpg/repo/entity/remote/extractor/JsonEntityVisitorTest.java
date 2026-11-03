package fr.becpg.repo.entity.remote.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.security.SecurityService;

/**
 * Unit tests of the <code>d:noderef</code> values of the JSON remote export (#37820): a referenced
 * node the user cannot read is reported as <code>#AccessDenied</code>, without its name or codes.
 */
public class JsonEntityVisitorTest {

	private static final QName TYPE_FINISHED_PRODUCT = QName.createQName(BeCPGModel.BECPG_URI, "finishedProduct");

	private static final QName PROP_SOURCES = QName.createQName(BeCPGModel.BECPG_URI, "rclSourcesV2");

	private static final QName PROP_SOURCE = QName.createQName(BeCPGModel.BECPG_URI, "rclSource");

	private static final String ACCESS_DENIED = "#AccessDenied";

	private static final String READABLE_NAME = "Readable source";

	private static final String HIDDEN_NAME = "Hidden source";

	private static final String HIDDEN_CODE = "PF-HIDDEN";

	private final NodeRef product = new NodeRef("workspace://SpacesStore/product");

	private final NodeRef readableSource = new NodeRef("workspace://SpacesStore/readableSource");

	private final NodeRef hiddenSource = new NodeRef("workspace://SpacesStore/hiddenSource");

	private final NodeService nodeService = mock(NodeService.class);

	private final EntityDictionaryService entityDictionaryService = mock(EntityDictionaryService.class);

	private final PermissionService permissionService = mock(PermissionService.class);

	private JsonEntityVisitor visitor;

	@Before
	public void setUp() {
		mockSource(readableSource, READABLE_NAME, "PF-READABLE", AccessStatus.ALLOWED);
		mockSource(hiddenSource, HIDDEN_NAME, HIDDEN_CODE, AccessStatus.DENIED);
		mockNodeRefProperty(PROP_SOURCES);
		mockNodeRefProperty(PROP_SOURCE);
		when(entityDictionaryService.toPrefixString(any(QName.class))).thenAnswer(invocation -> prefixed(invocation.getArgument(0)));

		RemoteServiceRegisty registry = mock(RemoteServiceRegisty.class);
		when(registry.nodeService()).thenReturn(nodeService);
		when(registry.unsecuredNodeService()).thenReturn(nodeService);
		when(registry.mlNodeService()).thenReturn(nodeService);
		NamespaceService namespaceService = mock(NamespaceService.class);
		when(namespaceService.getPrefixes(BeCPGModel.BECPG_URI)).thenReturn(List.of(BeCPGModel.BECPG_PREFIX));
		when(namespaceService.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI)).thenReturn(List.of(NamespaceService.CONTENT_MODEL_PREFIX));
		when(registry.namespaceService()).thenReturn(namespaceService);
		when(registry.entityDictionaryService()).thenReturn(entityDictionaryService);
		when(registry.securityService()).thenReturn(mock(SecurityService.class));
		when(registry.permissionService()).thenReturn(permissionService);

		visitor = new JsonEntityVisitor(registry);
		visitor.setParams(new RemoteParams(RemoteEntityFormat.json));
	}

	@Test
	public void marksUnreadableReferenceOfMultiValuedPropertyAsAccessDenied() throws Exception {
		JSONObject attributes = exportProperty(PROP_SOURCES, new ArrayList<>(List.of(readableSource, hiddenSource)));
		JSONArray sources = attributes.getJSONArray(prefixed(PROP_SOURCES));

		assertEquals(READABLE_NAME, sources.getJSONObject(0).getString(prefixed(ContentModel.PROP_NAME)));
		assertAccessDenied(sources.getJSONObject(1));
		assertNothingDisclosed(attributes);
	}

	@Test
	public void marksUnreadableReferenceOfSingleValuedPropertyAsAccessDenied() throws Exception {
		JSONObject attributes = exportProperty(PROP_SOURCE, hiddenSource);

		assertAccessDenied(attributes.getJSONObject(prefixed(PROP_SOURCE)));
		assertNothingDisclosed(attributes);
	}

	private JSONObject exportProperty(QName propName, Serializable value) throws Exception {
		JSONObject attributes = new JSONObject();
		visitor.visitProps(product, attributes, null, Map.of(propName, value), new RemoteJSONContext(product));
		return attributes;
	}

	private void assertAccessDenied(JSONObject source) throws Exception {
		assertEquals(hiddenSource.getId(), source.getString(RemoteEntityService.ATTR_ID));
		assertEquals(ACCESS_DENIED, source.getString(prefixed(ContentModel.PROP_NAME)));
		assertEquals(2, source.length());
	}

	private void assertNothingDisclosed(JSONObject attributes) {
		String json = attributes.toString();
		assertFalse(json.contains(HIDDEN_NAME));
		assertFalse(json.contains(HIDDEN_CODE));
	}

	private void mockSource(NodeRef source, String name, String code, AccessStatus readAccess) {
		when(nodeService.exists(source)).thenReturn(true);
		when(nodeService.getType(source)).thenReturn(TYPE_FINISHED_PRODUCT);
		when(nodeService.getProperties(source)).thenReturn(Map.of(ContentModel.PROP_NAME, name, BeCPGModel.PROP_CODE, code));
		when(permissionService.hasPermission(source, PermissionService.READ)).thenReturn(readAccess);
	}

	private void mockNodeRefProperty(QName propName) {
		DataTypeDefinition nodeRefType = mock(DataTypeDefinition.class);
		when(nodeRefType.getName()).thenReturn(DataTypeDefinition.NODE_REF);
		PropertyDefinition definition = mock(PropertyDefinition.class);
		when(definition.getDataType()).thenReturn(nodeRefType);
		when(definition.getName()).thenReturn(propName);
		when(entityDictionaryService.getProperty(propName)).thenReturn(definition);
	}

	private static String prefixed(QName qName) {
		return qName.getLocalName();
	}
}
