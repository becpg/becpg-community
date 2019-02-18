package fr.becpg.repo.project.extractor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.report.search.impl.DefaultExcelReportSearchPlugin;

@Service
public class TaskListExcelReportSearch extends DefaultExcelReportSearchPlugin{

	
	@Autowired
	private ProjectService projectService;
	
	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public boolean isApplicable(QName itemType, String[] parameters) {

		return ProjectModel.TYPE_TASK_LIST.equals(itemType);
	}

	
	@Override 
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			Map<QName, Serializable> properties, final Map<NodeRef, Map<String, Object>> cache) {
		
		Map<String, Object> ret =  super.doExtract(nodeRef, itemType, metadataFields, properties, cache);
		
		String resources = "";
		
		
		if(entityDictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)){
			for(NodeRef resourceNoderef : projectService.extractResources(entityListDAO.getEntity(nodeRef), associationService.getTargetAssocs(nodeRef, ProjectModel.ASSOC_TL_RESOURCES))){
				if(!resources.isEmpty()){
					resources += ",";
				}
				resources += (String) nodeService.getProperty(resourceNoderef, nodeService.getType(resourceNoderef).equals(ContentModel.TYPE_AUTHORITY_CONTAINER) ? 
						(nodeService.getProperty(resourceNoderef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME) != null ? ContentModel.PROP_AUTHORITY_DISPLAY_NAME : ContentModel.PROP_AUTHORITY_NAME) 
						: ContentModel.PROP_USERNAME);
				
			}
		}
		
		if(!resources.isEmpty()){
			ret.put("assoc_pjt_tlResources", resources);
		}

		return ret;
	}
	


}
