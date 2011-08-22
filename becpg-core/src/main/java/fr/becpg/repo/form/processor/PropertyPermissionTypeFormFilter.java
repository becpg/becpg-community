/*
 * 
 */
package fr.becpg.repo.form.processor;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class PropertyPermissionTypeFormFilter.
 *
 * @author querephi
 */
public class PropertyPermissionTypeFormFilter  extends PropertyPermissionFormFilter<TypeDefinition>{

	/** The logger. */
	private static Log logger = LogFactory.getLog(PropertyPermissionTypeFormFilter.class);
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#afterGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
	 */
	@Override
	public void afterGenerate(TypeDefinition type, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {
		
		logger.debug("afterGenerate");
		checkPermissions(type.getDefaultAspectNames(), form);			
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#afterPersist(java.lang.Object, org.alfresco.repo.forms.FormData, org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public void afterPersist(TypeDefinition type, FormData data, NodeRef arg2) {
		// TODO Auto-generated method stub		
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#beforeGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
	 */
	@Override
	public void beforeGenerate(TypeDefinition type, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {
		// TODO Auto-generated method stub		
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#beforePersist(java.lang.Object, org.alfresco.repo.forms.FormData)
	 */
	@Override
	public void beforePersist(TypeDefinition type, FormData data) {
		// TODO Auto-generated method stub
		
	}
}
