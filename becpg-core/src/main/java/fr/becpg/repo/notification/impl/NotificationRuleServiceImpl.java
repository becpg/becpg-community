package fr.becpg.repo.notification.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.notification.NotificationRuleService;
import fr.becpg.repo.notification.data.NotificationRuleListDataItem;
import fr.becpg.repo.notification.data.NotificationRuleTimeType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("notificationRuleService")
public class NotificationRuleServiceImpl implements NotificationRuleService {

	private static final Log logger = LogFactory.getLog(NotificationRuleServiceImpl.class);
	
	private static final int WEEKLY = 7;
	private static final int MONTHLY = 30;
	
	private static final String SUBJECT = "subject";
	private static final String SITE_NAME = "siteName";
	private static final String DATE_FIELD = "dateField";
	private static final String ENTITY_TYPE = "entityType";
	private static final String ENTITY_NAME = "entityName";
	private static final String ENTITY_NODEREF = "entityNodeRef";
	private static final String CONDITIONS = "conditions";
	private static final String TARGET_PATH = "targetPath";
	private static final String PARENT_FOLDER = "parentFolder";
	private static final String DATE_FIELD_VALUE = "dateFieldValue";
	private static final String NOTIFICATION_NODEREF = "notificationNodeRef";

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private SiteService siteService;
	
	@Autowired
	private BeCPGMailService mailService;
	
	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;
	
	@Autowired
	private NamespaceService namespaceService;
	
	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private AlfrescoRepository<NotificationRuleListDataItem> alfrescoRepository;

	
	
