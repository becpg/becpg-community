/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
 */
public interface ListValueService {

	int SUGGEST_PAGE_SIZE = 10;
	
	String PROP_SOURCE_TYPE = "sourcetype";
	
	String PROP_CLASS_NAME = "className";
	
	String PROP_ATTRIBUTE_NAME = "attributeName";
	
	String PROP_PATH = "path";
	
	String PROP_PARENT = "parent";
	
	String PROP_NODEREF = "nodeRef";
	
	String PROP_LOCALE = "locale";

	String PROP_CLASS_NAMES = "classNames";
	
	String PROP_EXCLUDE_CLASS_NAMES = "excludeClassNames";

	String PROP_PRODUCT_TYPE = "productType";

	String EXTRA_PARAM = "extra";

	String PROP_FILTER = "filter";

	String PROP_EXCLUDE_PROPS = "excludeProps";
	
	
	ListValuePage suggestBySourceType(String sourceType, String query, Integer pageNum,
									  Integer pageSize, Map<String, Serializable> props);
}
