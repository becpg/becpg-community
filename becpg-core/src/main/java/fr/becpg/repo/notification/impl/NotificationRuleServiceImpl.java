package fr.becpg.repo.notification.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.notification.NotificationRuleFilter;
import fr.becpg.repo.notification.NotificationRuleService;
import fr.becpg.repo.notification.data.NotificationRuleListDataItem;
import fr.becpg.repo.notification.data.NotificationRuleTimeType;
import fr.becpg.repo.notification.data.VersionFilterType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.AdvSearchPlugin;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>NotificationRuleServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("notificationRuleService")
public class NotificationRuleServiceImpl implements NotificationRuleService {


	private static final Log logger = LogFactory.getLog(NotificationRuleServiceImpl.class);
	
	private static final String DATE_FIELD = "dateField";
	private static final String NODE_TYPE = "type";
	private static final String NODE = "node";
	private static final String NOTIFICATION = "notification";
	private static final String TARGET_PATH = "targetPath";
	private static final String ENTITYV2_SUBTYPE = "isEntityV2SubType";
	private static final String DISPLAY_PATH = "displayPath";
	
	private static final String QUERY = "query";
	private static final String ENTITY_FILTER = "entityFilter";
	private static final String ENTITY_TYPE = "entityType";
	private static final String NODE_FILTER = "nodeFilter";
	private static final String CRITERIA = "criteria";
	
	private static final String SEPARATOR = "\\-";
	
	 private final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>(){
		 	@Override
	    	protected SimpleDateFormat initialValue() {
	    		return new SimpleDateFormat("yyyy" + SEPARATOR + "MM" + SEPARATOR + "dd");
	    	}

		};
	

	@Autowired
	private NodeService nodeService;
	
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
	private VersionService versionService;
	
	@Autowired
	private AlfrescoRepository<NotificationRuleListDataItem> alfrescoRepository;

	@Autowired
	private EntityService entityService;
	
	@Autowired
	private AdvSearchService advSearchService;
	
