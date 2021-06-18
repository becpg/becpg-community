/*
 * 
 */
package fr.becpg.repo.security.filter;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The Class PropertyPermissionTypeFormFilter.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class PropertyPermissionTypeFormFilter  extends AbstractPropertyPermissionFormFilter<TypeDefinition>{

	
	private static final Log logger = LogFactory.getLog(PropertyPermissionTypeFormFilter.class);
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#afterGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
	 */
	/** {@inheritDoc} */
	@Override
	public void afterGenerate(TypeDefinition type, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {
		
		logger.debug("afterGenerate"+form.toString());
		filterFormFields(null, type.getName(), form);			
	
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#afterPersist(java.lang.Object, org.alfresco.repo.forms.FormData, org.alfresco.service.cmr.repository.NodeRef)
	 */
	/** {@inheritDoc} */
	@Override
	public void afterPersist(TypeDefinition type, FormData data, NodeRef arg2) {
		// TODO Auto-generated method stub		
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#beforeGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
	 */
	/** {@inheritDoc} */
	@Override
	public void beforeGenerate(TypeDefinition type, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#beforePersist(java.lang.Object, org.alfresco.repo.forms.FormData)
	 */
	/** {@inheritDoc} */
	@Override
	public void beforePersist(TypeDefinition type, FormData data) {
		// TODO Auto-generated method stub
		
	}
}
