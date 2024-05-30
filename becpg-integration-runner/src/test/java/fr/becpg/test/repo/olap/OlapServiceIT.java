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
package fr.becpg.test.repo.olap;

import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.olap.OlapService;
import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;
import fr.becpg.test.RepoBaseTestCase;

public class OlapServiceIT extends RepoBaseTestCase {

	private static final Log logger = LogFactory.getLog(OlapServiceIT.class);

	@Autowired
	private OlapService olapService;

	@Value("${becpg.olap.enabled}")
	private Boolean isOlapEnabled;

	@Test
	public void testOlapService() {

		if (Boolean.TRUE.equals(isOlapEnabled)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				List<OlapChart> charts = olapService.retrieveOlapCharts();
				for (OlapChart olapChart : charts) {
					logger.info(olapChart.toJSONObject().toString());
					OlapChartData olapChartData = olapService.retrieveChartData(olapChart.getQueryId());
					assertNotNull(olapChartData);
					logger.info(olapChartData.toJSONObject().toString());
				}
				return null;

			}, false, true);
		}

	}

	@Test
	public void testOlapInit() {
		if (Boolean.TRUE.equals(isOlapEnabled)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				// First initialization
				initRepoVisitorService.run(repositoryHelper.getCompanyHome());
				NodeRef systemNodeRef = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM);
				NodeRef OLAPfolderNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_OLAP_QUERIES);

				// Test every file is present (ie : nb of files == 17)
				int numberOfChilds = nodeService.getChildAssocs(OLAPfolderNodeRef).size();
				assertEquals(numberOfChilds, 17);

				// Delete the first file found
				NodeRef firstChildNode = nodeService.getChildAssocs(OLAPfolderNodeRef).get(0).getChildRef();
				String firstChildFileName = (String) nodeService.getProperty(firstChildNode, ContentModel.PROP_NAME);
				nodeService.deleteNode(firstChildNode);

				// Test one file was deleted (ie : nb of files == 16)
				numberOfChilds = nodeService.getChildAssocs(OLAPfolderNodeRef).size();
				assertEquals(numberOfChilds, 16);

				// Test the new first file found is different from  the previous one
				NodeRef newFirstChildNode = nodeService.getChildAssocs(OLAPfolderNodeRef).get(0).getChildRef();
				String newfirstChildFileName = (String) nodeService.getProperty(newFirstChildNode, ContentModel.PROP_NAME);
				assertNotEquals(firstChildFileName, newfirstChildFileName);

				// Second initialization
				initRepoVisitorService.run(repositoryHelper.getCompanyHome());
				systemNodeRef = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM);
				OLAPfolderNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_OLAP_QUERIES);

				// Test the file is still deleted (ie : nb of files == 16)
				numberOfChilds = nodeService.getChildAssocs(OLAPfolderNodeRef).size();
				assertEquals(numberOfChilds, 16);

				// Test the new first file found is the same as the previous one
				NodeRef newNewFirstChildNode = nodeService.getChildAssocs(OLAPfolderNodeRef).get(0).getChildRef();
				String newNewfirstChildFileName = (String) nodeService.getProperty(newNewFirstChildNode, ContentModel.PROP_NAME);
				assertEquals(newfirstChildFileName, newNewfirstChildFileName);

				// Delete the OLAP requests folder
				nodeService.deleteNode(OLAPfolderNodeRef);

				// Third initialization
				initRepoVisitorService.run(repositoryHelper.getCompanyHome());
				systemNodeRef = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM);
				OLAPfolderNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_OLAP_QUERIES);

				// Test every file is present (ie : nb of files == 17)
				numberOfChilds = nodeService.getChildAssocs(OLAPfolderNodeRef).size();
				assertEquals(numberOfChilds, 17);
				return true;

			}, false, true);
		}
	}

}