	@Override
	public void sendNotifications() {
		
		NotificationRuleListDataItem notification;
		Map<String, Object> templateArgs;
		List<Object> entitiesByUser;
		Map<NodeRef, Object> entities;
		Path targetPath = null;
		QName entityType = null, dateField = null;
		String destinationPath = null, fromQuery = null, toQuery = null;
		
		
		for (NodeRef notificationNodeRef : getAllNotificationRule()) {
			templateArgs = new HashMap<>();
			entities = new HashMap<>();
			
			notification = alfrescoRepository.findOne(notificationNodeRef);

			if(notification.getEntityType() == null || notification.getDateField() == null || notification.getAuthorities() == null
					|| !isAllowed(notification.getFrequencyStartDate(), notification.getFrequency())){
				logger.warn("Skip notification : " + notificationNodeRef);
				continue ;
			}
			
			entityType = QName.createQName(notification.getEntityType(), namespaceService);
			dateField = QName.createQName(notification.getDateField(), namespaceService);
			targetPath = nodeService.getPath(notification.getTarget());
			
			destinationPath = targetPath.subPath(2, targetPath.size() - 1).toDisplayPath(nodeService, permissionService) + "/"
					+ nodeService.getProperty(notification.getTarget(), ContentModel.PROP_NAME);
			
			Calendar date = Calendar.getInstance();
			
			if(notification.getTimeType().equals(NotificationRuleTimeType.After)){
				date.add(Calendar.DATE, Math.abs(notification.getDays()));
				if(notification.getDays() > 0){
					fromQuery = "NOW";
					toQuery = formatDate(date);
				}else{
					fromQuery = formatDate(date);
					toQuery = "MAX";
				}
			}else if(notification.getTimeType().equals(NotificationRuleTimeType.Before)){
				date.add(Calendar.DATE, -Math.abs(notification.getDays()));
				if(notification.getDays() > 0){
					fromQuery = formatDate(date);
					toQuery = "NOW";
				}else {
					fromQuery = "MIN";
					toQuery = formatDate(date);
				}
			} else{
				date.add(Calendar.DATE, notification.getDays());
				fromQuery = formatDate(date);
			}

			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery()
					.ofType(entityType)
					.andPropQuery(dateField, notification.getTimeType().equals(NotificationRuleTimeType.Equals)? fromQuery : "[" + fromQuery + " TO " + toQuery + "]")
					.inSubPath(targetPath.toPrefixString(namespaceService));
			
			if (notification.getCondtions() != null && !notification.getCondtions().isEmpty()){
				queryBuilder.andFTSQuery(notification.getCondtions());
			}
			
			List<NodeRef> items = queryBuilder.list();
			
			if(items.isEmpty() || items == null){
				logger.info("No object found for notification: " + notification.getNodeRef());
				continue;
			}
			
			templateArgs.put(SUBJECT, notification.getSubject());
			templateArgs.put(ENTITY_TYPE, dictionaryService.getType(entityType).getTitle(serviceRegistry.getDictionaryService()));
			templateArgs.put(DATE_FIELD, dictionaryService.getProperty(dateField).getTitle(serviceRegistry.getDictionaryService()));
			templateArgs.put(NOTIFICATION_NODEREF, notificationNodeRef.toString());
			templateArgs.put(CONDITIONS, notification.getCondtions());
			templateArgs.put(TARGET_PATH, destinationPath);
			
			
			String emailTemplate = notification.getEmail() != null ? nodeService.getPath(notification.getEmail()).toPrefixString(namespaceService)
					: RepoConsts.EMAIL_NOTIF_RULE_LIST_TEMPLATE;
			
			for (NodeRef nodeRef : items) {
				Map<String, Object> item = new HashMap<>();
				item.put(ENTITY_NODEREF, nodeRef.toString());
				item.put(PARENT_FOLDER, (String) nodeService.getProperty(nodeService.getPrimaryParent(nodeRef).getParentRef(),ContentModel.PROP_NAME));
				item.put(ENTITY_NAME, (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
				item.put(DATE_FIELD_VALUE, (Date) nodeService.getProperty(nodeRef, dateField));
				item.put(SITE_NAME, siteService.getSite(nodeRef).getShortName());
				entities.put(nodeRef, item);
			}
			
			Set<String> authorities = new HashSet<>();
			for(NodeRef authorityRef : notification.getAuthorities()){
				
				for(String userName : extractAuthoritiesFromGroup(authorityRef)){
					
					if(authorities.contains(userName)){
						continue;
					}
					
					authorities.add(userName);
					entitiesByUser = new ArrayList<>();
					
					for(NodeRef nodeRef : items){
						if( AuthenticationUtil.runAs(()->{
							if(permissionService.hasPermission(nodeRef, PermissionService.READ_PERMISSIONS).equals(AccessStatus.ALLOWED)){
								return true;
							}
							return false;
						}, userName)){	
							
							entitiesByUser.add(entities.get(nodeRef));
							
						}
					}
					if(!entitiesByUser.isEmpty()){
						Map<String, Object> templateModel = new HashMap<>();
						HashMap<String, Object> userTemplateArgs = new HashMap<>(templateArgs);
						userTemplateArgs.put("entities", entitiesByUser);
						templateModel.put("args", userTemplateArgs);
						
						logger.info("send mail: "+templateModel);
						mailService.sendMail(Arrays.asList(authorityService.getAuthorityNodeRef(userName)), notification.getSubject(), emailTemplate, templateModel, false);
						
					}
				}
			}								
		}
		
	}

	
	private Set<String> extractAuthoritiesFromGroup(NodeRef authority) {
		Set<String> ret = new HashSet<>();
		if(nodeService.getType(authority).equals(ContentModel.TYPE_AUTHORITY_CONTAINER)){
			String authorityName = (String) nodeService.getProperty(authority, ContentModel.PROP_AUTHORITY_NAME);
			ret = authorityService.getContainedAuthorities(AuthorityType.USER, authorityName, false);
		}else {
			ret.add( (String) nodeService.getProperty(authority, ContentModel.PROP_USERNAME));
		}
		
		return ret;		
	}
	
	
	private String formatDate(Calendar date){
		return date.get(Calendar.YEAR) + "\\-" + 
			   (date.get(Calendar.MONTH) + 1) + "\\-" + 
			   date.get(Calendar.DAY_OF_MONTH);
	}
	
	
	private List<NodeRef> getAllNotificationRule() {
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_NOTIFICATIONRULELIST).inDB();
		List<NodeRef> notificationsNodeRef = queryBuilder.list();
		return notificationsNodeRef;
	}
	
	
	private boolean isAllowed(Date frequencyDate, int frequency){
		final Calendar today = Calendar.getInstance();
		if (today.before(frequencyDate)){
			return false;
		}
		Calendar date = Calendar.getInstance();
		date.setTime(frequencyDate);
		
		switch(frequency){
			case WEEKLY :
				return today.get(Calendar.DAY_OF_WEEK) == date.get(Calendar.DAY_OF_WEEK);
			
			case MONTHLY: 
				return today.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH);
			
			default: 
				return frequency == 0 ? false 
				   : ((TimeUnit.DAYS.convert((today.getTime().getTime() - frequencyDate.getTime()), TimeUnit.MILLISECONDS) % frequency) == 0);
		}
		
	}

}
