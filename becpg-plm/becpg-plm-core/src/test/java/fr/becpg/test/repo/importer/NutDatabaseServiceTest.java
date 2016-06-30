package fr.becpg.test.repo.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.product.NutDatabaseService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

public class NutDatabaseServiceTest extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(NutDatabaseServiceTest.class);

	@Autowired
	private NutDatabaseService nutDatabaseService;

	private NodeRef createEmptyRM() {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef createdRM = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test");
			return createdRM;
		}, false, true);
	}

	private String extractCharactName(NodeRef node) {
		return (String) nodeService.getProperty(node, BeCPGModel.PROP_CHARACT_NAME);
	}

	private NodeRef getTestCSVFile() {
		return nutDatabaseService.getNutDatabases().stream().filter(info -> info.getName().equals("import.csv")).collect(Collectors.toList()).get(0)
				.getNodeRef();
	}

	@Test
	public void testImportRM() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef csvFile = getTestCSVFile();
			NodeRef emptyRM = createEmptyRM();
			NodeRef importedRM1 = nutDatabaseService.createProduct(csvFile, "5450", emptyRM);
			assertNotNull(importedRM1);

			ProductData importedRMdata = alfrescoRepository.findOne(importedRM1);
			assertNotNull(importedRMdata);
			assertEquals(3, importedRMdata.getNutList().size());
			int checks = 0;

			for (NutListDataItem nut : importedRMdata.getNutList()) {

				if ("Nut 2".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(0.153d, nut.getValue());
					++checks;
				} else if ("Nut 4".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(256.2d, nut.getValue());
					++checks;
				} else if ("Nut 8".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(564.1d, nut.getValue());
					++checks;
				} else {
					logger.debug("Failed on nut: " + extractCharactName(nut.getCharactNodeRef()) + ", value: " + nut.getValue());
					fail();
				}
			}

			assertEquals(3, checks);

			checks = 0;
			NodeRef importedRM2 = nutDatabaseService.createProduct(csvFile, "5451", emptyRM);
			assertNotNull(importedRM2);
			for (NutListDataItem nut : alfrescoRepository.findOne(importedRM2).getNutList()) {

				if ("Nut 2".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(2.64d, nut.getValue());
					++checks;
				} else if ("Nut 4".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(null, nut.getValue());
					++checks;
				} else if ("Nut 8".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(1254.22d, nut.getValue());
					++checks;
				} else {
					logger.debug("Failed on nut: " + extractCharactName(nut.getCharactNodeRef()) + ", value: " + nut.getValue());
					fail();
				}
			}
			assertEquals(3, checks);

			checks = 0;
			NodeRef importedRM3 = nutDatabaseService.createProduct(csvFile, "5452", emptyRM);
			assertNotNull(importedRM3);
			for (NutListDataItem nut : alfrescoRepository.findOne(importedRM3).getNutList()) {

				if ("Nut 2".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(null, nut.getValue());
					++checks;
				} else if ("Nut 4".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(656.254d, nut.getValue());
					++checks;
				} else if ("Nut 8".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(3d, nut.getValue());
					++checks;
				} else {
					logger.debug("Failed on nut: " + extractCharactName(nut.getCharactNodeRef()) + ", value: " + nut.getValue());
					fail();
				}
			}
			;
			assertEquals(3, checks);

			checks = 0;
			List<NodeRef> importedRMList = new ArrayList<NodeRef>(Arrays.asList(importedRM1, importedRM2, importedRM3));
			for (ChildAssociationRef child : nodeService.getChildAssocs(emptyRM)) {
				if (importedRMList.contains(child.getChildRef())) {
					++checks;
					importedRMList.remove(child.getChildRef());
				}
			}
			assertEquals(0, importedRMList.size());
			assertEquals(3, checks);

			return null;
		}, false, true);
	}

	@Test
	public void getNutsTest() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef csvFile = getTestCSVFile();

			List<NutListDataItem> nutList = nutDatabaseService.getNuts(csvFile, "544E");
			assertNotNull(nutList);

			assertEquals(3, nutList.size());

			int checks = 0;
			for (NutListDataItem nut : nutList) {
				if ("Nut 2".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(0.24d, nut.getValue());
					++checks;
				} else if ("Nut 4".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(null, nut.getValue());
					++checks;
				} else if ("Nut 8".equals(extractCharactName(nut.getCharactNodeRef()))) {
					assertEquals(null, nut.getValue());
					++checks;
				} else {
					logger.debug("Failed on nut: " + extractCharactName(nut.getCharactNodeRef()) + ", value: " + nut.getValue());
					fail();
				}
			}
			assertEquals(3, checks);

			return null;
		}, false, true);
	}

	@Test
	public void getNutDatabasesTest() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<FileInfo> databases = nutDatabaseService.getNutDatabases();
			int checks = 0;

			for (FileInfo file : databases) {

				if ("import.csv".equals(file.getName())) {

					++checks;
				}
			}

			assertEquals("import file is not in the system, try to clean and purge", 1, checks);

			return null;
		}, false, true);
	}

	@Test
	public void suggestTest() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			ListValuePage suggestions = nutDatabaseService.suggest(getTestCSVFile().toString(), "orange", 0, 10);
			assertEquals(2, suggestions.getResults().size());
			// check orange, must be 2 results
			int checks = 0;
			for (ListValueEntry suggestion : suggestions.getResults()) {
				logger.debug("Checking suggestion, named " + suggestion.getName());
				if ("544F - Chocolat, 85% de cacao, aux écorces d'orange".equals(suggestion.getName())) {
					++checks;
				} else if ("5450 - Orange, de madagascar".equals(suggestion.getName())) {
					++checks;
				}
			}
			assertEquals(2, checks);

			suggestions = nutDatabaseService.suggest(getTestCSVFile().toString(), "cacao", 0, 10);
			assertEquals(2, suggestions.getResults().size());
			checks = 0;
			for (ListValueEntry suggestion : suggestions.getResults()) {
				if ("544F - Chocolat, 85% de cacao, aux écorces d'orange".equals(suggestion.getName())) {
					++checks;
				} else if ("5451 - Chocolat au lait, 45% de cacao, enrichi en vitamine D".equals(suggestion.getName())) {
					++checks;
				}
			}
			assertEquals(2, checks);

			suggestions = nutDatabaseService.suggest(getTestCSVFile().toString(), "riz", 0, 10);
			logger.debug("suggestions using : " + getTestCSVFile().toString() + "= " + suggestions);
			assertEquals(0, suggestions.getResults().size());

			return null;
		}, false, true);
	}

}
