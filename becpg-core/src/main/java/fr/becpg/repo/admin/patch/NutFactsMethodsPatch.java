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

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportService;
import fr.becpg.repo.importer.impl.ImportServiceImpl;

/**
 * Add nutFactsMethods
 */
public class NutFactsMethodsPatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(NutFactsMethodsPatch.class);

	private static final String MSG_SUCCESS = "patch.bcpg.nutFactsMethods.result";
	
	private EntitySystemService entitySystemService;
	
	private ImportService importService;
	
	public void setEntitySystemService(EntitySystemService entitySystemService) {
		this.entitySystemService = entitySystemService;
	}

	public void setImportService(ImportService importService) {
		this.importService = importService;
	}

	@Override
	protected String applyInternal() throws Exception {

		logger.info("Apply nutFactsMethods");

		NodeRef systemFolder = searchFolder("/app:company_home/cm:System");
		
		if(systemFolder != null){
			Map<String,QName> entityLists = new HashMap<String,QName>();			
			entityLists.put(RepoConsts.PATH_NUT_FACTS_METHODS,BeCPGModel.TYPE_LIST_VALUE);			
			entitySystemService.createSystemEntity(systemFolder, RepoConsts.PATH_LISTS, entityLists);
			
			ImportContext importContext = new ImportContext();
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource resource = resolver.getResource(Locale.getDefault().equals(Locale.FRENCH) || Locale.getDefault().equals(Locale.FRANCE) ? "beCPG/import/fr/food/nutritionFactsMethods.csv" : "beCPG/import/en/food/nutritionFactsMethods.csv");
			importContext.setCsvReader(new CSVReader(new InputStreamReader(resource.getInputStream()), ImportServiceImpl.SEPARATOR));			
			importContext.setImportFileName(resource.getFilename());
			importContext.setStopOnFirstError(true);		
			importContext.setDoUpdate(false);
			importContext.setRequiresNewTransaction(false);
			importService.importCSV(importContext);
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}
}
