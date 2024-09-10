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
package fr.becpg.repo.entity;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;

/**
 * <p>EntityDictionaryService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityDictionaryService extends DictionaryService {

	/**
	 * <p>getDefaultPivotAssoc.</p>
	 *
	 * @param dataListItemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getDefaultPivotAssoc(QName dataListItemType);
	
	/**
	 * <p>isMultiLevelDataList.</p>
	 *
	 * @param dataListItemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isMultiLevelDataList(QName dataListItemType);
	
	/**
	 * <p>isMultiLevelLeaf.</p>
	 *
	 * @param entityType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isMultiLevelLeaf(QName entityType);

	/**
	 * <p>getPivotAssocDefs.</p>
	 *
	 * @param sourceType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.List} object.
	 */
	List<AssociationDefinition> getPivotAssocDefs(QName sourceType);


	/**
	 * <p>getPivotAssocDefs.</p>
	 *
	 * @param sourceType a {@link org.alfresco.service.namespace.QName} object.
	 * @param exactMatch a {@link java.lang.Boolean} object.
	 * @return a {@link java.util.List} object.
	 */
	List<AssociationDefinition> getPivotAssocDefs(QName sourceType, boolean exactMatch);
	
	
	/**
	 * <p>getTargetType.</p>
	 *
	 * @param createQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getTargetType(QName createQName);
	
	/**
	 * <p>getSubTypes.</p>
	 *
	 * @param typeQname a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<QName> getSubTypes(QName typeQname);

	/**
	 * <p>getPropDef.</p>
	 *
	 * @param fieldQname a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object.
	 */
	ClassAttributeDefinition getPropDef(QName fieldQname);

	/**
	 * <p>findMatchingPropDef.</p>
	 *
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param newItemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param fieldQname a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object.
	 */
	ClassAttributeDefinition findMatchingPropDef(QName itemType, QName newItemType, QName fieldQname);
	
	/**
	 * <p>isSubClass.</p>
	 *
	 * @param subClass a {@link org.alfresco.service.namespace.QName} object.
	 * @param subClassOf a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isSubClass(QName subClass, QName subClassOf);

	/**
	 * <p>getMultiLevelSecondaryPivot.</p>
	 *
	 * @param dataListItemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getMultiLevelSecondaryPivot(QName dataListItemType);
	
	
	/**
	 * <p>getMultiLevelGroupProperty.</p>
	 *
	 * @param dataListItemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getMultiLevelGroupProperty(QName dataListItemType);

	/**
	 * <p>registerPropDefMapping.</p>
	 *
	 * @param orig a {@link org.alfresco.service.namespace.QName} object.
	 * @param dest a {@link org.alfresco.service.namespace.QName} object.
	 */
	void registerPropDefMapping(QName orig, QName dest);
	

	/**
	 * <p>isAssoc.</p>
	 *
	 * @param propQname a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isAssoc(QName propQname);

	String toPrefixString(QName propertyQName);

	void registerExtraAssocsDefMapping(QName orig, QName dest);

	String getTitle(ClassAttributeDefinition attributeDefinition, QName nodeType);
	
	String getDescription(ClassAttributeDefinition attributeDefinition, QName nodeType);
	

}
