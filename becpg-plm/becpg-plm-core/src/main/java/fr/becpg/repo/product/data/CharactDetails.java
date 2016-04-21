/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
package fr.becpg.repo.product.data;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Contains charact details
 *
 * @author matthieu
 *
 */
public class CharactDetails {

	List<NodeRef> computedCharacts = null;
	
	final Map<NodeRef, List<CharactDetailsValue>> data = new LinkedHashMap<>();

	public CharactDetails(List<NodeRef> computedCharacts ) {
		super();
		this.computedCharacts = computedCharacts;
	}
	
	public void addKeyValue(NodeRef charactNodeRef, CharactDetailsValue value) {
		List<CharactDetailsValue> tmp = data.get(charactNodeRef);
		if(tmp == null){
			tmp = new LinkedList<>();
		}
		boolean match = false;
		for(CharactDetailsValue existingValue : tmp){
			if(existingValue.equals(value)){
				existingValue.add(value.getValue());
				match = true;
				break;
			}
			
		}
		
		if(!match){
			tmp.add(value);
		}
		
		data.put(charactNodeRef, tmp);
	}

	public boolean hasElement(NodeRef charactNodeRef) {
		return computedCharacts==null || computedCharacts.isEmpty() || computedCharacts.contains(charactNodeRef);
	}

	public Map<NodeRef, List<CharactDetailsValue>> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "CharactDetails [ data=" + data + "]";
	}

}
