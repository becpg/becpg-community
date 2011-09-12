package fr.becpg.repo.helper;

import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;

/**
 * Helper for lucene queries
 * @author querephi
 *
 */
public class LuceneHelper {

	private  static final String QUERY_COND_PROP_EQUAL_VALUE = " %s +@%s:\"%s\"";
	private  static final String QUERY_COND_PROP_CONTAINS_VALUE = " %s +@%s:%s";
	private  static final String QUERY_COND_PROP_ISNULL_VALUE = " %s ISNULL:\"%s\"";
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
		OR		
	}
}
