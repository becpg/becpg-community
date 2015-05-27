/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;
import fr.becpg.repo.designer.data.DesignerTree;
import fr.becpg.repo.designer.data.FormControl;
import fr.becpg.repo.designer.impl.DesignerTreeVisitor;
import fr.becpg.repo.designer.impl.FormModelVisitor;
import fr.becpg.repo.designer.impl.MetaModelVisitor;
import fr.becpg.test.RepoBaseTestCase;

public class DesignerServiceTest extends RepoBaseTestCase{ 

	private static Log logger = LogFactory.getLog(DesignerServiceTest.class);

	@Autowired
	private MetaModelVisitor metaModelVisitor;

	@Autowired
	private FormModelVisitor formModelVisitor;

	@Autowired
	private DesignerTreeVisitor designerTreeVisitor;

	@Autowired
	private DesignerService designerService;

	private static String PATH_TESTFOLDER = "DesignerTestFolder";

	

	@Test
	public void testMetaModelVisitor() {

		logger.info("testMetaModelVisitor");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
				if (folderNodeRef != null) {
					fileFolderService.delete(folderNodeRef);
				}
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

				InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/designer/testModel.xml");
				assertNotNull(in);

				M2Model m2Model = M2Model.createModel(in);

				NodeRef modelNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, DesignerModel.TYPE_M2_MODEL).getChildRef();

				// Try to parse becpgModel
				metaModelVisitor.visitModelNodeRef(modelNodeRef, m2Model);

				DesignerTree tree = designerTreeVisitor.visitModelTreeNodeRef(modelNodeRef);
				assertNotNull(tree);
				logger.debug(tree);

				// To Xml
				metaModelVisitor.visitModelXml(modelNodeRef, System.out);

				return null;

			}
		}, false, true);

	}

	@Test
	public void testFormModelVisitor() {

		logger.info("testFormModelVisitor");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
				if (folderNodeRef != null) {
					fileFolderService.delete(folderNodeRef);
				}
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

				InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/designer/testConfig.xml");
				assertNotNull(in);

				NodeRef modelNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_CONTENT).getChildRef();
				nodeService.addAspect(modelNodeRef, DesignerModel.ASPECT_CONFIG, new HashMap<QName, Serializable>());

				ChildAssociationRef childAssociationRef = nodeService.createNode(modelNodeRef, DesignerModel.ASSOC_DSG_CONFIG, DesignerModel.ASSOC_DSG_CONFIG,
						DesignerModel.TYPE_DSG_CONFIG);
				NodeRef configNodeRef = childAssociationRef.getChildRef();

				formModelVisitor.visitConfigNodeRef(configNodeRef, in);
				// To Xml
				formModelVisitor.visitConfigXml(configNodeRef, System.out);

				return null;

			}
		}, false, true);

	}

	@Test
	public void testDesignerService() {

		logger.info("testDesignerService");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
				if (folderNodeRef != null) {
					fileFolderService.delete(folderNodeRef);
				}
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

				List<FormControl> controls = designerService.getFormControls();

				assertNotNull(controls);
				assertTrue(controls.size() > 0);

				InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/designer/testModel.xml");
				assertNotNull(in);

				M2Model m2Model = M2Model.createModel(in);

				NodeRef modelNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, DesignerModel.TYPE_M2_MODEL).getChildRef();

				// Try to parse becpgModel
				metaModelVisitor.visitModelNodeRef(modelNodeRef, m2Model);

				Map<QName, Serializable> props = new HashMap<QName, Serializable>();
				props.put(DesignerModel.PROP_M2_NAME, "bcpg:test");

				NodeRef elNodeRef = designerService.createModelElement(modelNodeRef, DesignerModel.TYPE_M2_TYPE, DesignerModel.ASSOC_M2_TYPES, props, "templateModel_STARTTASK");

				assertEquals("bcpg:test", (String) nodeService.getProperty(elNodeRef, DesignerModel.PROP_M2_NAME));
				assertEquals("bpm:startTask", (String) nodeService.getProperty(elNodeRef, DesignerModel.PROP_M2_PARENT_NAME));

				return null;

			}
		}, false, true);

	}

	@Test
	public void testFindOrCreateModel() {

		logger.info("testFindOrCreateModel");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				String name = "testFindOrCreateModel.xml";

				Map<String, Object> templateContext = new HashMap<String, Object>();
				templateContext.put("processId", "processId");
				templateContext.put("modelName", "modelName");
				templateContext.put("engineId", "activiti");
				templateContext.put("prefix", "test");

				NodeRef modelNodeRef = designerService.findOrCreateModel(name, "extWorkflowModel.ftl", templateContext);

				NodeRef configNodeRef = designerService.findOrCreateConfig(name, "extWorkflowForm.ftl", templateContext);

				assertNotNull(modelNodeRef);
				assertNotNull(configNodeRef);

				nodeService.deleteNode(modelNodeRef);
				nodeService.deleteNode(configNodeRef);
				return null;

			}
		}, false, true);

	}

}
