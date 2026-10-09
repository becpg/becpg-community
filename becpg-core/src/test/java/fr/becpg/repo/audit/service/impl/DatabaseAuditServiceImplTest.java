package fr.becpg.repo.audit.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.model.AuditEntry;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditPage;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.plugin.ExtraQueryDatabaseAuditPlugin;

/**
 * Unit tests of the in-memory filters of {@link DatabaseAuditServiceImpl}, against an audit
 * database faked in memory that honours the identifier range and the key-value pair of the query.
 */
public class DatabaseAuditServiceImplTest {

	private static final String APPLICATION_ID = "beCPGExportSearchAudit";

	private static final String APPLICATION_PATH = "exportSearch";

	private static final String TEMPLATE = "template";

	private static final String USERNAME = "username";

	private static final String TEMPLATE_A = "workspace://SpacesStore/template-a";

	private static final String TEMPLATE_B = "workspace://SpacesStore/template-b";

	private static final String JALAL = "jalal";

	private static final String MATTHIEU = "matthieu";

	private static final String VALUES_KEY = "valuesKey";

	private static final String VALUES_VALUE = "valuesValue";

	private static final String ID_COLUMN = "id";

	private final List<AuditEntry> database = new ArrayList<>();

	@Mock
	private Audit audit;

	@Mock
	private AuditComponent auditComponent;

	@InjectMocks
	private DatabaseAuditServiceImpl databaseAuditService;

	private AutoCloseable mocks;

	private final DatabaseAuditPlugin plugin = mock(DatabaseAuditPlugin.class);

