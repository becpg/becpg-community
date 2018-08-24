package fr.becpg.repo.project.extractor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.report.search.impl.DefaultExcelReportSearchPlugin;

@Service
public class TaskListExcelReportSearch extends DefaultExcelReportSearchPlugin{

	
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
		
		TaskListExtractorHelper.extractTaskListResources(nodeRef, AttributeExtractorMode.XLSX, itemType, ret, nodeService);
		
		return ret;
	}

}
