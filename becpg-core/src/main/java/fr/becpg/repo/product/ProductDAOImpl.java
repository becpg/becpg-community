/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.product.data.CondSalesUnitData;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductDAOImpl.
 *
 * @author querephi
 */
public class ProductDAOImpl implements ProductDAO{
	
	/** The Constant RESOURCE_TITLE. */
	private static final String RESOURCE_TITLE = "bcpg_bcpgmodel.type.bcpg_%s.title";
	
	/** The Constant RESOURCE_DESCRIPTION. */
	private static final String RESOURCE_DESCRIPTION = "bcpg_bcpgmodel.type.bcpg_%s.description";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductDAOImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The ml node service. */
	private NodeService mlNodeService;
	
	private DictionaryService dictionaryService;
		
	private NamespaceService namespaceService;
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the file folder service.
	 *
	 * @param fileFolderService the new file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	/**
	 * Sets the ml node service.
	 *
	 * @param mlNodeService the new ml node service
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}	

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * Create a product that doesn't exit.
	 *
	 * @param parentNodeRef the parent node ref
	 * @param productData the product data
	 * @param dataLists the data lists
	 * @return the node ref
	 */
	@Override
	public NodeRef create(NodeRef parentNodeRef, ProductData productData, 	Collection<QName> dataLists) {
		    		
    	QName productType = BeCPGModel.TYPE_PRODUCT;
    	
    	if (productData instanceof CondSalesUnitData) {
			productType = BeCPGModel.TYPE_CONDSALESUNIT;			
		}
    	else if (productData instanceof FinishedProductData) {
			productType = BeCPGModel.TYPE_FINISHEDPRODUCT;			
		}
    	else if (productData instanceof LocalSemiFinishedProduct) {
			productType = BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT;			
		}
    	else if (productData instanceof PackagingKitData) {
			productType = BeCPGModel.TYPE_PACKAGINGKIT;			
		}
    	else if (productData instanceof PackagingMaterialData) {
			productType = BeCPGModel.TYPE_PACKAGINGMATERIAL;			
		}
    	else if (productData instanceof RawMaterialData) {
			productType = BeCPGModel.TYPE_RAWMATERIAL;			
		}
    	else if (productData instanceof SemiFinishedProductData) {
			productType = BeCPGModel.TYPE_SEMIFINISHEDPRODUCT;			
		}
    	    	
    	logger.debug("createProduct type: " + productType);    	
    	Map<QName, Serializable> properties = productData.getProperties();		
    	NodeRef productNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, productData.getName()), productType, properties).getChildRef();		
		createDataLists(productNodeRef, productData, dataLists);
		
		return productNodeRef;
	}
	
	/**
	 * Update an existing product.
	 *
	 * @param productNodeRef the product node ref
	 * @param productData the product data
	 * @param dataLists the data lists
	 */
	@Override
	public void update(NodeRef productNodeRef, ProductData productData, Collection<QName> dataLists) {
					
		Map<QName, Serializable> properties = productData.getProperties();
		for(Map.Entry<QName, Serializable> entry : properties.entrySet()){
			nodeService.setProperty(productNodeRef, entry.getKey(), entry.getValue());
		}		
				
		createDataLists(productNodeRef, productData, dataLists);		
	}

	/**
	 * Return an existing product.
	 *
	 * @param productNodeRef the product node ref
	 * @param dataLists the data lists
	 * @return the product data
	 */
	@Override
	public ProductData find(NodeRef productNodeRef, Collection<QName> dataLists) {
		
		ProductData productData = loadProduct(productNodeRef, dataLists);			
		return productData;
	}

	/**
	 * Delete an existing product.
	 *
	 * @param productNodeRef the product node ref
	 */
	@Override
	public void delete(NodeRef productNodeRef) {
		
		nodeService.deleteNode(productNodeRef);
	}
	
	/**
	 * **************************************************************************************************
	 * Private methods to load data
	 * **************************************************************************************************.
	 *
	 * @param productNodeRef the product node ref
	 * @param dataLists the data lists
	 * @return the product data
	 */			
    
	private ProductData loadProduct(NodeRef productNodeRef, Collection<QName> dataLists) {
		
		ProductData productData = null;
		
		//get type
		Map<QName, Serializable> properties = nodeService.getProperties(productNodeRef);
		QName productTypeQName = nodeService.getType(productNodeRef);
		SystemProductType systemProductType = SystemProductType.valueOf(productTypeQName);
				
		switch(systemProductType){
		
			case CondSalesUnit:				
				productData = new CondSalesUnitData();				
				break;
				
			case FinishedProduct:				
				productData = new FinishedProductData();
				break;
			
			case LocalSemiFinishedProduct:				
				productData = new LocalSemiFinishedProduct();
				break;
			
			case PackagingKit:				
				productData = new PackagingKitData();
				break;
				
			case PackagingMaterial:				
				productData = new PackagingMaterialData();
				break;
				
			case RawMaterial:				
				productData = new RawMaterialData();				
				break;
				
			case SemiFinishedProduct:				
				productData = new SemiFinishedProductData();
				break;
					
			default:				
				productData = new ProductData();
				break;
		}
		
		//set properties
		productData.setNodeRef(productNodeRef);
		productData.setProperties(properties);
		
		//load datalists
		NodeRef listsContainerNodeRef = getListContainer(productNodeRef);
    	productData.setListsContainer(listsContainerNodeRef);
    	
    	if(dataLists != null){
			for(QName dataList : dataLists)
	    	{
	    		if (dataList.equals(BeCPGModel.TYPE_ALLERGENLIST)) {
	    			productData.setAllergenList(loadAllergenList(listsContainerNodeRef));
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_COMPOLIST)) {
	    			productData.setCompoList(loadCompoList(listsContainerNodeRef));
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_COSTLIST)) {
	    			productData.setCostList(loadCostList(listsContainerNodeRef));
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_INGLIST)) {
	    			productData.setIngList(loadIngList(listsContainerNodeRef));
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_NUTLIST)) {
	    			productData.setNutList(loadNutList(listsContainerNodeRef));
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_ORGANOLIST)) {
	    			productData.setOrganoList(loadOrganoList(listsContainerNodeRef));
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_INGLABELINGLIST)) {
	    			productData.setIngLabelingList(loadIngLabelingList(listsContainerNodeRef));
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_MICROBIOLIST)) {
	    			productData.setMicrobioList(loadMicrobioList(listsContainerNodeRef));
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_PHYSICOCHEMLIST)) {
	    			productData.setPhysicoChemList(loadPhysicoChemList(listsContainerNodeRef));
	    		}
	    		else{
	    			// specific TODO
	    			logger.error(String.format("DataList '%s' is not loaded since it is not implemented.", dataList));
	    		}
	    	}		
    	}
    	
		return productData;

	}

    /**
     * Load allergen list.
     *
     * @param listContainerNodeRef the list container node ref
     * @return the list
     */
    private List<AllergenListDataItem> loadAllergenList(NodeRef listContainerNodeRef)
    {
    	List<AllergenListDataItem> allergenList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef allergenListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_ALLERGENLIST);
    		
    		if(allergenListNodeRef != null)
    		{
    			allergenList = new ArrayList<AllergenListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(allergenListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		    	
		    		List<AssociationRef> allergenAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.PROP_ALLERGENLIST_ALLERGEN);
		    		NodeRef allergenNodeRef = (allergenAssocRefs.get(0)).getTargetRef();
		    		
		    		List<AssociationRef> volSourcesAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY_SOURCES);
		    		List<NodeRef> volSources = new ArrayList<NodeRef>(volSourcesAssocRefs.size());
		    		for(AssociationRef assocRef : volSourcesAssocRefs){
		    			volSources.add(assocRef.getTargetRef());
		    		}
		    		
		    		List<AssociationRef> inVolSourcesAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY_SOURCES);
		    		List<NodeRef> inVolSources = new ArrayList<NodeRef>(volSourcesAssocRefs.size());
		    		for(AssociationRef assocRef : inVolSourcesAssocRefs){
		    			inVolSources.add(assocRef.getTargetRef());
		    		}
		    		
		    		AllergenListDataItem allergenListDataItem = new AllergenListDataItem(nodeRef, (Boolean)properties.get(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY), (Boolean)properties.get(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY), volSources, inVolSources, allergenNodeRef);
		    		allergenList.add(allergenListDataItem);
		    	}
    		}    		
    	}
    	
    	return allergenList;
    }
    
    /**
     * Load compo list.
     *
     * @param listContainerNodeRef the list container node ref
     * @return the list
     */
    private List<CompoListDataItem> loadCompoList(NodeRef listContainerNodeRef)
    {
    	List<CompoListDataItem> compoList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef compoListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);
    		
    		if(compoListNodeRef != null)
    		{
    			compoList = new ArrayList<CompoListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(compoListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		    	
		    		List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		    		NodeRef part = (compoAssocRefs.get(0)).getTargetRef();		    		
		    		CompoListUnit compoListUnit = CompoListUnit.valueOf((String)properties.get(BeCPGModel.PROP_COMPOLIST_UNIT));
		    		
		    		CompoListDataItem compoListDataItem = new CompoListDataItem(nodeRef, (Integer)properties.get(BeCPGModel.PROP_DEPTH_LEVEL), (Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY), (Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA), compoListUnit, (Float)properties.get(BeCPGModel.PROP_COMPOLIST_LOSS_PERC), (String)properties.get(BeCPGModel.PROP_COMPOLIST_DECL_GRP), (String)properties.get(BeCPGModel.PROP_COMPOLIST_DECL_TYPE), part);
		    		compoList.add(compoListDataItem);
		    	}
    		}    		
    	}
    	
    	return compoList;
    }    
    
    /**
     * Load cost list.
     *
     * @param listContainerNodeRef the list container node ref
     * @return the list
     */
    private List<CostListDataItem> loadCostList(NodeRef listContainerNodeRef)
    {
    	List<CostListDataItem> costList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef costListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
    		
    		if(costListNodeRef != null)
    		{
    			costList = new ArrayList<CostListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(costListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		    	
		    		List<AssociationRef> costAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_COSTLIST_COST);
		    		NodeRef costNodeRef = (costAssocRefs.get(0)).getTargetRef();
		    		
		    		CostListDataItem costListDataItem = new CostListDataItem(nodeRef, (Float)properties.get(BeCPGModel.PROP_COSTLIST_VALUE), (String)properties.get(BeCPGModel.PROP_COSTLIST_UNIT), costNodeRef);
		    		costList.add(costListDataItem);
		    	}
    		}    		
    	}
    	
    	return costList;
    }    
    
    /**
     * Load ing list.
     *
     * @param listContainerNodeRef the list container node ref
     * @return the list
     */
    private List<IngListDataItem> loadIngList(NodeRef listContainerNodeRef)
    {
    	List<IngListDataItem> ingList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef ingListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_INGLIST);
    		
    		if(ingListNodeRef != null)
    		{
    			ingList = new ArrayList<IngListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(ingListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		    	
		    		List<AssociationRef> ingAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_INGLIST_ING);
		    		NodeRef ingNodeRef = (ingAssocRefs.get(0)).getTargetRef();
		    		
		    		List<AssociationRef> geoOriginAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_INGLIST_GEO_ORIGIN);
		    		List<NodeRef> geoOrigins = new ArrayList<NodeRef>(geoOriginAssocRefs.size());
		    		for(AssociationRef assocRef : geoOriginAssocRefs){
		    			geoOrigins.add(assocRef.getTargetRef());
		    		}
		    		
		    		List<AssociationRef> bioOriginAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_INGLIST_BIO_ORIGIN);
		    		List<NodeRef> bioOrigins = new ArrayList<NodeRef>(bioOriginAssocRefs.size());
		    		for(AssociationRef assocRef : bioOriginAssocRefs){
		    			bioOrigins.add(assocRef.getTargetRef());
		    		}
		    				    		
		    		IngListDataItem ingListDataItem = new IngListDataItem(nodeRef, (Float)properties.get(BeCPGModel.PROP_INGLIST_QTY_PERC), geoOrigins, bioOrigins, (Boolean)properties.get(BeCPGModel.PROP_INGLIST_IS_GMO), (Boolean)properties.get(BeCPGModel.PROP_INGLIST_IS_IONIZED), ingNodeRef);
		    		ingList.add(ingListDataItem);
		    	}
    		}    		
    	}
    	
    	return ingList;
    }
    
    /**
     * Load nut list.
     *
     * @param listContainerNodeRef the list container node ref
     * @return the list
     */
    private List<NutListDataItem> loadNutList(NodeRef listContainerNodeRef)
    {
    	List<NutListDataItem> nutList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef nutListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_NUTLIST);
    		
    		if(nutListNodeRef != null)
    		{
    			nutList = new ArrayList<NutListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(nutListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		    	
		    		List<AssociationRef> nutAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_NUTLIST_NUT);
		    		NodeRef nutNodeRef = (nutAssocRefs.get(0)).getTargetRef();
		    		NutListDataItem nutListDataItem = new NutListDataItem(nodeRef, (Float)properties.get(BeCPGModel.PROP_NUTLIST_VALUE), (String)properties.get(BeCPGModel.PROP_NUTLIST_UNIT), (String)nodeService.getProperty(nutNodeRef, BeCPGModel.PROP_NUTGROUP), nutNodeRef);
		    		nutList.add(nutListDataItem);
		    	}
    		}    		
    	}
    	
    	return nutList;
    }
    
    /**
     * Load organo list.
     *
     * @param listContainerNodeRef the list container node ref
     * @return the list
     */
    private List<OrganoListDataItem> loadOrganoList(NodeRef listContainerNodeRef)
    {
    	List<OrganoListDataItem> organoList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef organoListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_ORGANOLIST);
    		
    		if(organoListNodeRef != null)
    		{
    			organoList = new ArrayList<OrganoListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(organoListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		    	
		    		List<AssociationRef> organoAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_ORGANOLIST_ORGANO);
		    		NodeRef organoNodeRef = (organoAssocRefs.get(0)).getTargetRef();
		    		OrganoListDataItem organoListDataItem = new OrganoListDataItem(nodeRef, (String)properties.get(BeCPGModel.PROP_ORGANOLIST_VALUE), organoNodeRef);
		    		organoList.add(organoListDataItem);
		    	}
    		}    		
    	}
    	
    	return organoList;
    }
    
    /**
     * Load ing labeling list.
     *
     * @param listContainerNodeRef the list container node ref
     * @return the list
     */
    private List<IngLabelingListDataItem> loadIngLabelingList(NodeRef listContainerNodeRef)
    {
    	List<IngLabelingListDataItem> ingLabelingList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef ingLabelingListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);
    		
    		if(ingLabelingListNodeRef != null)
    		{
    			ingLabelingList = new ArrayList<IngLabelingListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(ingLabelingListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
	    			
	    			//Grp
	    			String grp = (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_ILL_GRP);
		    		
		    		//illValue
		    		MLText illValue = (MLText)mlNodeService.getProperty(nodeRef, BeCPGModel.PROP_ILL_VALUE);
//		            I18NUtil.setContentLocale(Locale.FRENCH);
//		    		illValue.addValue(Locale.FRENCH, (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_ILL_VALUE));
//		            I18NUtil.setContentLocale(Locale.ENGLISH);
//		    		illValue.addValue(Locale.ENGLISH, (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_ILL_VALUE));
		            
		    		IngLabelingListDataItem ingLabelingListDataItem = new IngLabelingListDataItem(nodeRef, grp, illValue);
		    		ingLabelingList.add(ingLabelingListDataItem);
		    	}
    		}    		
    	}
    	
    	return ingLabelingList;
    }
    
    /**
     * Load microbio list.
     *
     * @param listContainerNodeRef the list container node ref
     * @return the list
     */
    private List<MicrobioListDataItem> loadMicrobioList(NodeRef listContainerNodeRef)
    {
    	List<MicrobioListDataItem> microbioList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef microbioListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_MICROBIOLIST);    		
    		
    		if(microbioListNodeRef != null)
    		{
    			microbioList = new ArrayList<MicrobioListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(microbioListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		    	
		    		List<AssociationRef> microbioAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO);
		    		NodeRef organoNodeRef = (microbioAssocRefs.get(0)).getTargetRef();
		    		MicrobioListDataItem microbioListDataItem = new MicrobioListDataItem(nodeRef, (Float)properties.get(BeCPGModel.PROP_MICROBIOLIST_VALUE), (String)properties.get(BeCPGModel.PROP_MICROBIOLIST_UNIT), (Float)properties.get(BeCPGModel.PROP_MICROBIOLIST_MAXI), organoNodeRef);
		    		microbioList.add(microbioListDataItem);
		    	}
    		}    		
    	}
    	
    	return microbioList;
    }
    
    /**
     * Load physico chem list.
     *
     * @param listContainerNodeRef the list container node ref
     * @return the list
     */
    private List<PhysicoChemListDataItem> loadPhysicoChemList(NodeRef listContainerNodeRef)
    {
    	List<PhysicoChemListDataItem> physicoChemList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef physicoChemListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_PHYSICOCHEMLIST);
    		
    		if(physicoChemListNodeRef != null)
    		{
    			physicoChemList = new ArrayList<PhysicoChemListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(physicoChemListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		    	
		    		List<AssociationRef> physicoChemAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM);
		    		NodeRef physicoChemNodeRef = (physicoChemAssocRefs.get(0)).getTargetRef();
		    		PhysicoChemListDataItem physicoChemListDataItem = new PhysicoChemListDataItem(nodeRef, (Float)properties.get(BeCPGModel.PROP_PHYSICOCHEMLIST_VALUE), (String)properties.get(BeCPGModel.PROP_PHYSICOCHEMLIST_UNIT), (Float)properties.get(BeCPGModel.PROP_PHYSICOCHEMLIST_MINI), (Float)properties.get(BeCPGModel.PROP_PHYSICOCHEMLIST_MAXI), physicoChemNodeRef);
		    		physicoChemList.add(physicoChemListDataItem);
		    	}
    		}    		
    	}
    	
    	return physicoChemList;
    }
    
    /**
     * **************************************************************************************************
     * Private methods for creation														*
     * **************************************************************************************************.
     *
     * @param productNodeRef the product node ref
     * @param productData the product data
     * @param dataLists the data lists
     */
    
