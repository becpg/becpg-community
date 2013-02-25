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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.productList.AllergenType;
import fr.becpg.repo.search.BeCPGSearchService;

@Service
public class EntityTplServiceImpl implements EntityTplService {

	private static final String QUERY_ENTITY_TEMPLATE = " +TYPE:\"%s\" +@bcpg\\:entityTplEnabled:true +@bcpg\\:entityTplIsDefault:true";
	private static final String QUERY_LOAD_CHARACTS = " +TYPE:\"%s\"";
	
	private NodeService nodeService;
	
	private EntityListDAO entityListDAO;
	
	private DictionaryService dictionaryService;
	
	private BeCPGSearchService beCPGSearchService;
	
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
}
