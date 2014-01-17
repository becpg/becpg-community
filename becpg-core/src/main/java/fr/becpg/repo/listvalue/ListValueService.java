/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface ListValueService.
 *
 * @author querephi
 */
public interface ListValueService {

	/** Prop Constants **/
	

	public static final int SUGGEST_PAGE_SIZE = 10;
	
	public static final String PROP_SOURCE_TYPE = "sourcetype";
	
	public static final String PROP_CLASS_NAME = "className";
	
	public static final String PROP_PATH = "path";
	
	public static final String PROP_PARENT = "parent";
	
	public static final String PROP_NODEREF = "nodeRef";
	
	public static final String PROP_LOCALE = "locale";

	public static final String PROP_CLASS_NAMES = "classNames";
	
	public static final String PROP_EXCLUDE_CLASS_NAMES = "excludeClassNames";

	public static final String PROP_PRODUCT_TYPE = "productType";

	public static final String EXTRA_PARAM = "extra";
	
	
	
	/**
	 * 
	 * @param sourceType
	 * @param query
	 * @param pageNum
	 * @param pageSize 
	 * @param props
	 * @return
	 */
	public ListValuePage suggestBySourceType(String sourceType, String query, Integer pageNum,
			Integer pageSize, Map<String, Serializable> props);
}
