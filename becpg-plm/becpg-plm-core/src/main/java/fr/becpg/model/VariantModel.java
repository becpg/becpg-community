/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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

@Deprecated
public class VariantModel {

	public static final String VARIANT_URI = "http://www.bcpg.fr/model/var/1.0";

	public static final String VARIANT_PREFIX = "var";

	public static final QName MODEL = QName.createQName(VARIANT_URI, "varmodel");
	
	//charact
	public static final QName TYPE_CHARACT = QName.createQName(VARIANT_URI,
			"charact");
		
}
