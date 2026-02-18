/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.supplier.impl;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.project.WorkflowPackageHandler;
import fr.becpg.repo.project.data.ProjectData;

/**	
 * Adds the project supplier to the workflow package so that
 * {@code getWorkflowIdsForContent(supplierNodeRef)} correctly finds the active
 * workflow when the supplier portal wizard is opened with the supplier nodeRef.
 *
 * @author matthieu
 */
//@Service
@Deprecated 
public class SupplierWorkflowPackageHandler implements WorkflowPackageHandler
{

	private static final Log logger = LogFactory.getLog(SupplierWorkflowPackageHandler.class);

	@Autowired
	private NodeService nodeService;

	/** {@inheritDoc} */
	@Override
	public void addToWorkflowPackage(NodeRef wfPackage, ProjectData projectData)
	{
		List<AssociationRef> supplierAssocs = nodeService.getTargetAssocs(projectData.getNodeRef(), PLMModel.ASSOC_SUPPLIERS);
		for (AssociationRef assoc : supplierAssocs)
		{
			NodeRef supplierRef = assoc.getTargetRef();
			if (nodeService.exists(supplierRef))
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Adding supplier " + supplierRef + " to workflow package for project " + projectData.getNodeRef());
				}
				nodeService.addChild(wfPackage, supplierRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, ContentModel.ASSOC_CHILDREN);
			}
		}
	}

}
