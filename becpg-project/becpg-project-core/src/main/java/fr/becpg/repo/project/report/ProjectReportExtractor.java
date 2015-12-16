package fr.becpg.repo.project.report;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor;

@Service
public class ProjectReportExtractor extends DefaultEntityReportExtractor {
	
	@Override
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt, Map<String, byte[]> images) {

		if (assocDef != null && assocDef.getName() != null) {
			if (assocDef.getName().equals(ProjectModel.ASSOC_PROJECT_ENTITY)) {				
				List<NodeRef> nodeRefs = associationService.getTargetAssocs(entityNodeRef, assocDef.getName());
				for (NodeRef nodeRef : nodeRefs) {
					QName qName = nodeService.getType(nodeRef);
					Element nodeElt = entityElt.addElement(qName.getLocalName());
					extractEntity(entityNodeRef, nodeElt,images);
				}				
				return true;
			}
		}

		return false;
	}
	
	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return dictionaryService.isSubClass(type, ProjectModel.TYPE_PROJECT) ? EntityReportExtractorPriority.NORMAL : EntityReportExtractorPriority.NONE;
	}
	
}
