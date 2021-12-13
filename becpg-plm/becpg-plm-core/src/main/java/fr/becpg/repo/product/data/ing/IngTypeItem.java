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
package fr.becpg.repo.product.data.ing;

import java.util.Locale;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

/**
 * <p>IngTypeItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:ingTypeItem")
@AlfCacheable
public class IngTypeItem extends LabelingComponent{

	private static final long serialVersionUID = 182156222574786727L;

    /** Constant <code>DEFAULT_GROUP</code> */
    public static final IngTypeItem DEFAULT_GROUP = new IngTypeItem();

	private static final String LAST_GROUP = "LastGroup";
	
	private Double decThreshold;
	
	private String lvValue;

	private String lvCode;
	
	private Boolean isDoNotDeclare;
	
	private Boolean isLastGroup;
		
	private NodeRef origNodeRef;
	
	
	/**
	 * <p>Constructor for IngTypeItem.</p>
	 */
	public IngTypeItem(){
		super();
	}

	/**
	 * <p>Constructor for IngTypeItem.</p>
	 *
	 * @param ingTypeItem a {@link fr.becpg.repo.product.data.ing.IngTypeItem} object.
	 */
	public IngTypeItem(IngTypeItem ingTypeItem) {
		super(ingTypeItem);
		this.decThreshold = ingTypeItem.decThreshold;
		this.lvValue = ingTypeItem.lvValue;
		this.lvCode = ingTypeItem.lvCode;
		this.isDoNotDeclare = ingTypeItem.isDoNotDeclare;
		this.isLastGroup = ingTypeItem.isLastGroup;
	}

	
	/**
	 * <p>Getter for the field <code>isDoNotDeclare</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingTypeDoNotDeclare")
	public Boolean getIsDoNotDeclare() {
		return isDoNotDeclare;
	}
	


	/**
	 * <p>doNotDeclare.</p>
	 *
	 * @return a boolean.
	 */
	public boolean doNotDeclare() {
		return Boolean.TRUE.equals(this.isDoNotDeclare);
	}

	/**
	 * <p>Setter for the field <code>isDoNotDeclare</code>.</p>
	 *
	 * @param doNotDeclare a {@link java.lang.Boolean} object.
	 */
	public void setIsDoNotDeclare(Boolean doNotDeclare) {
		this.isDoNotDeclare = doNotDeclare;
	}


	/**
	 * <p>Getter for the field <code>origNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getOrigNodeRef() {
		return origNodeRef;
	}

	/**
	 * <p>Setter for the field <code>origNodeRef</code>.</p>
	 *
	 * @param origNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setOrigNodeRef(NodeRef origNodeRef) {
		this.origNodeRef = origNodeRef;
	}
	
	/**
	 * <p>Getter for the field <code>isLastGroup</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingTypeIsLastGroup")
	public Boolean getIsLastGroup() {
		return isLastGroup;
	}
	
	
	/**
	 * <p>lastGroup.</p>
	 *
	 * @return a boolean.
	 */
	public boolean lastGroup() {
		return Boolean.TRUE.equals(isLastGroup) || LAST_GROUP.equals(lvValue);
	}

	/**
	 * <p>Setter for the field <code>isLastGroup</code>.</p>
	 *
	 * @param isLastGroup a {@link java.lang.Boolean} object.
	 */
	public void setIsLastGroup(Boolean isLastGroup) {
		this.isLastGroup = isLastGroup;
	}

	/**
	 * <p>Getter for the field <code>lvCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:lvCode")
	public String getLvCode() {
		return lvCode;
	}

	/**
	 * <p>Setter for the field <code>lvCode</code>.</p>
	 *
	 * @param lvCode a {@link java.lang.String} object.
	 */
	public void setLvCode(String lvCode) {
		this.lvCode = lvCode;
	}

	/**
	 * <p>Getter for the field <code>lvValue</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:lvValue")
	public String getLvValue() {
		return lvValue;
	}

	/**
	 * <p>Setter for the field <code>lvValue</code>.</p>
	 *
	 * @param lvValue a {@link java.lang.String} object.
	 */
	public void setLvValue(String lvValue) {
		this.lvValue = lvValue;
	}
	
	/**
	 * <p>Getter for the field <code>decThreshold</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingTypeDecThreshold")
	public Double getDecThreshold() {
		return decThreshold;
	}


	/**
	 * <p>Setter for the field <code>decThreshold</code>.</p>
	 *
	 * @param decThreshold a {@link java.lang.Double} object.
	 */
	public void setDecThreshold(Double decThreshold) {
		this.decThreshold = decThreshold;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getLegalName(Locale locale) {
		String ret = MLTextHelper.getClosestValue(legalName, locale);

		if(ret==null || ret.isEmpty()){
			return lvValue;
		}
		
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public IngTypeItem createCopy() {
		return new IngTypeItem(this);
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "IngTypeItem [decThreshold=" + decThreshold + ", lvValue=" + lvValue + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(decThreshold, isDoNotDeclare, isLastGroup, lvCode, lvValue, origNodeRef);
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
		IngTypeItem other = (IngTypeItem) obj;
		return Objects.equals(decThreshold, other.decThreshold) && Objects.equals(isDoNotDeclare, other.isDoNotDeclare)
				&& Objects.equals(isLastGroup, other.isLastGroup) && Objects.equals(lvCode, other.lvCode) && Objects.equals(lvValue, other.lvValue)
				&& Objects.equals(origNodeRef, other.origNodeRef);
	}


}
