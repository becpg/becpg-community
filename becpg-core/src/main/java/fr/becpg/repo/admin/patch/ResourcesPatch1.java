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

import java.io.IOException;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.admin.InitRepoVisitorImpl;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;

/**
 * Override alfresco email template
 */
public class ResourcesPatch1 extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(ResourcesPatch1.class);

	private static final String MSG_SUCCESS = "patch.bcpg.resourcesPatch1.result";

	private ReportTplService reportTplService;

	private DictionaryService dictionaryService;
	
	private RepoService repoService;	
	
	protected BehaviourFilter policyBehaviourFilter;
	
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	protected String applyInternal() throws Exception {

		// - add messages in properties files
		// - product reports : remove for RM, packaging and update for FP and
		// SF, add product report for FP and SF
		// - add extension *.pdf to report tpl
		// - update icons file
		// - update Mapping files

		logger.info("Apply ResourcesPatch1");
		
		removeProductReports();
		updateIconsFiles();
		updateMappingFiles();
		
		try{
			policyBehaviourFilter.disableBehaviour(ReportModel.TYPE_REPORT_TPL);
			updateProductReports();
		}
		finally{
			policyBehaviourFilter.enableBehaviour(ReportModel.TYPE_REPORT_TPL);
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void removeProductReports() {

		QName[] productTypes = { BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT,
				BeCPGModel.TYPE_PACKAGINGMATERIAL, BeCPGModel.TYPE_PACKAGINGKIT, BeCPGModel.TYPE_RESOURCEPRODUCT};
		
		NodeRef folderNodeRef = searchFolder("/app:company_home/cm:System/cm:Reports/cm:ProductReportTemplates");

		if(folderNodeRef != null){
			for (QName productType : productTypes) {

				ClassDefinition classDef = dictionaryService.getClass(productType);
				
				NodeRef reportFolderNodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, classDef.getTitle());
				if(reportFolderNodeRef != null){
					logger.debug("Delete report folder for type " + classDef.getTitle());
					nodeService.deleteNode(reportFolderNodeRef);
				}			
			}
		}		
	}
	
	private void updateProductReports() {

		String productReportClientName = I18NUtil.getMessage("path.productreportclienttemplate");
		String productReportProductionName = I18NUtil.getMessage("path.productreportproductiontemplate");
		
		NodeRef folderNodeRef = searchFolder("/app:company_home/cm:System/cm:Reports/cm:ProductReportTemplates");
		
		if(folderNodeRef != null){
			
			QName [] productTypes = {BeCPGModel.TYPE_FINISHEDPRODUCT, BeCPGModel.TYPE_RAWMATERIAL};
			Boolean [] defaultReport = {true, true};
			int i =0;
			
			for(QName productType : productTypes){
				ClassDefinition classDef = dictionaryService.getClass(productType);			
				NodeRef reportFolderNodeRef = repoService.getOrCreateFolderByPath(folderNodeRef, classDef.getTitle(), classDef.getTitle());			
				NodeRef defaultReportNodeRef = nodeService.getChildByName(reportFolderNodeRef, ContentModel.ASSOC_CONTAINS, classDef.getTitle());
				
				String productReportClientPath = Locale.getDefault().equals(Locale.FRENCH) || Locale.getDefault().equals(Locale.FRANCE) ? InitRepoVisitorImpl.PRODUCT_REPORT_CLIENT_PATH : InitRepoVisitorImpl.PRODUCT_REPORT_CLIENT_EN_PATH;
				
				if(defaultReportNodeRef != null){
					nodeService.setProperty(defaultReportNodeRef, ContentModel.PROP_NAME, productReportClientName);				
					contentHelper.addFilesResources(defaultReportNodeRef, productReportClientPath, true);
				}
				else{
					try {
						reportTplService.createTplRptDesign(reportFolderNodeRef, productReportClientName,
								productReportClientPath, 
								ReportType.Document, ReportFormat.PDF, productType, true, defaultReport[i], false);
					} catch (IOException e) {
						logger.error("Failed to add production report template", e);
					}
				}
				i++;
			}
			
			QName [] productTypes2 = {BeCPGModel.TYPE_FINISHEDPRODUCT, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT};
			Boolean [] defaultReport2 = {false, true};
			i =0;
			
			for(QName productType : productTypes2){
				ClassDefinition classDef = dictionaryService.getClass(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT);
				NodeRef reportFolderNodeRef = repoService.getOrCreateFolderByPath(folderNodeRef, classDef.getTitle(), classDef.getTitle());
				NodeRef reportNodeRef = nodeService.getChildByName(reportFolderNodeRef, ContentModel.ASSOC_CONTAINS, productReportProductionName);
						
				if(reportNodeRef == null){				
					try {
						reportTplService.createTplRptDesign(reportFolderNodeRef, productReportProductionName,
								InitRepoVisitorImpl.PRODUCT_REPORT_PRODUCTION_PATH, 
								ReportType.Document, ReportFormat.PDF, productType, true, defaultReport2[i], false);
					} catch (IOException e) {
						logger.error("Failed to add production report template", e);
					}
				}
				i++;
			}			
		}			
	}
	
	private void updateIconsFiles(){
		NodeRef folderNodeRef = searchFolder("/app:company_home/cm:System/cm:Icons");
		if (folderNodeRef != null) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/images/*.png");
		}		
	}

	private void updateMappingFiles(){
		NodeRef folderNodeRef = searchFolder("/app:company_home/cm:System/cm:Exchange/cm:Import/cm:Mapping");
		if (folderNodeRef != null) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/import/mapping/*.xml");
		}		
	}	
}
