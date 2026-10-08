package fr.becpg.repo.dictionary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.opencmis.dictionary.CMISDictionaryRegistry;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.tenant.TenantService;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEvent;

/**
 * Unit tests for {@link LazyCMISDictionaryService}.
 */
public class LazyCMISDictionaryServiceTest {

	private static final String TENANT = "acme";
	private static final String REGISTRY_KEY = "key.openCmisDictionaryRegistry." + TENANT + "." + CmisVersion.CMIS_1_1;

	private final DictionaryDAO dictionaryDAO = mock(DictionaryDAO.class);
	@SuppressWarnings("unchecked")
	private final SimpleCache<String, CMISDictionaryRegistry> registryCache = mock(SimpleCache.class);
	private final LazyCMISDictionaryService service = new LazyCMISDictionaryService();

	@Before
	public void setUp() {
		CMISMapping cmisMapping = mock(CMISMapping.class);
		when(cmisMapping.getCmisVersion()).thenReturn(CmisVersion.CMIS_1_1);
		TenantService tenantService = mock(TenantService.class);
		when(tenantService.getCurrentUserDomain()).thenReturn(TENANT);

		service.setDictionaryDAO(dictionaryDAO);
		service.setSingletonCache(registryCache);
		service.setCmisMapping(cmisMapping);
		service.setTenantService(tenantService);
	}

	@Test
	public void listensToDictionaryChangesAtBootstrap() {
		service.onBootstrap(mock(ApplicationEvent.class));

		verify(dictionaryDAO).registerListener(service);
	}

	@Test
	public void doesNotBuildTheRegistryAtBootstrap() {
		service.onBootstrap(mock(ApplicationEvent.class));

		verify(registryCache, never()).put(anyString(), any());
	}

	@Test
	public void dropsTheRegistryWhenTheDictionaryReloads() {
		service.afterDictionaryInit();

		verify(registryCache).remove(REGISTRY_KEY);
		verify(registryCache, never()).put(anyString(), any());
	}
}
