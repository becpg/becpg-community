package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.productList.AllergenType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.search.BeCPGSearchService;

@Service
public class EntityTplServiceImpl implements EntityTplService {

	private static final String QUERY_ENTITY_TEMPLATE = " +TYPE:\"%s\" +@bcpg\\:entityTplEnabled:true +@bcpg\\:entityTplIsDefault:true -ASPECT:\"bcpg:compositeVersion\"";
	private static final String QUERY_LOAD_CHARACTS = " +TYPE:\"%s\"";
	
	private static Log logger = LogFactory.getLog(EntityTplServiceImpl.class);
	
	private NodeService nodeService;
	
	private EntityListDAO entityListDAO;
	
	private DictionaryService dictionaryService;
	
	private BeCPGSearchService beCPGSearchService;
	
	private FormulationService<RepositoryEntity> formulationService;
	
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setFormulationService(FormulationService<RepositoryEntity> formulationService) {
		this.formulationService = formulationService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setRepositoryEntityDefReader(RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader) {
		this.repositoryEntityDefReader = repositoryEntityDefReader;
	}

	/**
	 * Create the entityTpl
	 * @param entityTplsNodeRef
	 * @param entityType
	 */
	@Override
	public NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, boolean enabled, Set<QName> entityLists, Set<String> subFolders){
		
		TypeDefinition typeDef = dictionaryService.getType(entityType);
		String entityTplName = typeDef.getTitle();
		
		// entityTpl
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, entityTplName);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_ENABLED, enabled);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_IS_DEFAULT, true);
		
		NodeRef entityTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, entityTplName);
		if(entityTplNodeRef == null){
			entityTplNodeRef = nodeService.createNode(parentNodeRef, 
														ContentModel.ASSOC_CONTAINS, 
														QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, 
																			entityType.getLocalName()), 
																			entityType, properties).getChildRef();
		}
		
		
		//  entityLists			
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityTplNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = entityListDAO.createListContainer(entityTplNodeRef);
		}
		
		if(entityLists != null){
			for(QName entityList : entityLists){
				
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, entityList);
				if(listNodeRef == null){
					listNodeRef = entityListDAO.createList(listContainerNodeRef, entityList);
					initializeList(listNodeRef, entityList);
				}
			}
		}	
		
		// subFolders
		if(subFolders != null){				
			for(String subFolder : subFolders){
				
				properties.clear();
				properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(subFolder));
				NodeRef documentsFolderNodeRef = nodeService.getChildByName(entityTplNodeRef, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));
				if(documentsFolderNodeRef == null){			
					nodeService.createNode(entityTplNodeRef, 
											ContentModel.ASSOC_CONTAINS, 
											QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(subFolder)), 
											ContentModel.TYPE_FOLDER, 
											properties).getChildRef();
				}								
			}
		}
		
		return entityTplNodeRef;
	}

	/**
	 * Get the entityTpl
	 */
	@Override
	public NodeRef getEntityTpl(QName nodeType) {
		
		if(nodeType == null){
			return null;
		}
		
		List<NodeRef> tplsNodeRef = beCPGSearchService.luceneSearch(String.format(QUERY_ENTITY_TEMPLATE, nodeType));        
        return tplsNodeRef!=null && !tplsNodeRef.isEmpty() ? tplsNodeRef.get(0) : null;
	}
	
	private void initializeList(NodeRef listNodeRef, QName listType){
		
		String query = null;
		QName associationQName = null;
		
		//TODO : to do more generic
		if (listType.equals(BeCPGModel.TYPE_ALLERGENLIST)) {
			query = String.format(QUERY_LOAD_CHARACTS, BeCPGModel.TYPE_ALLERGEN);
			query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_ALLERGEN_TYPE, AllergenType.Major.toString(), LuceneHelper.Operator.AND);
			associationQName = BeCPGModel.ASSOC_ALLERGENLIST_ALLERGEN;
		} else if (listType.equals(BeCPGModel.TYPE_COSTLIST)) {
			query = String.format(QUERY_LOAD_CHARACTS, BeCPGModel.TYPE_COST);
			associationQName = BeCPGModel.ASSOC_COSTLIST_COST;
		} else if (listType.equals(BeCPGModel.TYPE_NUTLIST)) {
			query = String.format(QUERY_LOAD_CHARACTS, BeCPGModel.TYPE_NUT);
			associationQName = BeCPGModel.ASSOC_NUTLIST_NUT;
		} else if (listType.equals(BeCPGModel.TYPE_ORGANOLIST)) {
			query = String.format(QUERY_LOAD_CHARACTS, BeCPGModel.TYPE_ORGANO);
			associationQName = BeCPGModel.ASSOC_ORGANOLIST_ORGANO;
		} /*else if (listType.equals(BeCPGModel.TYPE_PHYSICOCHEMLIST)) {
			query = String.format(QUERY_LOAD_CHARACTS, BeCPGModel.TYPE_PHYSICO_CHEM);
			associationQName = BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM;
		}*/
		
		if(query != null){
			
			List<NodeRef> characts = beCPGSearchService.luceneSearch(query, LuceneHelper.getSort(ContentModel.PROP_NAME), RepoConsts.MAX_RESULTS_256);
			
			for(NodeRef charact : characts){
				
				Map<QName, List<NodeRef>> associations = new HashMap<QName, List<NodeRef>>();
				List<NodeRef> targetNodes = new ArrayList<NodeRef>();
				targetNodes.add(charact);
				associations.put(associationQName, targetNodes);
				
				entityListDAO.createListItem(listNodeRef, listType, new HashMap<QName, Serializable>(), associations);
			}
		}		
	}

	@Override
	public void synchronizeEntities(NodeRef tplNodeRef) {
		
		logger.debug("synchronizeEntities");
		RepositoryEntity entityTpl = alfrescoRepository.findOne(tplNodeRef);
		Map<QName, List<? extends RepositoryEntity>> datalistsTpl = repositoryEntityDefReader.getDataLists(entityTpl);
		
		if (datalistsTpl != null && !datalistsTpl.isEmpty()) {
			
			List<NodeRef> entityNodeRefs = getEntitiesToUpdate(tplNodeRef);
			logger.debug("synchronize entityNodeRefs, size " + entityNodeRefs.size());
			
			for(NodeRef entityNodeRef : entityNodeRefs){
				
				RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
				Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);
				NodeRef listContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(entity);
				
				for (QName dataListQName : datalistsTpl.keySet()) {
					
					List<BeCPGDataObject> dataListItems = (List)datalists.get(dataListQName);
					boolean update = false;
					
					for(RepositoryEntity dataListItemTpl : datalistsTpl.get(dataListQName)){
						
						Map<QName, Serializable> identAttrTpl = repositoryEntityDefReader.getIdentifierAttributes(dataListItemTpl);
						
						if(!identAttrTpl.isEmpty()){
							boolean isFound = false;
							
							// look on instance
							for(RepositoryEntity dataListItem : dataListItems){
								
								Map<QName, Serializable> identAttr = repositoryEntityDefReader.getIdentifierAttributes(dataListItem);																
								if(identAttrTpl.equals(identAttr)){
									isFound = true;
									break;
								}
							}
							
							if(!isFound){
								dataListItemTpl.setNodeRef(null);
								dataListItemTpl.setParentNodeRef(null);
								dataListItems.add((BeCPGDataObject)dataListItemTpl);
								update = true;
							}
						}						
					}		
					if(update){
						alfrescoRepository.saveDataList(listContainerNodeRef, dataListQName, dataListQName, dataListItems);
					}					
				}								
			}
		}		
	}

	@Override
	public void formulateEntities(NodeRef tplNodeRef) throws FormulateException {
		
		List<NodeRef> entityNodeRefs = getEntitiesToUpdate(tplNodeRef);
		
		for(NodeRef entityNodeRef : entityNodeRefs){
			formulationService.formulate(entityNodeRef);
		}
	}
	
	private List<NodeRef> getEntitiesToUpdate(NodeRef tplNodeRef){
		
		List<NodeRef> entityNodeRefs = new ArrayList<>();
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(tplNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);
		
		for(AssociationRef assocRef : assocRefs){
			if(!nodeService.hasAspect(assocRef.getSourceRef(), BeCPGModel.ASPECT_COMPOSITE_VERSION)){
				entityNodeRefs.add(assocRef.getSourceRef());
			}
		}
		
		return entityNodeRefs;
	}
}
