package fr.becpg.repo.admin.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.admin.InitVisitorService;


@Service("initVisitorService")
public class InitVisitorServiceImpl implements InitVisitorService {

	private static Log logger = LogFactory.getLog(InitVisitorServiceImpl.class);
	
	@Autowired
	private InitVisitor[] initVisitors;
	

	@Override
	public void run(NodeRef companyHomeNodeRef) {
		for(InitVisitor initVisitor : initVisitors) {
			if(logger.isDebugEnabled()) {
				logger.debug("Run visitor : "+initVisitor.getClass().getName());
			}
			
			initVisitor.visitContainer(companyHomeNodeRef);
		}
		
	}


	@Override
	public boolean shouldInit(NodeRef companyHomeNodeRef) {
		for(InitVisitor initVisitor : initVisitors) {
			if(logger.isDebugEnabled()) {
				logger.debug("Test should init for visitor : "+initVisitor.getClass().getName());
			}
			
			if(initVisitor.shouldInit(companyHomeNodeRef)) {
				return true;
			}
		}
		return false;
	}
}
