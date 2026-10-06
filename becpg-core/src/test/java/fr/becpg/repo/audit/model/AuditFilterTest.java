package fr.becpg.repo.audit.model;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.audit.exception.BeCPGAuditException;

/**
 * Unit tests of the parsing and the matching of {@link AuditFilter}.
 */
public class AuditFilterTest {

	private static final String TEMPLATE_KEY = "template";

	private static final String TEMPLATE_NODE_REF = "workspace://SpacesStore/8529f58b-2615-4d69-a9f5-8b26153d6902";

	@Test
	public void parsesKeyAndValue() {
		AuditFilter filter = AuditFilter.parse(" " + TEMPLATE_KEY + " = " + TEMPLATE_NODE_REF + " ");

		Assert.assertEquals(new AuditFilter(TEMPLATE_KEY, TEMPLATE_NODE_REF), filter);
	}

	@Test
	public void keepsSeparatorsOfValue() {
		AuditFilter filter = AuditFilter.parse("filename=a=b");

		Assert.assertEquals("a=b", filter.value());
	}

	@Test(expected = BeCPGAuditException.class)
	public void rejectsFilterWithoutSeparator() {
		AuditFilter.parse(TEMPLATE_KEY);
	}

	@Test
	public void matchesValueOnItsText() {
		AuditFilter filter = new AuditFilter("resultsSize", "275");

		Assert.assertTrue(filter.matches(275));
		Assert.assertFalse(filter.matches(276));
	}

	@Test
	public void doesNotMatchMissingValue() {
		Assert.assertFalse(new AuditFilter(TEMPLATE_KEY, TEMPLATE_NODE_REF).matches(null));
	}
}
