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
package fr.becpg.config.mapping;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.namespace.QName;


/**
 * Class that represent the mapping for importing a property or an association of a node
 * 
 * <column id="modifier" attribute="cm:modifier" />
 * <column id="suppliers" attribute="bcpg:supplierAssoc" />.
 *
 * @author querephi
 */
public class AttributeMapping extends AbstractAttributeMapping {

	private QName targetClass;
	
	public QName getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(QName targetClass) {
		this.targetClass = targetClass;
	}

	public AttributeMapping(String id, ClassAttributeDefinition attribute) {
		super(id, attribute);
	}

	@Override
	public String toString() {
		return "AttributeMapping [targetClass=" + targetClass + ", getId()=" + getId() + ", getAttribute()="
				+ getAttribute() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

	
}
