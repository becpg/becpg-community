/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.olap.OlapService;
import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;
import fr.becpg.test.RepoBaseTestCase;


public class OlapServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(OlapServiceTest.class);

	@Resource
	private OlapService olapService;

	@Test
	public void testOlapService() {
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				try {
					List<OlapChart> charts = olapService.retrieveOlapCharts();
					for (Iterator<OlapChart> iterator = charts.iterator(); iterator.hasNext();) {
						OlapChart olapChart = (OlapChart) iterator.next();
						logger.info(olapChart.toJSONObject().toString());
						OlapChartData olapChartData = olapService.retrieveChartData(olapChart.getQueryId());
						assertNotNull(olapChartData);
						logger.info(olapChartData.toJSONObject().toString());
					}
				} catch (Exception e) {
					logger.warn("Maybe no running saiku !", e);
				}
				return null;

			}
		}, false, true);
		
		

	}

}
