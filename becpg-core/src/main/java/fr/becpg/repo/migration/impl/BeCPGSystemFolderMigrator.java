package fr.becpg.repo.migration.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.hierarchy.HierarchyHelper;
import fr.becpg.repo.product.hierarchy.HierarchyService;
import fr.becpg.repo.search.BeCPGSearchService;

//MOVE that to admin.patch
@Deprecated
public class BeCPGSystemFolderMigrator {

	public static final String PATH_PRODUCT_HIERARCHY = "System/ProductHierarchy";

	
	private static final QName PROP_LINKED_VALUE_VALUE = QName.createQName(BeCPGModel.BECPG_URI,
			"lkvValue");
	
	private static final String PATH_QUERY_SUGGEST_LKV_VALUE_ALL  = " +PATH:\"/app:company_home/%s/*\" +TYPE:\"bcpg:linkedValue\" +@bcpg\\:lkvPrevValue:\"%s\" ";
	

	private static final String PATH_HIERARCHY_RAWMATERIAL_HIERARCHY = "RawMaterial_Hierarchy";
	private static final String PATH_HIERARCHY_PACKAGINGMATERIAL_HIERARCHY = "PackagingMaterial_Hierarchy";
	private static final String PATH_HIERARCHY_SEMIFINISHEDPRODUCT_HIERARCHY = "SemiFinishedProduct_Hierarchy";
	private static final String PATH_HIERARCHY_FINISHEDPRODUCT_HIERARCHY = "FinishedProduct_Hierarchy";
	private static final String PATH_HIERARCHY_LOCASEMIFINISHEDPRODUCT_HIERARCHY = "LocalSemiFinishedProduct_Hierarchy";
	private static final String PATH_HIERARCHY_PACKAGINGKIT_HIERARCHY = "PackagingKit_Hierarchy";
	private static final String PATH_HIERARCHY_RESOURCEPRODUCT_HIERARCHY = "ResourceProduct_Hierarchy";

	private static Log logger = LogFactory.getLog(BeCPGSystemFolderMigrator.class);
	
	private Repository repository;

	private NodeService nodeService;

	private RepoService repoService;

	private FileFolderService fileFolderService;

	private InitVisitor initRepoVisitor;

	private BeCPGSearchService beCPGSearchService;
	
	private NamespaceService namespaceService;
	
	private EntitySystemService entitySystemService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	private VersionService versionService;
	
	private NodeService dbNodeService;
	
	private DictionaryService dictionaryService;
	
	private HierarchyService hierarchyService;
	
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setInitRepoVisitor(InitVisitor initRepoVisitor) {
		this.initRepoVisitor = initRepoVisitor;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}
	
