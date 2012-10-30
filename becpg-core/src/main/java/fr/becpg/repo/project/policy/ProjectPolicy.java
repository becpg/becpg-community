/*
 * 
 */
package fr.becpg.repo.project.policy;

import java.util.ArrayList;
import java.util.Collection;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectService;

/**
 * The Class ProjectPolicy.
 * 
 * @author querephi
 */
@Service
public class ProjectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProjectPolicy.class);

	private EntityListDAO entityListDAO;
	private ProjectService projectService;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init ProjectPolicy...");
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ProjectModel.TYPE_PROJECT, ProjectModel.ASSOC_PROJECT_TPL, new JavaBehaviour(this,
						"onCreateAssociation"));

	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		// copy datalist from Tpl to project
		logger.debug("copy datalists");
		Collection<QName> dataLists = new ArrayList<QName>();
		dataLists.add(ProjectModel.TYPE_TASK_LIST);
		entityListDAO.copyDataLists(assocRef.getTargetRef(), assocRef.getSourceRef(), dataLists, true);

		projectService.start(assocRef.getSourceRef());
	}

}
