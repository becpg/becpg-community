package fr.becpg.repo.audit.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests of the filters of {@link AuditQuery}.
 */
public class AuditQueryTest {

	private static final String TEMPLATE_FILTER = "template=workspace://SpacesStore/8529f58b-2615-4d69-a9f5-8b26153d6902";

	private static final String USERNAME_FILTER = "username=jalal.moumtez@becpg.fr";

	private static final String ASYNC_FILTER = "async=true";

	@Test
	public void readsFirstFilterFromDatabase() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(TEMPLATE_FILTER, USERNAME_FILTER, ASYNC_FILTER));

		Assert.assertEquals(TEMPLATE_FILTER, query.getFilter());
	}

	@Test
	public void appliesNextFiltersInMemory() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(TEMPLATE_FILTER, USERNAME_FILTER, ASYNC_FILTER));

		Assert.assertEquals(List.of(USERNAME_FILTER, ASYNC_FILTER), query.getInMemoryFilters());
	}

	@Test
	public void singleFilterHasNoInMemoryFilter() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(TEMPLATE_FILTER));

		Assert.assertEquals(TEMPLATE_FILTER, query.getFilter());
		Assert.assertTrue(query.getInMemoryFilters().isEmpty());
	}

	@Test
	public void emptyFiltersLeaveQueryUnfiltered() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of());

		Assert.assertNull(query.getFilter());
		Assert.assertTrue(query.getInMemoryFilters().isEmpty());
	}

	@Test
	public void listsEveryFilterDatabaseOneFirst() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(TEMPLATE_FILTER, USERNAME_FILTER, ASYNC_FILTER));

		Assert.assertEquals(List.of(TEMPLATE_FILTER, USERNAME_FILTER, ASYNC_FILTER), query.getFilters());
	}

	@Test
	public void unfilteredQueryListsNoFilter() {
		Assert.assertTrue(AuditQuery.createQuery().getFilters().isEmpty());
	}

	@Test
	public void emptyFiltersRemovePreviousFilters() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(TEMPLATE_FILTER, USERNAME_FILTER)).filters(List.of());

		Assert.assertTrue(query.getFilters().isEmpty());
	}

	@Test
	public void copyKeepsInMemoryFiltersWhenDatabaseFilterChanges() {
		AuditQuery query = AuditQuery.createQuery().filters(List.of(TEMPLATE_FILTER, USERNAME_FILTER));

		AuditQuery copy = query.copy().filter(ASYNC_FILTER);

		Assert.assertEquals(List.of(USERNAME_FILTER), copy.getInMemoryFilters());
		Assert.assertEquals(TEMPLATE_FILTER, query.getFilter());
	}
}
