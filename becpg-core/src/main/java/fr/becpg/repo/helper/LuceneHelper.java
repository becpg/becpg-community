package fr.becpg.repo.helper;

import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;

/**
 * Helper for lucene queries
 * @author querephi
 *
 */
public class LuceneHelper {

	private  static final String PATH_QUERY_COND_PROP_EQUAL_VALUE = " %s +@%s:\"%s\"";
	
	/**
	 * Return an equal condition on a property
	 * @param property
	 * @param value
	 * @return
	 */
	public static String getCondEqualValue(QName property, String value, Operator operator){
		
		return String.format(PATH_QUERY_COND_PROP_EQUAL_VALUE, operator != null ? operator:"",  Repository.escapeQName(property), value);
	}
	
	public enum Operator{
		AND,
		OR		
	}
}
