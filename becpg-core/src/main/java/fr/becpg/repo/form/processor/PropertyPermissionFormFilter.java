/*
 * 
 */
package fr.becpg.repo.form.processor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * Check property permission according to aspect permissions (WriteProperty, ReadProperty, EditProperty).
 *
 * @param <ItemType> the generic type
 * @author querephi
 */
public class PropertyPermissionFormFilter<ItemType> extends AbstractFilter<ItemType, NodeRef> {

	/** The logger. */
	private static Log logger = LogFactory.getLog(PropertyPermissionFormFilter.class);
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;	
	
	/** The service registry. */
	private ServiceRegistry serviceRegistry;
	
	/** The authority service. */
	private AuthorityService authorityService;	
	
	/** The authentication service. */
	private AuthenticationService authenticationService;
	
	/**
	 * Sets the dictionary service.
	 *
	 * @param dictionaryService the new dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	/**
	 * Sets the service registry.
	 *
	 * @param serviceRegistry the new service registry
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	/**
	 * Sets the authority service.
	 *
	 * @param authorityService the new authority service
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
	/**
	 * Sets the authentication service.
	 *
	 * @param authenticationService the new authentication service
	 */
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.forms.processor.Filter#afterGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
	 */
	@Override
	public void afterGenerate(ItemType arg0, List<String> arg1,
			List<String> arg2, Form arg3, Map<String, Object> arg4) {
		// ignored		
	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.forms.processor.Filter#afterPersist(java.lang.Object, org.alfresco.repo.forms.FormData, java.lang.Object)
	 */
	@Override
	public void afterPersist(ItemType arg0, FormData arg1, NodeRef arg2) {
		// ignored		
	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.forms.processor.Filter#beforeGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
	 */
	@Override
	public void beforeGenerate(ItemType arg0, List<String> arg1,
			List<String> arg2, Form arg3, Map<String, Object> arg4) {
		// ignored		
	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.forms.processor.Filter#beforePersist(java.lang.Object, org.alfresco.repo.forms.FormData)
	 */
	@Override
	public void beforePersist(ItemType arg0, FormData arg1) {
		// ignored		
	}
	
	/**
	 * Check permission according to aspects.
	 *
	 * @param aspects the aspects
	 * @param form the form
	 */
	protected void checkPermissions(Set<QName> aspects, Form form){

//		logger.debug("checkPermissions");	
//		
//		String currentUser = authenticationService.getCurrentUserName();
//		logger.debug("currentUser: " + currentUser);
//		logger.debug("aspects: " + aspects);
//		
//		for(QName aspect : aspects){
//					
//			//TODO : remove or make it generic !
//			//TEST : TEMP CODE
//			if(aspect.equals(BeCPGModel.ASPECT_TRANSFORMATION)){
//				
//				if(!authorityService.getAuthoritiesForUser(currentUser).contains("GROUP_TEST_TRANSFO_WRITE")){
//					
//					logger.debug("User doesn't belong to TEST_TRANSFO_WRITE group");
//					NamespaceService namespaceService = serviceRegistry.getNamespaceService();		
//					
//					// set properties to read only
//					Map<QName, PropertyDefinition> propertyDefs = dictionaryService.getPropertyDefs(BeCPGModel.ASPECT_TRANSFORMATION);
//					for(QName propertyQName : propertyDefs.keySet()){
//						for(FieldDefinition fieldDefinition : form.getFieldDefinitions()){							
//							QName qName = QName.createQName(fieldDefinition.getName(), namespaceService);
//							if(propertyQName.equals(qName)){
//								
//								logger.debug("Set protected field: " + propertyQName.getLocalName());
//																
//								fieldDefinition.setProtectedField(true);
//							}
//						}
//					}	
//				}	
//			}
//			
//		}
    }

}
