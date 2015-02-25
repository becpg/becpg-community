package fr.becpg.test.designer;

import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.designer.DesignerInitService;
import fr.becpg.test.RepoBaseTestCase;

public abstract class AbstractDesignerServiceTest extends RepoBaseTestCase {

	@Autowired
	protected DesignerInitService designerInitService;

	@Override
	protected boolean shouldInit() {
		return super.shouldInit() || designerInitService.getConfigsNodeRef()==null;
	}
	
}
