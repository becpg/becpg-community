package fr.becpg.repo.web.scripts.supplier;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.mail.BeCPGMailService;

public class SupplierAccountWebScript extends  AbstractWebScript {

	private static final Log logger = LogFactory.getLog(SupplierAccountWebScript.class);
	
	private static final String PARAM_ENTITY_NODEREF = "nodeRef";
	private static final String PARAM_EMAIL_ADDRESS = "emailAddress";
	private static final String PARAM_NOTIFY_SUPPLIER = "notifySupplier";
	private static final String SUPPLIER_PREFIX = "supplier";
	
	NodeService nodeService;
	
	PersonService personService;
	
	AuthorityService authorityService;
	
	@Autowired
	TransactionService transactionService;
	
	@Autowired
	BeCPGMailService mailService;

	@Autowired
	MutableAuthenticationService authenticationService;
	
	
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_ENTITY_NODEREF));
		Boolean notifySupplier = Boolean.parseBoolean(req.getParameter(PARAM_NOTIFY_SUPPLIER));
		String supplierEmail = req.getParameter(PARAM_EMAIL_ADDRESS);
		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(()->{

			List<AssociationRef> associations = nodeService.getTargetAssocs(nodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNT);
			
			String userName = SUPPLIER_PREFIX +"-"+ (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE);
			String password = UUID.randomUUID().toString();
			
			if(associations == null || associations.isEmpty()){				
						
				if(!authenticationService.authenticationExists(userName)){
					
					if(logger.isDebugEnabled()){
						logger.debug("Create external user: " + userName + " pwd: " + password);
					}
					
					authenticationService.createAuthentication(userName, password.toCharArray());
					
					Map<QName, Serializable> propMap = new HashMap<>();
					propMap.put(ContentModel.PROP_USERNAME, userName);
					propMap.put(ContentModel.PROP_LASTNAME, userName);
					propMap.put(ContentModel.PROP_FIRSTNAME, (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
					propMap.put(ContentModel.PROP_EMAIL, supplierEmail);
					NodeRef userRef = personService.createPerson(propMap);
					authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.ExternalUser.toString()), userName);
					
					nodeService.createAssociation(nodeRef, userRef, PLMModel.ASSOC_SUPPLIER_ACCOUNT);
					
					String creator = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR);
					mailService.sendMailNewUser(personService.getPersonOrNull(creator), userName, password, true);
					
					// notify supplier
					if(notifySupplier){
						mailService.sendMailNewUser(userRef, userName, password, false);
					} 
					
					
				} else{
					
					if(logger.isDebugEnabled()){
						logger.debug("Reassign to an existed user");
					}
					
					nodeService.createAssociation(nodeRef, personService.getPerson(userName), PLMModel.ASSOC_SUPPLIER_ACCOUNT);
				}
				
			} else {
				throw new WebScriptException("Cannot Create User: "+userName);				
			}
			
			try {
				JSONObject ret = new JSONObject();
				ret.put("login", userName);
				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				ret.write(res.getWriter());
			} catch (JSONException e) {
				throw new WebScriptException("Unable to serialize JSON", e);
			}
			
			return null;
		},true, false);
		
	}



	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}


	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	
	
	
	

}
