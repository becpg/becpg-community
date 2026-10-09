/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
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
package fr.becpg.repo.entity.remote.extractor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * <p>RemoteHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class RemoteHelper {

	/** Constant <code>ISO_DAY_FORMATTER</code> */
	private static final DateTimeFormatter ISO_DAY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	/** Constant <code>ISO_DAY_PATTERN</code> */
	private static final Pattern ISO_DAY_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    /**
     * <p>Constructor for RemoteHelper.</p>
     */
    private RemoteHelper() {
    	//DO Nothing
    }
	
	
	/**
	 * <p>getPropName.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param dictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public static QName getPropName(QName type, EntityDictionaryService dictionaryService) {
		if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_LINKED_VALUE)){
			return BeCPGModel.PROP_LKV_VALUE;
		} else if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_CHARACT)){
		    return BeCPGModel.PROP_CHARACT_NAME;
		} else if(ContentModel.TYPE_PERSON.equals(type)){
			return ContentModel.PROP_USERNAME;
		} else if(ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)){
			return ContentModel.PROP_AUTHORITY_NAME;
		}
		return ContentModel.PROP_NAME;
	}

	/**
	 * <p>isJSONValue.</p>
	 *
	 * @param propType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	public static boolean isJSONValue(QName propType) {
		return BeCPGModel.PROP_ENTITY_SCORE.equals(propType) || BeCPGModel.PROP_ACTIVITYLIST_DATA.equals(propType);
	}

	/**
	 * <p>States whether a property denotes a calendar day rather than an instant.</p>
	 *
	 * @param propType a {@link org.alfresco.service.namespace.QName} object.
	 * @param dictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 * @return a boolean.
	 */
	public static boolean isDayProperty(QName propType, EntityDictionaryService dictionaryService) {
		PropertyDefinition propertyDefinition = dictionaryService.getProperty(propType);
		return (propertyDefinition != null) && DataTypeDefinition.DATE.equals(propertyDefinition.getDataType().getName());
	}

	/**
	 * Renders a <code>d:date</code> as the calendar day the repository displays.
	 * <p>
	 * The repository holds a day as an instant and whoever writes it picks a time zone.
	 * Publishing that instant lets every reader re-interpret it in its own zone, which moves the
	 * day by one on a server ahead of UTC. A plain day carries no zone, so it cannot move.
	 *
	 * @param value a {@link java.util.Date} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String formatDay(Date value) {
		return ISO_DAY_FORMATTER.format(value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	}

	/**
	 * Reads back a calendar day, anchored at midnight in the time zone of this server.
	 * <p>
	 * Anything else yields an empty result, so that the ISO instants written by earlier versions
	 * keep going through the standard Alfresco conversion.
	 *
	 * @param value a {@link java.lang.String} object.
	 * @return a {@link java.util.Optional} object.
	 */
	public static Optional<Date> parseDay(String value) {
		if ((value == null) || !ISO_DAY_PATTERN.matcher(value).matches()) {
			return Optional.empty();
		}

		return Optional.of(Date.from(LocalDate.parse(value, ISO_DAY_FORMATTER).atStartOfDay(ZoneId.systemDefault()).toInstant()));
	}

}
