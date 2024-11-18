/*
Copyright (C) 2010-2021 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.repo.search.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

import fr.becpg.repo.RepoConsts;

/**
 * <p>Abstract AbstractBeCPGQueryBuilder class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractBeCPGQueryBuilder {

	private static final String QUERY_COND_PROP_EQUAL_VALUE = "%s:\"%s\"";
	private static final String QUERY_COND_PROP_CONTAINS_VALUE = "%s:%s";
	private static final String QUERY_COND_PROP_ISNULL_VALUE = "ISNULL:\"%s\"";
	private static final String QUERY_COND_PROP_ISNULL_OR_ISUNSET_VALUE = "(ISNULL:\"%s\" OR ISUNSET:\"%s\")";
	
	private static final String QUERY_COND_PATH = "PATH:\"/app:company_home/%s/*\"";
	private static final String QUERY_SUB_PATH = "PATH:\"/app:company_home/%s//*\"";
	private static final String QUERY_COND_EXACT_PATH = "PATH:\"%s\"";
	private static final String QUERY_COND_MEMBERS = "PATH:\"/app:company_home/%s/member\"";
	private static final String QUERY_COND_ID = "ID:\"%s\"";

	private static final String QUERY_COND_PARENT = "PARENT:\"%s\"";
	private static final String QUERY_COND_TYPE = "TYPE:\"%s\"";
	private static final String QUERY_COND_SITE = "SITE:\"%s\"";
	private static final String QUERY_COND_EXACT_TYPE = "EXACTTYPE:\"%s\"";
	private static final String QUERY_COND_ASPECT = "ASPECT:\"%s\"";
	private static final String QUERY_COND = " %s %s";

	protected String language = SearchService.LANGUAGE_FTS_ALFRESCO;

	/**
	 * <p>getCondEqualValue.</p>
	 *
	 * @param property a {@link org.alfresco.service.namespace.QName} object.
	 * @param value a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondEqualValue(QName property, String value) {
		if (value == null || value.isEmpty()) {
			return getCondIsNullValue(property);
		} else {
			return String.format(QUERY_COND_PROP_EQUAL_VALUE, SearchService.LANGUAGE_LUCENE.equals(language) ? "@"+escapeQName(property) : property,
					value);
		}
	}

	/**
	 * <p>getCondEqualID.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondEqualID(NodeRef nodeRef) {

		return String.format(QUERY_COND_ID, nodeRef);
	}

	/**
	 * <p>getCondContainsValue.</p>
	 *
	 * @param property a {@link org.alfresco.service.namespace.QName} object.
	 * @param value a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondContainsValue(QName property, String value) {

		return String
				.format(QUERY_COND_PROP_CONTAINS_VALUE, SearchService.LANGUAGE_LUCENE.equals(language) ? "@"+escapeQName(property) : property, value);
	}

	/**
	 * <p>getCondIsNullValue.</p>
	 *
	 * @param property a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondIsNullValue(QName property) {
		return String.format(QUERY_COND_PROP_ISNULL_VALUE, property);
	}
	
	/**
	 * <p>getCondIsNullOrIsUnsetValue.</p>
	 *
	 * @param property a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondIsNullOrIsUnsetValue(QName property) {
		return String.format(QUERY_COND_PROP_ISNULL_OR_ISUNSET_VALUE, property, property);
	}
	
	/**
	 * <p>getCondExactPath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondExactPath(String path) {
		return String.format(QUERY_COND_EXACT_PATH,path);
	}

	/**
	 * <p>getCondPath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondPath(String path) {
		return String.format(QUERY_COND_PATH, encodePath(path));
	}
	
	/**
	 * <p>getCondSubPath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondSubPath(String path) {
		return String.format(QUERY_SUB_PATH, encodePath(path));
	}

	/**
	 * <p>getCondMembers.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondMembers(String path) {
		return String.format(QUERY_COND_MEMBERS, encodePath(path));
	}

	/**
	 * <p>getCondParent.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondParent(NodeRef nodeRef) {
		return String.format(QUERY_COND_PARENT, nodeRef);
	}

	/**
	 * <p>getCondType.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondType(QName type) {
		return String.format(QUERY_COND_TYPE, type);
	}
	
	/**
	 * <p>getCondSite.</p>
	 *
	 * @param siteId a {@link java.lang.String}  object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondSite(String siteId) {
		return String.format(QUERY_COND_SITE, siteId);
	}
	
	/**
	 * <p>getCondExactType.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondExactType(QName type) {
		return String.format(QUERY_COND_EXACT_TYPE, type);
	}

	/**
	 * <p>getCondAspect.</p>
	 *
	 * @param aspect a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCondAspect(QName aspect) {
		return String.format(QUERY_COND_ASPECT, aspect);
	}

	/**
	 * <p>getCond.</p>
	 *
	 * @param cond a {@link java.lang.String} object.
	 * @param operator a {@link org.alfresco.service.cmr.search.SearchParameters.Operator} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getCond(String cond, Operator operator) {
		return String.format(QUERY_COND, operator != null ? operator : "", cond);
	}

	/**
	 * <p>mandatory.</p>
	 *
	 * @param condType a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String mandatory(String condType) {
		return (SearchService.LANGUAGE_LUCENE.equals(language) ? " +" : " AND +") + condType;
	}
	
	/**
	 * <p>equalsQuery.</p>
	 *
	 * @param condType a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String equalsQuery(String condType) {
		return (SearchService.LANGUAGE_LUCENE.equals(language) ? " +" : " AND =") + condType;
	}
	

	/**
	 * <p>prohibided.</p>
	 *
	 * @param condType a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String prohibided(String condType) {		
		return (SearchService.LANGUAGE_LUCENE.equals(language) ? " -" : " AND -=") + condType;
	}

	/**
	 * <p>or.</p>
	 *
	 * @param condType a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String or(String condType) {
		return  (SearchService.LANGUAGE_LUCENE.equals(language) ? " " : " OR " ) + condType;
	}
	
	/**
	 * <p>optional.</p>
	 *
	 * @param condType a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String optional(String condType) {
		return   " " + condType;
	}
	
	/**
	 * <p>boost.</p>
	 *
	 * @param condType a {@link java.lang.String} object.
	 * @param boostFactor a {@link java.lang.Integer} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String boost(String condType,Integer boostFactor) {
		return condType+"^"+boostFactor;
	}

	/**
	 * <p>startGroup.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected String startGroup() {
		return "(";
	}

	/**
	 * <p>endGroup.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected String endGroup() {
		return ")";
	}

	/**
	 * <p>getGroup.</p>
	 *
	 * @param op1 a {@link java.lang.String} object.
	 * @param op2 a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getGroup(String op1, String op2) {
		return " (" + op1 + " " + op2 + ")";
	}
	
	/**
	 * <p>getMandatoryOrGroup.</p>
	 *
	 * @param op1 a {@link java.lang.String} object.
	 * @param op2 a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getMandatoryOrGroup(String op1, String op2) {
		return " AND (" + op1 + " OR " + op2 + ")";
	}

	/**
	 * <p>getSortProp.</p>
	 *
	 * @param field a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getSortProp(QName field) {
		return "@" + field;
	}

	/**
	 * <p>getSort.</p>
	 *
	 * @param field a {@link org.alfresco.service.namespace.QName} object.
	 * @param asc a boolean.
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<String, Boolean> getSort(QName field, boolean asc) {
		Map<String, Boolean> sort = new HashMap<>();
		sort.put(getSortProp(field), asc);

		return sort;
	}

	/**
	 * <p>getSort.</p>
	 *
	 * @param field a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<String, Boolean> getSort(QName field) {
		return getSort(field, true);
	}

	/**
	 * <p>escapeQName.</p>
	 *
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String escapeQName(QName qName) {
		String string = qName.toString();
		StringBuilder buf = new StringBuilder(string.length() + 4);
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if ((c == '{') || (c == '}') || (c == ':') || (c == '-')) {
				buf.append('\\');
			}
			buf.append(c);
		}
		return buf.toString();
	}

	/**
	 * <p>encodePath.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String encodePath(String path) {
		
		if (path.indexOf("/app:company_home/") == 0) {
			path = path.replace("/app:company_home/", "");
		}  else {
			
			if (path.indexOf("/") == 0) {
			  path = path.substring(1);
			}

			StringBuilder pathBuffer = new StringBuilder(64);
			String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
	
			for (String folder : arrPath) {
				if (!folder.contains("bcpg:") && !folder.contains("cm:") && !folder.contains("app:") && !folder.contains("st:")) {
					pathBuffer.append("/cm:");
					pathBuffer.append(ISO9075.encode(folder));
				} else {
					pathBuffer.append("/").append(folder);
				}
			}
	
			// remove 1st character '/'
			return pathBuffer.substring(1);
			
		}
		return path;

	}

}
