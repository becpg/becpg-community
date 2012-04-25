package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SpelEditorListValuePlugin extends EntityListValuePlugin {
	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(SpelEditorListValuePlugin.class);


	/** The Constant SOURCE_TYPE_TARGET_ASSOC. */
	private static final String SOURCE_TYPE_SPELEDITOR = "speleditor";
	
	
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_SPELEDITOR };
	}

	
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
		//Class is a Java class
		try {
            Class<?> c = Class.forName(className);
            Field[] fields =  c.getDeclaredFields();
            List<ListValueEntry> ret = new ArrayList<ListValueEntry>();
            for(int i = 0; i < fields.length; i++){
              ret.add(new ListValueEntry(fields[i].getName(), fields[i].getName(), fields[i].getDeclaringClass().getSimpleName()));
            }
            return new ListValuePage(ret, pageNum, pageSize,null);
            
         }
         catch (ClassNotFoundException e) {
        	 logger.debug(e,e);
         }
		
		QName type = QName.createQName(className, namespaceService);
		
		return suggestTargetAssoc(type, query,pageNum, pageSize, null);
		
	}

}
