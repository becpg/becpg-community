package fr.becpg.repo.helper;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.web.bean.repository.Repository;

import fr.becpg.repo.RepoConsts;

/**
 * Helper for lucene queries
 * @author querephi
 *
 */
public class LuceneHelper {

	private  static final String QUERY_COND_PROP_EQUAL_VALUE = " %s +@%s:\"%s\"";
	private  static final String QUERY_COND_PROP_CONTAINS_VALUE = " %s +@%s:%s";
	private  static final String QUERY_COND_PROP_ISNULL_VALUE = " %s ISNULL:\"%s\"";
	private  static final String QUERY_COND_ID = " %s ID:\"%s\"";
	
	/**
	 * Return an equal condition on a property
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondEqualValue(QName property, String value, Operator operator){
		
		return String.format(QUERY_COND_PROP_EQUAL_VALUE, operator != null ? operator:"",  Repository.escapeQName(property), value);
	}
	
	/**
	 * Return an equal condition on ID of nodeRef
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondEqualID(NodeRef nodeRef, Operator operator){
		
		return String.format(QUERY_COND_ID, operator != null ? operator:"", nodeRef);
	}
	
	/**
	 * Return a contain condition on a property
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondContainsValue(QName property, String value, Operator operator){
		
		return String.format(QUERY_COND_PROP_CONTAINS_VALUE, operator != null ? operator:"",  Repository.escapeQName(property), value);
	}
	
	/**
	 * Return a ISNULL condition on a property
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondIsNullValue(QName property, Operator operator){
		
		return String.format(QUERY_COND_PROP_ISNULL_VALUE, operator != null ? operator:"",  Repository.escapeQName(property));
	}
	
	public enum Operator{
		AND,
		OR, 
		NOT
	}
	
	/**
     * Encode path.
     *
     * @param path the path
     * @return the string
     */
	public static  String encodePath(String path){
    	
    	StringBuilder pathBuffer = new StringBuilder(64);
    	String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
    	
    	for(String folder : arrPath){
    		if(!folder.contains("bcpg:")){
	    		pathBuffer.append("/cm:");
	    		pathBuffer.append(ISO9075.encode(folder)); 
    		} else {
    			pathBuffer.append("/"+folder);
    		}
    	}
    	
    	//remove 1st character '/'
    	return pathBuffer.substring(1);
    }
}