	public void setEntitySystemService(EntitySystemService entitySystemService) {
		this.entitySystemService = entitySystemService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	public void setDbNodeService(NodeService dbNodeService) {
		this.dbNodeService = dbNodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

	public void migrate()  {
		logger.info("start migration");
		
		NodeRef systemNodeRef = getFolder(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM);
		NodeRef caractNodeRef = getFolder(systemNodeRef, RepoConsts.PATH_CHARACTS);
		NodeRef productHierarchyNodeRef = getFolder(systemNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);

			logger.info("Move old folders");
			try {
				fileFolderService.move(caractNodeRef, systemNodeRef, "TODELETE_Characteristics");
				
				fileFolderService.move(productHierarchyNodeRef, systemNodeRef, "TODELETE_Producthierarchy");
	
				logger.info("Start init repo");
				initRepoVisitor.visitContainer(repository.getCompanyHome());
	
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_NUTS), caractNodeRef, RepoConsts.PATH_NUTS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_INGS), caractNodeRef, RepoConsts.PATH_INGS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_ORGANOS), caractNodeRef, RepoConsts.PATH_ORGANOS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_ALLERGENS), caractNodeRef, RepoConsts.PATH_ALLERGENS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_COSTS), caractNodeRef, RepoConsts.PATH_COSTS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_PHYSICO_CHEM), caractNodeRef, RepoConsts.PATH_PHYSICO_CHEM);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_MICROBIOS), caractNodeRef, RepoConsts.PATH_MICROBIOS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_GEO_ORIGINS), caractNodeRef, RepoConsts.PATH_GEO_ORIGINS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_BIO_ORIGINS), caractNodeRef, RepoConsts.PATH_BIO_ORIGINS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_SUBSIDIARIES), caractNodeRef, RepoConsts.PATH_SUBSIDIARIES);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_TRADEMARKS), caractNodeRef, RepoConsts.PATH_TRADEMARKS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_PLANTS), caractNodeRef, RepoConsts.PATH_PLANTS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_CERTIFICATIONS), caractNodeRef, RepoConsts.PATH_CERTIFICATIONS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_APPROVALNUMBERS), caractNodeRef, RepoConsts.PATH_APPROVALNUMBERS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_PROCESSSTEPS), caractNodeRef, RepoConsts.PATH_PROCESSSTEPS);
				migratePath(getCharactDataList(getSystemCharactsEntity(systemNodeRef), RepoConsts.PATH_VARIANT_CHARACTS), caractNodeRef, RepoConsts.PATH_VARIANT_CHARACTS);
	
				NodeRef listNodeRef = getFolder(caractNodeRef, RepoConsts.PATH_LISTS);
	
				migratePath(getCharactDataList(getSystemListValuesEntity(systemNodeRef), RepoConsts.PATH_ING_TYPES), listNodeRef, RepoConsts.PATH_ING_TYPES);
	
				recreateExistinghierarchy(getSystemHierachiesEntity(systemNodeRef));
			
			} catch (FileExistsException e) {
				logger.error(e,e);
			} catch (FileNotFoundException e) {
				logger.error(e,e);
			}


	}
	
	private void recreateExistinghierarchy(NodeRef hierarchiesNodeRef) {
		Map<String, QName> entityLists = new HashMap<String, QName>();

		entityLists.put(PATH_HIERARCHY_RAWMATERIAL_HIERARCHY, BeCPGModel.TYPE_RAWMATERIAL);
		entityLists.put(PATH_HIERARCHY_PACKAGINGMATERIAL_HIERARCHY, BeCPGModel.TYPE_PACKAGINGMATERIAL);
		entityLists.put(PATH_HIERARCHY_SEMIFINISHEDPRODUCT_HIERARCHY, BeCPGModel.TYPE_SEMIFINISHEDPRODUCT);
		entityLists.put(PATH_HIERARCHY_FINISHEDPRODUCT_HIERARCHY, BeCPGModel.TYPE_FINISHEDPRODUCT);
		entityLists.put(PATH_HIERARCHY_LOCASEMIFINISHEDPRODUCT_HIERARCHY, BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT);
		entityLists.put(PATH_HIERARCHY_PACKAGINGKIT_HIERARCHY, BeCPGModel.TYPE_PACKAGINGKIT);
		entityLists.put(PATH_HIERARCHY_RESOURCEPRODUCT_HIERARCHY, BeCPGModel.TYPE_RESOURCEPRODUCT);

		for (Map.Entry<String, QName> entry : entityLists.entrySet()) {
			List<String> hierachies1 = getExistingHierachies1(entry.getKey());
			NodeRef dataListNodeRef = getCharactDataList(hierarchiesNodeRef, entry.getKey());
			for (String hierarchy1 : hierachies1) {
				NodeRef hierarchy1NodeRef = hierarchyService.createHierarchy1(dataListNodeRef, hierarchy1);
				List<String> hierachies2 = getExistingHierachies2(entry.getKey(), hierarchy1);
				for (String hierarchy2 : hierachies2) {
					NodeRef hierarchy2NodeRef  = hierarchyService.createHierarchy2(dataListNodeRef, hierarchy1NodeRef, hierarchy2);
					migrateProduct(hierarchiesNodeRef, entry.getValue(),hierarchy1,hierarchy2,hierarchy1NodeRef,hierarchy2NodeRef);
				}
			}
		}
	}

	private void migrateProduct(NodeRef hierarchiesNodeRef, QName type, String hierarchy1, String hierarchy2, NodeRef hierarchy1NodeRef, NodeRef hierarchy2NodeRef) {
		logger.info("Migrate all products of type: "+type.getLocalName()+" hierarchy1 :"+hierarchy1+" hierarchy2 :"+hierarchy2);

		String QUERY_SUGGEST_PRODUCT_BY_NAME = " +TYPE:\"%s\" +@bcpg\\:productHierarchy1:(%s) +@bcpg\\:productHierarchy2:(%s)";
		
		String queryPath = String.format(QUERY_SUGGEST_PRODUCT_BY_NAME,type.toPrefixString(namespaceService), hierarchy1, hierarchy2  );

		logger.debug("search products, queryPath: " + queryPath);
		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, LuceneHelper.getSort(ContentModel.PROP_NAME));
		
		if(!ret.isEmpty()){
			logger.info("Found "+ret.size()+" product to migrate");
			for (NodeRef nodeRef : ret) {
				
				logger.debug("migrate product : " + nodeRef);
				
				try{
					//disable policy to classify on product
					policyBehaviourFilter.disableBehaviour(nodeRef, BeCPGModel.ASPECT_PRODUCT);
					policyBehaviourFilter.disableBehaviour(nodeRef, ReportModel.ASPECT_REPORT_ENTITY);
					policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
					
					nodeService.removeProperty(nodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1);
					nodeService.setProperty(nodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1, hierarchy1NodeRef);
					nodeService.removeProperty(nodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2);
					nodeService.setProperty(nodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2, hierarchy2NodeRef);
				}			
		        finally{
		        	policyBehaviourFilter.disableBehaviour(nodeRef, BeCPGModel.ASPECT_PRODUCT);
		        	policyBehaviourFilter.enableBehaviour(nodeRef, ReportModel.ASPECT_REPORT_ENTITY);	
		        	policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
		        }	
								
				migrateHistory(hierarchiesNodeRef, type, nodeRef, hierarchy1, hierarchy1NodeRef, hierarchy2, hierarchy2NodeRef);
			}			
		}		
	}
	
	private void migrateHistory(NodeRef hierarchiesNodeRef, QName productType, NodeRef nodeRef, String hierarchy1, NodeRef hierarchy1NodeRef, String hierarchy2, NodeRef hierarchy2NodeRef){
		
		// update version history
		VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);								
		
		if(versionHistory != null){
			for(Version version : versionHistory.getAllVersions()){
				
				// workaround to work on node with dbNodeService (before: versionStore://version2Store/81ad3333-ec94-4b39-a2cb-00528df1b572, after: workspace://version2Store/81ad3333-ec94-4b39-a2cb-00528df1b572)
				NodeRef beforeVersionNodeRef = version.getFrozenStateNodeRef();
				NodeRef afterVersionNodeRef = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, "version2Store", beforeVersionNodeRef.getId());
				logger.debug("Update version history: " + afterVersionNodeRef);
				
				if(dbNodeService.exists(afterVersionNodeRef)){
					
					// check
					String versionHierarchy1 = (String)nodeService.getProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1);
					if(versionHierarchy1 != null){
						
						if(versionHierarchy1.equals(hierarchy1)){
							logger.debug("getOrcreateDeletedHierarchy hierarchy1: " + versionHierarchy1);
							hierarchy1NodeRef = getOrcreateDeletedHierarchy(hierarchiesNodeRef, productType, null, versionHierarchy1);
						}						
					}
					else{
						hierarchy1NodeRef = null;
					}
					String versionHierarchy2 = (String)nodeService.getProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2);
					if(versionHierarchy2 != null){
						
						if(!versionHierarchy2.equals(hierarchy2)){
							logger.debug("getOrcreateDeletedHierarchy hierarchy2: " + versionHierarchy2);
							hierarchy2NodeRef = getOrcreateDeletedHierarchy(hierarchiesNodeRef, productType, hierarchy1NodeRef, versionHierarchy2);
						}						
					}
					else{
						hierarchy2NodeRef = null;
					}
					
					if(hierarchy1NodeRef != null && hierarchy2NodeRef!= null){
						dbNodeService.removeProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1);
						dbNodeService.setProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1, hierarchy1NodeRef);					
						dbNodeService.removeProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2);
						dbNodeService.setProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2, hierarchy2NodeRef);
					}					
				}						
			}
		}	
	}
	
	/**
	 * Fix the deleted hierarchies
	 */
	public void fixDeletedHierarchies(){
				
		logger.debug("Start fixDeletedHierarchies");
		NodeRef systemNodeRef = getFolder(repository.getCompanyHome(), RepoConsts.PATH_SYSTEM);
		NodeRef hierarchiesNodeRef = getSystemHierachiesEntity(systemNodeRef);
		
		for(QName productType : dictionaryService.getSubTypes(BeCPGModel.TYPE_PRODUCT, false)){
			
			logger.debug("fix product type: " + productType);
			
			String QUERY_PRODUCT_TO_FIX = String.format("+TYPE:\"%s\" -@bcpg\\:productHierarchy1:\"workspace:\"  -@bcpg\\:productHierarchy2:\"workspace:\"",
														productType);
			
			List<NodeRef> productNodeRefs = beCPGSearchService.luceneSearch(QUERY_PRODUCT_TO_FIX);
			
			for(NodeRef productNodeRef : productNodeRefs){
				
				logger.debug("fix product: " + productNodeRef);
				
				//hierarchy1
				NodeRef hierarchy1NodeRef = null;
				String hierarchy1 = (String)nodeService.getProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1);
				if(hierarchy1 == null){
					hierarchy1NodeRef = null;
				}
				else if(!hierarchy1.contains("/")){
					
					hierarchy1NodeRef = getOrcreateDeletedHierarchy(hierarchiesNodeRef, productType, null, hierarchy1);					
				}
				
				//hierarchy2
				NodeRef hierarchy2NodeRef = null;
				String hierarchy2 = (String)nodeService.getProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2);
				if(hierarchy2 == null){
					hierarchy2NodeRef = null;
				}
				else if(!hierarchy2.contains("/")){
					
					hierarchy2NodeRef = getOrcreateDeletedHierarchy(hierarchiesNodeRef, productType, hierarchy1NodeRef, hierarchy2);
				}				
				
				if(hierarchy1NodeRef == null){
				
					logger.error("hierarchy1 is not found: " + hierarchy1);
					continue;
				}
				
				if(hierarchy2NodeRef == null){
					
					logger.error("hierarchy2 is not found: " + hierarchy2);
					continue;
				}
				
				try{
					//disable policy to classify on product
					policyBehaviourFilter.disableBehaviour(productNodeRef, BeCPGModel.ASPECT_PRODUCT);
					policyBehaviourFilter.disableBehaviour(productNodeRef, ReportModel.ASPECT_REPORT_ENTITY);
					policyBehaviourFilter.disableBehaviour(productNodeRef, ContentModel.ASPECT_VERSIONABLE);					
					
					logger.debug("set hierarchy1: " + hierarchy1 + " - " + hierarchy1NodeRef);
					logger.debug("set hierarchy2: " + hierarchy2 + " - " + hierarchy2NodeRef);
					nodeService.setProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1, hierarchy1NodeRef);				
					nodeService.setProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2, hierarchy2NodeRef);
					
				}			
		        finally{
		        	policyBehaviourFilter.enableBehaviour(productNodeRef, BeCPGModel.ASPECT_PRODUCT);
		        	policyBehaviourFilter.enableBehaviour(productNodeRef, ReportModel.ASPECT_REPORT_ENTITY);	
		        	policyBehaviourFilter.enableBehaviour(productNodeRef, ContentModel.ASPECT_VERSIONABLE);
		        }	
				
				migrateHistory(hierarchiesNodeRef, productType, productNodeRef, hierarchy1, hierarchy1NodeRef, hierarchy2, hierarchy2NodeRef);				
			}			
		}
	}
	