	@Autowired(required = false)
	private AdvSearchPlugin[] advSearchPlugins;
	
	
	/** {@inheritDoc} */
	@Override
	public void sendNotifications() {
		
		NotificationRuleListDataItem notification;
		Map<String, Object> templateArgs;
		List<Object> entitiesByUser;
		Map<NodeRef, Object> entities;
		Path targetPath = null;
		QName nodeType = null;
		QName dateField = null;
		String destinationPath = null;
		String fromQuery = null;
		String toQuery = null;
		
		
		for (NodeRef notificationNodeRef : getAllNotificationRule()) {
			templateArgs = new HashMap<>();
			entities = new HashMap<>();
			
			notification = alfrescoRepository.findOne(notificationNodeRef);
			
			if(notification.getNodeType() == null || notification.getTarget() == null ||
					!nodeService.exists(notification.getTarget())
					|| notification.getAuthorities() == null || !isAllowed(notification)){
				logger.warn("Skip notification : " + notification);
				continue ;
			}
			//TODO update notification startDate
			notification.setFrequencyStartDate(new Date());
			alfrescoRepository.save(notification);
			
			nodeType = QName.createQName(notification.getNodeType(), namespaceService);
			dateField = QName.createQName(notification.getDateField(), namespaceService);
			targetPath = nodeService.getPath(notification.getTarget());
			
			destinationPath = targetPath.subPath(2, targetPath.size() - 1).toDisplayPath(nodeService, permissionService) + "/"
					+ nodeService.getProperty(notification.getTarget(), ContentModel.PROP_NAME);
			
			Calendar date = Calendar.getInstance();
			
			switch(notification.getTimeType()){
			case After : //[(NOW+DATE) , MAX]
				date.add(Calendar.DATE, notification.getDays());
				fromQuery = formatDate(date);
				toQuery = "MAX";
				break;
			case To : //[NOW , (NOW+DATE)]
				date.add(Calendar.DATE, notification.getDays());
				fromQuery = "NOW";
				toQuery = formatDate(date);
				break;
			case Before : //[MIN , (NOW-DATE)]
				date.add(Calendar.DATE, -notification.getDays());
				fromQuery = "MIN";
				toQuery = formatDate(date);
				break;
			case From : //[(NOW-DATE) , NOW]
				date.add(Calendar.DATE, -notification.getDays());
				fromQuery = formatDate(date);
				toQuery = "NOW";
				break;
			case Equals : // date = NOW + X
				date.add(Calendar.DATE, notification.getDays());
				fromQuery = formatDate(date);
				toQuery = fromQuery;
				break;
			}

			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(nodeType)
					.andPropQuery(dateField, notification.getTimeType().equals(NotificationRuleTimeType.Equals)? fromQuery : "[" + fromQuery + " TO " + toQuery + "]");	
			queryBuilder.inSubPath(targetPath.toPrefixString(namespaceService));
			
			NotificationRuleFilter filter = parseAdvFilter(notification.getCondtions());
			filter.setNodeType(nodeType);
			
			if (logger.isDebugEnabled()) {
				logger.debug("Filter: " + filter);
			}
			
			if (filter != null && !filter.getQuery().isEmpty()){
				queryBuilder.andFTSQuery(filter.getQuery());
			}
			
			List<NodeRef> items =  filter(queryBuilder, filter);
			
			//Versions history filter
			Map<NodeRef, Map<String, NodeRef>> itemVersions = new HashMap<>();
			final VersionFilterType versionType = notification.getVersionFilterType();
			if(!VersionFilterType.NONE.equals(versionType) && dateField.isMatch(ContentModel.PROP_MODIFIED)){
				Iterator<NodeRef> iter = items.iterator();
				while(iter.hasNext()){
					NodeRef item = iter.next();
					Map<String, NodeRef> temp = getOnlyAssociatedVersions(item, versionType, getDate(fromQuery), getDate(toQuery));
					if(!temp.isEmpty()){
						itemVersions.put(item, temp);
					}else {
						iter.remove();
					}
				}
			}
			
			if((items.isEmpty() || items == null || (itemVersions.isEmpty() && !VersionFilterType.NONE.equals(versionType) )) && !notification.isEnforced()){
				logger.warn("No object found for notification: " + notification.getNodeRef());
				continue;
			}
			
			templateArgs.put(NODE_TYPE, dictionaryService.getType(nodeType).getTitle(serviceRegistry.getDictionaryService()));
			templateArgs.put(DATE_FIELD, dictionaryService.getProperty(dateField).getTitle(serviceRegistry.getDictionaryService()));
			templateArgs.put(TARGET_PATH, destinationPath);
			templateArgs.put(NOTIFICATION, notification.getNodeRef());
			
			String emailTemplate = notification.getEmail() != null ? nodeService.getPath(notification.getEmail()).toPrefixString(namespaceService)
					: RepoConsts.EMAIL_NOTIF_RULE_LIST_TEMPLATE;
			
			for (NodeRef nodeRef : items) {
				Map<String, Object> item = new HashMap<>();
				item.put(NODE, nodeRef);
				item.put(DISPLAY_PATH, SiteHelper.extractSiteDisplayPath(nodeService.getPath(nodeRef), permissionService, nodeService, namespaceService));
				item.put(ENTITYV2_SUBTYPE, dictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITY_V2));
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
					
					for (NodeRef nodeRef : items) {
						if (Boolean.TRUE.equals(AuthenticationUtil.runAs(
								() -> permissionService.hasPermission(nodeRef, PermissionService.READ_PERMISSIONS).equals(AccessStatus.ALLOWED),
								userName))) {

							entitiesByUser.add(entities.get(nodeRef));

						}
					}
					if(!entitiesByUser.isEmpty() || notification.isEnforced()){			
						Map<String, Object> templateModel = new HashMap<>();
						HashMap<String, Object> userTemplateArgs = new HashMap<>(templateArgs);
						userTemplateArgs.put("entities", entitiesByUser);
						if(!VersionFilterType.NONE.equals(versionType)){
							userTemplateArgs.put("versions", itemVersions);
						}
						templateModel.put("args", userTemplateArgs);
						
						mailService.sendMail(Arrays.asList(authorityService.getAuthorityNodeRef(userName)), notification.getSubject(), emailTemplate, templateModel, false);	
					} 
				}
			}								
		}
		
	}

	
	private Map<String, NodeRef> getOnlyAssociatedVersions(NodeRef item, VersionFilterType versionType, Date from, Date to) {
		Map<String, NodeRef> ret = new HashMap<>();
		    if(from!=null && to !=null && versionService.getVersionHistory(item) != null){
		    	versionService.getVersionHistory(item).getAllVersions().forEach(version-> {
		    		Date createDate = version.getFrozenModifiedDate();
		    		if((versionType!=null && versionType.match(version.getVersionType())) && !RepoConsts.INITIAL_VERSION.equals(version.getVersionLabel())  
		    				&& (from.equals(to) ? formatter.get().format(createDate).equals(formatter.get().format(from)) : (createDate.after(from) && createDate.before(to)))){
		    			ret.put(version.getVersionLabel() + "|" + version.getDescription(), version.getFrozenStateNodeRef());
		    		}
		    	});
		    }
		return ret;
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

	
	private Date getDate(String strDate){
		Date date = null;
		
		if(strDate!=null) {
			if(strDate.equals("MAX")){
				date = new Date(Long.MAX_VALUE);
			}else if (strDate.equals("MIN")){
				date = new Date(0L);
			}else if (strDate.equals("NOW")){
				date = new Date();
			}else {
				try {
					date = formatter.get().parse(strDate);
				} catch (ParseException e) {
					logger.error("Wrong date format ", e);
				}			
			}
		}
		
		return date;
	}
	
	
	private List<NodeRef> getAllNotificationRule() {
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_NOTIFICATIONRULELIST).inDB();
		return queryBuilder.list();
	}
	
	
	private boolean isAllowed(NotificationRuleListDataItem notification){

		LocalDate now = LocalDate.now();
		
		int frequency = notification.getFrequency();
		
		Date date = notification.getFrequencyStartDate() != null ? notification.getFrequencyStartDate() 
				: (Date) nodeService.getProperty(notification.getNodeRef(), ContentModel.PROP_CREATED);

		LocalDate lastDate = date.toInstant()
			      .atZone(ZoneId.systemDefault())
			      .toLocalDate(); 

		if (now.isBefore(lastDate) || frequency < 1){
			return false;
		}
		
		switch(notification.getRecurringTime()){
		case Day:
			lastDate = lastDate.plusDays(frequency);
			break;
		case Week:
			lastDate = lastDate.plusWeeks(frequency);
			break;
		case Month:
			lastDate = lastDate.plusMonths(frequency);
			break;
		case Year:
			lastDate = lastDate.plusYears(frequency);
			break;
		}
		
		if(notification.getRecurringDay() != null){
			return (now.equals(lastDate) || now.isAfter(lastDate)) && now.getDayOfWeek().equals(notification.getRecurringDay());
		}
		
		return now.equals(lastDate) || now.isAfter(lastDate);
	}
	
	private List<NodeRef> filter(BeCPGQueryBuilder query, NotificationRuleFilter filter) {
		List<NodeRef> ret = advSearchService.queryAdvSearch(filter.getNodeType(), query, filter.getNodeCriteria(),
				RepoConsts.MAX_RESULTS_UNLIMITED);
		if(filter.getEntityCriteria() != null && filter.getEntityType() != null) {
			ret = filterByEntityCriteria(ret, filter);
		}
		return ret;
	}
	
	private List<NodeRef> filterByEntityCriteria(List<NodeRef> nodes, NotificationRuleFilter filter) {
		if(logger.isDebugEnabled()) {
			logger.debug("Filter by entity criteria, size before: "+nodes.size()); 
		}
		
		List<NodeRef> ret = new ArrayList<>();
		if(filter.getEntityCriteria()!=null && ! filter.getEntityCriteria().isEmpty()) {
			
			BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(filter.getEntityType()).excludeDefaults();
			List<NodeRef> entities = advSearchService.queryAdvSearch(filter.getEntityType(), queryBuilder, filter.getEntityCriteria(),
					RepoConsts.MAX_RESULTS_UNLIMITED);
			for(NodeRef nodeRef : nodes) {
				if(entities.contains(nodeRef)) {
					ret.add(nodeRef);
				}
			}
			
		} else {
			for(NodeRef nodeRef : nodes) {
				NodeRef entityRef = entityService.getEntityNodeRef(nodeRef, nodeService.getType(nodeRef));
				if(entityRef!=null && matchEntityType(entityRef, filter.getEntityType())) {
					ret.add(nodeRef);
				}
			}
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug(" - new size: "+ret.size()); 
		}
		return ret;
	}

	private boolean matchEntityType(NodeRef entityRef, QName entityType) {
		return dictionaryService.isSubClass(nodeService.getType(entityRef), entityType);
	}
	
	private NotificationRuleFilter parseAdvFilter(String jsonString) {
		NotificationRuleFilter filter = new NotificationRuleFilter();
		try {
			if ((jsonString != null) && !jsonString.isEmpty()) {
				JSONObject filterObject = new JSONObject(jsonString);
				if(filterObject.has(QUERY)) {
					filter.setQuery(filterObject.getString(QUERY));
				}
				if(filterObject.has(ENTITY_FILTER)) {
					JSONObject entityFilter = filterObject.getJSONObject(ENTITY_FILTER);
					if(entityFilter.has(ENTITY_TYPE)) {
						filter.setEntityType(QName.createQName(entityFilter.getString(ENTITY_TYPE), namespaceService));
					}
					if(entityFilter.has(CRITERIA)) {
						JSONObject entityAssocs = entityFilter.getJSONObject(CRITERIA);
						filter.setEntityCriteria(JsonHelper.extractCriteria(entityAssocs));
					}
				}
				if(filterObject.has(NODE_FILTER)) {
					JSONObject nodeFilter = filterObject.getJSONObject(NODE_FILTER);
					if(nodeFilter.has(CRITERIA)) {
						JSONObject nodeAssocs = nodeFilter.getJSONObject(CRITERIA);
						filter.setNodeCriteria(JsonHelper.extractCriteria(nodeAssocs));
					}
				}
			}

		} catch (JSONException e) {
			if(jsonString.contains("{")) {
				logger.warn("Invalid JSON notification filter", e);
			} else {
				filter.setQuery(jsonString);
			}
			 
		}
		
		return filter;
	}
	


}
