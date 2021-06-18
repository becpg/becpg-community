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
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * <p>PLMWorkflowModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PLMWorkflowModel {

	/** Constant <code>PLM_WORKFLOW_URI="http://www.bcpg.fr/model/workflow/1.0"</code> */
	public static final String PLM_WORKFLOW_URI = "http://www.bcpg.fr/model/workflow/1.0";

	/** Constant <code>PLM_WORKFLOW_PREFIX="bcpgwf"</code> */
	public static final String PLM_WORKFLOW_PREFIX = "bcpgwf";

	/** Constant <code>MODEL</code> */
	public static final QName MODEL = QName.createQName(PLM_WORKFLOW_URI, "varmodel");
	
	/** Constant <code>ASPECT_PRODUCT_VALIDATION_ASPECT</code> */
	public static final QName ASPECT_PRODUCT_VALIDATION_ASPECT = QName.createQName(PLM_WORKFLOW_URI,
			"productValidationAspect");
	
	/** Constant <code>PROP_PV_VALIDATION_DATE</code> */
	public static final QName PROP_PV_VALIDATION_DATE = QName.createQName(PLM_WORKFLOW_URI, "pvValidationDate");
	/** Constant <code>ASSOC_PV_CALLER_ACTOR</code> */
	public static final QName ASSOC_PV_CALLER_ACTOR = QName.createQName(PLM_WORKFLOW_URI, "pvCallerActor");
	
}
