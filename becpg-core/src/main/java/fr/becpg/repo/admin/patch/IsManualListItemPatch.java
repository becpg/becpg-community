package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * IsManualListItemPatch
 * 
 * @author quere
 * 
 */
public class IsManualListItemPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(IsManualListItemPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.isManualListItemPatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	private DictionaryService dictionaryService;
	

	private final int batchThreads = 3;
	private final int batchSize = 40;
	private final long count = batchThreads * batchSize;
	
	

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		HashMap<QName, List<PropertyDefinition>> mapAspect = new HashMap<>();
		for(QName aspect : dictionaryService.getAllAspects()){
			AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
			if(!aspectDef.getName().equals(ContentModel.ASPECT_AUDITABLE) && !aspectDef.getName().equals(ContentModel.ASPECT_REFERENCEABLE)){
				List<PropertyDefinition> propDefs = getEnforcedProps(aspectDef.getProperties().values());
				if(!propDefs.isEmpty()){
					logger.info("add aspect " + aspect + " propDefs " + propDefs);
					mapAspect.put(aspect, propDefs);
				}
			}				
		}
		HashMap<QName, List<PropertyDefinition>> mapType = new HashMap<>();
		for(QName type : dictionaryService.getAllTypes()){
			TypeDefinition typeDef = dictionaryService.getType(type);
			List<PropertyDefinition> propDefs = getEnforcedProps(typeDef.getProperties().values());
			if(!propDefs.isEmpty()){
				logger.info("add type " + type + " propDefs " + propDefs);
				mapType.put(type, propDefs);
			}			
		}
		for(QName aspect : mapAspect.keySet()){
			logger.info("aspect " + aspect);
			doForAspect(aspect, mapAspect, mapType, true);
		}
		
		for(QName type : mapType.keySet()){
			logger.info("type " + type);
			doForAspect(type, mapAspect, mapType, true);
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}
	
	private List<PropertyDefinition> getEnforcedProps(Collection<PropertyDefinition> propertyDefs){
		List<PropertyDefinition> propDefs = new ArrayList<>();		
		for(PropertyDefinition propDef : propertyDefs){
			if(propDef.isMandatoryEnforced() && propDef.getDefaultValue() != null && !propDef.getName().equals(ContentModel.PROP_NAME)){										
				propDefs.add(propDef);
			}
		}		
		return propDefs;
	}
		
	private void doForAspect(final QName aspect, final Map<QName, List<PropertyDefinition>> mapAspect, final Map<QName, List<PropertyDefinition>> mapTypes, final boolean isAspect) {
		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 1;
			long maxSearchNodeId = count;

			final Pair<Long, QName> val = getQnameDAO().getQName(aspect);

			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && minSearchNodeId < maxNodeId) {
						List<Long> nodeids;
						if(isAspect){
							nodeids = getPatchDAO().getNodesByAspectQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);
						}
						else{
							nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);
						}
						

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + count;
						maxSearchNodeId = maxSearchNodeId + count;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("IsManualListItemPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, batchThreads, batchSize, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

			public void afterProcess() throws Throwable {
				ruleService.enableRules();
				
			}

			public void beforeProcess() throws Throwable {
				ruleService.disableRules();
			}

			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			public void process(NodeRef dataListNodeRef) throws Throwable {
				if (nodeService.exists(dataListNodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					policyBehaviourFilter.disableBehaviour();
					for(QName aspect : mapAspect.keySet()){
						//logger.info("aspectDef " + aspectDef.getName());
						if(nodeService.hasAspect(dataListNodeRef, aspect)){
							updatePropertyDefs(dataListNodeRef, mapAspect.get(aspect));
						}
					}
					
					if(isAspect){
						QName type = nodeService.getType(dataListNodeRef);
						if(mapTypes.containsKey(type)){
							updatePropertyDefs(dataListNodeRef, mapTypes.get(type));
						}
					}

				} else {
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
				}
			}

		};

		// Now set the batch processor to work

		batchProcessor.process(worker, true);
	}
	
	private void updatePropertyDefs(NodeRef dataListNodeRef, Collection<PropertyDefinition> propertyDefs){
		for(PropertyDefinition propDef : propertyDefs){
			//logger.info("propDef " + propDef.getName());
			if(nodeService.getProperty(dataListNodeRef, propDef.getName()) == null){
				nodeService.setProperty(dataListNodeRef, propDef.getName(), propDef.getDefaultValue());
				logger.info("set enforced prop " + propDef.getName() + " default value " + propDef.getDefaultValue());
			}
		}
	}

	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

}
