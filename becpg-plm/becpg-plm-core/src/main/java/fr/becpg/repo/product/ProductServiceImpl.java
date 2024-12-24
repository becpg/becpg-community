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
package fr.becpg.repo.product;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulationPlugin;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.lexer.CompositionLexer;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * <p>ProductServiceImpl class.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
@Service("productService")
public class ProductServiceImpl implements ProductService, InitializingBean, FormulationPlugin {

	private static final Log logger = LogFactory.getLog(ProductServiceImpl.class);

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	private FormulationService<ProductData> formulationService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private CharactDetailsVisitorFactory charactDetailsVisitorFactory;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private EntityActivityService entityActivityService;

	@Autowired
	private EntityTplService entityTplService;

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		entityDictionaryService.registerPropDefMapping(PLMModel.PROP_PACKAGINGLIST_QTY, PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA);
		entityDictionaryService.registerExtraAssocsDefMapping(PLMModel.TYPE_PRODUCT, PLMModel.ASSOC_PRODUCTLIST_PRODUCT);

	}

	/** {@inheritDoc} */
	@Override
	public void formulate(NodeRef productNodeRef) {
		formulate(productNodeRef, null);
	}

	/** {@inheritDoc} */
	@Override
	public void formulate(NodeRef productNodeRef, String chainId) {
		try {
			policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

			L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {
				if (chainId == null) {
					formulationService.formulate(productNodeRef);
					entityActivityService.postEntityActivity(productNodeRef, ActivityType.Formulation, ActivityEvent.Update, null);
				} else {
					formulationService.formulate(productNodeRef, chainId);
				}
				return true;
			}), false, true);

		} finally {
			policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}

	}

	/** {@inheritDoc} */
	@Override
	public ProductData formulate(ProductData productData) {
		return formulationService.formulate(productData);
	}

	@Override
	public ProductData formulate(ProductData productData, String chainId) {
		return formulationService.formulate(productData, chainId);
	}
	
	/** {@inheritDoc} */
	@Override
	public CharactDetails formulateDetails(NodeRef productNodeRef, QName datatType, String dataListName, List<NodeRef> elements, Integer level) {
		ProductData productData = alfrescoRepository.findOne(productNodeRef);

		CharactDetailsVisitor visitor = charactDetailsVisitorFactory.getCharactDetailsVisitor(datatType, dataListName);
		return visitor.visit(productData, elements, level);
	}

	/** {@inheritDoc} */
	@Override
	public boolean shouldFormulate(NodeRef productNodeRef) {
		return formulationService.shouldFormulate(productNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public ProductData formulateText(String recipe) {

		if (logger.isDebugEnabled()) {
			logger.debug("Formulate text: " + recipe);
		}

		ProductData productData = new FinishedProductData();

		NodeRef defaultTplNodeRef = entityTplService.getEntityTpl(PLMModel.TYPE_FINISHEDPRODUCT);
		if (defaultTplNodeRef != null) {
			productData.setEntityTpl(alfrescoRepository.findOne(defaultTplNodeRef));
		}

		productData.getCompoListView().setCompoList(CompositionLexer.lexMultiLine(recipe));
		productData.getCompoListView().setDynamicCharactList(new ArrayList<>());
		productData.getPackagingListView().setPackagingList(new ArrayList<>());
		productData.getPackagingListView().setDynamicCharactList(new ArrayList<>());
		productData.getProcessListView().setProcessList(new ArrayList<>());
		productData.getProcessListView().setDynamicCharactList(new ArrayList<>());
		productData.getLabelingListView().setIngLabelingList(new ArrayList<>());

		if (logger.isDebugEnabled()) {
			logger.debug("Lexer result: " + productData);
		}

		L2CacheSupport.doInCacheOnly(() -> AuthenticationUtil.runAsSystem(() -> {
			formulationService.formulate(productData);
			return true;
		}));

		return productData;
	}

	/** {@inheritDoc} */
	@Override
	public FormulationPluginPriority getMatchPriority(QName type) {
		return entityDictionaryService.isSubClass(type, PLMModel.TYPE_PRODUCT) ? FormulationPluginPriority.NORMAL : FormulationPluginPriority.NONE;

	}

	/** {@inheritDoc} */
	@Override
	public void runFormulation(NodeRef entityNodeRef, String chainId) {
		formulate(entityNodeRef, chainId);
	}

}
