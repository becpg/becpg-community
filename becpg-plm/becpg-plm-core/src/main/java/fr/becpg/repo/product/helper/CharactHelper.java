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
package fr.becpg.repo.product.helper;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

public class CharactHelper {

	@Deprecated
	public static Double getCharactValue(NodeRef charactNodeRef, QName charactType, ProductData productData) {
		// TODO make more generic use an annotation instead
		if (charactType.equals(PLMModel.TYPE_COST)) {
			return getCharactValue(charactNodeRef, productData.getCostList());
		} else if (charactType.equals(PLMModel.TYPE_NUT)) {
			return getCharactValue(charactNodeRef, productData.getNutList());
		} else if (charactType.equals(PLMModel.TYPE_ING)) {
			return getCharactValue(charactNodeRef, productData.getIngList());
		}
		return null;
	}

	public static Double getCharactValue(NodeRef charactNodeRef, List<? extends SimpleCharactDataItem> charactList) {
	
		if (charactList != null && charactNodeRef != null) {
			for (SimpleCharactDataItem charactDataListItem : charactList) {
				if (charactNodeRef.equals(charactDataListItem.getCharactNodeRef())) {
					return charactDataListItem.getValue();
				}
			}
		}
		return null;
	}

}
