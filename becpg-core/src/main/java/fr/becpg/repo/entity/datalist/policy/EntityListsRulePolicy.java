package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public class EntityListsRulePolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.BeforeDeleteNodePolicy,
		NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

	private static final String ENTITY_LISTS_RULE_FOLDER = "/app:company_home/cm:System/cm:EntityListsRules";

	private static final String CACHE_KEY = EntityListsRulePolicy.class.getName();
	
	private Repository repositoryHelper;

	private FileFolderService fileFolderService;

	private ScriptService scriptService;

	private RuleService ruleService;
	
	private DictionaryService dictionaryService;
	
	private BeCPGCacheService beCPGCacheService;
	
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}
	
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	public void setScriptService(ScriptService scriptService) {
		this.scriptService = scriptService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
				BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "beforeDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
				BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onDeleteAssociation"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onUpdateProperties"));
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		queueNode("onUpdate", nodeRef);
	}

	@Override
	public void onDeleteAssociation(AssociationRef nodeAssocRef) {
//		queueNode("onDelete", nodeAssocRef.getTargetRef());
	}

	@Override
	public void onCreateAssociation(AssociationRef nodeAssocRef) {
		queueNode("onCreate", nodeAssocRef.getTargetRef());
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		queueNode("onCreate", childAssocRef.getChildRef());
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
//		queueNode("onDelete", nodeRef);
	}
	
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef pendingNode : pendingNodes) {
			executeRules(pendingNode, key);
		}
		return true;
	}

	private void executeRules(NodeRef listNodeRef, String ruleType) {
		String listName = nodeService.getType(listNodeRef).getLocalName();
		executeRule(ruleType, listName);
		executeRule(ruleType, "entityList");
	}

	private void executeRule(String ruleType, String listName) {
		
//		NodeRef spaceRef = null;
//        if (spaceRef == null)
//        {
//            // the actionedUponNodeRef may actually be a space
//            if (this.serviceRegistry.getDictionaryService().isSubClass(
//                    nodeService.getType(actionedUponNodeRef), ContentModel.TYPE_FOLDER))
//            {
//                spaceRef = actionedUponNodeRef;
//            }
//            else
//            {
//                spaceRef = nodeService.getPrimaryParent(actionedUponNodeRef).getParentRef();
//            }
//        }
//        
//        if (this.scriptLocation != null || (scriptRef != null && nodeService.exists(scriptRef) == true))
//        {
//            // get the references we need to build the default scripting data-model
//            String userName = this.serviceRegistry.getAuthenticationService().getCurrentUserName();
//            NodeRef personRef = null;
//            NodeRef homeSpaceRef = null;
//            
//           try {
//                personRef = this.personService.getPerson(userName);
//                homeSpaceRef = (NodeRef)nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
//                
//            } catch (NoSuchPersonException e) {
//				// beCPG Task 
//			}
//           
//           
//            // the default scripting model provides access to well known objects and searching
//            // facilities - it also provides basic create/update/delete/copy/move services
//            Map<String, Object> model = scriptService.buildDefaultModel(
//                    personRef,
//                    getCompanyHome(),
//                    homeSpaceRef,
//                    scriptRef,
//                    actionedUponNodeRef,
//                    spaceRef);
//            
//            // Add the action to the default model
//            ScriptAction scriptAction = new ScriptAction(this.serviceRegistry, action, this.actionDefinition);
//            model.put("action", scriptAction);
//
//            model.put("webApplicationContextUrl", UrlUtil.getAlfrescoUrl(sysAdminParams)); 
		NodeRef ruleNodeRef = findRuleNodeRef(listName + "-" + ruleType + ".js");
		if (ruleNodeRef != null) {
//			scriptService.executeScript(ruleNodeRef, ContentModel.PROP_CONTENT, null);
		}
	}

	private NodeRef findRuleNodeRef(String ruleName) {
		return beCPGCacheService.getFromCache(CACHE_KEY, ruleName, () -> {
			NodeRef folder = BeCPGQueryBuilder.createQuery().inDB().selectNodeByPath(repositoryHelper.getCompanyHome(),
					ENTITY_LISTS_RULE_FOLDER);
			if (folder != null) {
				Optional<NodeRef> opt = fileFolderService.listFiles(folder).stream().map(f -> f.getNodeRef())
						.filter(n -> nodeService.getProperty(n, ContentModel.PROP_NAME).equals(ruleName)).findFirst();
				if (opt.isPresent()) {
					return opt.get();
				}
			}
			return null;
		});
	}

}
