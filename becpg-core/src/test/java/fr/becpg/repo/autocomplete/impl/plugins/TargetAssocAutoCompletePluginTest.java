package fr.becpg.repo.autocomplete.impl.plugins;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * Unit tests of the search template a target type gets from the system configuration.
 */
public class TargetAssocAutoCompletePluginTest {

	private static final QName GEO_ORIGIN_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "geoOrigin");

	private static final String GEO_ORIGIN_CONF_KEY = "beCPG.autocomplete.searchTemplate.bcpg_geoOrigin";

	private static final String GEO_ORIGIN_TEMPLATE = "%(bcpg:charactName bcpg:geoOriginISOCode)";

	private static final String DEFAULT_TEMPLATE = "%(bcpg:charactName bcpg:erpCode bcpg:legalName)";

	@Mock
	private NamespaceService namespaceService;

	@Mock
	private SystemConfigurationService systemConfigurationService;

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
}
