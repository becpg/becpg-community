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
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;

/**
 * <p>LabelListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pack:labelingList")
public class LabelListDataItem extends AbstractManualDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3124185073682776997L;
	private NodeRef label;
	private String type;
	private String position;
	
	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="pack:llLabel")
	@DataListIdentifierAttr
	public NodeRef getLabel() {
		return label;
	}
	/**
	 * <p>Setter for the field <code>label</code>.</p>
	 *
	 * @param label a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setLabel(NodeRef label) {
		this.label = label;
	}
	
	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="pack:llType")
	public String getType() {
		return type;
	}
	/**
	 * <p>Setter for the field <code>type</code>.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * <p>Getter for the field <code>position</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="pack:llPosition")
	public String getPosition() {
		return position;
	}
	/**
	 * <p>Setter for the field <code>position</code>.</p>
	 *
	 * @param position a {@link java.lang.String} object.
	 */
	public void setPosition(String position) {
		this.position = position;
	}
	
	/**
	 * <p>Constructor for LabelListDataItem.</p>
	 */
	public LabelListDataItem(){
		super();
	}
	
	/**
	 * <p>Constructor for LabelListDataItem.</p>
	 *
	 * @param label a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param type a {@link java.lang.String} object.
	 * @param position a {@link java.lang.String} object.
	 */
	public LabelListDataItem(NodeRef label, String type, String  position) {
		super();
		this.label = label;
		this.type = type;
		this.position = position;
	}
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LabelListDataItem other = (LabelListDataItem) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LabelListDataItem [label=" + label + ", type=" + type + ", position=" + position + "]";
	}
	
}
