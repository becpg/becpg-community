/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
package fr.becpg.repo.quality;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>QualityControlService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface QualityControlService {

	/**
	 * <p>createSamplingList.</p>
	 *
	 * @param qcNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param controlPlanNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void createSamplingList(NodeRef qcNodeRef, NodeRef controlPlanNodeRef);
	/**
	 * <p>createControlList.</p>
	 *
	 * @param sampleListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void createControlList(NodeRef sampleListNodeRef);
	/**
	 * <p>updateControlListState.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void updateControlListState(NodeRef nodeRef);
	/**
	 * <p>createSamplingListId.</p>
	 *
	 * @param sampleListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void createSamplingListId(NodeRef sampleListNodeRef);
	/**
	 * <p>updateQualityControlState.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void updateQualityControlState(NodeRef nodeRef);
	/**
	 * <p>deleteSamplingListId.</p>
	 *
	 * @param sampleListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void deleteSamplingListId(NodeRef sampleListNodeRef);
	/**
	 * <p>copyProductDataList.</p>
	 *
	 * @param qcNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void copyProductDataList(NodeRef qcNodeRef, NodeRef productNodeRef);
}
