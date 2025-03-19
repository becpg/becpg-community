package fr.becpg.test.repo.search;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.search.SearchRuleService;
import fr.becpg.repo.search.data.SearchRuleFilter;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

public class SearchRuleServiceImplIT extends PLMBaseTestCase {

	@Autowired
	protected SearchRuleService searchRuleService;

	@Test
	public void testSearchRuleService() {

		SearchRuleFilter filter = new SearchRuleFilter();
		filter.setNodeType(PLMModel.TYPE_RAWMATERIAL);
		filter.setNodePath(nodeService.getPath(getTestFolderNodeRef()));

		inWriteTx(() -> {
			return BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP search rule test");
		});

		waitForSolr();

		List<NodeRef> results = searchRuleService.search(filter).getResults();
		Assert.assertEquals(1, results.size());

		filter = new SearchRuleFilter();
		filter.setNodeType(PLMModel.TYPE_ALLERGENLIST);
		filter.setNodePath(nodeService.getPath(getTestFolderNodeRef()));

		results = searchRuleService.search(filter).getResults();
		Assert.assertEquals(10, results.size());
	}
}
