package fr.becpg.repo.mail.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.search.BeCPGSearchService;


/**
 * 
 * @author matthieu
 *
 */
public class BeCPGMailServiceImpl  implements BeCPGMailService {
	
	private static Log _logger = LogFactory.getLog(BeCPGMailServiceImpl.class);

	
	public static final String EMAIL_MODEL_PATH_QUERY = "PATH:\"/app:company_home/app:dictionary/app:email_templates/.\"";
	private static final String PATH_WORKFLOW = "workflow";
	public static final String EMAIL_WORKFLOW_MODEL_PATH_QUERY = "PATH:\"/app:company_home/app:dictionary/app:email_templates/app:workflow/.\"";
	

	private NodeService nodeService;
	private TemplateService templateService;
	private JavaMailSender mailService;
	private ServiceRegistry serviceRegistry;
	private BeCPGSearchService beCPGSearchService;
	
	private NodeRef modelMailNodeRef;
	
	private NodeRef workflowModelMailNodeRef;
	
	private String mailFrom;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	public void setMailService(JavaMailSender mailService) {
		this.mailService = mailService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	public void sendMailNewUser(NodeRef personNodeRef,String userName, String password) {
		Map<String, Object> templateModel = new HashMap<String, Object>(8, 1.0f);

		templateModel.put("person", new TemplateNode(personNodeRef, serviceRegistry, null));
		templateModel.put("username",userName );
		templateModel.put("password",password );
		
		
		String email = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL);
		if(!StringUtils.isEmpty(email)){
			sendMail(email, I18NUtil.getMessage("becpg.mail.newUser.title"), RepoConsts.EMAIL_NEW_USER_TEMPLATE, templateModel);
		}
		
	}

	
	
	
	
	private void sendMail(String email,String subjet ,String templateName, Map<String, Object> templateModel){

		
		MimeMessage l_mimeMessage = mailService.createMimeMessage();
		
		try {
			MimeMessageHelper messageHelper = new MimeMessageHelper(l_mimeMessage,true);
			messageHelper.setTo(email);
			messageHelper.setSubject(subjet);
			NodeRef templateNodeRef  = nodeService.getChildByName(getModelMailNodeRef(), 
  					ContentModel.ASSOC_CONTAINS,templateName);
			
			if(templateName!=null && templateNodeRef!=null){

			   String contenuText = templateService.processTemplate("freemarker",templateNodeRef.toString(), templateModel);
				

				 messageHelper.setText(contenuText);
				_logger.debug("Message subject = " + l_mimeMessage.getSubject());
				_logger.debug("Message content = " + contenuText);
			} else {
				_logger.warn("Mail model not found : [NOK]");
				messageHelper.setText(I18NUtil.getMessage("becpg.mail.template.notfound"), true);
			}

			messageHelper.setFrom(mailFrom);

			mailService.send(l_mimeMessage);
			
		} catch (Exception e) {
			_logger.error("Cannot send email " + email + "", e);
		}
		
	}

	@Override
	public NodeRef getModelMailNodeRef() {
		if(modelMailNodeRef==null){
			List<NodeRef> nodeRefs = beCPGSearchService.luceneSearch(EMAIL_MODEL_PATH_QUERY, RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		     modelMailNodeRef =  nodeRefs.size() > 0 ? nodeRefs.get(0) : null;
		}
		
		return modelMailNodeRef;
	}

	@Override
	public NodeRef getWorkflowModelMailNodeRef() {
		
		if(workflowModelMailNodeRef==null){
			List<NodeRef> nodeRefs = beCPGSearchService.luceneSearch(EMAIL_WORKFLOW_MODEL_PATH_QUERY, RepoConsts.MAX_RESULTS_SINGLE_VALUE);
			
			if(nodeRefs.size() > 0){
				workflowModelMailNodeRef = nodeRefs.get(0);
			}
			else{
				// create folder
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    	properties.put(ContentModel.PROP_NAME, I18NUtil.getMessage("path.email.workflow"));
		    	
		    	workflowModelMailNodeRef = nodeService.createNode(getModelMailNodeRef(), ContentModel.ASSOC_CONTAINS, 
											QName.createQName(NamespaceService.APP_MODEL_1_0_URI, PATH_WORKFLOW), 
											ContentModel.TYPE_FOLDER, properties).getChildRef();
			}
			
		}
		
		return workflowModelMailNodeRef;		
	}	
	
}
