/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.project;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.test.project.AbstractProjectTestCase;

public class ProjectListSortIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(ProjectListSortIT.class);

	@Autowired
	protected AlfrescoRepository<ProjectData> alfrescoRepository;

	@Autowired
	protected DataListExtractorFactory dataListExtractorFactory;

	@Test
	public void testSort() throws Exception {

		inWriteTx(() -> {

			for (int i = 0; i < 39; i++) {

				int modulo = i % 2;

				NodeRef hierarchy2NodeRef = modulo == 0 ? PROJECT_HIERARCHY2_CRUSTACEAN_REF
						: PROJECT_HIERARCHY2_FISH_REF;
				ProjectData projectData = new ProjectData();
				projectData.setName("Raw material " + ((i % 3) == 0 ? "aaa" : "bbb") + i);
				projectData.setHierarchy1(PROJECT_HIERARCHY1_SEA_FOOD_REF);
				projectData.setHierarchy2(hierarchy2NodeRef);
				projectData.setParentNodeRef(getTestFolderNodeRef());
				projectData = alfrescoRepository.save(projectData);
			}

			return null;
		});

		waitForSolr();

		inWriteTx(new RetryingTransactionCallback<NodeRef>() {
			@SuppressWarnings("unchecked")
			@Override
			public NodeRef execute() throws Throwable {

				final BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(ProjectModel.TYPE_PROJECT)
						.excludeVersions().parent(getTestFolderNodeRef())
						.addSort(ProjectModel.PROP_PROJECT_HIERARCHY2, true);

				List<NodeRef> projectNodeRefs = queryBuilder.list();

				assertEquals(40, projectNodeRefs.size());

				for (NodeRef projectNodeRef : projectNodeRefs) {
					logger.info(
							"Simple query test " + getHierarchy(projectNodeRef, ProjectModel.PROP_PROJECT_HIERARCHY2));
				}

				DataListFilter dataListFilter = new DataListFilter();
				dataListFilter.setDataListName("projectList");
				dataListFilter.setDataType(ProjectModel.TYPE_PROJECT);
				dataListFilter.setSortId("ProjectList");
				dataListFilter.setParentNodeRef(getTestFolderNodeRef());

				dataListFilter.getPagination().setMaxResults(-1);
				dataListFilter.getPagination().setPageSize(25);
				dataListFilter.setHasWriteAccess(true);

				List<AttributeExtractorField> metadataFields = new LinkedList<>();
				metadataFields.add(new AttributeExtractorField("cm:name", null));
				metadataFields.add(new AttributeExtractorField("bcpg:code", null));
				metadataFields.add(new AttributeExtractorField("pjt:projectOverdue", null));
				metadataFields.add(new AttributeExtractorField("pjt:projectHierarchy1", null));
				metadataFields.add(new AttributeExtractorField("pjt:projectHierarchy2", null));

				DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListFilter);
				PaginatedExtractedItems extractedItems = extractor.extract(dataListFilter, metadataFields);

				assertEquals(25, extractedItems.getPageItems().size());
				boolean isFish = false;
				for (int i = 0; i < extractedItems.getPageItems().size(); i++) {
					Map<String, Object> item = extractedItems.getPageItems().get(i);
					Map<String, Object> itemData = (Map<String, Object>) item.get("itemData");
					Map<String, Object> nameData = (Map<String, Object>) itemData.get("prop_cm_name");
					Map<String, Object> hierarchy2Data = (Map<String, Object>) itemData
							.get("prop_pjt_projectHierarchy2");
					String projectHierarchy2 = (String) hierarchy2Data.get("displayValue");
					String projectName = (String) nameData.get("displayValue");
					logger.info("project sorted " + projectName + " - " + projectHierarchy2);
					if ("Fish".equals(projectHierarchy2)) {
						isFish = true;
					}

					if (isFish && "Crustacean".equals(projectHierarchy2)) {
						fail("No Crustacean should apears after fish");
					}

				}

				return null;
			}

		});
	}

	private String getHierarchy(NodeRef nodeRef, QName hierarchyQName) {

		NodeRef hierarchyNodeRef = (NodeRef) nodeService.getProperty(nodeRef, hierarchyQName);
		return HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService);
	}
}
