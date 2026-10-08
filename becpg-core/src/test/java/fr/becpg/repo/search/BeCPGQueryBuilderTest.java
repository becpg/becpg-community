package fr.becpg.repo.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;

/**
 * Unit tests for the queries {@link BeCPGQueryBuilder} sends to the database engine.
 */
public class BeCPGQueryBuilderTest {

	private static final String CODE = "PF-1";
	private static final NodeRef NODE_REF = new NodeRef("workspace://SpacesStore/2bd2bc27-7c5b-4d9a-9b55-3c0f4a8f0e11");

	@Test
	public void buildsAnFtsQueryInDatabase() {
		String query = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2).andPropEquals(BeCPGModel.PROP_CODE, CODE).inDB()
				.toString();

		assertFalse(query.contains("SELECT"));
		assertTrue(query.contains("TYPE:\"" + BeCPGModel.TYPE_ENTITY_V2 + "\""));
		assertTrue(query.contains("=" + BeCPGModel.PROP_CODE + ":\"" + CODE + "\""));
	}

	@Test
	public void testsNullOrUnsetWithIsNullOnlyInDatabase() {
		String query = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2).isNullOrUnset(BeCPGModel.PROP_SORT).inDB().toString();

		assertTrue(query.contains("ISNULL:\"" + BeCPGModel.PROP_SORT + "\""));
		assertFalse(query.contains("ISUNSET"));
	}

	@Test
	public void keepsIsUnsetOutsideDatabase() {
		String query = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2).isNullOrUnset(BeCPGModel.PROP_SORT).toString();

		assertTrue(query.contains("ISUNSET:\"" + BeCPGModel.PROP_SORT + "\""));
	}

	@Test
	public void keepsIdsInTheDatabaseQuery() {
		String query = BeCPGQueryBuilder.createQuery().andID(NODE_REF).inDB().toString();

		assertTrue(query.contains("ID:\"" + NODE_REF + "\""));
	}

	@Test
	public void excludesIdsInTheDatabaseQuery() {
		String query = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2).andNotID(NODE_REF).inDB().toString();

		assertTrue(query.contains("-=ID:\"" + NODE_REF + "\""));
	}
}
