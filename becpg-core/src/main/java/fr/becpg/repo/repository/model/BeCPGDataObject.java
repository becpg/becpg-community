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
package fr.becpg.repo.repository.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.InternalField;

/**
 * <p>Abstract BeCPGDataObject class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public  abstract class  BeCPGDataObject  extends BaseObject implements RepositoryEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7452420206089817249L;

	protected NodeRef nodeRef;
	
	protected NodeRef parentNodeRef;
	
	protected String name;
	
	protected Set<QName> aspects = new HashSet<>();
	
	protected Set<QName> aspectsToRemove = new HashSet<>();

	protected Map<QName,Serializable> extraProperties = new HashMap<>();
	
	protected transient boolean isTransient = false;
	
	private transient long dbHashCode;
	
	/**
	 * <p>Getter for the field <code>dbHashCode</code>.</p>
	 *
	 * @return a long.
	 */
	@InternalField
	public long getDbHashCode() {
		return dbHashCode;
	}

	/** {@inheritDoc} */
	public void setDbHashCode(long dbHashCode) {
		this.dbHashCode = dbHashCode;
	}

	/**
	 * <p>Constructor for BeCPGDataObject.</p>
	 */
	protected BeCPGDataObject() {
		super();
	}

	/**
	 * <p>Constructor for BeCPGDataObject.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 */
	protected BeCPGDataObject(NodeRef nodeRef, String name) {
		super();
		this.nodeRef = nodeRef;
		this.name = name;
	}
	

	/**
	 * <p>Constructor for BeCPGDataObject.</p>
	 *
	 * @param beCPGDataObject a {@link fr.becpg.repo.repository.model.BeCPGDataObject} object.
	 */
	protected BeCPGDataObject(BeCPGDataObject beCPGDataObject) 
	{
	    this.nodeRef = beCPGDataObject.nodeRef;
	    this.parentNodeRef = beCPGDataObject.parentNodeRef;
	    this.name = beCPGDataObject.name;
	    this.aspects = beCPGDataObject.aspects;
	    this.extraProperties = beCPGDataObject.extraProperties;
	    this.isTransient = beCPGDataObject.isTransient;
	    this.dbHashCode = beCPGDataObject.dbHashCode;
	}

	/**
	 * <p>Getter for the field <code>nodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@InternalField
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/** {@inheritDoc} */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="cm:name")
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>Getter for the field <code>parentNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@InternalField
	public NodeRef getParentNodeRef() {
		return parentNodeRef;
	}

	/** {@inheritDoc} */
	public void setParentNodeRef(NodeRef parentNodeRef) {
		this.parentNodeRef = parentNodeRef;
	}

	/**
	 * <p>Getter for the field <code>aspects</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@InternalField
	public Set<QName> getAspects() {
		return aspects;
	}

	/**
	 * <p>Setter for the field <code>aspects</code>.</p>
	 *
	 * @param aspects a {@link java.util.Set} object.
	 */
	public void setAspects(Set<QName> aspects) {
		this.aspects = aspects;
	}

	/**
	 * <p>Getter for the field <code>aspectsToRemove</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@InternalField
	public Set<QName> getAspectsToRemove() {
		return aspectsToRemove;
	}

	/**
	 * <p>Setter for the field <code>aspectsToRemove</code>.</p>
	 *
	 * @param aspectsToRemove a {@link java.util.Set} object.
	 */
	public void setAspectsToRemove(Set<QName> aspectsToRemove) {
		this.aspectsToRemove = aspectsToRemove;
	}
	
	/**
	 * <p>Getter for the field <code>extraProperties</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	@InternalField
	public Map<QName, Serializable> getExtraProperties() {
		return extraProperties;
	}

	/** {@inheritDoc} */
	public void setExtraProperties(Map<QName, Serializable> extraProperties) {
		this.extraProperties = extraProperties;
	}

	/**
	 * <p>isTransient.</p>
	 *
	 * @return a boolean.
	 */
	@InternalField
	public boolean isTransient() {
		return isTransient;
	}

	/**
	 * <p>setTransient.</p>
	 *
	 * @param isTransient a boolean.
	 */
	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(aspects, aspectsToRemove, extraProperties, name, nodeRef, parentNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeCPGDataObject other = (BeCPGDataObject) obj;
		return Objects.equals(aspects, other.aspects) && Objects.equals(aspectsToRemove, other.aspectsToRemove)
				&& Objects.equals(extraProperties, other.extraProperties) && Objects.equals(name, other.name)
				&& Objects.equals(nodeRef, other.nodeRef) && Objects.equals(parentNodeRef, other.parentNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "BeCPGDataObject [nodeRef=" + nodeRef + ", parentNodeRef=" + parentNodeRef + ", name=" + name + ", aspects=" + aspects
				+ ", aspectsToRemove=" + aspectsToRemove + ", extraProperties=" + extraProperties + "]";
	}


	
}
