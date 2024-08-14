package fr.becpg.repo.project.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormatService;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.web.scripts.process.EntityProcessListPlugin;

/**
 * <p>ProjectProcessListPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("projectProcessListPlugin")
public class ProjectProcessListPlugin implements EntityProcessListPlugin {
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private AssociationService associationService;

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	@Autowired
	private PropertyFormatService propertyFormatService;
	
	/** {@inheritDoc} */
	@Override
	public List<Map<String, Object>> buildModel(NodeRef nodeRef){
		

		PropertyFormats formater = propertyFormatService.getPropertyFormats(FormatMode.PROCESS, true);
		
		List<Map<String, Object>> ret = new ArrayList<>();
			
		List<NodeRef> projectNodeRefs = associationService.getSourcesAssocs(nodeRef, ProjectModel.ASSOC_PROJECT_ENTITY);
		
		projectNodeRefs.forEach(pjtNodeRef -> {
			
			ProjectData data = alfrescoRepository.findOne(pjtNodeRef);
			Map<String, Object> temp = new HashMap<>();
			
			temp.put(PROCESS_INSTANCE_NODEREF, pjtNodeRef.toString());
			temp.put(PROCESS_INSTANCE_TYPE, getType());
			
			temp.put(PROCESS_INSTANCE_TITLE, data.getName());
			
			String projectTitle = (String) nodeService.getProperty(pjtNodeRef, ContentModel.PROP_TITLE);
			if(projectTitle != null && !projectTitle.isEmpty()){
				temp.put(PROCESS_INSTANCE_MESSAGE, projectTitle);
			}
			
			if(data.getStartDate() != null){
				temp.put(PROCESS_INSTANCE_START_DATE, formater.formatDate(data.getStartDate()));
			}
			if(data.getDueDate() != null){
				temp.put(PROCESS_INSTANCE_DUE_DATE, formater.formatDate(data.getDueDate()));
			}
			
			temp.put(PROCESS_INSTANCE_IS_ACTIVE, data.getProjectState().equals(ProjectState.Completed) ? false : true);
			temp.put(PROCESS_INSTANCE_STATE, data.getProjectState().toString());
			
			NodeRef personRef = null;
			if(data.getProjectManager()!=null){
				personRef = data.getProjectManager();
			} else if(personService.personExists(data.getCreator())){
				personRef = personService.getPerson(data.getCreator());
			}
			
			temp.put(PROCESS_INSTANCE_INITIATOR, getPersonModel(personRef, data.getCreator()));
			
			ret.add(temp);
				
			
		});
		
		return ret;
		
	}

	  
	private Map<String, Object> getPersonModel(NodeRef person, String creator){

		Map<String, Object> model = new HashMap<>();
		if(person != null){
			model.put(PERSON_USER_NAME, nodeService.getProperty(person, ContentModel.PROP_USERNAME));
			NodeRef avatarRef = associationService.getTargetAssoc(person, ContentModel.ASSOC_AVATAR);
			if (avatarRef != null ){
				model.put(PERSON_AVATAR, "api/node/" + avatarRef.toString().replace("://", "/") + "/content/thumbnails/avatar");
			}
		} else {
			model.put(PERSON_USER_NAME, creator);
		}

		return model;
		
	}
	  
	
	/** {@inheritDoc} */
	@Override
	public String getType() {
		return ProjectModel.TYPE_PROJECT.getLocalName();
	}

	
	
	
}
