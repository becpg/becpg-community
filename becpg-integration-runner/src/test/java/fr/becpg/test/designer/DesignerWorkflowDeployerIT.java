/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.repo.designer.DesignerService;
import fr.becpg.repo.designer.impl.FormModelVisitor;
import fr.becpg.repo.designer.impl.MetaModelVisitor;
import fr.becpg.repo.designer.workflow.DesignerWorkflowDeployer;

public class DesignerWorkflowDeployerIT extends AbstractDesignerServiceTest {

	private static final Log logger = LogFactory.getLog(DesignerWorkflowDeployerIT.class);

	@Autowired
	private MetaModelVisitor metaModelVisitor;

	@Autowired
	private FormModelVisitor formModelVisitor;

	@Autowired
	private DesignerWorkflowDeployer designerWorkflowDeployer;

	@Autowired
	private DesignerService designerService;

	@Test
	public void testCreateMissingFormsAndType() {

		logger.info("testCreateMissingFormsAndType");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				InputStream in = (new ClassPathResource("beCPG/designer/testWorkflow.xml")).getInputStream();
				assertNotNull(in);

				String fileName = "testWorkflow.xml";
				logger.debug("add file " + fileName);

				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, fileName);
				properties.put(WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID, ActivitiConstants.ENGINE_ID);

				NodeRef workflowNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
						WorkflowModel.TYPE_WORKFLOW_DEF, properties).getChildRef();

				ContentWriter writer = contentService.getWriter(workflowNodeRef, ContentModel.PROP_CONTENT, true);

				writer.setMimetype(mimetypeService.guessMimetype(fileName));
				writer.putContent(in);
				in.close();

				designerWorkflowDeployer.createMissingFormsAndType(workflowNodeRef);
				// Do it twice to test update
				designerWorkflowDeployer.createMissingFormsAndType(workflowNodeRef);

				NodeRef modelNodeRef = designerService.findOrCreateModel(fileName, null, null);

				metaModelVisitor.visitModelXml(modelNodeRef, System.out);

				NodeRef configNodeRef = designerService.findOrCreateConfig(fileName, null, null);

				assertNotNull(configNodeRef);

				formModelVisitor.visitConfigXml(configNodeRef, System.out);

				nodeService.deleteNode(nodeService.getPrimaryParent(modelNodeRef).getParentRef());
				nodeService.deleteNode(nodeService.getPrimaryParent(configNodeRef).getParentRef());
				return null;

			}
		}, false, true);

	}

}
