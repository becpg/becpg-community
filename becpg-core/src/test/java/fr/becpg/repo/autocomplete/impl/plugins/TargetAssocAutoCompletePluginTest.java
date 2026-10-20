package fr.becpg.repo.autocomplete.impl.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
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
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * Unit tests of the search template a target type gets from the system configuration, and of the relevance order of
 * the suggestions (#37670).
 */
public class TargetAssocAutoCompletePluginTest {

	private static final QName GEO_ORIGIN_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "geoOrigin");

	private static final String GEO_ORIGIN_CONF_KEY = "beCPG.autocomplete.searchTemplate.bcpg_geoOrigin";

	private static final String GEO_ORIGIN_TEMPLATE = "%(bcpg:charactName bcpg:geoOriginISOCode)";

	private static final String DEFAULT_TEMPLATE = "%(bcpg:charactName bcpg:erpCode bcpg:legalName)";

	@Mock
	private NamespaceService namespaceService;

	private static final QName INGREDIENT_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "ing");

	private static final NodeRef TAP_WATER = new NodeRef("workspace://SpacesStore/tap-water");

	private static final NodeRef WATER = new NodeRef("workspace://SpacesStore/water");

	@Mock
	private SystemConfigurationService systemConfigurationService;

	@Mock
	private NodeService nodeService;

	@Mock
	private EntityDictionaryService entityDictionaryService;

	@InjectMocks
	private TargetAssocAutoCompletePlugin plugin;

	private AutoCloseable mocks;

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		when(namespaceService.getPrefixes(BeCPGModel.BECPG_URI)).thenReturn(List.of(BeCPGModel.BECPG_PREFIX));
	}

	@After
	public void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	public void testConfiguredTemplateReplacesDefault() {
		when(systemConfigurationService.confValue(GEO_ORIGIN_CONF_KEY)).thenReturn(GEO_ORIGIN_TEMPLATE);

		assertEquals(GEO_ORIGIN_TEMPLATE, plugin.resolveTypeSearchTemplate(GEO_ORIGIN_TYPE, DEFAULT_TEMPLATE));
	}

	@Test
	public void testDefaultTemplateKeptWithoutConfiguration() {
		assertEquals(DEFAULT_TEMPLATE, plugin.resolveTypeSearchTemplate(GEO_ORIGIN_TYPE, DEFAULT_TEMPLATE));
	}

	@Test
	public void testDefaultTemplateKeptWhenConfigurationIsBlank() {
		when(systemConfigurationService.confValue(GEO_ORIGIN_CONF_KEY)).thenReturn("  ");

		assertEquals(DEFAULT_TEMPLATE, plugin.resolveTypeSearchTemplate(GEO_ORIGIN_TYPE, DEFAULT_TEMPLATE));
	}

	@Test
	public void testCharacteristicsRankedOnTheirNameNotOnTheirNodeName() {
		when(entityDictionaryService.isSubClass(INGREDIENT_TYPE, BeCPGModel.TYPE_CHARACT)).thenReturn(true);
		when(nodeService.getProperty(TAP_WATER, BeCPGModel.PROP_CHARACT_NAME)).thenReturn("Eau du r\u00e9seau");
		when(nodeService.getProperty(WATER, BeCPGModel.PROP_CHARACT_NAME)).thenReturn("Eau");
		when(nodeService.getProperty(WATER, ContentModel.PROP_NAME)).thenReturn("Eau du r\u00e9seau");

		assertEquals(List.of(WATER, TAP_WATER), sortByRelevance(INGREDIENT_TYPE, List.of(TAP_WATER, WATER)));
	}

	@Test
	public void testCharacteristicsReadTheirNameOnly() {
		when(entityDictionaryService.isSubClass(INGREDIENT_TYPE, BeCPGModel.TYPE_CHARACT)).thenReturn(true);

		assertEquals(List.of(BeCPGModel.PROP_CHARACT_NAME), plugin.resolveLabelProperties(INGREDIENT_TYPE));
	}

	@Test
	public void testNodeNameRanksNodesWithoutCharacteristicName() {
		when(nodeService.getProperty(TAP_WATER, ContentModel.PROP_NAME)).thenReturn("Eau du r\u00e9seau");
		when(nodeService.getProperty(WATER, ContentModel.PROP_NAME)).thenReturn("Eau");

		assertEquals(List.of(WATER, TAP_WATER), sortByRelevance(BeCPGModel.TYPE_ENTITY_V2, List.of(TAP_WATER, WATER)));
	}

	@Test
	public void testMultilingualNameRankedInContentLocale() {
		when(entityDictionaryService.isSubClass(BeCPGModel.TYPE_LIST_VALUE, BeCPGModel.TYPE_LIST_VALUE)).thenReturn(true);
		Locale previousLocale = I18NUtil.getContentLocale();
		I18NUtil.setContentLocale(Locale.FRENCH);
		try {
			when(nodeService.getProperty(TAP_WATER, BeCPGModel.PROP_LV_VALUE)).thenReturn(new MLText(Locale.FRENCH, "Eau du r\u00e9seau"));
			when(nodeService.getProperty(WATER, BeCPGModel.PROP_LV_VALUE)).thenReturn(new MLText(Locale.FRENCH, "Eau"));

			assertEquals(List.of(WATER, TAP_WATER), sortByRelevance(BeCPGModel.TYPE_LIST_VALUE, List.of(TAP_WATER, WATER)));
		} finally {
			I18NUtil.setContentLocale(previousLocale);
		}
	}

	@Test
	public void testPeopleAreNotRanked() {
		when(entityDictionaryService.isSubClass(ContentModel.TYPE_PERSON, ContentModel.TYPE_PERSON)).thenReturn(true);

		assertTrue(plugin.resolveLabelProperties(ContentModel.TYPE_PERSON).isEmpty());
	}

	private List<NodeRef> sortByRelevance(QName type, List<NodeRef> suggestions) {
		return plugin.sortByRelevance("eau", plugin.resolveLabelProperties(type), suggestions);
	}
}
