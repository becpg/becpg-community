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
 * @version $Id: $Id
 */
public class CharactDetails {

	List<NodeRef> computedCharacts = null;
	
	final Map<NodeRef, List<CharactDetailsValue>> data = new LinkedHashMap<>();
	
	private String totalOperation = "SUM";

	/**
	 * <p>Constructor for CharactDetails.</p>
	 *
	 * @param computedCharacts a {@link java.util.List} object.
	 */
	public CharactDetails(List<NodeRef> computedCharacts ) {
		super();
		this.computedCharacts = computedCharacts;
	}
	
	/**
	 * <p>Setter for the field <code>totalOperation</code>.</p>
	 *
	 * @param totalOperation a {@link java.lang.String} object
	 */
	public void setTotalOperation(String totalOperation) {
		this.totalOperation = totalOperation;
	}
	
	/**
	 * <p>Getter for the field <code>totalOperation</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getTotalOperation() {
		return totalOperation;
	}
	
	/**
	 * <p>addKeyValue.</p>
	 *
	 * @param charactNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param value a {@link fr.becpg.repo.product.data.CharactDetailsValue} object.
	 */
	public void addKeyValue(NodeRef charactNodeRef, CharactDetailsValue value) {
		List<CharactDetailsValue> tmp = data.get(charactNodeRef);
		if(tmp == null){
			tmp = new LinkedList<>();
		}
		boolean match = false;
		for(CharactDetailsValue existingValue : tmp){
			if(existingValue.keyEquals(value)){
				existingValue.add(value);
				match = true;
				break;
			}
			
		}
		
		if(!match){
			tmp.add(value);
		}
		
		data.put(charactNodeRef, tmp);
	}

	/**
	 * <p>isMultiple.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isMultiple() {
		return computedCharacts!=null && computedCharacts.size()>1; 
	}
	
	/**
	 * <p>hasElement.</p>
	 *
	 * @param charactNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	public boolean hasElement(NodeRef charactNodeRef) {
		return computedCharacts==null || computedCharacts.isEmpty() || computedCharacts.contains(charactNodeRef);
	}

	/**
	 * <p>Getter for the field <code>data</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<NodeRef, List<CharactDetailsValue>> getData() {
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CharactDetails [ data=" + data + "]";
	}

}
