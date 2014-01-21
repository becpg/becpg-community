/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;

public class SpelEditorListValuePlugin extends EntityListValuePlugin {

	/** The logger. */
	private static Log logger = LogFactory.getLog(SpelEditorListValuePlugin.class);

	/** The Constant SOURCE_TYPE_TARGET_ASSOC. */
	private static final String SOURCE_TYPE_SPELEDITOR = "speleditor";

	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_SPELEDITOR };
	}

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {

		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);

		if (className != null && className.length() > 0 && className.contains(PARAM_VALUES_SEPARATOR)) {
			String[] arrClassNames = className != null ? className.split(PARAM_VALUES_SEPARATOR) : null;
			return suggestTargetAssoc(BeCPGModel.TYPE_PRODUCT, query, pageNum, pageSize, arrClassNames, props);
		}
		// Class is a Java class
		try {
			Class<?> c = Class.forName(className);
			Field[] fields = c.getDeclaredFields();
			List<ListValueEntry> ret = new ArrayList<ListValueEntry>();
			for (int i = 0; i < fields.length; i++) {
				ret.add(new ListValueEntry(fields[i].getName(), fields[i].getName(), fields[i].getType()
						.getSimpleName()));
			}
			return new ListValuePage(ret, pageNum, pageSize, null);

		} catch (ClassNotFoundException e) {
			logger.debug(e, e);
		}

		QName type = QName.createQName(className, namespaceService);

		if (type.equals(BeCPGModel.TYPE_DYNAMICCHARACTLIST)) {

			return suggestDatalistItem(new NodeRef((String) props.get(ListValueService.PROP_NODEREF)), type,
					BeCPGModel.PROP_DYNAMICCHARACT_TITLE, query, pageNum, pageSize);
		}

		return suggestTargetAssoc(type, query, pageNum, pageSize, null, props);
	}
}
