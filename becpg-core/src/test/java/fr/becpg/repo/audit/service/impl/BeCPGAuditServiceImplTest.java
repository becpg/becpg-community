package fr.becpg.repo.audit.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditPage;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.service.DatabaseAuditService;

/**
 * Unit tests of the ordering {@link BeCPGAuditServiceImpl} applies to the filters of a query, on
 * the filter hierarchy of the audit plugin.
 */
public class BeCPGAuditServiceImplTest {

	private static final String DOWNLOAD_FILTER = "downloadNodeRef=workspace://SpacesStore/download";

	private static final String USERNAME_FILTER = "username=jalal.moumtez@becpg.fr";

	private static final String TEMPLATE_FILTER = "template=workspace://SpacesStore/template";

	private static final String FILENAME_FILTER = "filename=export.xlsx";

	private static final String ASYNC_FILTER = "async=true";

	private static final String ID_FILTER = AuditPlugin.ID + "=151933";

	private final DatabaseAuditService databaseAuditService = mock(DatabaseAuditService.class);

	private final DatabaseAuditPlugin plugin = mock(DatabaseAuditPlugin.class);

	private final BeCPGAuditServiceImpl beCPGAuditService = new BeCPGAuditServiceImpl();

	@Before
	public void setUp() throws ReflectiveOperationException {
		when(plugin.applyTo(AuditType.EXPORT_SEARCH)).thenReturn(true);
		when(plugin.isDatabaseEnable()).thenReturn(true);
		when(plugin.getFilterHierarchy()).thenReturn(List.of("downloadNodeRef", "username", "template"));
		when(databaseAuditService.listAuditPage(eq(plugin), any())).thenReturn(AuditPage.empty());
		inject("auditPlugins", new AuditPlugin[] { plugin });
		inject("databaseAuditService", databaseAuditService);
	}

	@Test
	public void ordersFiltersOnPluginHierarchy() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(TEMPLATE_FILTER, DOWNLOAD_FILTER, USERNAME_FILTER));

		Assert.assertEquals(List.of(DOWNLOAD_FILTER, USERNAME_FILTER, TEMPLATE_FILTER), pageQuery(query).getFilters());
	}

	@Test
	public void putsIdentifierFirst() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(DOWNLOAD_FILTER, ID_FILTER));

		Assert.assertEquals(ID_FILTER, pageQuery(query).getFilter());
	}

	@Test
	public void placesIdentifierWhereHierarchyListsIt() {
		when(plugin.getFilterHierarchy()).thenReturn(List.of("downloadNodeRef", AuditPlugin.ID));
		AuditQuery query = AuditQuery.createQuery().filters(List.of(ID_FILTER, DOWNLOAD_FILTER));

		Assert.assertEquals(DOWNLOAD_FILTER, pageQuery(query).getFilter());
	}

	@Test
	public void putsKeysMissingFromHierarchyLastInTheirOrder() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(FILENAME_FILTER, ASYNC_FILTER, USERNAME_FILTER));

		Assert.assertEquals(List.of(USERNAME_FILTER, FILENAME_FILTER, ASYNC_FILTER), pageQuery(query).getFilters());
	}

	@Test
	public void leavesCallerQueryUntouched() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(TEMPLATE_FILTER, DOWNLOAD_FILTER));

		pageQuery(query);

		Assert.assertEquals(List.of(TEMPLATE_FILTER, DOWNLOAD_FILTER), query.getFilters());
	}

	@Test
	public void ordersFiltersOfUnpagedList() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(TEMPLATE_FILTER, USERNAME_FILTER));

		beCPGAuditService.listAuditEntries(AuditType.EXPORT_SEARCH, query);

		ArgumentCaptor<AuditQuery> captor = ArgumentCaptor.forClass(AuditQuery.class);
		verify(databaseAuditService).listAuditEntries(eq(plugin), captor.capture());
		Assert.assertEquals(List.of(USERNAME_FILTER, TEMPLATE_FILTER), captor.getValue().getFilters());
	}

	@Test(expected = BeCPGAuditException.class)
	public void rejectsPluginNotRecordingInDatabase() {
		when(plugin.isDatabaseEnable()).thenReturn(false);

		beCPGAuditService.listAuditPage(AuditType.EXPORT_SEARCH, AuditQuery.createQuery());
	}

	private AuditQuery pageQuery(AuditQuery query) {
		beCPGAuditService.listAuditPage(AuditType.EXPORT_SEARCH, query);

		ArgumentCaptor<AuditQuery> captor = ArgumentCaptor.forClass(AuditQuery.class);
		verify(databaseAuditService).listAuditPage(eq(plugin), captor.capture());
		return captor.getValue();
	}

	private void inject(String fieldName, Object value) throws ReflectiveOperationException {
		Field field = BeCPGAuditServiceImpl.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(beCPGAuditService, value);
	}
}
