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
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.Map;

/**
 * The Interface ListValueService.
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface ListValueService {

	/** Constant <code>SUGGEST_PAGE_SIZE=10</code> */
	int SUGGEST_PAGE_SIZE = 10;
	
	/** Constant <code>PROP_SOURCE_TYPE="sourcetype"</code> */
	String PROP_SOURCE_TYPE = "sourcetype";
	
	/** Constant <code>PROP_CLASS_NAME="className"</code> */
	String PROP_CLASS_NAME = "className";
	
	/** Constant <code>PROP_ATTRIBUTE_NAME="attributeName"</code> */
	String PROP_ATTRIBUTE_NAME = "attributeName";
	
	/** Constant <code>PROP_PATH="path"</code> */
	String PROP_PATH = "path";
	
	/** Constant <code>PROP_PARENT="parent"</code> */
	String PROP_PARENT = "parent";
	
	/** Constant <code>PROP_NODEREF="nodeRef"</code> */
	String PROP_NODEREF = "nodeRef";
	
	/** Constant <code>PROP_ENTITYNODEREF="entityNodeRef"</code> */
	String PROP_ENTITYNODEREF = "entityNodeRef";
	
	/** Constant <code>PROP_LOCALE="locale"</code> */
	String PROP_LOCALE = "locale";

	/** Constant <code>PROP_CLASS_NAMES="classNames"</code> */
	String PROP_CLASS_NAMES = "classNames";
	
	/** Constant <code>PROP_EXCLUDE_CLASS_NAMES="excludeClassNames"</code> */
	String PROP_EXCLUDE_CLASS_NAMES = "excludeClassNames";

	/** Constant <code>PROP_PRODUCT_TYPE="productType"</code> */
	String PROP_PRODUCT_TYPE = "productType";

	/** Constant <code>EXTRA_PARAM="extra"</code> */
	String EXTRA_PARAM = "extra";

	/** Constant <code>PROP_FILTER="filter"</code> */
	String PROP_FILTER = "filter";

	/** Constant <code>PROP_EXCLUDE_PROPS="excludeProps"</code> */
	String PROP_EXCLUDE_PROPS = "excludeProps";

	String PROP_AND_PROPS = "andProps";
	
	
	/**
	 * <p>suggestBySourceType.</p>
	 *
	 * @param sourceType a {@link java.lang.String} object.
	 * @param query a {@link java.lang.String} object.
	 * @param pageNum a {@link java.lang.Integer} object.
	 * @param pageSize a {@link java.lang.Integer} object.
	 * @param props a {@link java.util.Map} object.
	 * @return a {@link fr.becpg.repo.listvalue.ListValuePage} object.
	 */
	ListValuePage suggestBySourceType(String sourceType, String query, Integer pageNum,
									  Integer pageSize, Map<String, Serializable> props);
}
