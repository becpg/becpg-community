package org.alfresco.repo.domain.qname;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Checks that a lookup with the wrong case, matched by a case insensitive database, does not poison the QName caches (#37186).
 */
public class AbstractQNameDAOImplTest {

	private static final String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";
	private static final QName COST_LIST_COST = QName.createQName(BECPG_URI, "costListCost");
	private static final QName COST_LIST_COST_WRONG_CASE = QName.createQName(BECPG_URI, "costListcost");

	private CaseInsensitiveQNameDAO qnameDAO;

	@Before
	public void setUp() {
		qnameDAO = new CaseInsensitiveQNameDAO();
		qnameDAO.setNamespaceCache(new MapCache<>());
		qnameDAO.setQnameCache(new MapCache<>());
	}

	@Test
	public void testWrongCaseLookupReturnsTheStoredQName() {
		qnameDAO.getOrCreateQName(COST_LIST_COST);

		Assert.assertEquals(COST_LIST_COST, qnameDAO.getQName(COST_LIST_COST_WRONG_CASE).getSecond());
	}

	@Test
	public void testWrongCaseLookupKeepsTheStoredQName() {
		Long id = qnameDAO.getOrCreateQName(COST_LIST_COST).getFirst();

		qnameDAO.getQName(COST_LIST_COST_WRONG_CASE);

		Assert.assertEquals(COST_LIST_COST, qnameDAO.getQName(id).getSecond());
	}

	@Test
	public void testWrongCaseNamespaceLookupKeepsTheStoredUri() {
		Long id = qnameDAO.getOrCreateNamespace(BECPG_URI).getFirst();

		qnameDAO.getNamespace(BECPG_URI.toUpperCase(Locale.ROOT));

		Assert.assertEquals(BECPG_URI, qnameDAO.getNamespace(id).getSecond());
	}

	@Test
	public void testExactQNameIsFound() {
		Long id = qnameDAO.getOrCreateQName(COST_LIST_COST).getFirst();

		Pair<Long, QName> found = qnameDAO.getQName(COST_LIST_COST);

		Assert.assertEquals(id, found.getFirst());
	}

	/**
	 * In-memory QName DAO that matches names regardless of their case, as a MySQL case insensitive collation does.
	 */
	private static class CaseInsensitiveQNameDAO extends AbstractQNameDAOImpl {

		private final List<NamespaceEntity> namespaces = new ArrayList<>();
		private final List<QNameEntity> qnames = new ArrayList<>();

		@Override
		protected NamespaceEntity findNamespaceEntityById(Long id) {
			for (NamespaceEntity entity : namespaces) {
				if (entity.getId().equals(id)) {
					return entity;
				}
			}
			return null;
		}

		@Override
		protected NamespaceEntity findNamespaceEntityByUri(String uri) {
			for (NamespaceEntity entity : namespaces) {
				if (entity.getUriSafe().equalsIgnoreCase(uri)) {
					return entity;
				}
			}
			return null;
		}

		@Override
		protected NamespaceEntity createNamespaceEntity(String uri) {
			NamespaceEntity entity = new NamespaceEntity();
			entity.setId((long) namespaces.size() + 1);
			entity.setUriSafe(uri);
			namespaces.add(entity);
			return entity;
		}

		@Override
		protected int updateNamespaceEntity(NamespaceEntity entity, String uri) {
			entity.setUriSafe(uri);
			return 1;
		}

		@Override
		protected QNameEntity findQNameEntityById(Long id) {
			for (QNameEntity entity : qnames) {
				if (entity.getId().equals(id)) {
					return entity;
				}
			}
			return null;
		}

		@Override
		protected QNameEntity findQNameEntityByNamespaceAndLocalName(Long nsId, String localName) {
			for (QNameEntity entity : qnames) {
				if (entity.getNamespaceId().equals(nsId) && entity.getLocalNameSafe().equalsIgnoreCase(localName)) {
					return entity;
				}
			}
			return null;
		}

		@Override
		protected QNameEntity createQNameEntity(Long nsId, String localName) {
			QNameEntity entity = new QNameEntity();
			entity.setId((long) qnames.size() + 1);
			entity.setNamespaceId(nsId);
			entity.setLocalNameSafe(localName);
			qnames.add(entity);
			return entity;
		}

		@Override
		protected int updateQNameEntity(QNameEntity entity, Long nsId, String localName) {
			entity.setNamespaceId(nsId);
			entity.setLocalNameSafe(localName);
			return 1;
		}

		@Override
		protected int deleteQNameEntity(QNameEntity entity) {
			return qnames.remove(entity) ? 1 : 0;
		}
	}

	/**
	 * Map backed cache standing for the shared Alfresco cache.
	 */
	private static class MapCache<K extends Serializable, V> implements SimpleCache<K, V> {

		private final Map<K, V> values = new HashMap<>();

		@Override
		public boolean contains(K key) {
			return values.containsKey(key);
		}

		@Override
		public Collection<K> getKeys() {
			return values.keySet();
		}

		@Override
		public V get(K key) {
			return values.get(key);
		}

		@Override
		public void put(K key, V value) {
			values.put(key, value);
		}

		@Override
		public void remove(K key) {
			values.remove(key);
		}

		@Override
		public void clear() {
			values.clear();
		}
	}
}
