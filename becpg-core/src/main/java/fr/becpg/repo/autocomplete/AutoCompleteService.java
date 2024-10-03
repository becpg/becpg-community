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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.Map;

/**
 * The Interface AutoCompleteService.
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface AutoCompleteService {

	/** Constant <code>SUGGEST_PAGE_SIZE=10</code> */
	final int SUGGEST_PAGE_SIZE = 10;
	
	/** Constant <code>PROP_SOURCE_TYPE="sourcetype"</code> */
	final String PROP_SOURCE_TYPE = "sourcetype";
	
	/** Constant <code>PROP_CLASS_NAME="className"</code> */
	final String PROP_CLASS_NAME = "className";
	
	/** Constant <code>PROP_ATTRIBUTE_NAME="attributeName"</code> */
	final String PROP_ATTRIBUTE_NAME = "attributeName";
	
	/** Constant <code>PROP_PATH="path"</code> */
	final String PROP_PATH = "path";
	
	/** Constant <code>PROP_PARENT="parent"</code> */
	final String PROP_PARENT = "parent";
	
	/** Constant <code>PROP_NODEREF="nodeRef"</code> */
	final String PROP_NODEREF = "nodeRef";
	
	/** Constant <code>PROP_ENTITYNODEREF="entityNodeRef"</code> */
	final String PROP_ENTITYNODEREF = "entityNodeRef";
	
	/** Constant <code>PROP_LOCALE="locale"</code> */
	final String PROP_LOCALE = "locale";

	/** Constant <code>PROP_CLASS_NAMES="classNames"</code> */
	final String PROP_CLASS_NAMES = "classNames";
	
	/** Constant <code>PROP_EXCLUDE_CLASS_NAMES="excludeClassNames"</code> */
	final String PROP_EXCLUDE_CLASS_NAMES = "excludeClassNames";

	/** Constant <code>PROP_PRODUCT_TYPE="productType"</code> */
	final String PROP_PRODUCT_TYPE = "productType";
	
	/** Constant <code>PROP_INCLUDE_DELETED="includeDeleted"</code> */
	final String PROP_INCLUDE_DELETED = "includeDeleted";
	
	/** Constant <code>EXTRA_PARAM="extra"</code> */
	final String EXTRA_PARAM = "extra";

	/** Constant <code>PROP_FILTER="filter"</code> */
	final String PROP_FILTER = "filter";

	/** Constant <code>PROP_EXCLUDE_PROPS="excludeProps"</code> */
	final String PROP_EXCLUDE_PROPS = "excludeProps";
	
	/** Constant <code>PROP_EXCLUDE_SOURCES="excludeSources"</code> */
	final String PROP_EXCLUDE_SOURCES = "excludeSources";
	
	/** Constant <code>PROP_ITEM_ID="itemId"</code> */
	final String PROP_ITEM_ID = "itemId";
	
	/** Constant <code>PROP_FIELD_NAME="fieldName"</code> */
	final String PROP_FIELD_NAME = "fieldName";

	/** Constant <code>PROP_AND_PROPS="andProps"</code> */
	final String PROP_AND_PROPS = "andProps";

	/** Constant <code>EXTRA_PARAM_ITEMID="itemId"</code> */
	final String EXTRA_PARAM_ITEMID = "itemId";
	
	/** Constant <code>EXTRA_PARAM_DESTINATION="destination"</code> */
	final String EXTRA_PARAM_DESTINATION = "destination";

	/** Constant <code>EXTRA_PARAM_LIST="list"</code> */
	final String EXTRA_PARAM_LIST = "list";

	/** Constant <code>EXTRA_PARAM_PATHS="paths"</code> */
	final String EXTRA_PARAM_PATHS = "paths";

	/** Constant <code>EXTRA_PARAM_DEPTH_LEVEL="depthLevel"</code> */
	final String EXTRA_PARAM_DEPTH_LEVEL = "depthLevel";

	/** Constant <code>EXTRA_PARAM_TASKID="taskId"</code> */
	final String EXTRA_PARAM_TASKID = "taskId";
	
	/** Constant <code>EXTRA_PARAM_SEARCH_TEMPLATE="searchTemplate"</code> */
	final String EXTRA_PARAM_SEARCH_TEMPLATE = "searchTemplate";

	
	
	/**
	 * <p>suggestBySourceType.</p>
	 *
	 * @param sourceType a {@link java.lang.String} object.
	 * @param query a {@link java.lang.String} object.
	 * @param pageNum a {@link java.lang.Integer} object.
	 * @param pageSize a {@link java.lang.Integer} object.
	 * @param props a {@link java.util.Map} object.
	 * @return a {@link fr.becpg.repo.autocomplete.AutoCompletePage} object.
	 */
	AutoCompletePage suggestBySourceType(String sourceType, String query, Integer pageNum,
									  Integer pageSize, Map<String, Serializable> props);
}
