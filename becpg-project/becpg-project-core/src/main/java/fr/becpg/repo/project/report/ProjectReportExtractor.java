package fr.becpg.repo.project.report;

import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor;

/**
 * <p>ProjectReportExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProjectReportExtractor extends DefaultEntityReportExtractor {
	
	/** {@inheritDoc} */
	@Override
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt,  DefaultExtractorContext context) {
		
		if (assocDef != null && assocDef.getName() != null) {
			if (assocDef.getName().equals(ProjectModel.ASSOC_PROJECT_ENTITY)) {					
				List<NodeRef> nodeRefs = associationService.getTargetAssocs(entityNodeRef, assocDef.getName());
				for (NodeRef nodeRef : nodeRefs) {
					QName qName = nodeService.getType(nodeRef);
					Element pjtEntityElt = entityElt.addElement("projectEntity");
					Element nodeElt = pjtEntityElt.addElement(qName.getLocalName());
					EntityReportExtractorPlugin extractor = entityReportService.retrieveExtractor(nodeRef);
					if(extractor instanceof DefaultEntityReportExtractor){
						((DefaultEntityReportExtractor)extractor).extractEntity(nodeRef, nodeElt, context);
					} else {
						extractEntity(nodeRef, nodeElt, context);
					}
				}				
				return true;
			}
			else{
				super.loadTargetAssoc(entityNodeRef, assocDef, entityElt, context);
			}
		}

		return false;
	}
	
	/** {@inheritDoc} */
	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return dictionaryService.isSubClass(type, ProjectModel.TYPE_PROJECT) ? EntityReportExtractorPriority.NORMAL : EntityReportExtractorPriority.NONE;
	}
	
	
}
