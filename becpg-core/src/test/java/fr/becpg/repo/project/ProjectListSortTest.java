package fr.becpg.repo.project;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.test.RepoBaseTestCase;

public class ProjectListSortTest extends RepoBaseTestCase {

	private static Log logger = LogFactory.getLog(ProjectListSortTest.class);

	@Resource
	protected AlfrescoRepository<ProjectData> alfrescoRepository;

	@Resource
	protected DataListExtractorFactory dataListExtractorFactory;

	@Test
	public void testSort() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				for (int i = 0; i < 40; i++) {

					int modulo = i % 2;

					NodeRef hierarchy2NodeRef = modulo == 0 ? PROJECT_HIERARCHY2_CRUSTACEAN_REF
							: PROJECT_HIERARCHY2_FISH_REF;
					ProjectData projectData = new ProjectData();
					projectData.setName("Raw material " + (i % 3 == 0 ? "aaa" : "bbb") + i);
					projectData.setHierarchy1(PROJECT_HIERARCHY1_SEA_FOOD_REF);
					projectData.setHierarchy2(hierarchy2NodeRef);

					projectData.setParentNodeRef(testFolderNodeRef);
					projectData = (ProjectData) alfrescoRepository.save(projectData);
				}

				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@SuppressWarnings("unchecked")
			@Override
			public NodeRef execute() throws Throwable {

				String query = LuceneHelper.mandatory(LuceneHelper.getCondType(ProjectModel.TYPE_PROJECT))
						+ LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION));
				List<NodeRef> projectNodeRefs = beCPGSearchService.luceneSearch(query,LuceneHelper.getSort(ProjectModel.PROP_PROJECT_HIERARCHY2));

				for (NodeRef projectNodeRef : projectNodeRefs) {
					logger.debug("project " + getHierarchy(projectNodeRef, ProjectModel.PROP_PROJECT_HIERARCHY2));
				}

				String dataListName = "projectList";
				DataListFilter dataListFilter = new DataListFilter();
				dataListFilter.setDataType(ProjectModel.TYPE_PROJECT);
				dataListFilter.setFilterQuery(query);
				dataListFilter.setSortId("ProjectList");

				DataListPagination pagination = new DataListPagination();
				pagination.setMaxResults(-1);
				pagination.setPageSize(25);

				List<String> metadataFields = new LinkedList<String>();
				metadataFields.add("cm:name");
				metadataFields.add("bcpg:code");
				metadataFields.add("pjt:projectOverdue");
				metadataFields.add("pjt:projectHierarchy1");
				metadataFields.add("pjt:projectHierarchy2");

				DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListFilter, dataListName);
				PaginatedExtractedItems extractedItems = extractor.extract(dataListFilter, metadataFields, pagination,
						true);
				
				assertEquals(25, extractedItems.getItems().size());

				for (int i = 0; i < extractedItems.getItems().size(); i++) {
					Map<String, Object> item = (Map<String, Object>) extractedItems.getItems().get(i);
					Map<String, Object> itemData = (Map<String, Object>) item.get("itemData");
					Map<String, Object> nameData = (Map<String, Object>) itemData.get("prop_cm_name");
					Map<String, Object> hierarchy2Data = (Map<String, Object>) itemData.get("prop_pjt_projectHierarchy2");					
					String projectHierarchy2 = (String)hierarchy2Data.get("displayValue");
					String projectName = (String)nameData.get("displayValue");
					logger.debug("project sorted " + projectName + " - "
							+ projectHierarchy2);
					
					if(i<20){
						assertEquals("Crustacean", projectHierarchy2);
					}
					else{
						assertEquals("Fish", projectHierarchy2);
					}
				}

				return null;
			}
		}, false, true);
	}

	private String getHierarchy(NodeRef nodeRef, QName hierarchyQName) {

		NodeRef hierarchyNodeRef = (NodeRef) nodeService.getProperty(nodeRef, hierarchyQName);
		return HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService);
	}
}
