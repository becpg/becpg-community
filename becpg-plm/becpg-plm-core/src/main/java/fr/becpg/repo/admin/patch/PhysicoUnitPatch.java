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
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * Patch physico unit
 */
public class PhysicoUnitPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(PhysicoUnitPatch.class);

	private Repository repositoryHelper;
	
	@Autowired
	private FileFolderService fileFolderService;

	@Override
	protected String applyInternal() throws Exception {

		logger.info("Importing physico units");

		List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().selectNodesByPath(
				nodeService.getRootNode(RepoConsts.SPACES_STORE),
				"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/Lists/bcpg:entityLists/PhysicoUnits") + "/*");

		NodeRef entityListsFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(
				nodeService.getRootNode(RepoConsts.SPACES_STORE),
				"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/Lists/bcpg:entityLists") + "/.");

		NodeRef listsFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(
				nodeService.getRootNode(RepoConsts.SPACES_STORE),
				"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/Lists/bcpg:entityLists/PhysicoUnits") + "/.");
		
		logger.info("nodeRefs: "+nodeRefs+", listsFolder: "+listsFolder+", entityListsFolder: "+entityListsFolder);
		
		// tester si list physico existe
		if(nodeRefs.isEmpty()){
			
			
			
			transactionHelper.doInTransaction(() -> {  
				Map<QName, Serializable> properties;
				
				NodeRef physicoListsFolder = listsFolder;
				
				if(listsFolder == null){
					physicoListsFolder = fileFolderService.create(entityListsFolder, "PhysicoUnits", ContentModel.TYPE_FOLDER).getNodeRef();
				}
				
				// si non la creer
				logger.info("Creating folder, listsFolder: "+physicoListsFolder);
				NodeRef physicoUnitsFolder = nodeService.createNode(physicoListsFolder, ContentModel.ASSOC_CONTAINS, 
						ContentModel.ASSOC_CONTAINS, DataListModel.TYPE_DATALIST).getChildRef();
	
				logger.info("PhyUnits folder: " + physicoUnitsFolder);
	
				// ajouter les valeurs comme ds test unitaire plm base test case
				String[] physicoUnits = { "%", "-", "L", "mL", "kg", "g", "m", "cm", "mm", "µm", "m2", "m3", "P", "%", "%Vol", "°C", "K", "°Cent", "°D", "J",
						"eV", "g/kg", "mg/kg", "µg/kg", "pg/kg", "meqO2/kg", "meq/kg", "g/L", "mg/L", "µg/L", "g/mL", "mosm/L", "mol/L", "mPa.s", "Pa.s", "s",
						"min", "°B", "/0,2g", "/25g", "/100g", "g/100g", "mg/100g", "g/2,2L", "g/15g", "ppm", "ppb", "µg/g", "mg/g", "mL/g", "UB", "cps",
						"mg KOH/g", "mg NAOH/g", "A/P" };
	
				for (String physicoUnit : physicoUnits) {
					properties = new HashMap<>();
					properties.put(BeCPGModel.PROP_LV_VALUE, physicoUnit);
					nodeService.createNode(physicoUnitsFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
							BeCPGModel.TYPE_LIST_VALUE, properties);
				}
				
				return true;
			}, false, true);
		}

		logger.info("PhysicoUnits added");

		return "PhysicoUnit patch: Success";
	}

	public Repository getRepositoryHelper() {
		return repositoryHelper;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

}