//	//debug method : reset hierarchies with product ones
//	public void migrateVersion() {
//
//		logger.info("Start to migrate versions of products");
//		String QUERY_SUGGEST_PRODUCT = " +TYPE:\"bcpg:product\"";
//		List<NodeRef> ret = beCPGSearchService.unProtLuceneSearch(QUERY_SUGGEST_PRODUCT, getSort(ContentModel.PROP_NAME),-1);
//		
//		if(ret.size()>0){
//			logger.info("Found "+ret.size()+" product to migrate");
//			for (NodeRef nodeRef : ret) {
//				
//				logger.debug("migrate product : " + nodeRef);				
//								
//				// update version history
//				VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);								
//				
//				if(versionHistory != null){
//					for(Version version : versionHistory.getAllVersions()){
//						
//						logger.debug("version.getVersionedNodeRef(): " + version.getVersionedNodeRef());
//						logger.debug("version: " + version);
//						
//						NodeRef beforeVersionNodeRef = version.getFrozenStateNodeRef();
//						NodeRef afterVersionNodeRef = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, "version2Store", beforeVersionNodeRef.getId());
//						logger.debug("Update version history, before: " + beforeVersionNodeRef);
//						logger.debug("Update version history, after: " + afterVersionNodeRef);
//						
//						if(dbNodeService.exists(afterVersionNodeRef)){
//							
//							logger.debug("set property: hierarchy1 " + nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1));
//							logger.debug("name: " + (String)dbNodeService.getProperty(afterVersionNodeRef, ContentModel.PROP_NAME));
//							
//							dbNodeService.removeProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1);
//							dbNodeService.setProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1, 
//									nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY1));
//							dbNodeService.removeProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2);
//							dbNodeService.setProperty(afterVersionNodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2, 
//									nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_HIERARCHY2));
//						}						
//					}
//				}				
//			}			
//		}		
//	}


	private List<String> getExistingHierachies1(String path) {

		String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_VALUE_ALL, LuceneHelper.encodePath(gethierarchy1Path(path)));

		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, LuceneHelper.getSort(ContentModel.PROP_NAME),-1);
		logger.info("Found "+ret.size()+" hierarchy to migrate under "+queryPath);
		return extract(ret, ContentModel.PROP_NAME);
	}

	private String gethierarchy1Path(String path) {
		return RepoConsts.PATH_SYSTEM+"/"+"TODELETE_Producthierarchy" + "/" + path + "1";
	}

	private String gethierarchy2Path(String path) {
		return RepoConsts.PATH_SYSTEM+"/"+"TODELETE_Producthierarchy" + "/" + path + "2";
	}

	private List<String> getExistingHierachies2(String path, String hierarchy1) {
		String queryPath = String.format(PATH_QUERY_SUGGEST_LKV_VALUE_ALL, LuceneHelper.encodePath(gethierarchy2Path(path)), hierarchy1);

		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, LuceneHelper.getSort(ContentModel.PROP_NAME),-1);
		logger.info("Found "+ret.size()+" hierarchy to migrate under "+queryPath);
		
		return extract(ret, PROP_LINKED_VALUE_VALUE);
	}

	private List<String> extract(List<NodeRef> nodeRefs, QName prop) {
		List<String> ret = new ArrayList<String>();
		for (NodeRef nodeRef : nodeRefs) {
			ret.add((String) nodeService.getProperty(nodeRef, prop));
		}
		return ret;
	}	

	private void migratePath(NodeRef destRef, NodeRef sourceRef, String path) throws FileExistsException, InvalidNodeRefException, FileNotFoundException {
		logger.info("Migrate path : " + path);
		NodeRef sourceFolder = getFolder(sourceRef, path);
		if(sourceFolder!=null){
			for (FileInfo file : fileFolderService.list(sourceFolder)) {
				logger.debug("Move : "+file.getName()+" to "+nodeService.getPath(destRef).toPrefixString(namespaceService));
				fileFolderService.move(file.getNodeRef(), destRef, file.getName());
			}
		}

	}

	public NodeRef getSystemCharactsEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_CHARACTS);
	}

	public NodeRef getSystemListValuesEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_LISTS);
	}

	public NodeRef getSystemHierachiesEntity(NodeRef parentNodeRef) {
		return entitySystemService.getSystemEntity(parentNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);
	}

	private NodeRef getCharactDataList(NodeRef systemEntityNodeRef, String dataListPath) {
		return entitySystemService.getSystemEntityDataList(systemEntityNodeRef, dataListPath);
	}

	private NodeRef getFolder(NodeRef parentNodeRef, String folderPath) {
		String folderName = TranslateHelper.getTranslatedPath(folderPath);
		if (folderName == null) {
			folderName = folderPath;
		}
		return repoService.getFolderByPath(parentNodeRef, folderPath);
	}

	
	/**
	 * Look for the hierarchy, if it doesn't exist, create a deleted hierarchy
	 * @param hierarchiesNodeRef
	 * @param productType
	 * @param hierarchy1NodeRef
	 * @param hierarchy
	 * @return
	 */
	private NodeRef getOrcreateDeletedHierarchy(NodeRef hierarchiesNodeRef, QName productType, NodeRef parentHierarchyNodeRef, String hierarchy){
		
		NodeRef dataListNodeRef = getCharactDataList(hierarchiesNodeRef, productType.getLocalName() + HierarchyHelper.HIERARCHY_SUFFIX);
		NodeRef hierarchyNodeRef = null;
		
		if(parentHierarchyNodeRef == null){
			hierarchyNodeRef = hierarchyService.getHierarchy1(productType, hierarchy);			
		}
		else{
			hierarchyNodeRef = hierarchyService.getHierarchy2(productType, parentHierarchyNodeRef, hierarchy);			
		}
		
		// create deleted hierarchy
		if(hierarchyNodeRef == null){
			
			if(parentHierarchyNodeRef == null){
				hierarchyNodeRef = hierarchyService.createHierarchy1(dataListNodeRef, hierarchy);
			}
			else{
				hierarchyNodeRef = hierarchyService.createHierarchy2(dataListNodeRef, parentHierarchyNodeRef, hierarchy);
			}			
			nodeService.setProperty(hierarchyNodeRef, BeCPGModel.PROP_IS_DELETED, true);
		}
		
		return hierarchyNodeRef;
	}
	
}