	private final ExtraQueryDatabaseAuditPlugin extraQueryPlugin = mock(ExtraQueryDatabaseAuditPlugin.class);

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		databaseAuditService.setMaxScannedWindows(8);
		stubPlugin(plugin);
		stubPlugin(extraQueryPlugin);
		when(extraQueryPlugin.extraQuery(any())).thenAnswer(invocation -> ((AuditQuery) invocation.getArgument(0)).filter(TEMPLATE + "=" + TEMPLATE_B));
		when(auditComponent.getAuditMinMaxByApp(eq(APPLICATION_ID), anyList())).thenAnswer(invocation -> idBounds());
		when(audit.listAuditEntries(eq(APPLICATION_ID), any())).thenAnswer(invocation -> queryDatabase(invocation.getArgument(1)));
	}

	@After
	public void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	public void pageKeepsEntriesMatchingEveryFilter() {
		record(1, TEMPLATE_A, JALAL);
		record(2, TEMPLATE_A, MATTHIEU);
		record(3, TEMPLATE_B, JALAL);

		AuditPage page = databaseAuditService.listAuditPage(plugin, templateAndUserQuery(10));

		Assert.assertEquals(List.of(1L), ids(page.entries()));
	}

	@Test
	public void pageReadsEntriesLeftBehindWindowsCutShort() {
		for (long id = 1; id <= 12; id++) {
			record(id, TEMPLATE_A, (id == 5) || (id == 9) ? JALAL : MATTHIEU);
		}

		AuditPage page = databaseAuditService.listAuditPage(plugin, templateAndUserQuery(2));

		Assert.assertEquals(List.of(5L, 9L), ids(page.entries()));
	}

	@Test
	public void pageResumesAfterLastEntryRead() {
		for (long id = 1; id <= 12; id++) {
			record(id, TEMPLATE_A, (id == 5) || (id == 9) || (id == 11) ? JALAL : MATTHIEU);
		}

		AuditPage firstPage = databaseAuditService.listAuditPage(plugin, templateAndUserQuery(2));
		AuditPage nextPage = databaseAuditService.listAuditPage(plugin, templateAndUserQuery(2).startAfterId(firstPage.nextStartAfterId()));

		Assert.assertEquals(List.of(11L), ids(nextPage.entries()));
	}

	@Test
	public void descendingPageReadsEntriesLeftBehindWindowsCutShort() {
		for (long id = 1; id <= 12; id++) {
			record(id, TEMPLATE_A, (id == 5) || (id == 9) ? JALAL : MATTHIEU);
		}

		AuditPage page = databaseAuditService.listAuditPage(plugin, templateAndUserQuery(2).dbAsc(false));

		Assert.assertEquals(List.of(9L, 5L), ids(page.entries()));
	}

	@Test
	public void descendingPageResumesAfterLastEntryRead() {
		for (long id = 1; id <= 12; id++) {
			record(id, TEMPLATE_A, (id == 2) || (id == 5) || (id == 9) ? JALAL : MATTHIEU);
		}

		AuditPage firstPage = databaseAuditService.listAuditPage(plugin, templateAndUserQuery(2).dbAsc(false));
		AuditPage nextPage = databaseAuditService.listAuditPage(plugin,
				templateAndUserQuery(2).dbAsc(false).startAfterId(firstPage.nextStartAfterId()));

		Assert.assertEquals(List.of(2L), ids(nextPage.entries()));
	}

	@Test
	public void extraQueryEntriesBeyondWindowCutShortAreReadOnce() {
		for (long id = 1; id <= 4; id++) {
			record(id, TEMPLATE_A, id == 4 ? JALAL : MATTHIEU);
		}
		record(5, TEMPLATE_B, JALAL);
		record(6, TEMPLATE_B, JALAL);

		AuditPage firstPage = databaseAuditService.listAuditPage(extraQueryPlugin, templateAndUserQuery(2));
		AuditPage nextPage = databaseAuditService.listAuditPage(extraQueryPlugin, templateAndUserQuery(2).startAfterId(firstPage.nextStartAfterId()));

		Assert.assertEquals(List.of(4L, 5L), ids(firstPage.entries()));
		Assert.assertEquals(List.of(6L), ids(nextPage.entries()));
	}

	@Test
	public void listKeepsEntriesMatchingEveryFilter() {
		record(1, TEMPLATE_A, JALAL);
		record(2, TEMPLATE_A, MATTHIEU);

		List<JSONObject> entries = databaseAuditService.listAuditEntries(plugin, templateAndUserQuery(10));

		Assert.assertEquals(List.of(1L), ids(entries));
	}

	@Test(expected = BeCPGAuditException.class)
	public void rejectsUnknownInMemoryFilterKey() {
		record(1, TEMPLATE_A, JALAL);

		databaseAuditService.listAuditPage(plugin, AuditQuery.createQuery().filters(List.of(TEMPLATE + "=" + TEMPLATE_A, "unknown=value")));
	}

	private static void stubPlugin(DatabaseAuditPlugin auditPlugin) {
		when(auditPlugin.getAuditApplicationId()).thenReturn(APPLICATION_ID);
		when(auditPlugin.getAuditApplicationPath()).thenReturn(APPLICATION_PATH);
		when(auditPlugin.getKeyMap()).thenReturn(Map.of(TEMPLATE, AuditDataType.STRING, USERNAME, AuditDataType.STRING));
	}

	private AuditQuery templateAndUserQuery(int maxResults) {
		return AuditQuery.createQuery().filters(List.of(TEMPLATE + "=" + TEMPLATE_A, USERNAME + "=" + JALAL)).maxResults(maxResults);
	}

	private void record(long id, String template, String username) {
		Map<String, Serializable> values = new HashMap<>();
		values.put(valuePath(AuditPlugin.ID), id);
		values.put(valuePath(TEMPLATE), template);
		values.put(valuePath(USERNAME), username);
		database.add(new AuditEntry(id, APPLICATION_ID, null, null, values));
	}

	private static String valuePath(String auditKey) {
		return "/" + APPLICATION_ID + "/" + APPLICATION_PATH + "/" + auditKey + "/value";
	}

	private Map<String, Long> idBounds() {
		Map<String, Long> bounds = new HashMap<>();
		bounds.put("min", database.stream().map(AuditEntry::getId).min(Long::compare).orElse(null));
		bounds.put("max", database.stream().map(AuditEntry::getId).max(Long::compare).orElse(null));
		return bounds;
	}

	private CollectionWithPagingInfo<AuditEntry> queryDatabase(Parameters params) {
		WhereClause where = new WhereClause();
		QueryHelper.walk(params.getQuery(), where);

		List<AuditEntry> matching = new ArrayList<>();
		for (AuditEntry entry : database) {
			if (where.matches(entry)) {
				matching.add(entry);
			}
		}

		Comparator<AuditEntry> byId = Comparator.comparing(AuditEntry::getId);
		matching.sort(params.getSorting().get(0).asc ? byId : byId.reversed());
		List<AuditEntry> page = matching.subList(0, Math.min(matching.size(), params.getPaging().getMaxItems()));
		return CollectionWithPagingInfo.asPaged(params.getPaging(), page);
	}

	private static List<Long> ids(List<JSONObject> entries) {
		List<Long> ids = new ArrayList<>();
		for (JSONObject entry : entries) {
			ids.add(entry.getLong(ID_COLUMN));
		}
		return ids;
	}

	/**
	 * The part of the audit 'where' clause the fake database honours.
	 */
	private static class WhereClause extends QueryHelper.WalkerCallbackAdapter {

		private final Map<String, String> equalities = new HashMap<>();

		private Long fromId;

		private Long toId;

		@Override
		public void comparison(int type, String propertyName, String propertyValue, boolean negated) {
			equalities.put(propertyName, propertyValue);
		}

		@Override
		public void between(String propertyName, String firstValue, String secondValue, boolean negated) {
			if (ID_COLUMN.equals(propertyName)) {
				fromId = Long.valueOf(firstValue);
				toId = Long.valueOf(secondValue);
			}
		}

		@Override
		public void and() {
			// conjunctions only, as the audit query of Alfresco
		}

		private boolean matches(AuditEntry entry) {
			boolean inRange = (fromId == null) || ((entry.getId() >= fromId) && (entry.getId() <= toId));
			String valuesKey = equalities.get(VALUES_KEY);
			if (!inRange || (valuesKey == null)) {
				return inRange;
			}
			return Objects.equals(String.valueOf(entry.getValues().get(valuesKey)), equalities.get(VALUES_VALUE));
		}
	}
}
