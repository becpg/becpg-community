package fr.becpg.repo.project.data;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "pjt:projectTpl")
public class ProjectTplData extends AbstractProjectData {
	
	

	public ProjectTplData() {
		super();
	}

	public ProjectTplData(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	
	
}
