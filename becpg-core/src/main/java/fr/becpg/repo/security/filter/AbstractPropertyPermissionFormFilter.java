/*
 * 
 */
package fr.becpg.repo.security.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.SecurityModel;
import fr.becpg.repo.security.SecurityService;

// TODO: Auto-generated Javadoc
/**
 * Check property permission according to securityService
 *
 * @param <ItemType> the generic type
 * @author matthieu
 */
public abstract class AbstractPropertyPermissionFormFilter<ItemType> extends AbstractFilter<ItemType, NodeRef> {

	/** The logger. */
	private static Log logger = LogFactory.getLog(AbstractPropertyPermissionFormFilter.class);

	
	private SecurityService securityService;
	
	protected NamespacePrefixResolver namespacePrefixResolver;
	
	
	
	
	
	public void setNamespacePrefixResolver(
			NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}


	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	
	protected void filterFormFields(QName nodeType ,Form form){
		
		Iterator<FieldDefinition> it = form.getFieldDefinitions().iterator();
		while(it.hasNext()){
			FieldDefinition fieldDefinition = it.next();
			int access_mode = securityService.computeAccessMode(nodeType, fieldDefinition.getName());
			
			switch (access_mode) {
			case SecurityService.READ_ACCESS:
				if(logger.isDebugEnabled()){
					logger.debug("Mark as read only :"+fieldDefinition.getName());
				}
				fieldDefinition.setProtectedField(true);
				break;
			case SecurityService.NONE_ACCESS:
				if(logger.isDebugEnabled()){
					logger.debug("Remove field from form :"+fieldDefinition.getName());
				}
				it.remove();
				break;
			default:
				break;
			}
			
			
		}
		
		
	}
	
	

	protected void computeCustomPropsField(Form form, NodeRef item) {
		
		
		
		Iterator<FieldDefinition> it = form.getFieldDefinitions().iterator();
		while(it.hasNext()){
			FieldDefinition fieldDefinition = it.next();
			
			if(SecurityModel.PROP_ACL_PROPNAME.toPrefixString(namespacePrefixResolver).equals(fieldDefinition.getName())){
				
		
				List<FieldConstraint> constraints = new ArrayList<PropertyFieldDefinition.FieldConstraint>(); 
				
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("caseSensitive", true);
				params.put("allowedValues",securityService.extractProps(item));
				
				constraints.add(new FieldConstraint("LIST", params));
				
				logger.debug("Add constraint :"+constraints.get(0).getParametersAsJSON());
				
				
				((PropertyFieldDefinition) fieldDefinition).setConstraints(constraints);
				
				break;
			}
			
			
		}
		
		
	}

	


}
