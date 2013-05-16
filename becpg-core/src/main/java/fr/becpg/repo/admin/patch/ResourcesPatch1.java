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

import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.admin.InitRepoVisitorImpl;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

/**
 * Override alfresco email template
 */
public class ResourcesPatch1 extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(ResourcesPatch1.class);

	private static final String MSG_SUCCESS = "patch.bcpg.resourcesPatch1.result";

	private ReportTplService reportTplService;

	private DictionaryService dictionaryService;
	
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
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
		updateProductClientReports();
		updateIconsFiles();
		updateMappingFiles();
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void removeProductReports() {

		QName[] productTypes = { BeCPGModel.TYPE_RAWMATERIAL, BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT,
				BeCPGModel.TYPE_PACKAGINGMATERIAL, BeCPGModel.TYPE_PACKAGINGKIT, BeCPGModel.TYPE_RESOURCEPRODUCT,
				BeCPGModel.TYPE_SEMIFINISHEDPRODUCT};
		
		NodeRef folderNodeRef = searchFolder("/app:company_home/cm:System/cm:Reports/cm:ProductReportTemplates");

		for (QName productType : productTypes) {

			ClassDefinition classDef = dictionaryService.getClass(productType);
			
			NodeRef reportFolderNodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, classDef.getTitle());
			if(reportFolderNodeRef != null){
				logger.debug("Delete report folder for type " + classDef.getTitle());
				nodeService.deleteNode(reportFolderNodeRef);
			}			
		}
	}
	
	private void updateProductClientReports() {

		QName[] productTypes = { BeCPGModel.TYPE_FINISHEDPRODUCT};

		for (QName productType : productTypes) {

			List<NodeRef> reportTplNodeRefs = reportTplService.getSystemReportTemplates(ReportType.Document,
					productType);

			for (NodeRef reportTplNodeRef : reportTplNodeRefs) {
				String reportName = (String)nodeService.getProperty(reportTplNodeRef, ContentModel.PROP_NAME);
				if(!reportName.endsWith(".pdf")){
					reportName += ".pdf";
					nodeService.setProperty(reportTplNodeRef, ContentModel.PROP_NAME, reportName);
				}
				
				String reportPath = Locale.getDefault().equals(Locale.FRENCH) ? InitRepoVisitorImpl.PRODUCT_REPORT_PATH : InitRepoVisitorImpl.PRODUCT_REPORT_EN_PATH;
				contentHelper.addFilesResources(reportTplNodeRef, reportPath, true);
			}						
		}
	}
	
	private void updateIconsFiles(){
		NodeRef folderNodeRef = searchFolder("/app:company_home/cm:System/cm:Icons");
		if (folderNodeRef != null) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/images/*.png");
		}
		else{
			logger.error("Failed to find icons folder");
		}
	}

	private void updateMappingFiles(){
		NodeRef folderNodeRef = searchFolder("/app:company_home/cm:System/cm:Exchange/cm:Import/cm:Mapping");
		if (folderNodeRef != null) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/import/mapping/*.xml");
		}
		else{
			logger.error("Failed to find mapping folder");
		}
	}
}
