/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
package fr.becpg.test.designer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author matthieu
 * 
 */
public class DesignerInitServiceTest extends AbstractDesignerServiceTest{

	/** The logger. */
	private static final Log logger = LogFactory.getLog(DesignerInitServiceTest.class);





	@Test
	public void testInitDesigner() {

		logger.info("testInitDesigner");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef workflowFolder = designerInitService.getWorkflowsNodeRef();

				Assert.assertNotNull(workflowFolder);
				Assert.assertNotNull(designerInitService.getModelsNodeRef());
				Assert.assertNotNull(designerInitService.getConfigsNodeRef());

				Assert.assertNotNull(nodeService.getChildByName(designerInitService.getModelsNodeRef(), ContentModel.ASSOC_CONTAINS, "extCustomModel.xml"));
				Assert.assertNotNull(nodeService.getChildByName(designerInitService.getConfigsNodeRef(), ContentModel.ASSOC_CONTAINS, "extCustomForm.xml"));

				return null;

			}
		}, false, true);

	}

}
