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
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.SiteHelper;

/**
 * @author matthieu
 *
 */
public abstract class AbstractBeCPGQueryBuilder {
	

    private  final String QUERY_COND_PROP_EQUAL_VALUE = "@%s:\"%s\"";
	private  final String QUERY_COND_PROP_CONTAINS_VALUE = "@%s:%s";
	private  final String QUERY_COND_PROP_ISNULL_VALUE = "ISNULL:\"%s\"";
	private  final String QUERY_COND_PATH = "PATH:\"/app:company_home/%s/*\"";
	private  final String QUERY_COND_MEMBERS = "PATH:\"/app:company_home/%s/member\"";
	private  final String QUERY_COND_ID = "ID:\"%s\"";
	
	private  final String QUERY_COND_PARENT = "PARENT:\"%s\"";
	private  final String QUERY_COND_TYPE = "TYPE:\"%s\"";
	private  final String QUERY_COND_ASPECT = "ASPECT:\"%s\"";
	private  final String QUERY_COND = " %s %s";

	protected String language = SearchService.LANGUAGE_LUCENE;

	//TODO should not be public
	@Deprecated
	public  String getCondEqualValue(QName property, String value) {
		if(value == null || value.isEmpty()){
			return getCondIsNullValue(property);
		}
		else{
			return String.format(QUERY_COND_PROP_EQUAL_VALUE, SearchService.LANGUAGE_LUCENE.equals(language)? escapeQName(property) : property, value);
		}	
	}
	

	protected  String getCondEqualID(NodeRef nodeRef) {

		return String.format(QUERY_COND_ID, nodeRef);
	}


	protected  String getCondContainsValue(QName property, String value) {

		return String.format(QUERY_COND_PROP_CONTAINS_VALUE, SearchService.LANGUAGE_LUCENE.equals(language)? escapeQName(property) : property, value);
	}

	
	protected  String getCondIsNullValue(QName property) {
		return String.format(QUERY_COND_PROP_ISNULL_VALUE, property);
	}
	
	

	protected  String getCondPath(String path) {
		if(path.startsWith(SiteHelper.SITES_SPACE_QNAME_PATH)){
			return String.format(QUERY_COND_PATH, path);
		}
		
		return String.format(QUERY_COND_PATH, encodePath(path));
	}

	protected  String getCondMembers(String path) {
		return String.format(QUERY_COND_MEMBERS, encodePath(path));
	}
	

	protected  String getCondParent(NodeRef nodeRef) {
		return String.format(QUERY_COND_PARENT, nodeRef);
	}

	
	protected  String getCondType(QName type) {
		return String.format(QUERY_COND_TYPE, type);
	}
	

	protected  String getCondAspect(QName aspect) {
		return String.format(QUERY_COND_ASPECT, aspect);
	}

	protected  String getCond(String cond, Operator operator) {
		return String.format(QUERY_COND, operator != null ? operator : "", cond);
	}

	protected  String mandatory(String condType) {
		return (SearchService.LANGUAGE_LUCENE.equals(language)?" +":" AND +")+condType;
	}

	protected  String prohibided(String condType) {
		return (SearchService.LANGUAGE_LUCENE.equals(language)?" -":" AND -")+condType;
	}

	protected  String optional(String condType) {
		return " "+condType;
	}

	protected  String startGroup() {
		return "(";
	}
	
     protected  String endGroup() {
		return ")";
	}
	
	protected  String getGroup(String op1, String op2) {
		return " ("+op1+" "+op2+")";
	}

	protected  String getSortProp(QName field) {
		return "@" + field;
	}
	
	protected  Map<String, Boolean> getSort(QName field, boolean asc) {
		Map<String, Boolean> sort = new HashMap<String, Boolean>();
		sort.put(getSortProp(field), asc);

		return sort;
	}

	protected  Map<String, Boolean> getSort(QName field) {
		return getSort(field, true);
	}

	protected  String escapeQName(QName qName) {
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


	protected  String encodePath(String path) {

		path = path.replace("/app:company_home/", "");
		
		if(path.indexOf("/")==0) {
				path = path.substring(1);
		}
		
		
		StringBuilder pathBuffer = new StringBuilder(64);
		String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);

		for (String folder : arrPath) {
			if (!folder.contains("bcpg:") && !folder.contains("cm:") && !folder.contains("app:")) {
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
