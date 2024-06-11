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
package fr.becpg.repo.quality.data.dataList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

/**
 * <p>ControlDefListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "qa:controlDefList")
public class ControlDefListDataItem extends ControlListDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2821254496316637207L;

	
	/**
	 * <p>Constructor for ControlDefListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param type a {@link java.lang.String} object.
	 * @param mini a {@link java.lang.Double} object.
	 * @param maxi a {@link java.lang.Double} object.
	 * @param required a {@link java.lang.Boolean} object.
	 * @param method a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param characts a {@link java.util.List} object.
	 */
	public ControlDefListDataItem(NodeRef nodeRef, String type, Double mini, Double maxi, Boolean required, NodeRef method, List<NodeRef> characts){
		setNodeRef(nodeRef);
		setType(type);
		setMini(mini);
		setMaxi(maxi);
		setRequired(required);
		setMethod(method);
		setCharacts(characts);
	}

	/**
	 * <p>Constructor for ControlDefListDataItem.</p>
	 */
	public ControlDefListDataItem() {
		super();
	}

	/**
	 * <p>Constructor for ControlDefListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public ControlDefListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ControlDefListDataItem [getType()=" + getType() + ", getMini()=" + getMini() + ", getMaxi()=" + getMaxi() + ", getRequired()="
				+ getRequired() + ", getSampleId()=" + getSampleId() + ", getValue()=" + getValue() + ", getTextCriteria()=" + getTextCriteria()
				+ ", getTarget()=" + getTarget() + ", getUnit()=" + getUnit() + ", getState()=" + getState() + ", getTemperature()="
				+ getTemperature() + ", getMethod()=" + getMethod() + ", getControlPoint()=" + getControlPoint() + ", getTimePeriod()="
				+ getTimePeriod() + "]";
	}
	
	
	
	
}
