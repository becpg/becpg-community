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
package fr.becpg.repo.quality.data.dataList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "qa:controlDefList")
public class ControlDefListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2821254496316637207L;
	String type;
	Double mini;
	Double maxi;
	Boolean required;
	NodeRef method;
	List<NodeRef> characts = new ArrayList<>();
	
	@AlfProp
	@AlfQname(qname = "qa:clType")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@AlfProp
	@AlfQname(qname = "qa:clMini")
	public Double getMini() {
		return mini;
	}

	public void setMini(Double mini) {
		this.mini = mini;
	}

	@AlfProp
	@AlfQname(qname = "qa:clMaxi")
	public Double getMaxi() {
		return maxi;
	}

	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	@AlfProp
	@AlfQname(qname = "qa:clRequired")
	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "qa:clMethod")
	public NodeRef getMethod() {
		return method;
	}

	public void setMethod(NodeRef method) {
		this.method = method;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "qa:clCharacts")
	public List<NodeRef> getCharacts() {
		return characts;
	}

	public void setCharacts(List<NodeRef> characts) {
		this.characts = characts;
	}
	
	
	public ControlDefListDataItem(NodeRef nodeRef, String type, Double mini, Double maxi, Boolean required, NodeRef method, List<NodeRef> characts){
		setNodeRef(nodeRef);
		setType(type);
		setMini(mini);
		setMaxi(maxi);
		setRequired(required);
		setMethod(method);
		setCharacts(characts);
	}

	public ControlDefListDataItem() {
		super();
	}

	public ControlDefListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((characts == null) ? 0 : characts.hashCode());
		result = prime * result + ((maxi == null) ? 0 : maxi.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((mini == null) ? 0 : mini.hashCode());
		result = prime * result + ((required == null) ? 0 : required.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ControlDefListDataItem other = (ControlDefListDataItem) obj;
		if (characts == null) {
			if (other.characts != null)
				return false;
		} else if (!characts.equals(other.characts))
			return false;
		if (maxi == null) {
			if (other.maxi != null)
				return false;
		} else if (!maxi.equals(other.maxi))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (mini == null) {
			if (other.mini != null)
				return false;
		} else if (!mini.equals(other.mini))
			return false;
		if (required == null) {
			if (other.required != null)
				return false;
		} else if (!required.equals(other.required))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ControlDefListDataItem [type=" + type + ", mini=" + mini + ", maxi=" + maxi + ", required=" + required + ", method=" + method + ", characts=" + characts + "]";
	}
	
	
	
	
}
