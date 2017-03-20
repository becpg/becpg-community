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
package fr.becpg.repo.product;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
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
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.lexer.CompositionLexer;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * @author querephi
 */
@Service("productService")
public class ProductServiceImpl implements ProductService, InitializingBean {

	private static Log logger = LogFactory.getLog(ProductService.class);
	
	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	private FormulationService<ProductData> formulationService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private CharactDetailsVisitorFactory charactDetailsVisitorFactory;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private EntityActivityService entityActivityService;

	@Override
	public void afterPropertiesSet() throws Exception {
		entityDictionaryService.registerPropDefMapping(PLMModel.PROP_PACKAGINGLIST_QTY, PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA);

	}

	@Override
	public void formulate(NodeRef productNodeRef) throws FormulateException {
		formulate(productNodeRef, false);
	}

	@Override
	public void formulate(NodeRef productNodeRef, boolean fast) throws FormulateException {
		if (permissionService.hasPermission(productNodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED) {
			try {
				policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

				L2CacheSupport.doInCacheContext(() -> {
					AuthenticationUtil.runAsSystem(() -> {
						formulationService.formulate(productNodeRef);
						if (!fast) {
							entityActivityService.postEntityActivity(productNodeRef, ActivityType.Formulation, ActivityEvent.Update);
						}

						return true;
					});

				}, false, true);

			} finally {
				policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}
		} else {
			throw new FormulateException("Sorry you don't have write access");
		}

	}

	@Override
	public ProductData formulate(ProductData productData) throws FormulateException {
		return formulationService.formulate(productData);
	}

	@Override
	public CharactDetails formulateDetails(NodeRef productNodeRef, QName datatType, String dataListName, List<NodeRef> elements, Integer level)
			throws FormulateException {

		ProductData productData = alfrescoRepository.findOne(productNodeRef);

		CharactDetailsVisitor visitor = charactDetailsVisitorFactory.getCharactDetailsVisitor(datatType, dataListName);
		return visitor.visit(productData, elements, level);
	}

	@Override
	public boolean shouldFormulate(NodeRef productNodeRef) {
		return formulationService.shouldFormulate(productNodeRef);
	}

	@Override
	public ProductData formulateText(String recipe, ProductData productData) throws FormulateException {

		if(logger.isDebugEnabled()){
			logger.debug("Formulate text: "+recipe);
		}
		
		productData.getCompoListView().setCompoList(CompositionLexer.lexMultiLine(recipe));
		productData.getPackagingListView().setPackagingList(new ArrayList<PackagingListDataItem>());
		
		if(logger.isDebugEnabled()){
			logger.debug("Lexer result: "+productData);
		}
		
		
		L2CacheSupport.doInCacheContext(() -> {
			AuthenticationUtil.runAsSystem(() -> {
				formulationService.formulate(productData);
				return true;
			});
			
		},true);

		return productData;
	}

}