//    private NodeRef saveProduct(NodeRef parentNodeRef, CondSalesUnitData p, Set<ProductDataList> dataLists){
//    	return saveProduct(parentNodeRef, p, FoodModel.TYPE_CONDSALESUNIT, dataLists);
//    }
//    
//    private NodeRef saveProduct(NodeRef parentNodeRef, FinishedProductData p, Set<ProductDataList> dataLists){
//    	return saveProduct(parentNodeRef, p, FoodModel.TYPE_FINISHEDPRODUCT, dataLists);
//    }
//    
//    private NodeRef saveProduct(NodeRef parentNodeRef, LocalSemiFinishedProduct p, Set<ProductDataList> dataLists){
//    	return saveProduct(parentNodeRef, p, FoodModel.TYPE_LOCALSEMIFINISHEDPRODUCT, dataLists);
//    }
//    
//    private NodeRef saveProduct(NodeRef parentNodeRef, PackagingKitData p, Set<ProductDataList> dataLists){
//    	return saveProduct(parentNodeRef, p, FoodModel.TYPE_PACKAGINGKIT, dataLists);
//    }
//    
//    private NodeRef saveProduct(NodeRef parentNodeRef, PackagingMaterialData p, Set<ProductDataList> dataLists){
//    	return saveProduct(parentNodeRef, p, FoodModel.TYPE_PACKAGINGMATERIAL, dataLists);
//    }
//    
//    private NodeRef saveProduct(NodeRef parentNodeRef, RawMaterialData p, Set<ProductDataList> dataLists){
//    	return saveProduct(parentNodeRef, p, FoodModel.TYPE_RAWMATERIAL, dataLists);
//    }
//    
//    private NodeRef saveProduct(NodeRef parentNodeRef, SemiFinishedProductData p, Set<ProductDataList> dataLists){
//    	return saveProduct(parentNodeRef, p, FoodModel.TYPE_SEMIFINISHEDPRODUCT, dataLists);
//    }       
    
    
    private void createDataLists(NodeRef productNodeRef, ProductData productData, Collection<QName> dataLists)
    {   
    	logger.debug("createDataLists of product " + productNodeRef);    	    
		 			
		//Container
		NodeRef containerNodeRef = getListContainer(productNodeRef);
		if(containerNodeRef == null){
			containerNodeRef = createListContainer(productNodeRef);
		}		
		productData.setListsContainer(containerNodeRef);
		
		//Lists		
		if(dataLists != null){
			for(QName dataList : dataLists)
	    	{
				if (dataList.equals(BeCPGModel.TYPE_ALLERGENLIST)) {
					createAllergenList(containerNodeRef, productData.getAllergenList());
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_COMPOLIST)) {
	    			createCompoList(containerNodeRef, productData.getCompoList());
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_COSTLIST)) {
	    			createCostList(containerNodeRef, productData.getCostList());
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_INGLIST)) {
	    			createIngList(containerNodeRef, productData.getIngList());
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_NUTLIST)) {
	    			createNutList(containerNodeRef, productData.getNutList());
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_ORGANOLIST)) {
	    			createOrganoList(containerNodeRef, productData.getOrganoList());
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_INGLABELINGLIST)) {
	    			createIngLabelingList(containerNodeRef, productData.getIngLabelingList());
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_MICROBIOLIST)) {
	    			createMicrobioList(containerNodeRef, productData.getMicrobioList());
	    		}
	    		else if (dataList.equals(BeCPGModel.TYPE_PHYSICOCHEMLIST)) {
	    			// TODO
	    			logger.error(String.format("DataList '%s' is not created since it is not implemented.", dataList));
	    		}
	    		else{
	    			// specific TODO
	    			logger.error(String.format("DataList '%s' is not created since it is not implemented.", dataList));
	    		}				
	    	}
		}    	
    }	   
	
	/**
	 * Creates the allergen list.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param allergenList the allergen list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createAllergenList(NodeRef listContainerNodeRef,List<AllergenListDataItem> allergenList) throws InvalidTypeException{
		
		if(listContainerNodeRef != null)
		{ 
			NodeRef allergenListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_ALLERGENLIST);
			
			if(allergenList == null){
				//delete existing list
				if(allergenListNodeRef != null)
					nodeService.deleteNode(allergenListNodeRef);
			}
			else{    		    			   			    			    			    	
	    		
	    		//allergen list, create if needed
	    		if(allergenListNodeRef == null)
	    		{									
		    		allergenListNodeRef = createList(listContainerNodeRef, BeCPGModel.TYPE_ALLERGENLIST);
	    		}
	    		
	    		List<FileInfo> files = fileFolderService.listFiles(allergenListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> allergenListToTreat = new ArrayList<NodeRef>();
	    		for(AllergenListDataItem allergenListDataItem : allergenList){
	    			allergenListToTreat.add(allergenListDataItem.getAllergen());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
	    		for(FileInfo file : files){
	    			
	    			List<AssociationRef> allergenAssocRefs = nodeService.getTargetAssocs(file.getNodeRef(), BeCPGModel.PROP_ALLERGENLIST_ALLERGEN);
		    		NodeRef allergenNodeRef = (allergenAssocRefs.get(0)).getTargetRef();	    			
	    			
	    			if(!allergenListToTreat.contains(allergenNodeRef)){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(allergenNodeRef, file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		
	    		for(AllergenListDataItem allergenListDataItem : allergenList)
	    		{    				    			
	    			NodeRef linkNodeRef = null;
	    			NodeRef allergenNodeRef = allergenListDataItem.getAllergen();	    			
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    			properties.put(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY, allergenListDataItem.getInVoluntary());
		    		properties.put(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, allergenListDataItem.getVoluntary());
		
		    		if(filesToUpdate.containsKey(allergenNodeRef)){
		    			//update
		    			linkNodeRef = filesToUpdate.get(allergenNodeRef);
		    			nodeService.setProperties(linkNodeRef, properties);
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(allergenListNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, allergenListDataItem.getAllergen().getId()), BeCPGModel.TYPE_ALLERGENLIST, properties);	    	
		    			linkNodeRef = childAssocRef.getChildRef();
		    			nodeService.createAssociation(linkNodeRef, allergenListDataItem.getAllergen(), BeCPGModel.PROP_ALLERGENLIST_ALLERGEN);			    		
		    		}
		    		
		    		//Voluntary
		    		List<AssociationRef> volSourcesAssocRefs = nodeService.getTargetAssocs(linkNodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY_SOURCES);
		    		
		    		if(allergenListDataItem.getVoluntarySources() != null){
		    			//remove from db
			    		for(AssociationRef assocRef : volSourcesAssocRefs){
			    			if(!allergenListDataItem.getVoluntarySources().contains(assocRef.getTargetRef()))
			    				nodeService.removeAssociation(linkNodeRef, assocRef.getTargetRef(), BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY_SOURCES);
			    			else
			    				allergenListDataItem.getVoluntarySources().remove(assocRef.getTargetRef());//already in db
			    		}
			    		//add nodes that are not in db
			    		for(NodeRef nodeRef : allergenListDataItem.getVoluntarySources()){
			    			nodeService.createAssociation(linkNodeRef, nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY_SOURCES);
			    		}
		    		}
		    		else{
		    			for(AssociationRef assocRef : volSourcesAssocRefs)
		    				nodeService.removeAssociation(linkNodeRef, assocRef.getTargetRef(), BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY_SOURCES);
		    		}
		    		
		    		
		    		//InVoluntary
	    			List<AssociationRef> inVolSourcesAssocRefs = nodeService.getTargetAssocs(linkNodeRef, BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY_SOURCES);
		    		
		    		if(allergenListDataItem.getInVoluntarySources() != null){
		    			//remove from db
			    		for(AssociationRef assocRef : inVolSourcesAssocRefs){
			    			if(!allergenListDataItem.getInVoluntarySources().contains(assocRef.getTargetRef()))
			    				nodeService.removeAssociation(linkNodeRef, assocRef.getTargetRef(), BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY_SOURCES);
			    			else
			    				allergenListDataItem.getInVoluntarySources().remove(assocRef.getTargetRef());//already in db
			    		}
			    		//add nodes that are not in db
			    		for(NodeRef nodeRef : allergenListDataItem.getInVoluntarySources()){
			    			nodeService.createAssociation(linkNodeRef, nodeRef, BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY_SOURCES);
			    		}
		    		}
		    		else{
		    			for(AssociationRef assocRef : inVolSourcesAssocRefs)
		    				nodeService.removeAssociation(linkNodeRef, assocRef.getTargetRef(), BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY_SOURCES);
		    		}
	    		}  
			}    		    		
		}
	}    
	
	/**
	 * Create/Update composition.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param compoList the compo list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createCompoList(NodeRef listContainerNodeRef, List<CompoListDataItem> compoList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{    	
			NodeRef compoListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);
			
			if(compoList == null){
				//delete existing list
				if(compoListNodeRef != null)
					nodeService.deleteNode(compoListNodeRef);
			}
			else{
	    		
				//compo list, create if needed    			
	    		if(compoListNodeRef == null)
	    		{					
					compoListNodeRef = createList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);					
	    		}    				    			    		    		
	
	    		List<FileInfo> files = fileFolderService.listFiles(compoListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> compoListToTreat = new ArrayList<NodeRef>();
	    		for(CompoListDataItem compoListDataItem : compoList){
	    			compoListToTreat.add(compoListDataItem.getNodeRef());
	    		}
	    		
	    		//remove deleted nodes
	    		List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
	    		for(FileInfo file : files){	    			
		    		
	    			if(!compoListToTreat.contains(file.getNodeRef())){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.add(file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		    			    		
	    		CompoListDataItem prevCompoListDataItem = null;
	    		for(CompoListDataItem compoListDataItem : compoList)
	    		{    				    			
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(BeCPGModel.PROP_DEPTH_LEVEL, compoListDataItem.getDepthLevel());
		    		properties.put(BeCPGModel.PROP_COMPOLIST_QTY, compoListDataItem.getQty());
		    		properties.put(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA, compoListDataItem.getQtySubFormula());
		    		properties.put(BeCPGModel.PROP_COMPOLIST_UNIT, compoListDataItem.getCompoListUnit() == CompoListUnit.Unknown ?  "" : compoListDataItem.getCompoListUnit().toString());
		    		properties.put(BeCPGModel.PROP_COMPOLIST_LOSS_PERC, compoListDataItem.getLossPerc());
		    		properties.put(BeCPGModel.PROP_COMPOLIST_DECL_GRP, compoListDataItem.getDeclGrp());
		    		properties.put(BeCPGModel.PROP_COMPOLIST_DECL_TYPE, compoListDataItem.getDeclType());		    		
	
		    		if(filesToUpdate.contains(compoListDataItem.getNodeRef())){
		    			//update
		    			nodeService.setProperties(compoListDataItem.getNodeRef(), properties);		    			
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(compoListNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, compoListDataItem.getProduct().getId()), BeCPGModel.TYPE_COMPOLIST, properties);
		    			compoListDataItem.setNodeRef(childAssocRef.getChildRef());			    		
		    		}			    			    	
		    		
		    		//Update product
		    		List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(compoListDataItem.getNodeRef(), BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		    		if(compoAssocRefs.size() > 0){
			    		NodeRef part = (compoAssocRefs.get(0)).getTargetRef();
			    		if(part != compoListDataItem.getProduct()){
				    		nodeService.removeAssociation(compoListDataItem.getNodeRef(), part, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);				    		
			    		}
		    		}
		    		nodeService.createAssociation(compoListDataItem.getNodeRef(), compoListDataItem.getProduct(), BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		    		
		    		//store father if level > 1
		    		if(compoListDataItem.getDepthLevel() > 1){
		    			
		    			boolean createFather = true;
		    			compoAssocRefs = nodeService.getTargetAssocs(compoListDataItem.getNodeRef(), BeCPGModel.ASSOC_COMPOLIST_FATHER);
		    			if(compoAssocRefs.size() > 0){
				    		NodeRef fatherNodeRef = (compoAssocRefs.get(0)).getTargetRef();
				    		
				    		if(fatherNodeRef != null && fatherNodeRef == prevCompoListDataItem.getNodeRef()){
				    			createFather = false;
				    		}
				    		else{
				    			nodeService.removeAssociation(compoListDataItem.getNodeRef(), fatherNodeRef, BeCPGModel.ASSOC_COMPOLIST_FATHER);
				    		}
		    			}
		    			if(createFather && prevCompoListDataItem != null)
		    				nodeService.createAssociation(compoListDataItem.getNodeRef(), prevCompoListDataItem.getNodeRef(), BeCPGModel.ASSOC_COMPOLIST_FATHER);		    				    						    	
		    		}
		    		
		    		prevCompoListDataItem = compoListDataItem;
	    		}	    		 
			}
		}
	}    
	
	/**
	 * Create/Update costs.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param costList the cost list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createCostList(NodeRef listContainerNodeRef, List<CostListDataItem> costList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{  
			NodeRef costListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
			
			if(costList == null){
				//delete existing list
				if(costListNodeRef != null)
					nodeService.deleteNode(costListNodeRef);
			}
			else{    			
	    		//cost list, create if needed	    		
	    		if(costListNodeRef == null)
	    		{		    						
		    		costListNodeRef = createList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
	    		}
			
	    		List<FileInfo> files = fileFolderService.listFiles(costListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> costListToTreat = new ArrayList<NodeRef>();
	    		for(CostListDataItem costListDataItem : costList){
	    			costListToTreat.add(costListDataItem.getCost());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
	    		for(FileInfo file : files){
	    			
	    			List<AssociationRef> costAssocRefs = nodeService.getTargetAssocs(file.getNodeRef(), BeCPGModel.ASSOC_COSTLIST_COST);
		    		NodeRef costNodeRef = (costAssocRefs.get(0)).getTargetRef();
		    		
	    			if(!costListToTreat.contains(costNodeRef)){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(costNodeRef, file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		    			    		
	    		for(CostListDataItem costListDataItem : costList)
	    		{    			
	    			NodeRef costNodeRef = costListDataItem.getCost();	  
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(BeCPGModel.PROP_COSTLIST_VALUE, costListDataItem.getValue());
		    		properties.put(BeCPGModel.PROP_COSTLIST_UNIT, costListDataItem.getUnit());
		    		
	
		    		if(filesToUpdate.containsKey(costNodeRef)){
		    			//update
		    			nodeService.setProperties(filesToUpdate.get(costNodeRef), properties);		    			
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(costListNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, costListDataItem.getCost().getId()), BeCPGModel.TYPE_COSTLIST, properties);	    	
			    		nodeService.createAssociation(childAssocRef.getChildRef(), costListDataItem.getCost(), BeCPGModel.ASSOC_COSTLIST_COST);
		    		}			    			    	
	    		}
			}
		}
	}    
	
	//TODO : create/Update
	/**
	 * Creates the ing list.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param ingList the ing list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createIngList(NodeRef listContainerNodeRef, List<IngListDataItem> ingList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{    		
			NodeRef ingListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_INGLIST);
			
			//delete list
			if(ingListNodeRef != null)
			{
				fileFolderService.delete(ingListNodeRef);
			}
			
			if(ingList != null){
					    		
	    		ingListNodeRef = createList(listContainerNodeRef, BeCPGModel.TYPE_INGLIST);
	    		
	    		for(IngListDataItem ingListDataItem : ingList)
	    		{    			
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>(); 
		    		properties.put(BeCPGModel.PROP_INGLIST_QTY_PERC, ingListDataItem.getQtyPerc());
		    		properties.put(BeCPGModel.PROP_INGLIST_IS_GMO, ingListDataItem.isGMO());
		    		properties.put(BeCPGModel.PROP_INGLIST_IS_IONIZED, ingListDataItem.isIonized());
		    		//properties.put(BeCPGModel.PROP_INGLIST_ING, ingListDataItem.getIng()); 			// not a property but an association	    			    	
	
		    		ChildAssociationRef childAssocRef = nodeService.createNode(ingListNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, ingListDataItem.getIng().getId()), BeCPGModel.TYPE_INGLIST, properties);	    	
		    		nodeService.createAssociation(childAssocRef.getChildRef(), ingListDataItem.getIng(), BeCPGModel.ASSOC_INGLIST_ING);
		    		
		    		if(ingListDataItem.getGeoOrigin() != null){
			    		for(NodeRef nodeRef : ingListDataItem.getGeoOrigin()){
			    			nodeService.createAssociation(childAssocRef.getChildRef(), nodeRef, BeCPGModel.ASSOC_INGLIST_GEO_ORIGIN);
			    		}
		    		}
		    		
		    		if(ingListDataItem.getBioOrigin() != null){
			    		for(NodeRef nodeRef : ingListDataItem.getBioOrigin()){
			    			nodeService.createAssociation(childAssocRef.getChildRef(), nodeRef, BeCPGModel.ASSOC_INGLIST_BIO_ORIGIN);
			    		}
		    		}
	    		}
			}
		}
	}
	
	/**
	 * Create/Update nuts.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param nutList the nut list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createNutList(NodeRef listContainerNodeRef, List<NutListDataItem> nutList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{    	
			NodeRef nutListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_NUTLIST);
			
			if(nutList == null){
				//delete existing list
				if(nutListNodeRef != null)
					nodeService.deleteNode(nutListNodeRef);
			}
			else{    		    			   			    			    			    	
	    		
	    		//nut list, create if needed
	    		if(nutListNodeRef == null)
	    		{					
		    		nutListNodeRef = createList(listContainerNodeRef, BeCPGModel.TYPE_NUTLIST);
	    		}
	    		
	    		List<FileInfo> files = fileFolderService.listFiles(nutListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> nutListToTreat = new ArrayList<NodeRef>();
	    		for(NutListDataItem nutListDataItem : nutList){
	    			nutListToTreat.add(nutListDataItem.getNut());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
	    		for(FileInfo file : files){
	    			
	    			List<AssociationRef> nutAssocRefs = nodeService.getTargetAssocs(file.getNodeRef(), BeCPGModel.ASSOC_NUTLIST_NUT);
		    		NodeRef nutNodeRef = (nutAssocRefs.get(0)).getTargetRef();	    			
	    			
	    			if(!nutListToTreat.contains(nutNodeRef)){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(nutNodeRef, file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		
	    		for(NutListDataItem nutListDataItem : nutList)
	    		{    				    			
	    			NodeRef nutNodeRef = nutListDataItem.getNut();	    			
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(BeCPGModel.PROP_NUTLIST_VALUE, nutListDataItem.getValue());
		    		properties.put(BeCPGModel.PROP_NUTLIST_UNIT, nutListDataItem.getUnit());
		    		properties.put(BeCPGModel.PROP_NUTLIST_GROUP, nutListDataItem.getGroup());
			    		
		    		if(filesToUpdate.containsKey(nutNodeRef)){
		    			//update
		    			nodeService.setProperties(filesToUpdate.get(nutNodeRef), properties);
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(nutListNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nutListDataItem.getNut().getId()), BeCPGModel.TYPE_NUTLIST, properties);
		    			nodeService.createAssociation(childAssocRef.getChildRef(), nutListDataItem.getNut(), BeCPGModel.ASSOC_NUTLIST_NUT);
		    		}		    				    		
	    		}  
			}    		
		}
	}
	
	/**
	 * Create/Update organos.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param organoList the organo list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createOrganoList(NodeRef listContainerNodeRef, List<OrganoListDataItem> organoList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{    		    		
			NodeRef organoListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_ORGANOLIST);
			    		
			if(organoList == null){
				//delete existing list
				if(organoListNodeRef != null)
					nodeService.deleteNode(organoListNodeRef);
			}
			else{
				//organo list, create if needed
	    		if(organoListNodeRef == null)
	    		{
		    		organoListNodeRef = createList(listContainerNodeRef, BeCPGModel.TYPE_ORGANOLIST);
	    		}
	    		
	    		List<FileInfo> files = fileFolderService.listFiles(organoListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> organoListToTreat = new ArrayList<NodeRef>();
	    		for(OrganoListDataItem organoListDataItem : organoList){
	    			organoListToTreat.add(organoListDataItem.getOrgano());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
	    		for(FileInfo file : files){
	    			
	    			List<AssociationRef> organoAssocRefs = nodeService.getTargetAssocs(file.getNodeRef(), BeCPGModel.ASSOC_ORGANOLIST_ORGANO);
		    		NodeRef organoNodeRef = (organoAssocRefs.get(0)).getTargetRef();	    			
	    			
	    			if(!organoListToTreat.contains(organoNodeRef)){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(organoNodeRef, file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		
	    		for(OrganoListDataItem organoListDataItem : organoList)
	    		{    				    			
	    			NodeRef organoNodeRef = organoListDataItem.getNodeRef();	    			
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(BeCPGModel.PROP_ORGANOLIST_VALUE, organoListDataItem.getValue());
			    		
		    		if(filesToUpdate.containsKey(organoNodeRef)){
		    			//update
		    			nodeService.setProperties(filesToUpdate.get(organoNodeRef), properties);
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(organoListNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, organoListDataItem.getOrgano().getId()), BeCPGModel.TYPE_ORGANOLIST, properties);
		    			nodeService.createAssociation(childAssocRef.getChildRef(), organoListDataItem.getOrgano(), BeCPGModel.ASSOC_ORGANOLIST_ORGANO);
		    		}		    				    		
	    		}
			}
	   	}
	}	
	
	/**
	 * Create/Update inn labeling list.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param ingLabelingList the ing labeling list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createIngLabelingList(NodeRef listContainerNodeRef, List<IngLabelingListDataItem> ingLabelingList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{  
			NodeRef illNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);
			
			if(ingLabelingList == null){
				//delete existing list
				if(illNodeRef != null)
					nodeService.deleteNode(illNodeRef);
			}
			else{
	    		//ingLabeling list, create if needed    		
	    		if(illNodeRef == null)
	    		{
		    		illNodeRef = createList(listContainerNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);
	    		}
	    		
	    		List<FileInfo> files = fileFolderService.listFiles(illNodeRef);
	    		
	    		//create temp list
	    		List<String> illToTreat = new ArrayList<String>();
	    		for(IngLabelingListDataItem illDataItem : ingLabelingList){
	    			illToTreat.add(illDataItem.getGrp());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<String, NodeRef> filesToUpdate = new HashMap<String, NodeRef>();
	    		for(FileInfo file : files){
	    			
	    			String grp = (String)nodeService.getProperty(file.getNodeRef(), BeCPGModel.PROP_ILL_GRP);	    			
	    			
	    			if(!illToTreat.contains(grp)){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(grp, file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		
	    		for(IngLabelingListDataItem illDataItem : ingLabelingList)
	    		{    				    			
	    			String grp = illDataItem.getGrp();	    			
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(BeCPGModel.PROP_ILL_GRP, illDataItem.getGrp());
		    		properties.put(BeCPGModel.PROP_ILL_VALUE, illDataItem.getValue());
			    		
		    		if(filesToUpdate.containsKey(grp)){
		    			//update
		    			nodeService.setProperties(filesToUpdate.get(grp), properties);
		    		}
		    		else{
		    			//create
		    			nodeService.createNode(illNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, illDataItem.getGrp()), BeCPGModel.TYPE_INGLABELINGLIST, properties);
		    		}		    				    		
	    		}
	    	} 
	   	}
	}
	
	/**
	 * Create/Update microbios.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param microbioList the microbio list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createMicrobioList(NodeRef listContainerNodeRef, List<MicrobioListDataItem> microbioList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{    	
			NodeRef microbioListNodeRef = getList(listContainerNodeRef, BeCPGModel.TYPE_MICROBIOLIST);
			
			if(microbioList == null){
				//delete existing list
				if(microbioListNodeRef != null)
					nodeService.deleteNode(microbioListNodeRef);
			}
			else{    		    			   			    			    			    	
	    		
	    		//microbio list, create if needed
	    		if(microbioListNodeRef == null)
	    		{					
	    			microbioListNodeRef = createList(listContainerNodeRef, BeCPGModel.TYPE_MICROBIOLIST);
	    		}
	    		
	    		List<FileInfo> files = fileFolderService.listFiles(microbioListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> microbioListToTreat = new ArrayList<NodeRef>();
	    		for(MicrobioListDataItem microbioListDataItem : microbioList){
	    			microbioListToTreat.add(microbioListDataItem.getMicrobio());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
	    		for(FileInfo file : files){
	    			
	    			List<AssociationRef> nutAssocRefs = nodeService.getTargetAssocs(file.getNodeRef(), BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO);
		    		NodeRef microbioNodeRef = (nutAssocRefs.get(0)).getTargetRef();	    			
	    			
	    			if(!microbioListToTreat.contains(microbioNodeRef)){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(microbioNodeRef, file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		
	    		for(MicrobioListDataItem microbioListDataItem : microbioList)
	    		{    				    			
	    			NodeRef microbioNodeRef = microbioListDataItem.getMicrobio();	    			
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(BeCPGModel.PROP_MICROBIOLIST_VALUE, microbioListDataItem.getValue());
		    		properties.put(BeCPGModel.PROP_MICROBIOLIST_UNIT, microbioListDataItem.getUnit());
		    		properties.put(BeCPGModel.PROP_MICROBIOLIST_MAXI, microbioListDataItem.getMaxi());
			    		
		    		if(filesToUpdate.containsKey(microbioNodeRef)){
		    			//update
		    			nodeService.setProperties(filesToUpdate.get(microbioNodeRef), properties);
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(microbioListNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, microbioListDataItem.getMicrobio().getId()), BeCPGModel.TYPE_MICROBIOLIST, properties);
		    			nodeService.createAssociation(childAssocRef.getChildRef(), microbioListDataItem.getMicrobio(), BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO);
		    		}		    				    		
	    		}  
			}    		
		}
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductDAO#getListContainer(org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public NodeRef getListContainer(NodeRef productNodeRef) {
		// TODO Refactor the code to use this method in other classes
				
		return nodeService.getChildByName(productNodeRef, BeCPGModel.ASSOC_PRODUCTLISTS, RepoConsts.CONTAINER_DATALISTS);
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductDAO#getList(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
	 */
	@Override
	public NodeRef getList(NodeRef listContainerNodeRef, QName productListQName) {
		// TODO Refactor the code to use this method
		
		if(productListQName== null){
			return null;
		}
			
		NodeRef listNodeRef = null;		
		if(listContainerNodeRef != null){
			listNodeRef = nodeService.getChildByName(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, productListQName.getLocalName());		
		}
		return listNodeRef;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductDAO#getLink(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public NodeRef getLink(NodeRef listContainerNodeRef, QName propertyQName, NodeRef nodeRef) {
		// TODO Refactor the code to use this method
		
		if(listContainerNodeRef != null && propertyQName != null && nodeRef != null){
			
			List<FileInfo> fileInfos = fileFolderService.listFiles(listContainerNodeRef);
    		
    		for(FileInfo fileInfo : fileInfos){
    			
    			List<AssociationRef> assocRefs = nodeService.getTargetAssocs(fileInfo.getNodeRef(), propertyQName);
    			if(assocRefs.size() > 0 && nodeRef.equals(assocRefs.get(0).getTargetRef())){
    				return fileInfo.getNodeRef();
    			}
	    		
	    	}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductDAO#createListContainer(org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public NodeRef createListContainer(NodeRef productNodeRef) {
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, RepoConsts.CONTAINER_DATALISTS);
		properties.put(ContentModel.PROP_TITLE, RepoConsts.CONTAINER_DATALISTS);
		return nodeService.createNode(productNodeRef, BeCPGModel.ASSOC_PRODUCTLISTS, BeCPGModel.ASSOC_PRODUCTLISTS, ContentModel.TYPE_FOLDER, properties).getChildRef();
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductDAO#createList(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
	 */
	@Override
	public NodeRef createList(NodeRef listContainerNodeRef, QName productListQName) {
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, productListQName.getLocalName());
		properties.put(ContentModel.PROP_TITLE, I18NUtil.getMessage(String.format(RESOURCE_TITLE, productListQName.getLocalName())));
		properties.put(ContentModel.PROP_DESCRIPTION, I18NUtil.getMessage(String.format(RESOURCE_DESCRIPTION, productListQName.getLocalName())));
		properties.put(DataListModel.PROP_DATALISTITEMTYPE, BeCPGModel.BECPG_PREFIX + ":" + productListQName.getLocalName());
		return nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, productListQName, DataListModel.TYPE_DATALIST, properties).getChildRef();
	}

	@Override
	public Set<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef) {
		
		Set<NodeRef> existingLists = new HashSet<NodeRef>();
		
		if(listContainerNodeRef != null){
			List<FileInfo> nodes = fileFolderService.listFolders(listContainerNodeRef);
			
			for(FileInfo node : nodes){
				
				NodeRef listNodeRef = node.getNodeRef();
				String dataListType = (String)nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
				
				if(dataListType != null && !dataListType.isEmpty()){
					
					QName dataListTypeQName = QName.createQName(dataListType, namespaceService);
					
					if(dictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_PRODUCTLIST_ITEM)){
						existingLists.add(listNodeRef);
					}
				}
				
			}								
		}
		return existingLists;
	}

	@Override
	public Set<QName> getExistingListsQName(NodeRef listContainerNodeRef) {
		
		Set<QName> existingLists = new HashSet<QName>();
		
		if(listContainerNodeRef != null){
			List<FileInfo> nodes = fileFolderService.listFolders(listContainerNodeRef);
			
			for(FileInfo node : nodes){
				
				NodeRef listNodeRef = node.getNodeRef();
				String dataListType = (String)nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
				
				if(dataListType != null && !dataListType.isEmpty()){
					
					existingLists.add(QName.createQName(dataListType, namespaceService));					
				}
			}								
		}
		return existingLists;				
	}

}
