/*
Copyright (C) 2010-2014 beCPG. 
 
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
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

import fr.becpg.repo.RepoConsts;

/**
 * @author matthieu
 *
 */
public class QueryBuilderHelper {
	
	
	/*-- Path query --*/			

		
	
    private static final String QUERY_COND_PROP_EQUAL_VALUE = "@%s:\"%s\"";
	
	private static final String QUERY_COND_PROP_CONTAINS_VALUE = " %s +@%s:%s";
	private static final String QUERY_COND_PROP_ISNULL_VALUE = "ISNULL:\"%s\"";
	private static final String QUERY_COND_PATH = " %s +PATH:\"/app:company_home/%s/*\"";
	private static final String QUERY_COND_MEMBERS = " %s +PATH:\"/app:company_home/%s/member\"";
	private static final String QUERY_COND_ID = " %s ID:\"%s\"";
	private static final String QUERY_COND_BY_SORT = " %s +@%s:[%s TO %s]";
	private static final String QUERY_COND_PARENT = " %s +PARENT:\"%s\"";
	private static final String QUERY_COND_TYPE = "TYPE:\"%s\"";
	private static final String QUERY_COND_ASPECT = "ASPECT:\"%s\"";
	private static final String QUERY_COND = " %s %s";


	
	/**
	 * Return an equal condition on a property
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondEqualValue(QName property, String value, Operator operator) {

		return getCond(getCondEqualValue( property,  value), operator);
		
	}

	
	public static String getCondEqualValue(QName property, String value) {
		if(value == null || value.isEmpty()){
			return getCondIsNullValue(property);
		}
		else{
			return String.format(QUERY_COND_PROP_EQUAL_VALUE, escapeQName(property), value);
		}	
	}
	
	/**
	 * Return an equal condition on ID of nodeRef
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondEqualID(NodeRef nodeRef, Operator operator) {

		return String.format(QUERY_COND_ID, operator != null ? operator : "", nodeRef);
	}

	/**
	 * Return a contain condition on a property
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondContainsValue(QName property, String value, Operator operator) {

		return String.format(QUERY_COND_PROP_CONTAINS_VALUE, operator != null ? operator : "", escapeQName(property), value);
	}

	/**
	 * Return a ISNULL condition on a property
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondIsNullValue(QName property) {
		return String.format(QUERY_COND_PROP_ISNULL_VALUE, escapeQName(property));
	}
	
	
	public static String getCondIsNullValue(QName property,Operator operator) {
		return getCond(String.format(QUERY_COND_PROP_ISNULL_VALUE, escapeQName(property)),operator);
	}

	/**
	 * Return a +PATH condition (encode path)
	 * 
	 * @param path
	 * @param operator
	 * @return
	 */
	public static String getCondPath(String path, Operator operator) {
		return String.format(QUERY_COND_PATH, operator != null ? operator : "", encodePath(path));
	}

	/**
	 * @param membersPath
	 * @param and
	 * @return
	 */
	public static Object getCondMembers(String path, Operator operator) {
		return String.format(QUERY_COND_MEMBERS, operator != null ? operator : "", encodePath(path));
	}
	
	/**
	 * Get conditions on sort
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static String getCondMinMax(QName property, String min, String max, Operator operator) {
		return String.format(QUERY_COND_BY_SORT, operator != null ? operator : "", escapeQName(property), min, max);
	}

	/**
	 * Return a parent condition on nodeRef
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondParent(NodeRef nodeRef, Operator operator) {
		return String.format(QUERY_COND_PARENT, operator != null ? operator : "", nodeRef);
	}

	/**
	 * Return a type condition on QName
	 * 
	 * @param type
	 * @param operator
	 * @return
	 */
	public static String getCondType(QName type) {

		return String.format(QUERY_COND_TYPE, type);
	}
	
	/**
	 * Return an aspect condition on QName
	 * 
	 * @param type
	 * @param operator
	 * @return
	 */
	public static String getCondAspect(QName aspect) {

		return String.format(QUERY_COND_ASPECT, aspect);
	}

	public static String getCond(String cond, Operator operator) {

		return String.format(QUERY_COND, operator != null ? operator : "", cond);
	}

	public enum Operator {
		AND, OR, NOT
	}


	

	public static String getSortProp(QName field) {
		return "@" + field;
	}
	
	public static Map<String, Boolean> getSort(QName field, boolean asc) {

		Map<String, Boolean> sort = new HashMap<String, Boolean>();
		sort.put(getSortProp(field), asc);

		return sort;
	}

	public static Map<String, Boolean> getSort(QName field) {

		return getSort(field, true);
	}

	public static String escapeQName(QName qName) {
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


	public static String mandatory(String condType) {
		
		return " +"+condType;
	}

	public static String exclude(String condType) {
		return " -"+condType;
	}


	public static String getGroup(String op1, String op2) {
		return " ("+op1+" "+op2+")";
	}
	
	public static String encodePath(String path) {

		StringBuilder pathBuffer = new StringBuilder(64);
		String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);

		for (String folder : arrPath) {
			if (!folder.contains("bcpg:") && !folder.contains("cm:")) {
				pathBuffer.append("/cm:");
				pathBuffer.append(ISO9075.encode(folder));
			} else {
				pathBuffer.append("/" + folder);
			}
		}

		// remove 1st character '/'
		return pathBuffer.substring(1);
	}



}
