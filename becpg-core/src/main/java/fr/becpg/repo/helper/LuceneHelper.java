package fr.becpg.repo.helper;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

import fr.becpg.repo.RepoConsts;

/**
 * Helper for lucene queries
 * 
 * @author querephi
 * 
 */
public class LuceneHelper {

	private static final String QUERY_COND_PROP_EQUAL_VALUE = " %s +@%s:\"%s\"";
	private static final String QUERY_COND_PROP_CONTAINS_VALUE = " %s +@%s:%s";
	private static final String QUERY_COND_PROP_ISNULL_VALUE = " %s ISNULL:\"%s\"";
	private static final String QUERY_COND_PATH = " %s +PATH:\"/app:company_home/%s/*\"";
	private static final String QUERY_COND_ID = " %s ID:\"%s\"";
	private static final String QUERY_COND_BY_SORT = " %s +@%s:[%s TO %s]";
	private static final String QUERY_COND_PARENT = " %s +PARENT:\"%s\"";
	private static final String QUERY_COND_TYPE = " %s +TYPE:\"%s\"";
	private static final String QUERY_COND_ASPECT = " %s +ASPECT:\"%s\"";
	private static final String QUERY_COND = " %s %s";

	/**
	 * Return an equal condition on a property
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondEqualValue(QName property, String value, Operator operator) {

		if(value == null || value.isEmpty()){
			return getCondIsNullValue(property, operator);
		}
		else{
			return String.format(QUERY_COND_PROP_EQUAL_VALUE, operator != null ? operator : "", escapeQName(property), value);
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
	public static String getCondIsNullValue(QName property, Operator operator) {

		return String.format(QUERY_COND_PROP_ISNULL_VALUE, operator != null ? operator : "", escapeQName(property));
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
	public static String getCondType(QName type, Operator operator) {

		return String.format(QUERY_COND_TYPE, operator != null ? operator : "", type);
	}
	
	/**
	 * Return an aspect condition on QName
	 * 
	 * @param type
	 * @param operator
	 * @return
	 */
	public static String getCondAspect(QName aspect, Operator operator) {

		return String.format(QUERY_COND_ASPECT, operator != null ? operator : "", aspect);
	}

	public static String getCond(String cond, Operator operator) {

		return String.format(QUERY_COND, operator != null ? operator : "", cond);
	}

	public enum Operator {
		AND, OR, NOT
	}

	/**
	 * Encode path.
	 * 
	 * @param path
	 *            the path
	 * @return the string
	 */
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

	public static Map<String, Boolean> getSort(QName field, boolean asc) {

		Map<String, Boolean> sort = new HashMap<String, Boolean>();
		sort.put("@" + field, asc);

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

	public static String getSiteSearchPath(String siteId, String containerId) {

		String path = SiteHelper.SITES_SPACE_QNAME_PATH;
		if (siteId != null && siteId.length() > 0) {
			path += "cm:" + ISO9075.encode(siteId) + "/";
		} else {
			path += "*/";
		}
		if (containerId != null && containerId.length() > 0) {
			path += "cm:" + ISO9075.encode(containerId) + "/";
		} else {
			path += "*/";
		}
		
		return "+PATH:\"" + path + "/*\"" ;
	}

}
