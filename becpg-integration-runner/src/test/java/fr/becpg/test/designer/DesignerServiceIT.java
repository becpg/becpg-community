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
package fr.becpg.test.designer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;
import fr.becpg.repo.designer.data.DesignerTree;
import fr.becpg.repo.designer.data.FormControl;
import fr.becpg.repo.designer.impl.DesignerTreeVisitor;
import fr.becpg.repo.designer.impl.FormModelVisitor;
import fr.becpg.repo.designer.impl.MetaModelVisitor;

public class DesignerServiceIT extends AbstractDesignerServiceTest {

	private static final Log logger = LogFactory.getLog(DesignerServiceIT.class);

	@Autowired
	private MetaModelVisitor metaModelVisitor;

	@Autowired
	private FormModelVisitor formModelVisitor;

	@Autowired
	private DesignerTreeVisitor designerTreeVisitor;

	@Autowired
	private DesignerService designerService;

	@Test
	public void testMetaModelVisitor() {

		logger.info("testMetaModelVisitor");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			InputStream in = (new ClassPathResource("beCPG/designer/testModel.xml")).getInputStream();
			assertNotNull(in);

			M2Model m2Model = M2Model.createModel(in);

			NodeRef modelNodeRef = nodeService
					.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, DesignerModel.TYPE_M2_MODEL)
					.getChildRef();

			// Try to parse becpgModel
			metaModelVisitor.visitModelNodeRef(modelNodeRef, m2Model);

			DesignerTree tree = designerTreeVisitor.visitModelTreeNodeRef(modelNodeRef);
			assertNotNull(tree);
			logger.debug(tree);
			File tmp2 = File.createTempFile("testDesignerConfig", "xml");

			try (FileOutputStream tmpStream = new FileOutputStream(tmp2)) {
				// To Xml
				metaModelVisitor.visitModelXml(modelNodeRef, tmpStream);
			}

			return null;

		}, false, true);

	}

	@Test
	public void testFormModelVisitor() {

		logger.info("testFormModelVisitor");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			InputStream in = (new ClassPathResource("beCPG/designer/testConfig.xml")).getInputStream();
			assertNotNull(in);

			NodeRef modelNodeRef = nodeService
					.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_CONTENT)
					.getChildRef();
			nodeService.addAspect(modelNodeRef, DesignerModel.ASPECT_CONFIG, new HashMap<QName, Serializable>());

			ChildAssociationRef childAssociationRef = nodeService.createNode(modelNodeRef, DesignerModel.ASSOC_DSG_CONFIG,
					DesignerModel.ASSOC_DSG_CONFIG, DesignerModel.TYPE_DSG_CONFIG);
			NodeRef configNodeRef = childAssociationRef.getChildRef();

			formModelVisitor.visitConfigNodeRef(configNodeRef, in);
			// To Xml

			File tmp2 = File.createTempFile("testDesignerConfig", "xml");

			try (FileOutputStream tmpStream = new FileOutputStream(tmp2)) {
				formModelVisitor.visitConfigXml(configNodeRef, tmpStream);
			}

			return null;

		}, false, true);

	}

	@Test
	public void testDesignerService() {

		logger.info("testDesignerService");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<FormControl> controls = designerService.getFormControls();

			assertNotNull(controls);
			assertTrue(controls.size() > 0);

			InputStream in = (new ClassPathResource("beCPG/designer/testModel.xml")).getInputStream();
			assertNotNull(in);

			M2Model m2Model = M2Model.createModel(in);

			NodeRef modelNodeRef = nodeService
					.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, DesignerModel.TYPE_M2_MODEL)
					.getChildRef();

			// Try to parse becpgModel
			metaModelVisitor.visitModelNodeRef(modelNodeRef, m2Model);

			Map<QName, Serializable> props = new HashMap<>();
			props.put(DesignerModel.PROP_M2_NAME, "bcpg:test");

			NodeRef elNodeRef = designerService.createModelElement(modelNodeRef, DesignerModel.TYPE_M2_TYPE, DesignerModel.ASSOC_M2_TYPES, props,
					"templateModel_STARTTASK");

			assertEquals("bcpg:test", (String) nodeService.getProperty(elNodeRef, DesignerModel.PROP_M2_NAME));
			assertEquals("bpm:startTask", (String) nodeService.getProperty(elNodeRef, DesignerModel.PROP_M2_PARENT_NAME));

			return null;

		}, false, true);

	}

	@Test
	public void testFindOrCreateModel() {

		logger.info("testFindOrCreateModel");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			String name = "testFindOrCreateModel.xml";

			Map<String, Object> templateContext = new HashMap<>();
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

		}, false, true);

	}

}
