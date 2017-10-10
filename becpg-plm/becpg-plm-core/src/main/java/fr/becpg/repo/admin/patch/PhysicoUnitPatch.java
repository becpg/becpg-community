/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * Add physico chemical units on old systems
 */
public class PhysicoUnitPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(PhysicoUnitPatch.class);

	private BehaviourFilter policyBehaviourFilter;
	
	private EntityListDAO entityListDAO;
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		try {
			policyBehaviourFilter.disableBehaviour();

			logger.info("Importing physico chemical units");

			NodeRef systemHome = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
					"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System") + "/.");

			if (systemHome != null) {

				List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().selectNodesByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
						"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/Lists/bcpg:entityLists/PhysicoUnits") + "/*");

				NodeRef entityListsFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
						"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/Lists/bcpg:entityLists") + "/.");

				NodeRef physicoListsFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
						"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/Lists/bcpg:entityLists/PhysicoUnits") + "/.");

				logger.debug("nodeRefs: " + nodeRefs + ", listsFolder: " + physicoListsFolder + ", entityListsFolder: " + entityListsFolder);

				// tester si list physico existe
				if (nodeRefs.isEmpty()) {
					Map<QName, Serializable> properties;


					// si non la creer
					if (physicoListsFolder == null) {
						MLText entityListTranslated = TranslateHelper.getTranslatedPathMLText(PlmRepoConsts.PATH_PHYSICO_UNITS);
						physicoListsFolder = entityListDAO.createList(entityListsFolder, PlmRepoConsts.PATH_PHYSICO_UNITS,  BeCPGModel.TYPE_LIST_VALUE);
						nodeService.setProperty(physicoListsFolder, ContentModel.PROP_TITLE, entityListTranslated);						
					}

					// ajouter les valeurs comme ds test unitaire plm base test
					// case
					String[] physicoUnits = { "%", "-", "L", "mL", "kg", "g", "m", "cm", "mm", "µm", "m2", "m3", "P", "%", "%Vol", "°C", "K", "°Cent",
							"°D", "J", "eV", "g/kg", "mg/kg", "µg/kg", "pg/kg", "meqO2/kg", "meq/kg", "g/L", "mg/L", "µg/L", "g/mL", "mosm/L",
							"mol/L", "mPa.s", "Pa.s", "s", "min", "°B", "/0,2g", "/25g", "/100g", "g/100g", "mg/100g", "g/2,2L", "g/15g", "ppm",
							"ppb", "µg/g", "mg/g", "mL/g", "UB", "cps", "mg KOH/g", "mg NAOH/g", "A/P" };

					for (String physicoUnit : physicoUnits) {
						properties = new HashMap<>();
						properties.put(BeCPGModel.PROP_LV_VALUE, physicoUnit);
						nodeService.createNode(physicoListsFolder, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
								BeCPGModel.TYPE_LIST_VALUE, properties);
					}
				}
			}

		} finally {
			policyBehaviourFilter.enableBehaviour();
		}

		return "PhysicoUnit patch: Success";
	}

	public BehaviourFilter getPolicyBehaviourFilter() {
		return policyBehaviourFilter;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	
	
	public NodeRef createSystemEntity(NodeRef parentNodeRef, String entityPath, Map<String, QName> entitySystemDataLists) {

		try {

			// disable policy in order to have getTranslatedPath in cm:name
			policyBehaviourFilter.disableBehaviour(DataListModel.TYPE_DATALIST);

			MLText translatedPathMLText = TranslateHelper.getTranslatedPathMLText(entityPath);

			String entityName = TranslateHelper.getTranslatedPath(entityPath);
			if (entityName == null) {
				entityName = entityPath;
			}

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, entityName);
			properties.put(ContentModel.PROP_TITLE, translatedPathMLText);

			NodeRef entityNodeRef = null;
			List<NodeRef> matchingEntities = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_SYSTEM_ENTITY)
					.andPropEquals(ContentModel.PROP_NAME, entityName).inDB().list();

			if (!matchingEntities.isEmpty()) {
				entityNodeRef = matchingEntities.get(0);
			}

			if (entityNodeRef == null) {
				entityNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(entityPath)),
						BeCPGModel.TYPE_SYSTEM_ENTITY, properties).getChildRef();
			}

			// entityLists
		
			return entityNodeRef;

		} finally {
			policyBehaviourFilter.enableBehaviour(DataListModel.TYPE_DATALIST);
		}
	}
	
}
