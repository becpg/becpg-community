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
 * <p>MPMModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MPMModel {

	/** Constant <code>MPM_URI="http://www.bcpg.fr/model/mpm/1.0"</code> */
	public static final String MPM_URI = "http://www.bcpg.fr/model/mpm/1.0";

	/** Constant <code>MPM_PREFIX="mpm"</code> */
	public static final String MPM_PREFIX = "mpm";

	/** Constant <code>MODEL</code> */
	public static final QName MODEL = QName.createQName(MPM_URI, "mpmmodel");
	
	//processStep
	/** Constant <code>TYPE_PROCESSSTEP</code> */
	public static final QName TYPE_PROCESSSTEP = QName.createQName(MPM_URI,
			"processStep");
	
	//processList
	/** Constant <code>TYPE_PROCESSLIST</code> */
	public static final QName TYPE_PROCESSLIST = QName.createQName(MPM_URI,
			"processList");
	/** Constant <code>PROP_PL_QTY</code> */
	public static final QName PROP_PL_QTY = QName.createQName(MPM_URI,
			"plQty");
	/** Constant <code>PROP_PL_QTY_RESOURCE</code> */
	public static final QName PROP_PL_QTY_RESOURCE = QName.createQName(MPM_URI,
			"plQtyResource");
	/** Constant <code>PROP_PL_RATE_RESOURCE</code> */
	public static final QName PROP_PL_RATE_RESOURCE = QName.createQName(MPM_URI,
			"plRateResource");
	/** Constant <code>PROP_PL_YIELD</code> */
	public static final QName PROP_PL_YIELD = QName.createQName(MPM_URI,
			"plYield");
	/** Constant <code>PROP_PL_RATE_PRODUCT</code> */
	public static final QName PROP_PL_RATE_PRODUCT = QName.createQName(MPM_URI,
			"plRateProduct");
	/** Constant <code>ASSOC_PL_STEP</code> */
	public static final QName ASSOC_PL_STEP = QName.createQName(MPM_URI,
			"plStep");
	/** Constant <code>ASSOC_PL_PRODUCT</code> */
	public static final QName ASSOC_PL_PRODUCT = QName.createQName(MPM_URI,
			"plProduct");
	/** Constant <code>ASSOC_PL_RESOURCE</code> */
	public static final QName ASSOC_PL_RESOURCE = QName.createQName(MPM_URI,
			"plResource");

	/** Constant <code>TYPE_RESOURCEPARAMLIST</code> */
	public static final QName TYPE_RESOURCEPARAMLIST = QName.createQName(MPM_URI,
			"resourceParamList");

	/** Constant <code>TYPE_RESOURCEPARAM</code> */
	public static final QName TYPE_RESOURCEPARAM = QName.createQName(MPM_URI,
			"resourceParam");
}
