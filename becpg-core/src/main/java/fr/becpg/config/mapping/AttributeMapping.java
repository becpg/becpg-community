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
package fr.becpg.config.mapping;

import java.util.Objects;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.namespace.QName;


/**
 * Class that represent the mapping for importing a property or an association of a node
 *
 * <column id="modifier" attribute="cm:modifier" />
 * <column id="suppliers" attribute="bcpg:supplierAssoc" />.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class AttributeMapping extends AbstractAttributeMapping {

	private QName targetClass;
	private boolean isMLText = false;
	
	/**
	 * <p>Getter for the field <code>targetClass</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getTargetClass() {
		return targetClass;
	}

	/**
	 * <p>Setter for the field <code>targetClass</code>.</p>
	 *
	 * @param targetClass a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setTargetClass(QName targetClass) {
		this.targetClass = targetClass;
	}

	
	/**
	 * <p>isMLText.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isMLText() {
		return isMLText;
	}

	/**
	 * <p>setMLText.</p>
	 *
	 * @param isMLText a boolean.
	 */
	public void setMLText(boolean isMLText) {
		this.isMLText = isMLText;
	}

	/**
	 * <p>Constructor for AttributeMapping.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 * @param attribute a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object.
	 */
	public AttributeMapping(String id, ClassAttributeDefinition attribute) {
		super(id, attribute);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "AttributeMapping [targetClass=" + targetClass + ", isMLText=" + isMLText + ", id=" + id + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(isMLText, targetClass);
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
		AttributeMapping other = (AttributeMapping) obj;
		return isMLText == other.isMLText && Objects.equals(targetClass, other.targetClass);
	}

	

	
}
