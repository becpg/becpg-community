package fr.becpg.repo.project.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.web.scripts.process.EntityProcessListPlugin;

@Service("projectProcessListPlugin")
public class ProjectProcessListPlugin implements EntityProcessListPlugin {
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private AssociationService associationService;

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;
	
	
	@Override
	public List<Map<String, Object>> buildModel(NodeRef nodeRef){
		
		List<Map<String, Object>> ret = new ArrayList<>();
		
		List<NodeRef> projectNodeRefs = BeCPGQueryBuilder.createQuery()
				.ofType(ProjectModel.TYPE_PROJECT)
				.excludeAspect(BeCPGModel.ASPECT_ENTITY_TPL)
				.excludeAspect(ContentModel.ASPECT_CHECKED_OUT)
				.excludeAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION)
				.excludeProp(ProjectModel.PROP_PROJECT_STATE, ProjectState.Cancelled.toString())
				.list();
			
		projectNodeRefs.forEach(pjtNodeRef -> {
			NodeRef assocRef = associationService.getTargetAssoc(pjtNodeRef, ProjectModel.ASSOC_PROJECT_ENTITY);
			if(assocRef !=null && assocRef.equals(nodeRef)){
				
				ProjectData data = alfrescoRepository.findOne(pjtNodeRef);
				Map<String, Object> temp = new HashMap<>();
				
				temp.put(PROCESS_INSTANCE_NODEREF, pjtNodeRef.toString());
				temp.put(PROCESS_INSTANCE_TYPE, getType());
				
				temp.put(PROCESS_INSTANCE_TITLE, data.getName());
				temp.put(PROCESS_INSTANCE_START_DATE, FORMATER.format(data.getStartDate()));
				temp.put(PROCESS_INSTANCE_DUE_DATE, FORMATER.format(data.getDueDate()));
				
				temp.put(PROCESS_INSTANCE_IS_ACTIVE, data.getProjectState().equals(ProjectState.Completed) ? false : true);
				
				temp.put(PROCESS_INSTANCE_INITIATOR, getPersonModel(data.getCreator()));
				
				ret.add(temp);
			}
			
		});
		
		return ret;
		
	}

	  
	private Map<String, Object> getPersonModel(String name){

		Map<String, Object> model = new HashMap<>();
		model.put(PERSON_USER_NAME, name);
		
		if (personService.personExists(name))
		{
			NodeRef person = personService.getPerson(name);
			NodeRef avatarRef = associationService.getTargetAssoc(person, ContentModel.ASSOC_AVATAR);
			model.put(PERSON_AVATAR, "api/node/" + avatarRef.toString().replace("://", "/") + "/content/thumbnails/avatar");
		}
		
		return model;
		
	}
	  
	
	@Override
	public String getType() {
		return ProjectModel.TYPE_PROJECT.getLocalName();
	}

	
	
	
}
