/*
 * 
 */
package fr.becpg.repo.security.filter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.SecurityModel;

// TODO: Auto-generated Javadoc
/**
 * The Class PropertyPermissionTypeFormFilter.
 *
 * @author querephi
 */
public class PropertyPermissionTypeFormFilter  extends AbstractPropertyPermissionFormFilter<TypeDefinition>{

	/** The logger. */
	private static Log logger = LogFactory.getLog(PropertyPermissionTypeFormFilter.class);
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#afterGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
	 */
	@Override
	public void afterGenerate(TypeDefinition type, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {

		logger.debug("afterGenerate");
		filterFormFields(type.getName(), form);			
//TODO
//		if(SecurityModel.TYPE_ACL_ENTRY.equals(type.getName())){
//			logger.debug("Acl entry : compute require props");
//			Iterator<FieldDefinition> it = form.getFieldDefinitions().iterator();
//			while(it.hasNext()){
//				FieldDefinition fieldDefinition = it.next();
//				
//				if(SecurityModel.PROP_ACL_PROPNAME.toPrefixString(namespacePrefixResolver).equals(fieldDefinition.getName())){
//					it.remove();
//					break;
//				}
//			}
//		}
//		
		
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
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#beforePersist(java.lang.Object, org.alfresco.repo.forms.FormData)
	 */
	@Override
	public void beforePersist(TypeDefinition type, FormData data) {
		// TODO Auto-generated method stub
		
	}
}
