package fr.becpg.test.repo.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.impl.BeCPGCacheServiceImpl;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.test.RepoBaseTestCase;

public class EntityDAOIT extends RepoBaseTestCase {

	private Log logger = LogFactory.getLog(EntityDAOIT.class);

	@Autowired
	@Qualifier("entityListDAO2")
	private EntityListDAO entityListDAOV2;

	@Autowired
	private NodeDAO nodeDAO;
	
	
	private NodeRef createTestNode(NodeRef parentNodeRef, QName type, String name) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef entityNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI, name), type).getChildRef();
			nodeService.setProperty(entityNodeRef, ContentModel.PROP_NAME, name);
			return entityNodeRef;

		}, false, true);
	}

	@Test
	public void testEntityListDao() {

		logger.info("Perf test with cache:");
		daoServicePerfTest(false);

//		logger.info("Perf test without cache:");
//		daoServicePerfTest(true);

	}

	private void daoServicePerfTest(boolean disableCache) {
		try {
			((BeCPGCacheServiceImpl) beCPGCacheService).setDisableAllCache(disableCache);
			Map<String, Long> perfs1 = new HashMap<>();
			Map<String, Long> perfs2 = new HashMap<>();
			daoServicePerfTest("V1" +disableCache, entityListDAO, perfs1);
			daoServicePerfTest("V2" +disableCache, entityListDAOV2, perfs2);


			logger.info("Performance results");
			for (Map.Entry<String, Long> entry : perfs1.entrySet()) {
				Long t1 = entry.getValue();
				Long t2 = perfs2.get(entry.getKey());
				Double perc = 0d;
				if (t1 != 0 && t1!=null && t2!=null) {
					perc = ((t1 - t2) / (t1 * 1d)) * 100d;
				}

				logger.info(" - " + entry.getKey() + " # V1 time: " + t1 + "ms, V2 time: " + t2 + "ms, speed gain: " + perc + "%");

			}

		} finally {
			((BeCPGCacheServiceImpl) beCPGCacheService).setDisableAllCache(false);
		}

	}

	private void daoServicePerfTest(String name, EntityListDAO service, Map<String, Long> perfs) {

		java.util.List<NodeRef> nodes = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			NodeRef entityNodeRef = createTestNode(getTestFolderNodeRef(), BeCPGModel.TYPE_ENTITY_V2, name + "EntityDaoTest " + i);

			NodeRef listContainerNodeRef = perfs("createListContainer", () -> service.createListContainer(entityNodeRef), perfs);

			if (listContainerNodeRef != null) {

				NodeRef listNodeRef = perfs("createList", () -> service.createList(listContainerNodeRef, BeCPGModel.TYPE_LIST_VALUE), perfs);

				for (int j = 0; j < 50; j++) {
					Map<QName, Serializable> prop = new HashMap<>();
					prop.put(BeCPGModel.PROP_LV_VALUE, "Val" + j);
					prop.put(BeCPGModel.PROP_LV_CODE, j);
					perfs("createListItem", () -> service.createListItem(listNodeRef, BeCPGModel.TYPE_LIST_VALUE, prop, new HashMap<>()), perfs);
				}

			}

			nodes.add(entityNodeRef);

		}
		
		//Clear alfresco cache
		nodeDAO.clear();

		for (NodeRef entityNodeRef : nodes) {
			NodeRef listContainerNodeRef = perfs("getListContainer", () -> service.getListContainer(entityNodeRef), perfs);

			if (listContainerNodeRef != null) {

				NodeRef listNodeRef = perfs("getList", () -> service.getList(listContainerNodeRef, BeCPGModel.TYPE_LIST_VALUE), perfs);

				List<NodeRef> ret = perfs("getListItems", () -> service.getListItems(listNodeRef, BeCPGModel.TYPE_LIST_VALUE), perfs);
				Assert.assertEquals(ret.size(),50);
				
				for (NodeRef listItemNodeRef : ret) {
					NodeRef tmp = perfs("getEntity", () -> service.getEntity(listItemNodeRef), perfs);
					nodeDAO.clear();
					if (!entityNodeRef.equals(tmp)) {

						logger.error(tmp + " doesn-t match " + nodeService.getType(tmp) + " " + nodeService.getProperty(tmp, ContentModel.PROP_NAME));
						Assert.fail();
					}
				}
				
				Map<String, Boolean> defaultSortProps = new LinkedHashMap<>();
				defaultSortProps.put("@cm:created", false);
				
				ret = perfs("getListItemsSort",
						() -> service.getListItems(listNodeRef, BeCPGModel.TYPE_LIST_VALUE, defaultSortProps), perfs);
				Assert.assertEquals(ret.size(),50);
				for (NodeRef listItemNodeRef : ret) {
					Assert.assertTrue(entityNodeRef.equals(perfs("getEntity", () -> service.getEntity(listItemNodeRef), perfs)));
				}
			}
		}

	}

	private <T> T perfs(String key, Supplier<T> supplier, Map<String, Long> perfs) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		try {
			return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				return supplier.get();

			}, false, true);
		} finally {
			stopWatch.stop();
			Long time = perfs.get(key);
			if (time == null) {
				time = stopWatch.getTime();
			} else {
				time += stopWatch.getTime();
			}
			perfs.put(key, time);

		}
	}

}
