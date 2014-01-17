package fr.becpg.test;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.designer.DesignerInitService;

/**
 * Mockup class for test
 * @author matthieu
 *
 */
public class DesignerInitServiceMock implements DesignerInitService {


	@Override
	public NodeRef getWorkflowsNodeRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeRef getModelsNodeRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeRef getConfigsNodeRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addReadOnlyDesignerFiles(String pattern) {
		// TODO Auto-generated method stub

	}

}
