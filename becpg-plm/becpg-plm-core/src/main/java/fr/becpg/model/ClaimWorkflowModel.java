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
 * <p>ClaimWorkflowModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ClaimWorkflowModel {

	/** Constant <code>NC_URI="http://www.bcpg.fr/model/nc-workflow/1."{trunked}</code> */
	public static final String NC_URI = "http://www.bcpg.fr/model/nc-workflow/1.0";
	/** Constant <code>PROP_REJECTED_STATE</code> */
	public static final QName PROP_REJECTED_STATE = QName.createQName(NC_URI, "claimRejectedState");
	/** Constant <code>PROP_REJECTED_CAUSE</code> */
	public static final QName PROP_REJECTED_CAUSE = QName.createQName(NC_URI, "claimRejectedCause");
	
	
}
