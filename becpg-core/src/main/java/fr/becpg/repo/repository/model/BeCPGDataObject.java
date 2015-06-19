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
package fr.becpg.repo.repository.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;

public  abstract class  BeCPGDataObject  extends BaseObject implements RepositoryEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7452420206089817249L;

	protected NodeRef nodeRef;
	
	protected NodeRef parentNodeRef;
	
	protected String name;
	
	protected Set<QName> aspects = new HashSet<>();
	
	protected Map<QName,Serializable> extraProperties = new HashMap<>();
	
	protected transient boolean isTransient = false;
	
	private transient int dbHashCode;
	

	public int getDbHashCode() {
		return dbHashCode;
	}

	public void setDbHashCode(int dbHashCode) {
		this.dbHashCode = dbHashCode;
	}

	public BeCPGDataObject() {
		super();
	}

	public BeCPGDataObject(NodeRef nodeRef, String name) {
		super();
		this.nodeRef = nodeRef;
		this.name = name;
	}
	

	public BeCPGDataObject(BeCPGDataObject beCPGDataObject) 
	{
	    this.nodeRef = beCPGDataObject.nodeRef;
	    this.parentNodeRef = beCPGDataObject.parentNodeRef;
	    this.name = beCPGDataObject.name;
	    this.aspects = beCPGDataObject.aspects;
	    this.extraProperties = beCPGDataObject.extraProperties;
	    this.isTransient = beCPGDataObject.isTransient;
	    this.dbHashCode = beCPGDataObject.dbHashCode;
	}

	
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	@AlfProp
	@AlfQname(qname="cm:name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NodeRef getParentNodeRef() {
		return parentNodeRef;
	}

	public void setParentNodeRef(NodeRef parentNodeRef) {
		this.parentNodeRef = parentNodeRef;
	}

	
	public Set<QName> getAspects() {
		return aspects;
	}

	public void setAspects(Set<QName> aspects) {
		this.aspects = aspects;
	}

	public Map<QName, Serializable> getExtraProperties() {
		return extraProperties;
	}

	public void setExtraProperties(Map<QName, Serializable> extraProperties) {
		this.extraProperties = extraProperties;
	}

	public boolean isTransient() {
		return isTransient;
	}

	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aspects == null) ? 0 : aspects.hashCode());
		result = prime * result + ((extraProperties == null) ? 0 : extraProperties.hashCode());
		result = prime * result + (isTransient ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((parentNodeRef == null) ? 0 : parentNodeRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeCPGDataObject other = (BeCPGDataObject) obj;
		if (aspects == null) {
			if (other.aspects != null)
				return false;
		} else if (!aspects.equals(other.aspects))
			return false;
		if (extraProperties == null) {
			if (other.extraProperties != null)
				return false;
		} else if (!extraProperties.equals(other.extraProperties))
			return false;
		if (isTransient != other.isTransient)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (parentNodeRef == null) {
			if (other.parentNodeRef != null)
				return false;
		} else if (!parentNodeRef.equals(other.parentNodeRef))
			return false;
		return true;
	}


	
	

	
}
