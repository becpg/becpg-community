package fr.becpg.repo.autocomplete.impl.plugins;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AttributeExtractorService;

/**
 * Unit tests of the ParentValue suggestions on a property holding the codes of a list of values.
 */
public class DataListItemAutoCompletePluginTest {

	private static final String SOURCE_TYPE_PARENT_VALUE = "ParentValue";

	private static final String COMPONENT_LIST_TYPE = "bcpg:componentList";

	private static final String COMPONENT_PROPERTY = "bcpg:component";

	private static final QName COMPONENT_LIST_QNAME = QName.createQName(BeCPGModel.BECPG_URI, "componentList");

	private static final QName COMPONENT_QNAME = QName.createQName(BeCPGModel.BECPG_URI, "component");

	private static final NodeRef ENTITY = new NodeRef("workspace://SpacesStore/entity");

	private static final NodeRef LISTS_CONTAINER = new NodeRef("workspace://SpacesStore/container");

	private static final NodeRef COMPONENT_LIST = new NodeRef("workspace://SpacesStore/list");

	private static final NodeRef BOTTLE = new NodeRef("workspace://SpacesStore/bottle");

	private static final NodeRef PUMP = new NodeRef("workspace://SpacesStore/pump");

	private static final NodeRef CAP = new NodeRef("workspace://SpacesStore/cap");

	@Mock
	private NamespaceService namespaceService;

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private NodeService nodeService;

	@Mock
	private EntityListDAO entityListDAO;

	@Mock
	private AttributeExtractorService attributeExtractorService;

	@InjectMocks
	private DataListItemAutoCompletePlugin plugin;

	private AutoCloseable mocks;

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		when(namespaceService.getNamespaceURI(BeCPGModel.BECPG_PREFIX)).thenReturn(BeCPGModel.BECPG_URI);
		when(entityListDAO.getListContainer(ENTITY)).thenReturn(LISTS_CONTAINER);
		when(entityListDAO.getList(LISTS_CONTAINER, COMPONENT_LIST_QNAME)).thenReturn(COMPONENT_LIST);
		when(entityListDAO.getListItems(COMPONENT_LIST, COMPONENT_LIST_QNAME)).thenReturn(List.of(BOTTLE, PUMP, CAP));
		mockListOfValuesProperty();
		mockComponent(BOTTLE, "Bottle", "Flacon");
		mockComponent(PUMP, "Pump", "Pompe");
		mockComponent(CAP, "Cap", "Bouchon");
		when(nodeService.getProperty(PUMP, BeCPGModel.PROP_PARENT_LEVEL)).thenReturn(BOTTLE);
	}

	@After
	public void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	public void testMatchesTranslatedLabelRatherThanCode() {
		assertEquals(List.of("Flacon"), suggestedNames("Fla", null));
	}

	@Test
	public void testDisplaysTranslatedLabels() {
		assertEquals(List.of("Flacon", "Pompe", "Bouchon"), suggestedNames("", null));
	}

	@Test
	public void testMatchIgnoresCase() {
		assertEquals(List.of("Bouchon"), suggestedNames("bou", null));
	}

	@Test
	public void testExcludesEditedItemAndItsDescendants() {
		assertEquals(List.of("Bouchon"), suggestedNames("", BOTTLE));
	}

	@Test
	public void testSuggestsListItemsNotValueCodes() {
		AutoCompletePage page = plugin.suggest(SOURCE_TYPE_PARENT_VALUE, "Bou", 1, 10, props(null));

		assertEquals(CAP.toString(), page.getResults().get(0).getValue());
	}

	private List<String> suggestedNames(String query, NodeRef itemId) {
		AutoCompletePage page = plugin.suggest(SOURCE_TYPE_PARENT_VALUE, query, 1, 10, props(itemId));
		List<String> names = new ArrayList<>();
		for (AutoCompleteEntry entry : page.getResults()) {
			names.add(entry.getName());
		}
		return names;
	}

	private Map<String, Serializable> props(NodeRef itemId) {
		Map<String, Serializable> props = new HashMap<>();
		props.put(AutoCompleteService.PROP_ENTITYNODEREF, ENTITY.toString());
		props.put(AutoCompleteService.PROP_CLASS_NAME, COMPONENT_LIST_TYPE);
		props.put(AutoCompleteService.PROP_ATTRIBUTE_NAME, COMPONENT_PROPERTY);
		if (itemId != null) {
			HashMap<String, String> extras = new HashMap<>();
			extras.put(AutoCompleteService.EXTRA_PARAM_ITEMID, itemId.toString());
			props.put(AutoCompleteService.EXTRA_PARAM, extras);
		}
		return props;
	}

	private void mockListOfValuesProperty() {
		ConstraintDefinition constraintDef = mock(ConstraintDefinition.class);
		when(constraintDef.getConstraint()).thenReturn(new ListOfValuesConstraint());
		PropertyDefinition propertyDef = mock(PropertyDefinition.class);
		when(propertyDef.getName()).thenReturn(COMPONENT_QNAME);
		when(propertyDef.getConstraints()).thenReturn(List.of(constraintDef));
		when(dictionaryService.getProperty(COMPONENT_QNAME)).thenReturn(propertyDef);
	}

	private void mockComponent(NodeRef item, String code, String label) {
		when(nodeService.getProperty(item, COMPONENT_QNAME)).thenReturn(code);
		when(attributeExtractorService.getStringValue(any(PropertyDefinition.class), eq(code), any())).thenReturn(label);
	}
}
