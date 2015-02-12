/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

public interface GS1Model {


	public static final String GS1_URI = "http://www.bcpg.fr/model/gs1/1.0";

	public static final String BECPG_PREFIX = "gs1";


	/** The Constant MODEL. */
	public static final QName MODEL = QName.createQName(GS1_URI, "gs1Model");
	
	public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(GS1_URI,"DeliveryChannel");
	
	
}
