/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.data.hierarchicalList.AbstractComponent;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostDetailsListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharachListItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem.NullableBoolean;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductDAOImpl.
 * 
 * @author querephi
 */
public class ProductDAOImpl implements ProductDAO {

	public static final String KEY_COST_DETAILS = "%s-%s";

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductDAOImpl.class);

	/** The node service. */
	private NodeService nodeService;

	/** The ml node service. */
	private NodeService mlNodeService;

	private EntityListDAO entityListDAO;

	private BehaviourFilter policyBehaviourFilter;

	private AssociationService associationService;

	/**
	 * Sets the node service.
	 * 
	 * @param nodeService
	 *            the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Sets the ml node service.
	 * 
	 * @param mlNodeService
	 *            the new ml node service
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * Create a product that doesn't exit.
	 * 
	 * @param parentNodeRef
	 *            the parent node ref
	 * @param productData
	 *            the product data
	 * @param dataLists
	 *            the data lists
	 * @return the node ref
	 */
	@Override
	public NodeRef create(NodeRef parentNodeRef, ProductData productData, Collection<QName> dataLists) {

		logger.debug("Create product name: " + productData.getName());

		QName productType = BeCPGModel.TYPE_PRODUCT;

		if (productData instanceof FinishedProductData) {
			productType = BeCPGModel.TYPE_FINISHEDPRODUCT;
		} else if (productData instanceof LocalSemiFinishedProduct) {
			productType = BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT;
		} else if (productData instanceof PackagingKitData) {
			productType = BeCPGModel.TYPE_PACKAGINGKIT;
		} else if (productData instanceof PackagingMaterialData) {
			productType = BeCPGModel.TYPE_PACKAGINGMATERIAL;
		} else if (productData instanceof RawMaterialData) {
			productType = BeCPGModel.TYPE_RAWMATERIAL;
		} else if (productData instanceof SemiFinishedProductData) {
			productType = BeCPGModel.TYPE_SEMIFINISHEDPRODUCT;
		} else if (productData instanceof ResourceProductData) {
			productType = BeCPGModel.TYPE_RESOURCEPRODUCT;
		}

		Map<QName, Serializable> properties = productData.getProperties();
		NodeRef productNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(productData.getName())), productType, properties).getChildRef();
		createDataLists(productNodeRef, productData, dataLists);

		return productNodeRef;
	}

	/**
	 * Update an existing product.
	 * 
	 * @param productNodeRef
	 *            the product node ref
	 * @param productData
	 *            the product data
	 * @param dataLists
	 *            the data lists
	 */
	@Override
	public void update(NodeRef productNodeRef, ProductData productData, Collection<QName> dataLists) {

		Map<QName, Serializable> properties = productData.getProperties();
		for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
			nodeService.setProperty(productNodeRef, entry.getKey(), entry.getValue());
		}

		createDataLists(productNodeRef, productData, dataLists);
	}

	/**
	 * Return an existing product.
	 * 
	 * @param productNodeRef
	 *            the product node ref
	 * @param dataLists
	 *            the data lists
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
	 * @param productNodeRef
	 *            the product node ref
	 */
	@Override
	public void delete(NodeRef productNodeRef) {

		nodeService.deleteNode(productNodeRef);
	}

	/**
	 * *************************************************************************
	 * ************************* Private methods to load data
	 * *******************
	 * *******************************************************
	 * ************************.
	 * 
	 * @param productNodeRef
	 *            the product node ref
	 * @param dataLists
	 *            the data lists
	 * @return the product data
	 */

	private ProductData loadProduct(NodeRef productNodeRef, Collection<QName> dataLists) {

		ProductData productData = null;

		// get type
		Map<QName, Serializable> properties = nodeService.getProperties(productNodeRef);
		QName productTypeQName = nodeService.getType(productNodeRef);
		SystemProductType systemProductType = SystemProductType.valueOf(productTypeQName);

		switch (systemProductType) {

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

		case ResourceProduct:
			productData = new ResourceProductData();
			break;

		default:
			productData = new ProductData();
			break;
		}

		// set properties
		productData.setNodeRef(productNodeRef);
		productData.setProperties(properties);

		// load datalists
		NodeRef listsContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
		productData.setListsContainer(listsContainerNodeRef);

		if (dataLists != null) {
			for (QName dataList : dataLists) {
				if (dataList.equals(BeCPGModel.TYPE_ALLERGENLIST)) {
					productData.setAllergenList(loadAllergenList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_COMPOLIST)) {
					productData.setCompoList(loadCompoList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_COSTLIST)) {
					productData.setCostList(loadCostList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_COSTDETAILSLIST)) {
					productData.setCostDetailsList(loadCostDetailsList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_PRICELIST)) {
					productData.setPriceList(loadPriceList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_INGLIST)) {
					productData.setIngList(loadIngList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_NUTLIST)) {
					productData.setNutList(loadNutList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_ORGANOLIST)) {
					productData.setOrganoList(loadOrganoList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_INGLABELINGLIST)) {
					productData.setIngLabelingList(loadIngLabelingList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_MICROBIOLIST)) {
					productData.setMicrobioList(loadMicrobioList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_PHYSICOCHEMLIST)) {
					productData.setPhysicoChemList(loadPhysicoChemList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_PACKAGINGLIST)) {
					productData.setPackagingList(loadPackagingList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_FORBIDDENINGLIST)) {
					productData.setForbiddenIngList(loadForbiddenIngList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_REQCTRLLIST)) {
					productData.setReqCtrlList(loadReqCtrlList(listsContainerNodeRef));
				} else if (dataList.equals(MPMModel.TYPE_PROCESSLIST)) {
					productData.setProcessList(loadProcessList(listsContainerNodeRef));
				} else if (dataList.equals(BeCPGModel.TYPE_DYNAMICCHARCATLIST)){
					productData.setDynamicCharachList(loadDynamicCharachList(listsContainerNodeRef));
				} else {
					logger.debug(String.format("DataList '%s' is not loaded since it is not implemented.",
					 dataList));
				}
			}
		}

		return productData;

	}

	
	/**
	 * Load allergen list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	@Override
	public List<AllergenListDataItem> loadAllergenList(NodeRef listContainerNodeRef) {
		List<AllergenListDataItem> allergenList = null;

		if (listContainerNodeRef != null) {
			NodeRef allergenListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ALLERGENLIST);

			if (allergenListNodeRef != null) {
				allergenList = new ArrayList<AllergenListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(allergenListNodeRef, BeCPGModel.TYPE_ALLERGENLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					allergenList.add(loadAllergenListItem(listItemNodeRef));
				}
			}
		}

		return allergenList;
	}
	
	@Override
	public AllergenListDataItem loadAllergenListItem(NodeRef listItemNodeRef) {
		List<AssociationRef> allergenAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.PROP_ALLERGENLIST_ALLERGEN);
		NodeRef allergenNodeRef = (allergenAssocRefs.get(0)).getTargetRef();

		List<AssociationRef> volSourcesAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES);
		List<NodeRef> volSources = new ArrayList<NodeRef>(volSourcesAssocRefs.size());
		for (AssociationRef assocRef : volSourcesAssocRefs) {
			volSources.add(assocRef.getTargetRef());
		}

		List<AssociationRef> inVolSourcesAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES);
		List<NodeRef> inVolSources = new ArrayList<NodeRef>(volSourcesAssocRefs.size());
		for (AssociationRef assocRef : inVolSourcesAssocRefs) {
			inVolSources.add(assocRef.getTargetRef());
		}

		return new AllergenListDataItem(listItemNodeRef, (Boolean) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY),
				(Boolean) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY), volSources, inVolSources, allergenNodeRef,
				(Boolean) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM));
	}

	@Override
	public List<DynamicCharachListItem> loadDynamicCharachList(NodeRef listContainerNodeRef) {
		 List<DynamicCharachListItem> dynamicCharachList =  new ArrayList<DynamicCharachListItem>();
		 if(listContainerNodeRef!=null){
			 NodeRef dynamicCharachListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);
			 if (dynamicCharachListNodeRef != null) {
					List<NodeRef> listItemNodeRefs = listItems(dynamicCharachListNodeRef, BeCPGModel.TYPE_DYNAMICCHARCATLIST);
					for (NodeRef listItemNodeRef : listItemNodeRefs) {
						dynamicCharachList.add(loadDynamicCharachListItem(listItemNodeRef));
					}
				}
		 }
		 
		return dynamicCharachList;
	}

	
	@Override
	public DynamicCharachListItem loadDynamicCharachListItem(NodeRef listItemNodeRef) {
		Map<QName,Serializable> properties = nodeService.getProperties(listItemNodeRef);
		
		return new DynamicCharachListItem(listItemNodeRef, (String)properties.get(BeCPGModel.PROP_DYNAMICCHARCAT_TITLE)
				, (String)properties.get(BeCPGModel.PROP_DYNAMICCHARCAT_FORMULA),
				properties.get(BeCPGModel.PROP_DYNAMICCHARCAT_VALUE));
	}

	
	/**
	 * Load compo list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<CompoListDataItem> loadCompoList(NodeRef listContainerNodeRef) {
		List<CompoListDataItem> compoList = null;

		if (listContainerNodeRef != null) {
			NodeRef compoListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);

			if (compoListNodeRef != null) {
				compoList = new ArrayList<CompoListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(compoListNodeRef, BeCPGModel.TYPE_COMPOLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);

					List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
					NodeRef part = compoAssocRefs.size() > 0 ? (compoAssocRefs.get(0)).getTargetRef() : null;
					CompoListUnit compoListUnit = CompoListUnit.valueOf((String) properties.get(BeCPGModel.PROP_COMPOLIST_UNIT));

					CompoListDataItem compoListDataItem = new CompoListDataItem(listItemNodeRef, (Integer) properties.get(BeCPGModel.PROP_DEPTH_LEVEL),
							(Double) properties.get(BeCPGModel.PROP_COMPOLIST_QTY), (Double) properties.get(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA),
							(Double) properties.get(BeCPGModel.PROP_COMPOLIST_QTY_AFTER_PROCESS), compoListUnit, (Double) properties.get(BeCPGModel.PROP_COMPOLIST_LOSS_PERC),
							(Double) properties.get(BeCPGModel.PROP_COMPOLIST_YIELD_PERC), (String) properties.get(BeCPGModel.PROP_COMPOLIST_DECL_GRP),
							(String) properties.get(BeCPGModel.PROP_COMPOLIST_DECL_TYPE), part);
					compoList.add(compoListDataItem);
				}
			}
		}

		return compoList;
	}

	/**
	 * Load cost list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	@Override
	public List<CostListDataItem> loadCostList(NodeRef listContainerNodeRef) {
		List<CostListDataItem> costList = null;

		if (listContainerNodeRef != null) {
			NodeRef costListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);

			if (costListNodeRef != null) {
				costList = new ArrayList<CostListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(costListNodeRef, BeCPGModel.TYPE_COSTLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					costList.add(loadCostListItem(listItemNodeRef));
				}
			}
		}

		return costList;
	}

	@Override
	public CostListDataItem loadCostListItem(NodeRef listItemNodeRef) {

		List<AssociationRef> costAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_COSTLIST_COST);
		NodeRef costNodeRef = (costAssocRefs.get(0)).getTargetRef();
		
		return new CostListDataItem(listItemNodeRef, (Double) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_COSTLIST_VALUE), (String) nodeService.getProperty(
				listItemNodeRef, BeCPGModel.PROP_COSTLIST_UNIT), (Double) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_COSTLIST_MAXI), costNodeRef,
				(Boolean) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM));
	}

	/**
	 * Load cost details list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<CostDetailsListDataItem> loadCostDetailsList(NodeRef listContainerNodeRef) {
		List<CostDetailsListDataItem> costDetailsList = null;

		if (listContainerNodeRef != null) {
			NodeRef costDetailsListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTDETAILSLIST);

			if (costDetailsListNodeRef != null) {
				costDetailsList = new ArrayList<CostDetailsListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(costDetailsListNodeRef, BeCPGModel.TYPE_COSTDETAILSLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {
					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);

					List<AssociationRef> costAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_COSTDETAILSLIST_COST);
					NodeRef costNodeRef = (costAssocRefs.get(0)).getTargetRef();

					List<AssociationRef> sourceAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_COSTDETAILSLIST_SOURCE);
					NodeRef sourceNodeRef = (sourceAssocRefs.get(0)).getTargetRef();

					CostDetailsListDataItem costDetailsListDataItem = new CostDetailsListDataItem(listItemNodeRef, (Double) properties.get(BeCPGModel.PROP_COSTDETAILSLIST_VALUE),
							(String) properties.get(BeCPGModel.PROP_COSTDETAILSLIST_UNIT), (Double) properties.get(BeCPGModel.PROP_COSTDETAILSLIST_PERC), costNodeRef, sourceNodeRef);
					costDetailsList.add(costDetailsListDataItem);
				}
			}
		}

		return costDetailsList;
	}

	/**
	 * Load price list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<PriceListDataItem> loadPriceList(NodeRef listContainerNodeRef) {
		List<PriceListDataItem> priceList = null;

		if (listContainerNodeRef != null) {
			NodeRef priceListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_PRICELIST);

			if (priceListNodeRef != null) {
				priceList = new ArrayList<PriceListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(priceListNodeRef, BeCPGModel.TYPE_PRICELIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {
					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);

					List<AssociationRef> costAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_PRICELIST_COST);
					NodeRef costNodeRef = (costAssocRefs.get(0)).getTargetRef();

					List<NodeRef> suppliersNodeRef = new ArrayList<NodeRef>();
					List<AssociationRef> targetAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_SUPPLIERS);
					for (AssociationRef assocRef : targetAssocRefs) {
						suppliersNodeRef.add(assocRef.getTargetRef());
					}

					PriceListDataItem priceListDataItem = new PriceListDataItem(listItemNodeRef, (Double) properties.get(BeCPGModel.PROP_PRICELIST_VALUE),
							(String) properties.get(BeCPGModel.PROP_PRICELIST_UNIT), (Double) properties.get(BeCPGModel.PROP_PRICELIST_PURCHASE_VALUE),
							(String) properties.get(BeCPGModel.PROP_PRICELIST_PURCHASE_UNIT), (Integer) properties.get(BeCPGModel.PROP_PRICELIST_PREF_RANK),
							(Date) properties.get(BeCPGModel.PROP_START_EFFECTIVITY), (Date) properties.get(BeCPGModel.PROP_END_EFFECTIVITY), costNodeRef, suppliersNodeRef);
					priceList.add(priceListDataItem);
				}
			}
		}

		return priceList;
	}

	/**
	 * Load ing list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	@Override
	public List<IngListDataItem> loadIngList(NodeRef listContainerNodeRef) {
		List<IngListDataItem> ingList = null;

		if (listContainerNodeRef != null) {
			NodeRef ingListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_INGLIST);

			if (ingListNodeRef != null) {
				ingList = new ArrayList<IngListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(ingListNodeRef, BeCPGModel.TYPE_INGLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					ingList.add(loadIngListItem(listItemNodeRef));
				}
			}
		}

		return ingList;
	}

	@Override
	public IngListDataItem loadIngListItem(NodeRef listItemNodeRef) {

		List<AssociationRef> ingAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_INGLIST_ING);
		NodeRef ingNodeRef = (ingAssocRefs.get(0)).getTargetRef();

		List<AssociationRef> geoOriginAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_INGLIST_GEO_ORIGIN);
		List<NodeRef> geoOrigins = new ArrayList<NodeRef>(geoOriginAssocRefs.size());
		for (AssociationRef assocRef : geoOriginAssocRefs) {
			geoOrigins.add(assocRef.getTargetRef());
		}

		List<AssociationRef> bioOriginAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_INGLIST_BIO_ORIGIN);
		List<NodeRef> bioOrigins = new ArrayList<NodeRef>(bioOriginAssocRefs.size());
		for (AssociationRef assocRef : bioOriginAssocRefs) {
			bioOrigins.add(assocRef.getTargetRef());
		}

		return new IngListDataItem(listItemNodeRef, (Double) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_INGLIST_QTY_PERC), geoOrigins, bioOrigins,
				(Boolean) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_INGLIST_IS_GMO), (Boolean) nodeService.getProperty(listItemNodeRef,
						BeCPGModel.PROP_INGLIST_IS_IONIZED), ingNodeRef, (Boolean) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM));
	}

	/**
	 * Load nut list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	@Override
	public List<NutListDataItem> loadNutList(NodeRef listContainerNodeRef) {
		List<NutListDataItem> nutList = null;

		if (listContainerNodeRef != null) {
			NodeRef nutListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_NUTLIST);

			if (nutListNodeRef != null) {
				nutList = new ArrayList<NutListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(nutListNodeRef, BeCPGModel.TYPE_NUTLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					nutList.add(loadNutListItem(listItemNodeRef));
				}
			}
		}

		return nutList;
	}

	@Override
	public NutListDataItem loadNutListItem(NodeRef listItemNodeRef) {

		List<AssociationRef> nutAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_NUTLIST_NUT);
		NodeRef nutNodeRef = (nutAssocRefs.get(0)).getTargetRef();
		return new NutListDataItem(listItemNodeRef, (Double) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_NUTLIST_VALUE), (String) nodeService.getProperty(
				listItemNodeRef, BeCPGModel.PROP_NUTLIST_UNIT), (Double) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_NUTLIST_MINI), (Double) nodeService.getProperty(
				listItemNodeRef, BeCPGModel.PROP_NUTLIST_MAXI), (String) nodeService.getProperty(nutNodeRef, BeCPGModel.PROP_NUTGROUP), nutNodeRef,
				(Boolean) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM));

	}

	/**
	 * Load organo list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<OrganoListDataItem> loadOrganoList(NodeRef listContainerNodeRef) {
		List<OrganoListDataItem> organoList = null;

		if (listContainerNodeRef != null) {
			NodeRef organoListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ORGANOLIST);

			if (organoListNodeRef != null) {
				organoList = new ArrayList<OrganoListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(organoListNodeRef, BeCPGModel.TYPE_ORGANOLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);

					List<AssociationRef> organoAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_ORGANOLIST_ORGANO);
					NodeRef organoNodeRef = (organoAssocRefs.get(0)).getTargetRef();
					OrganoListDataItem organoListDataItem = new OrganoListDataItem(listItemNodeRef, (String) properties.get(BeCPGModel.PROP_ORGANOLIST_VALUE), organoNodeRef);
					organoList.add(organoListDataItem);
				}
			}
		}

		return organoList;
	}

	/**
	 * Load ing labeling list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	@Override
	public List<IngLabelingListDataItem> loadIngLabelingList(NodeRef listContainerNodeRef) {
		List<IngLabelingListDataItem> ingLabelingList = null;

		if (listContainerNodeRef != null) {
			NodeRef ingLabelingListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);

			if (ingLabelingListNodeRef != null) {
				ingLabelingList = new ArrayList<IngLabelingListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(ingLabelingListNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					ingLabelingList.add(loadIngLabelingListItem(listItemNodeRef));
				}
			}
		}

		return ingLabelingList;
	}

	@Override
	public IngLabelingListDataItem loadIngLabelingListItem(NodeRef listItemNodeRef) {

		// Grp
		String grp = (String) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_ILL_GRP);

		// illValue
		MLText illValue = (MLText) mlNodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_ILL_VALUE);
		return new IngLabelingListDataItem(listItemNodeRef, grp, illValue, (Boolean) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM));
	}

	/**
	 * Load microbio list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<MicrobioListDataItem> loadMicrobioList(NodeRef listContainerNodeRef) {
		List<MicrobioListDataItem> microbioList = null;

		if (listContainerNodeRef != null) {
			NodeRef microbioListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_MICROBIOLIST);

			if (microbioListNodeRef != null) {
				microbioList = new ArrayList<MicrobioListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(microbioListNodeRef, BeCPGModel.TYPE_MICROBIOLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);

					List<AssociationRef> microbioAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO);
					NodeRef organoNodeRef = (microbioAssocRefs.get(0)).getTargetRef();
					MicrobioListDataItem microbioListDataItem = new MicrobioListDataItem(listItemNodeRef, (Double) properties.get(BeCPGModel.PROP_MICROBIOLIST_VALUE),
							(String) properties.get(BeCPGModel.PROP_MICROBIOLIST_UNIT), (Double) properties.get(BeCPGModel.PROP_MICROBIOLIST_MAXI),
							(String) properties.get(BeCPGModel.PROP_MICROBIOLIST_TEXT_CRITERIA), organoNodeRef);
					microbioList.add(microbioListDataItem);
				}
			}
		}

		return microbioList;
	}

	/**
	 * Load physico chem list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<PhysicoChemListDataItem> loadPhysicoChemList(NodeRef listContainerNodeRef) {
		List<PhysicoChemListDataItem> physicoChemList = null;

		if (listContainerNodeRef != null) {
			NodeRef physicoChemListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_PHYSICOCHEMLIST);

			if (physicoChemListNodeRef != null) {
				physicoChemList = new ArrayList<PhysicoChemListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(physicoChemListNodeRef, BeCPGModel.TYPE_PHYSICOCHEMLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);

					List<AssociationRef> physicoChemAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM);
					NodeRef physicoChemNodeRef = (physicoChemAssocRefs.get(0)).getTargetRef();
					PhysicoChemListDataItem physicoChemListDataItem = new PhysicoChemListDataItem(listItemNodeRef, (Double) properties.get(BeCPGModel.PROP_PHYSICOCHEMLIST_VALUE),
							(String) properties.get(BeCPGModel.PROP_PHYSICOCHEMLIST_UNIT), (Double) properties.get(BeCPGModel.PROP_PHYSICOCHEMLIST_MINI),
							(Double) properties.get(BeCPGModel.PROP_PHYSICOCHEMLIST_MAXI), physicoChemNodeRef);
					physicoChemList.add(physicoChemListDataItem);
				}
			}
		}

		return physicoChemList;
	}

	/**
	 * Load packaging list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<PackagingListDataItem> loadPackagingList(NodeRef listContainerNodeRef) {
		List<PackagingListDataItem> packagingList = null;

		if (listContainerNodeRef != null) {
			NodeRef pkgingListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_PACKAGINGLIST);

			if (pkgingListNodeRef != null) {
				packagingList = new ArrayList<PackagingListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(pkgingListNodeRef, BeCPGModel.TYPE_PACKAGINGLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);

					List<AssociationRef> productAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT);
					NodeRef productNodeRef = (productAssocRefs.get(0)).getTargetRef();
					PackagingListUnit packagingListUnit = PackagingListUnit.valueOf((String) properties.get(BeCPGModel.PROP_PACKAGINGLIST_UNIT));

					PackagingListDataItem packagingListDataItem = new PackagingListDataItem(listItemNodeRef, (Double) properties.get(BeCPGModel.PROP_PACKAGINGLIST_QTY),
							packagingListUnit, (String) properties.get(BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL), productNodeRef);
					packagingList.add(packagingListDataItem);
				}
			}
		}

		return packagingList;
	}

	/**
	 * Load forbiddenIng list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<ForbiddenIngListDataItem> loadForbiddenIngList(NodeRef listContainerNodeRef) {
		List<ForbiddenIngListDataItem> forbiddenIngList = null;

		if (listContainerNodeRef != null) {
			NodeRef forbiddenIngListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_FORBIDDENINGLIST);

			if (forbiddenIngListNodeRef != null) {
				forbiddenIngList = new ArrayList<ForbiddenIngListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(forbiddenIngListNodeRef, BeCPGModel.TYPE_FORBIDDENINGLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);
					NullableBoolean isGMO = NullableBoolean.valueOf((String) properties.get(BeCPGModel.PROP_FIL_IS_GMO), true);
					NullableBoolean isIonized = NullableBoolean.valueOf((String) properties.get(BeCPGModel.PROP_FIL_IS_IONIZED), true);

					List<AssociationRef> ingAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_FIL_INGS);
					List<NodeRef> ings = new ArrayList<NodeRef>(ingAssocRefs.size());
					for (AssociationRef assocRef : ingAssocRefs) {
						ings.add(assocRef.getTargetRef());
					}

					List<AssociationRef> geoOriginAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_FIL_GEO_ORIGINS);
					List<NodeRef> geoOrigins = new ArrayList<NodeRef>(geoOriginAssocRefs.size());
					for (AssociationRef assocRef : geoOriginAssocRefs) {
						geoOrigins.add(assocRef.getTargetRef());
					}

					List<AssociationRef> bioOriginAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_FIL_BIO_ORIGINS);
					List<NodeRef> bioOrigins = new ArrayList<NodeRef>(bioOriginAssocRefs.size());
					for (AssociationRef assocRef : bioOriginAssocRefs) {
						bioOrigins.add(assocRef.getTargetRef());
					}

					RequirementType reqType = null;
					String strReqType = (String) properties.get(BeCPGModel.PROP_FIL_REQ_TYPE);
					if (strReqType != null) {
						reqType = RequirementType.valueOf(strReqType);
					}

					ForbiddenIngListDataItem forbiddenIngListDataItem = new ForbiddenIngListDataItem(listItemNodeRef, reqType,
							(String) properties.get(BeCPGModel.PROP_FIL_REQ_MESSAGE), (Double) properties.get(BeCPGModel.PROP_FIL_QTY_PERC_MAXI), isGMO, isIonized, ings,
							geoOrigins, bioOrigins);

					forbiddenIngList.add(forbiddenIngListDataItem);
				}
			}
		}

		return forbiddenIngList;
	}

	/**
	 * Load reqCtrl list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<ReqCtrlListDataItem> loadReqCtrlList(NodeRef listContainerNodeRef) {
		List<ReqCtrlListDataItem> reqCtrlList = null;

		if (listContainerNodeRef != null) {
			NodeRef reqCtrlListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);

			if (reqCtrlListNodeRef != null) {
				reqCtrlList = new ArrayList<ReqCtrlListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(reqCtrlListNodeRef, BeCPGModel.TYPE_REQCTRLLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {
					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);

					List<AssociationRef> sourceAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_RCL_SOURCES);
					List<NodeRef> sources = new ArrayList<NodeRef>(sourceAssocRefs.size());
					for (AssociationRef assocRef : sourceAssocRefs) {
						sources.add(assocRef.getTargetRef());
					}

					RequirementType reqType = null;
					String strReqType = (String) properties.get(BeCPGModel.PROP_RCL_REQ_TYPE);
					if (strReqType != null) {
						reqType = RequirementType.valueOf(strReqType);
					}

					ReqCtrlListDataItem reqCtrlListDataItem = new ReqCtrlListDataItem(listItemNodeRef, reqType, (String) properties.get(BeCPGModel.PROP_RCL_REQ_MESSAGE), sources);

					reqCtrlList.add(reqCtrlListDataItem);
				}
			}
		}

		return reqCtrlList;
	}

	/**
	 * Load process list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @return the list
	 */
	private List<ProcessListDataItem> loadProcessList(NodeRef listContainerNodeRef) {
		List<ProcessListDataItem> processList = null;

		if (listContainerNodeRef != null) {
			NodeRef processListNodeRef = entityListDAO.getList(listContainerNodeRef, MPMModel.TYPE_PROCESSLIST);

			if (processListNodeRef != null) {
				processList = new ArrayList<ProcessListDataItem>();
				List<NodeRef> listItemNodeRefs = listItems(processListNodeRef, MPMModel.TYPE_PROCESSLIST);

				for (NodeRef listItemNodeRef : listItemNodeRefs) {
					Map<QName, Serializable> properties = nodeService.getProperties(listItemNodeRef);

					List<AssociationRef> stepAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, MPMModel.ASSOC_PL_STEP);
					NodeRef stepNodeRef = stepAssocRefs.size() > 0 ? (stepAssocRefs.get(0)).getTargetRef() : null;

					List<AssociationRef> productAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, MPMModel.ASSOC_PL_PRODUCT);
					NodeRef productNodeRef = productAssocRefs.size() > 0 ? (productAssocRefs.get(0)).getTargetRef() : null;

					List<AssociationRef> resourceAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, MPMModel.ASSOC_PL_RESOURCE);
					NodeRef resourceNodeRef = resourceAssocRefs.size() > 0 ? (resourceAssocRefs.get(0)).getTargetRef() : null;

					ProcessListDataItem processListDataItem = new ProcessListDataItem(listItemNodeRef, (Double) properties.get(MPMModel.PROP_PL_QTY),
							(Double) properties.get(MPMModel.PROP_PL_QTY_RESOURCE), (Double) properties.get(MPMModel.PROP_PL_RATE_RESOURCE),
							(Double) properties.get(MPMModel.PROP_PL_YIELD), (Double) properties.get(MPMModel.PROP_PL_RATE_PROCESS),
							(Double) properties.get(MPMModel.PROP_PL_RATE_PRODUCT), stepNodeRef, productNodeRef, resourceNodeRef);

					processList.add(processListDataItem);
				}
			}
		}

		return processList;
	}

	/**
	 * *************************************************************************
	 * ************************* Private methods for creation *
	 * *****************
	 * *********************************************************
	 * ************************.
	 * 
	 * @param productNodeRef
	 *            the product node ref
	 * @param productData
	 *            the product data
	 * @param dataLists
	 *            the data lists
	 */

	private void createDataLists(NodeRef productNodeRef, ProductData productData, Collection<QName> dataLists) {
		// Container
		NodeRef containerNodeRef = entityListDAO.getListContainer(productNodeRef);
		if (containerNodeRef == null) {
			containerNodeRef = entityListDAO.createListContainer(productNodeRef);
		}
		productData.setListsContainer(containerNodeRef);

		// Lists
		try {
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);

			if (dataLists != null) {
				for (QName dataList : dataLists) {

					if (dataList.equals(BeCPGModel.TYPE_ALLERGENLIST)) {
						createAllergenList(containerNodeRef, productData.getAllergenList());
					} else if (dataList.equals(BeCPGModel.TYPE_COMPOLIST)) {
						createCompoList(containerNodeRef, productData.getCompoList());
					} else if (dataList.equals(BeCPGModel.TYPE_COSTLIST)) {
						createCostList(containerNodeRef, productData.getCostList());
					} else if (dataList.equals(BeCPGModel.TYPE_COSTDETAILSLIST)) {
						createCostDetailsList(containerNodeRef, productData.getCostDetailsList());
					} else if (dataList.equals(BeCPGModel.TYPE_PRICELIST)) {
						createPriceList(containerNodeRef, productData.getPriceList());
					} else if (dataList.equals(BeCPGModel.TYPE_INGLIST)) {
						createIngList(containerNodeRef, productData.getIngList());
					} else if (dataList.equals(BeCPGModel.TYPE_NUTLIST)) {
						createNutList(containerNodeRef, productData.getNutList());
					} else if (dataList.equals(BeCPGModel.TYPE_ORGANOLIST)) {
						createOrganoList(containerNodeRef, productData.getOrganoList());
					} else if (dataList.equals(BeCPGModel.TYPE_INGLABELINGLIST)) {
						createIngLabelingList(containerNodeRef, productData.getIngLabelingList());
					} else if (dataList.equals(BeCPGModel.TYPE_MICROBIOLIST)) {
						createMicrobioList(containerNodeRef, productData.getMicrobioList());
					} else if (dataList.equals(BeCPGModel.TYPE_PHYSICOCHEMLIST)) {
						createPhysicoChemList(containerNodeRef, productData.getPhysicoChemList());
					} else if (dataList.equals(BeCPGModel.TYPE_PACKAGINGLIST)) {
						createPackagingList(containerNodeRef, productData.getPackagingList());
					} else if (dataList.equals(BeCPGModel.TYPE_FORBIDDENINGLIST)) {
						createForbiddenIngList(containerNodeRef, productData.getForbiddenIngList());
					} else if (dataList.equals(BeCPGModel.TYPE_REQCTRLLIST)) {
						createReqCtrlList(containerNodeRef, productData.getReqCtrlList());
					} else if (dataList.equals(MPMModel.TYPE_PROCESSLIST)) {
						createProcessList(containerNodeRef, productData.getProcessList());
					}	else if (dataList.equals(BeCPGModel.TYPE_DYNAMICCHARCATLIST)){
						createDynamicCharachList(containerNodeRef,productData.getDynamicCharachList());
					} else {
						 logger.debug(String.format("DataList '%s' is not created since it is not implemented.",
						 dataList));
					}
				}
			}
		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
		}
	}


	/**
	 * Creates the allergen list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param allergenList
	 *            the allergen list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createAllergenList(NodeRef listContainerNodeRef, List<AllergenListDataItem> allergenList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef allergenListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ALLERGENLIST);

			if (allergenList == null) {
				// delete existing list
				if (allergenListNodeRef != null)
					nodeService.deleteNode(allergenListNodeRef);
			} else {

				// allergen list, create if needed
				if (allergenListNodeRef == null) {
					allergenListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ALLERGENLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(allergenListNodeRef, BeCPGModel.TYPE_ALLERGENLIST);

				// create temp list
				List<NodeRef> allergenListToTreat = new ArrayList<NodeRef>();
				for (AllergenListDataItem allergenListDataItem : allergenList) {
					allergenListToTreat.add(allergenListDataItem.getAllergen());
				}

				// remove deleted nodes
				Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					List<AssociationRef> allergenAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.PROP_ALLERGENLIST_ALLERGEN);
					NodeRef allergenNodeRef = (allergenAssocRefs.get(0)).getTargetRef();

					if (!allergenListToTreat.contains(allergenNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.put(allergenNodeRef, listItemNodeRef);
					}
				}

				// update or create nodes
				int sortIndex = 1;
				for (AllergenListDataItem allergenListDataItem : allergenList) {
					NodeRef linkNodeRef = null;
					NodeRef allergenNodeRef = allergenListDataItem.getAllergen();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY, allergenListDataItem.getInVoluntary());
					properties.put(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, allergenListDataItem.getVoluntary());
					properties.put(BeCPGModel.PROP_IS_MANUAL_LISTITEM, allergenListDataItem.getIsManual());

					properties.put(BeCPGModel.PROP_SORT, sortIndex);
					sortIndex++;

					if (filesToUpdate.containsKey(allergenNodeRef)) {
						// update
						linkNodeRef = filesToUpdate.get(allergenNodeRef);
						nodeService.setProperties(linkNodeRef, properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(allergenListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, allergenListDataItem.getAllergen().getId()), BeCPGModel.TYPE_ALLERGENLIST, properties);
						linkNodeRef = childAssocRef.getChildRef();
						nodeService.createAssociation(linkNodeRef, allergenListDataItem.getAllergen(), BeCPGModel.PROP_ALLERGENLIST_ALLERGEN);
					}

					// Voluntary
					associationService.update(linkNodeRef, BeCPGModel.ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES, allergenListDataItem.getVoluntarySources());

					// InVoluntary
					associationService.update(linkNodeRef, BeCPGModel.ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES, allergenListDataItem.getInVoluntarySources());
				}
			}
		}
	}


	private void createDynamicCharachList(NodeRef listContainerNodeRef, List<DynamicCharachListItem> dynamicCharachList) {
		if (listContainerNodeRef != null) {
			NodeRef dynamicCharachListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);

			if (dynamicCharachList == null) {
				// delete existing list
				if (dynamicCharachListNodeRef != null){
					nodeService.deleteNode(dynamicCharachListNodeRef);
				}
			} else {

				// dynamicCharach list, create if needed
				if (dynamicCharachListNodeRef == null) {
					dynamicCharachListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(dynamicCharachListNodeRef, BeCPGModel.TYPE_DYNAMICCHARCATLIST);

				// create temp list
				List<NodeRef> dynamicCharachListToTreat = new ArrayList<NodeRef>();
				for (DynamicCharachListItem dynamicCharachListDataItem : dynamicCharachList) {
					if(dynamicCharachListDataItem.getNodeRef()!=null){
						dynamicCharachListToTreat.add(dynamicCharachListDataItem.getNodeRef());
					}
				}
				List<NodeRef> filesToUpdate  = new ArrayList<NodeRef>();
				// remove deleted nodes
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					if (!dynamicCharachListToTreat.contains(listItemNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.add(listItemNodeRef);
					}
				}

				// update or create nodes
				int sortIndex = 1;
				for (DynamicCharachListItem dynamicCharachListDataItem : dynamicCharachList) {
					NodeRef linkNodeRef = dynamicCharachListDataItem.getNodeRef();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_DYNAMICCHARCAT_TITLE, dynamicCharachListDataItem.getDynamicCharachTitle());
					properties.put(BeCPGModel.PROP_DYNAMICCHARCAT_FORMULA, dynamicCharachListDataItem.getDynamicCharachFormula());
					properties.put(BeCPGModel.PROP_DYNAMICCHARCAT_VALUE, (Serializable) dynamicCharachListDataItem.getDynamicCharachValue());

					properties.put(BeCPGModel.PROP_SORT, sortIndex);
					sortIndex++;

					if (filesToUpdate.contains(linkNodeRef)) {
						// update
						nodeService.setProperties(linkNodeRef, properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(dynamicCharachListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(dynamicCharachListDataItem.getDynamicCharachTitle()))
								, BeCPGModel.TYPE_DYNAMICCHARCATLIST, properties);
						linkNodeRef = childAssocRef.getChildRef();
					}

				}
			}
		}
		
	}
	
	
	/**
	 * Create/Update composition.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param compoList
	 *            the compo list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createCompoList(NodeRef listContainerNodeRef, List<CompoListDataItem> compoList) throws InvalidTypeException {
		if (listContainerNodeRef != null) {
			NodeRef compoListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);

			if (compoList == null) {
				// delete existing list
				if (compoListNodeRef != null)
					nodeService.deleteNode(compoListNodeRef);
			} else {

				// compo list, create if needed
				if (compoListNodeRef == null) {
					compoListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(compoListNodeRef, BeCPGModel.TYPE_COMPOLIST);

				// create temp list
				List<NodeRef> compoListToTreat = new ArrayList<NodeRef>();
				for (CompoListDataItem compoListDataItem : compoList) {
					compoListToTreat.add(compoListDataItem.getNodeRef());
				}

				// remove deleted nodes
				List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					if (!compoListToTreat.contains(listItemNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.add(listItemNodeRef);
					}
				}

				Composite<CompoListDataItem> composite = CompoListDataItem.getHierarchicalCompoList(compoList);
				int sortIndex = 1;
				createCompositeCompoListItem(compoListNodeRef, composite, filesToUpdate, sortIndex);
			}
		}
	}

	private int createCompositeCompoListItem(NodeRef compoListNodeRef, Composite<CompoListDataItem> composite, List<NodeRef> filesToUpdate, int sortIndex) {

		for (AbstractComponent<CompoListDataItem> component : composite.getChildren()) {

			CompoListDataItem compoListDataItem = component.getData();

			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(BeCPGModel.PROP_DEPTH_LEVEL, compoListDataItem.getDepthLevel());
			properties.put(BeCPGModel.PROP_COMPOLIST_QTY, compoListDataItem.getQty());
			properties.put(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA, compoListDataItem.getQtySubFormula());
			properties.put(BeCPGModel.PROP_COMPOLIST_QTY_AFTER_PROCESS, compoListDataItem.getQtyAfterProcess());
			properties.put(BeCPGModel.PROP_COMPOLIST_UNIT, compoListDataItem.getCompoListUnit() == CompoListUnit.Unknown ? "" : compoListDataItem.getCompoListUnit().toString());
			properties.put(BeCPGModel.PROP_COMPOLIST_LOSS_PERC, compoListDataItem.getLossPerc());
			properties.put(BeCPGModel.PROP_COMPOLIST_YIELD_PERC, compoListDataItem.getYieldPerc());
			properties.put(BeCPGModel.PROP_COMPOLIST_DECL_GRP, compoListDataItem.getDeclGrp());
			properties.put(BeCPGModel.PROP_COMPOLIST_DECL_TYPE, compoListDataItem.getDeclType());

			properties.put(BeCPGModel.PROP_SORT, sortIndex);
			sortIndex++;

			if (filesToUpdate.contains(compoListDataItem.getNodeRef())) {
				// update
				nodeService.setProperties(compoListDataItem.getNodeRef(), properties);
			} else {
				// create
				ChildAssociationRef childAssocRef = nodeService.createNode(compoListNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, compoListDataItem.getProduct().getId()), BeCPGModel.TYPE_COMPOLIST, properties);
				compoListDataItem.setNodeRef(childAssocRef.getChildRef());
			}

			// Update product
			List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(compoListDataItem.getNodeRef(), BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
			if (compoAssocRefs.size() > 0) {
				NodeRef part = (compoAssocRefs.get(0)).getTargetRef();
				if (part != compoListDataItem.getProduct()) {
					nodeService.removeAssociation(compoListDataItem.getNodeRef(), part, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
				}
			}
			nodeService.createAssociation(compoListDataItem.getNodeRef(), compoListDataItem.getProduct(), BeCPGModel.ASSOC_COMPOLIST_PRODUCT);

			// store father if level > 1
			if (compoListDataItem.getDepthLevel() > 1) {

				CompoListDataItem compositeCompoListDataItem = composite.getData();
				boolean createFather = true;
				compoAssocRefs = nodeService.getTargetAssocs(compoListDataItem.getNodeRef(), BeCPGModel.ASSOC_COMPOLIST_FATHER);
				if (compoAssocRefs.size() > 0) {
					NodeRef fatherNodeRef = (compoAssocRefs.get(0)).getTargetRef();

					if (fatherNodeRef != null && fatherNodeRef == compositeCompoListDataItem.getNodeRef()) {
						createFather = false;
					} else {
						nodeService.removeAssociation(compoListDataItem.getNodeRef(), fatherNodeRef, BeCPGModel.ASSOC_COMPOLIST_FATHER);
					}
				}
				if (createFather)
					nodeService.createAssociation(compoListDataItem.getNodeRef(), compositeCompoListDataItem.getNodeRef(), BeCPGModel.ASSOC_COMPOLIST_FATHER);
			}

			if (component instanceof Composite) {

				sortIndex = createCompositeCompoListItem(compoListNodeRef, (Composite<CompoListDataItem>) component, filesToUpdate, sortIndex);
			}

		}

		return sortIndex;
	}

	/**
	 * Create/Update costs.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param costList
	 *            the cost list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	public void createCostList(NodeRef listContainerNodeRef, List<CostListDataItem> costList) {
		if (listContainerNodeRef != null) {
			NodeRef costListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);

			if (costList == null) {
				// delete existing list
				if (costListNodeRef != null)
					nodeService.deleteNode(costListNodeRef);
			} else {
				// cost list, create if needed
				if (costListNodeRef == null) {
					costListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(costListNodeRef, BeCPGModel.TYPE_COSTLIST);

				// create temp list
				List<NodeRef> costListToTreat = new ArrayList<NodeRef>();
				for (CostListDataItem costListDataItem : costList) {
					costListToTreat.add(costListDataItem.getCost());
				}

				// remove deleted nodes
				Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					List<AssociationRef> costAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_COSTLIST_COST);
					NodeRef costNodeRef = (costAssocRefs.get(0)).getTargetRef();

					if (!costListToTreat.contains(costNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.put(costNodeRef, listItemNodeRef);
					}
				}

				// update or create nodes
				int sortIndex = 1;
				for (CostListDataItem costListDataItem : costList) {
					createCostListItem(costListNodeRef, costListDataItem, filesToUpdate, sortIndex);
					sortIndex++;
				}
			}
		}
	}

	public void createCostListItem(NodeRef costListNodeRef, CostListDataItem costListDataItem, Map<NodeRef, NodeRef> filesToUpdate, Integer sortIndex) {
		NodeRef costNodeRef = costListDataItem.getCost();
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(BeCPGModel.PROP_COSTLIST_VALUE, costListDataItem.getValue());
		properties.put(BeCPGModel.PROP_COSTLIST_UNIT, costListDataItem.getUnit());
		properties.put(BeCPGModel.PROP_COSTLIST_MAXI, costListDataItem.getMaxi());
		properties.put(BeCPGModel.PROP_IS_MANUAL_LISTITEM, costListDataItem.getIsManual());

		properties.put(BeCPGModel.PROP_SORT, sortIndex);

		if (filesToUpdate != null && filesToUpdate.containsKey(costNodeRef)) {
			// update
			nodeService.setProperties(filesToUpdate.get(costNodeRef), properties);
		} else {
			// create
			ChildAssociationRef childAssocRef = nodeService.createNode(costListNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, costListDataItem.getCost().getId()), BeCPGModel.TYPE_COSTLIST, properties);
			nodeService.createAssociation(childAssocRef.getChildRef(), costListDataItem.getCost(), BeCPGModel.ASSOC_COSTLIST_COST);
		}
	}

	/**
	 * Create/Update costs details.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param costList
	 *            the cost list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createCostDetailsList(NodeRef listContainerNodeRef, List<CostDetailsListDataItem> costDetailsList) throws InvalidTypeException {
		if (listContainerNodeRef != null) {
			NodeRef costDetailsListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTDETAILSLIST);

			if (costDetailsList == null) {
				// delete existing list
				if (costDetailsListNodeRef != null)
					nodeService.deleteNode(costDetailsListNodeRef);
			} else {
				// costDetails list, create if needed
				if (costDetailsListNodeRef == null) {
					costDetailsListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_COSTDETAILSLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(costDetailsListNodeRef, BeCPGModel.TYPE_COSTDETAILSLIST);

				// create temp list
				List<String> costDetailsListToTreat = new ArrayList<String>();
				for (CostDetailsListDataItem costDetailsListDataItem : costDetailsList) {
					costDetailsListToTreat.add(getCostDetailsKey(costDetailsListDataItem.getCost(), costDetailsListDataItem.getSource()));
				}

				// remove deleted nodes
				Map<String, NodeRef> filesToUpdate = new HashMap<String, NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					List<AssociationRef> costAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_COSTDETAILSLIST_COST);
					NodeRef costNodeRef = (costAssocRefs.get(0)).getTargetRef();

					List<AssociationRef> sourceAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_COSTDETAILSLIST_SOURCE);
					NodeRef sourceNodeRef = (sourceAssocRefs.get(0)).getTargetRef();

					String key = getCostDetailsKey(costNodeRef, sourceNodeRef);

					if (!costDetailsListToTreat.contains(key)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.put(key, listItemNodeRef);
					}
				}

				// update or create nodes
				int sortIndex = 1;
				for (CostDetailsListDataItem costDetailsListDataItem : costDetailsList) {
					NodeRef costNodeRef = costDetailsListDataItem.getCost();
					NodeRef sourceNodeRef = costDetailsListDataItem.getSource();
					String key = getCostDetailsKey(costNodeRef, sourceNodeRef);

					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_COSTDETAILSLIST_VALUE, costDetailsListDataItem.getValue());
					properties.put(BeCPGModel.PROP_COSTDETAILSLIST_UNIT, costDetailsListDataItem.getUnit());
					properties.put(BeCPGModel.PROP_COSTDETAILSLIST_PERC, costDetailsListDataItem.getPercentage());

					properties.put(BeCPGModel.PROP_SORT, sortIndex);
					sortIndex++;

					if (filesToUpdate.containsKey(key)) {
						// update
						nodeService.setProperties(filesToUpdate.get(key), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(costDetailsListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, costDetailsListDataItem.getCost().getId()), BeCPGModel.TYPE_COSTDETAILSLIST, properties);
						nodeService.createAssociation(childAssocRef.getChildRef(), costDetailsListDataItem.getCost(), BeCPGModel.ASSOC_COSTDETAILSLIST_COST);
						nodeService.createAssociation(childAssocRef.getChildRef(), costDetailsListDataItem.getSource(), BeCPGModel.ASSOC_COSTDETAILSLIST_SOURCE);
					}
				}
			}
		}
	}

	/**
	 * Create/Update price list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param costList
	 *            the price list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createPriceList(NodeRef listContainerNodeRef, List<PriceListDataItem> priceList) throws InvalidTypeException {
		if (listContainerNodeRef != null) {
			NodeRef priceListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_PRICELIST);

			if (priceList == null) {
				// delete existing list
				if (priceListNodeRef != null)
					nodeService.deleteNode(priceListNodeRef);
			} else {
				// price list, create if needed
				if (priceListNodeRef == null) {
					priceListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_PRICELIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(priceListNodeRef, BeCPGModel.TYPE_PRICELIST);

				// create temp list
				List<NodeRef> priceListToTreat = new ArrayList<NodeRef>();
				for (PriceListDataItem priceListDataItem : priceList) {
					priceListToTreat.add(priceListDataItem.getNodeRef());
				}

				// remove deleted nodes
				List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					if (!priceListToTreat.contains(listItemNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.add(listItemNodeRef);
					}
				}

				// update or create nodes
				int sortIndex = 1;
				for (PriceListDataItem priceListDataItem : priceList) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_PRICELIST_VALUE, priceListDataItem.getValue());
					properties.put(BeCPGModel.PROP_PRICELIST_UNIT, priceListDataItem.getUnit());
					properties.put(BeCPGModel.PROP_PRICELIST_PURCHASE_VALUE, priceListDataItem.getPurchaseValue());
					properties.put(BeCPGModel.PROP_PRICELIST_PURCHASE_UNIT, priceListDataItem.getPurchaseUnit());
					properties.put(BeCPGModel.PROP_PRICELIST_PREF_RANK, priceListDataItem.getPrefRank());

					properties.put(BeCPGModel.PROP_SORT, sortIndex);
					sortIndex++;

					if (filesToUpdate.contains(priceListDataItem.getNodeRef())) {
						// update
						nodeService.setProperties(priceListDataItem.getNodeRef(), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(priceListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), BeCPGModel.TYPE_PRICELIST, properties);

						associationService.update(childAssocRef.getChildRef(), BeCPGModel.ASSOC_PRICELIST_COST, priceListDataItem.getCost());
						associationService.update(childAssocRef.getChildRef(), BeCPGModel.ASSOC_SUPPLIERS, priceListDataItem.getSuppliers());
					}
				}
			}
		}
	}

	/**
	 * Create/Update ings.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param ingList
	 *            the ing list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createIngList(NodeRef listContainerNodeRef, List<IngListDataItem> ingList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef ingListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_INGLIST);

			if (ingList == null) {
				// delete existing list
				if (ingListNodeRef != null)
					nodeService.deleteNode(ingListNodeRef);
			} else {
				// ing list, create if needed
				if (ingListNodeRef == null) {
					ingListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_INGLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(ingListNodeRef, BeCPGModel.TYPE_INGLIST);

				// create temp list
				List<NodeRef> ingListToTreat = new ArrayList<NodeRef>();
				for (IngListDataItem ingListDataItem : ingList) {
					ingListToTreat.add(ingListDataItem.getIng());
				}

				// remove deleted nodes
				Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					List<AssociationRef> ingAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_INGLIST_ING);
					NodeRef ingNodeRef = (ingAssocRefs.get(0)).getTargetRef();

					if (!ingListToTreat.contains(ingNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.put(ingNodeRef, listItemNodeRef);
					}
				}

				// update or create nodes
				int sortIndex = 1;
				for (IngListDataItem ingListDataItem : ingList) {
					NodeRef linkNodeRef = ingListDataItem.getIng();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_INGLIST_QTY_PERC, ingListDataItem.getQtyPerc());
					properties.put(BeCPGModel.PROP_INGLIST_IS_GMO, ingListDataItem.isGMO());
					properties.put(BeCPGModel.PROP_INGLIST_IS_IONIZED, ingListDataItem.isIonized());
					properties.put(BeCPGModel.PROP_IS_MANUAL_LISTITEM, ingListDataItem.getIsManual());

					properties.put(BeCPGModel.PROP_SORT, sortIndex);
					sortIndex++;

					// Ing
					if (filesToUpdate.containsKey(linkNodeRef)) {
						// update
						linkNodeRef = filesToUpdate.get(linkNodeRef);
						nodeService.setProperties(linkNodeRef, properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(ingListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, ingListDataItem.getIng().getId()), BeCPGModel.TYPE_INGLIST, properties);
						linkNodeRef = childAssocRef.getChildRef();
						nodeService.createAssociation(linkNodeRef, ingListDataItem.getIng(), BeCPGModel.ASSOC_INGLIST_ING);
					}

					// GeoOrigins
					associationService.update(linkNodeRef, BeCPGModel.ASSOC_INGLIST_GEO_ORIGIN, ingListDataItem.getGeoOrigin());

					// BioOrigins
					associationService.update(linkNodeRef, BeCPGModel.ASSOC_INGLIST_BIO_ORIGIN, ingListDataItem.getBioOrigin());
				}
			}
		}
	}

	/**
	 * Create/Update nuts.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param nutList
	 *            the nut list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createNutList(NodeRef listContainerNodeRef, List<NutListDataItem> nutList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef nutListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_NUTLIST);

			if (nutList == null) {
				// delete existing list
				if (nutListNodeRef != null)
					nodeService.deleteNode(nutListNodeRef);
			} else {

				// nut list, create if needed
				if (nutListNodeRef == null) {
					nutListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_NUTLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(nutListNodeRef, BeCPGModel.TYPE_NUTLIST);

				// create temp list
				List<NodeRef> nutListToTreat = new ArrayList<NodeRef>();
				for (NutListDataItem nutListDataItem : nutList) {
					nutListToTreat.add(nutListDataItem.getNut());
				}

				// remove deleted nodes
				Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					List<AssociationRef> nutAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_NUTLIST_NUT);
					NodeRef nutNodeRef = (nutAssocRefs.get(0)).getTargetRef();

					if (!nutListToTreat.contains(nutNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.put(nutNodeRef, listItemNodeRef);
					}
				}

				// update or create nodes
				int sortIndex = 1;
				for (NutListDataItem nutListDataItem : nutList) {
					NodeRef nutNodeRef = nutListDataItem.getNut();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_NUTLIST_VALUE, nutListDataItem.getValue());
					properties.put(BeCPGModel.PROP_NUTLIST_UNIT, nutListDataItem.getUnit());
					properties.put(BeCPGModel.PROP_NUTLIST_MINI, nutListDataItem.getMini());
					properties.put(BeCPGModel.PROP_NUTLIST_MAXI, nutListDataItem.getMaxi());
					properties.put(BeCPGModel.PROP_NUTLIST_GROUP, nutListDataItem.getGroup());
					properties.put(BeCPGModel.PROP_IS_MANUAL_LISTITEM, nutListDataItem.getIsManual());

					properties.put(BeCPGModel.PROP_SORT, sortIndex);
					sortIndex++;

					if (filesToUpdate.containsKey(nutNodeRef)) {
						// update
						nodeService.setProperties(filesToUpdate.get(nutNodeRef), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(nutListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nutListDataItem.getNut().getId()), BeCPGModel.TYPE_NUTLIST, properties);
						nodeService.createAssociation(childAssocRef.getChildRef(), nutListDataItem.getNut(), BeCPGModel.ASSOC_NUTLIST_NUT);
					}
				}
			}
		}
	}

	/**
	 * Create/Update organos.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param organoList
	 *            the organo list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createOrganoList(NodeRef listContainerNodeRef, List<OrganoListDataItem> organoList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef organoListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ORGANOLIST);

			if (organoList == null) {
				// delete existing list
				if (organoListNodeRef != null)
					nodeService.deleteNode(organoListNodeRef);
			} else {
				// organo list, create if needed
				if (organoListNodeRef == null) {
					organoListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ORGANOLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(organoListNodeRef, BeCPGModel.TYPE_ORGANOLIST);

				// create temp list
				List<NodeRef> organoListToTreat = new ArrayList<NodeRef>();
				for (OrganoListDataItem organoListDataItem : organoList) {
					organoListToTreat.add(organoListDataItem.getOrgano());
				}

				// remove deleted nodes
				Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					List<AssociationRef> organoAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_ORGANOLIST_ORGANO);
					NodeRef organoNodeRef = (organoAssocRefs.get(0)).getTargetRef();

					if (!organoListToTreat.contains(organoNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.put(organoNodeRef, listItemNodeRef);
					}
				}

				// update or create nodes
				for (OrganoListDataItem organoListDataItem : organoList) {
					NodeRef organoNodeRef = organoListDataItem.getNodeRef();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_ORGANOLIST_VALUE, organoListDataItem.getValue());

					if (filesToUpdate.containsKey(organoNodeRef)) {
						// update
						nodeService.setProperties(filesToUpdate.get(organoNodeRef), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(organoListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, organoListDataItem.getOrgano().getId()), BeCPGModel.TYPE_ORGANOLIST, properties);
						nodeService.createAssociation(childAssocRef.getChildRef(), organoListDataItem.getOrgano(), BeCPGModel.ASSOC_ORGANOLIST_ORGANO);
					}
				}
			}
		}
	}

	/**
	 * Create/Update inn labeling list.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param ingLabelingList
	 *            the ing labeling list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createIngLabelingList(NodeRef listContainerNodeRef, List<IngLabelingListDataItem> ingLabelingList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef illNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);

			if (ingLabelingList == null) {
				// delete existing list
				if (illNodeRef != null)
					nodeService.deleteNode(illNodeRef);
			} else {
				// ingLabeling list, create if needed
				if (illNodeRef == null) {
					illNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(illNodeRef, BeCPGModel.TYPE_INGLABELINGLIST);

				// create temp list
				List<String> illToTreat = new ArrayList<String>();
				for (IngLabelingListDataItem illDataItem : ingLabelingList) {
					illToTreat.add(illDataItem.getGrp());
				}

				// remove deleted nodes
				Map<String, NodeRef> filesToUpdate = new HashMap<String, NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					String grp = (String) nodeService.getProperty(listItemNodeRef, BeCPGModel.PROP_ILL_GRP);

					if (!illToTreat.contains(grp)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.put(grp, listItemNodeRef);
					}
				}

				// update or create nodes
				for (IngLabelingListDataItem illDataItem : ingLabelingList) {
					String grp = illDataItem.getGrp();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_ILL_GRP, illDataItem.getGrp());
					properties.put(BeCPGModel.PROP_ILL_VALUE, illDataItem.getValue());
					properties.put(BeCPGModel.PROP_IS_MANUAL_LISTITEM, illDataItem.getIsManual());

					if (filesToUpdate.containsKey(grp)) {
						// update
						nodeService.setProperties(filesToUpdate.get(grp), properties);
					} else {
						// create
						nodeService.createNode(illNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, illDataItem.getGrp()),
								BeCPGModel.TYPE_INGLABELINGLIST, properties);
					}
				}
			}
		}
	}

	/**
	 * Create/Update microbios.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param microbioList
	 *            the microbio list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createMicrobioList(NodeRef listContainerNodeRef, List<MicrobioListDataItem> microbioList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef microbioListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_MICROBIOLIST);

			if (microbioList == null) {
				// delete existing list
				if (microbioListNodeRef != null)
					nodeService.deleteNode(microbioListNodeRef);
			} else {

				// microbio list, create if needed
				if (microbioListNodeRef == null) {
					microbioListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_MICROBIOLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(microbioListNodeRef, BeCPGModel.TYPE_MICROBIOLIST);

				// create temp list
				List<NodeRef> microbioListToTreat = new ArrayList<NodeRef>();
				for (MicrobioListDataItem microbioListDataItem : microbioList) {
					microbioListToTreat.add(microbioListDataItem.getMicrobio());
				}

				// remove deleted nodes
				Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					List<AssociationRef> nutAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO);
					NodeRef microbioNodeRef = (nutAssocRefs.get(0)).getTargetRef();

					if (!microbioListToTreat.contains(microbioNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.put(microbioNodeRef, listItemNodeRef);
					}
				}

				// update or create nodes
				for (MicrobioListDataItem microbioListDataItem : microbioList) {
					NodeRef microbioNodeRef = microbioListDataItem.getMicrobio();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_MICROBIOLIST_VALUE, microbioListDataItem.getValue());
					properties.put(BeCPGModel.PROP_MICROBIOLIST_UNIT, microbioListDataItem.getUnit());
					properties.put(BeCPGModel.PROP_MICROBIOLIST_MAXI, microbioListDataItem.getMaxi());
					properties.put(BeCPGModel.PROP_MICROBIOLIST_TEXT_CRITERIA, microbioListDataItem.getTextCriteria());

					if (filesToUpdate.containsKey(microbioNodeRef)) {
						// update
						nodeService.setProperties(filesToUpdate.get(microbioNodeRef), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(microbioListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, microbioListDataItem.getMicrobio().getId()), BeCPGModel.TYPE_MICROBIOLIST, properties);
						nodeService.createAssociation(childAssocRef.getChildRef(), microbioListDataItem.getMicrobio(), BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO);
					}
				}
			}
		}
	}

	/**
	 * Create/Update physicoChems.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param physicoChemList
	 *            the physicoChem list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createPhysicoChemList(NodeRef listContainerNodeRef, List<PhysicoChemListDataItem> physicoChemList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef physicoChemListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_PHYSICOCHEMLIST);

			if (physicoChemList == null) {
				// delete existing list
				if (physicoChemListNodeRef != null)
					nodeService.deleteNode(physicoChemListNodeRef);
			} else {
				// physicoChem list, create if needed
				if (physicoChemListNodeRef == null) {
					physicoChemListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_PHYSICOCHEMLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(physicoChemListNodeRef, BeCPGModel.TYPE_PHYSICOCHEMLIST);

				// create temp list
				List<NodeRef> physicoChemListToTreat = new ArrayList<NodeRef>();
				for (PhysicoChemListDataItem physicoChemListDataItem : physicoChemList) {
					physicoChemListToTreat.add(physicoChemListDataItem.getPhysicoChem());
				}

				// remove deleted nodes
				Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					List<AssociationRef> physicoChemAssocRefs = nodeService.getTargetAssocs(listItemNodeRef, BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM);
					NodeRef physicoChemNodeRef = (physicoChemAssocRefs.get(0)).getTargetRef();

					if (!physicoChemListToTreat.contains(physicoChemNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.put(physicoChemNodeRef, listItemNodeRef);
					}
				}

				// update or create nodes
				for (PhysicoChemListDataItem physicoChemListDataItem : physicoChemList) {
					NodeRef physicoChemNodeRef = physicoChemListDataItem.getPhysicoChem();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_PHYSICOCHEMLIST_VALUE, physicoChemListDataItem.getValue());
					properties.put(BeCPGModel.PROP_PHYSICOCHEMLIST_MINI, physicoChemListDataItem.getMini());
					properties.put(BeCPGModel.PROP_PHYSICOCHEMLIST_MAXI, physicoChemListDataItem.getMaxi());
					properties.put(BeCPGModel.PROP_PHYSICOCHEMLIST_UNIT, physicoChemListDataItem.getUnit());

					if (filesToUpdate.containsKey(physicoChemNodeRef)) {
						// update
						nodeService.setProperties(filesToUpdate.get(physicoChemNodeRef), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(physicoChemListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, physicoChemListDataItem.getPhysicoChem().getId()), BeCPGModel.TYPE_PHYSICOCHEMLIST,
								properties);
						nodeService.createAssociation(childAssocRef.getChildRef(), physicoChemListDataItem.getPhysicoChem(), BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM);
					}
				}
			}
		}
	}

	/**
	 * Create/Update packaging.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param packagingList
	 *            the packaging list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createPackagingList(NodeRef listContainerNodeRef, List<PackagingListDataItem> packagingList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef packagingListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_PACKAGINGLIST);

			if (packagingList == null) {
				// delete existing list
				if (packagingListNodeRef != null)
					nodeService.deleteNode(packagingListNodeRef);
			} else {

				// packaging list, create if needed
				if (packagingListNodeRef == null) {
					packagingListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_PACKAGINGLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(packagingListNodeRef, BeCPGModel.TYPE_PACKAGINGLIST);

				// create temp list
				List<NodeRef> packagingListToTreat = new ArrayList<NodeRef>();
				for (PackagingListDataItem packagingListDataItem : packagingList) {
					packagingListToTreat.add(packagingListDataItem.getNodeRef());
				}

				// remove deleted nodes
				List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					if (!packagingListToTreat.contains(listItemNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.add(listItemNodeRef);
					}
				}

				// update or create nodes
				for (PackagingListDataItem packagingListDataItem : packagingList) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_PACKAGINGLIST_QTY, packagingListDataItem.getQty());
					properties.put(BeCPGModel.PROP_PACKAGINGLIST_UNIT, packagingListDataItem.getPackagingListUnit() == PackagingListUnit.Unknown ? "" : packagingListDataItem
							.getPackagingListUnit().toString());
					properties.put(BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL, packagingListDataItem.getPkgLevel());

					if (filesToUpdate.contains(packagingListDataItem.getNodeRef())) {
						// update
						nodeService.setProperties(packagingListDataItem.getNodeRef(), properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(packagingListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, packagingListDataItem.getProduct().getId()), BeCPGModel.TYPE_PACKAGINGLIST, properties);
						packagingListDataItem.setNodeRef(childAssocRef.getChildRef());
					}

					// Update product
					associationService.update(packagingListDataItem.getNodeRef(), BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT, packagingListDataItem.getProduct());
				}
			}
		}
	}

	/**
	 * Create/Update forbiddenIng.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param forbiddenIngList
	 *            the forbiddenIng list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createForbiddenIngList(NodeRef listContainerNodeRef, List<ForbiddenIngListDataItem> forbiddenIngList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef forbiddenIngListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_FORBIDDENINGLIST);

			if (forbiddenIngList == null) {
				// delete existing list
				if (forbiddenIngListNodeRef != null)
					nodeService.deleteNode(forbiddenIngListNodeRef);
			} else {

				// forbiddenIng list, create if needed
				if (forbiddenIngListNodeRef == null) {
					forbiddenIngListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_FORBIDDENINGLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(forbiddenIngListNodeRef, BeCPGModel.TYPE_FORBIDDENINGLIST);

				// create temp list
				List<NodeRef> forbiddenIngListToTreat = new ArrayList<NodeRef>();
				for (ForbiddenIngListDataItem forbiddenIngListDataItem : forbiddenIngList) {
					forbiddenIngListToTreat.add(forbiddenIngListDataItem.getNodeRef());
				}

				// remove deleted nodes
				List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					if (!forbiddenIngListToTreat.contains(listItemNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.add(listItemNodeRef);
					}
				}

				// update or create nodes
				NodeRef linkNodeRef = null;
				for (ForbiddenIngListDataItem forbiddenIngListDataItem : forbiddenIngList) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_FIL_REQ_TYPE, forbiddenIngListDataItem.getReqType());
					properties.put(BeCPGModel.PROP_FIL_REQ_MESSAGE, forbiddenIngListDataItem.getReqMessage());
					properties.put(BeCPGModel.PROP_FIL_QTY_PERC_MAXI, forbiddenIngListDataItem.getQtyPercMaxi());
					properties.put(BeCPGModel.PROP_FIL_IS_GMO, TranslateHelper.getTranslatedNullableBoolean(forbiddenIngListDataItem.isGMO()));
					properties.put(BeCPGModel.PROP_FIL_IS_IONIZED, TranslateHelper.getTranslatedNullableBoolean(forbiddenIngListDataItem.isIonized()));

					if (filesToUpdate.contains(forbiddenIngListDataItem.getNodeRef())) {
						// update
						linkNodeRef = forbiddenIngListDataItem.getNodeRef();
						nodeService.setProperties(linkNodeRef, properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(forbiddenIngListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), BeCPGModel.TYPE_FORBIDDENINGLIST, properties);
						linkNodeRef = childAssocRef.getChildRef();
						forbiddenIngListDataItem.setNodeRef(childAssocRef.getChildRef());
					}

					// ings
					associationService.update(linkNodeRef, BeCPGModel.ASSOC_FIL_INGS, forbiddenIngListDataItem.getIngs());

					// GeoOrigins
					associationService.update(linkNodeRef, BeCPGModel.ASSOC_FIL_GEO_ORIGINS, forbiddenIngListDataItem.getGeoOrigins());

					// BioOrigins
					associationService.update(linkNodeRef, BeCPGModel.ASSOC_FIL_BIO_ORIGINS, forbiddenIngListDataItem.getBioOrigins());
				}
			}
		}
	}

	/**
	 * Create/Update reqCtrl.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param reqCtrlList
	 *            the reqCtrl list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createReqCtrlList(NodeRef listContainerNodeRef, List<ReqCtrlListDataItem> reqCtrlList) throws InvalidTypeException {

		if (listContainerNodeRef != null) {
			NodeRef reqCtrlListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);

			if (reqCtrlList == null) {
				// delete existing list
//				if (reqCtrlListNodeRef != null)
//					nodeService.deleteNode(reqCtrlListNodeRef);
			} else {

				// reqCtrl list, create if needed
				if (reqCtrlListNodeRef == null) {
					reqCtrlListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_COMPOLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(reqCtrlListNodeRef, BeCPGModel.TYPE_REQCTRLLIST);

				// create temp list
				List<NodeRef> reqCtrlListToTreat = new ArrayList<NodeRef>();
				for (ReqCtrlListDataItem reqCtrlListDataItem : reqCtrlList) {
					reqCtrlListToTreat.add(reqCtrlListDataItem.getNodeRef());
				}

				// remove deleted nodes
				List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					if (!reqCtrlListToTreat.contains(listItemNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.add(listItemNodeRef);
					}
				}

				// update or create nodes
				NodeRef linkNodeRef = null;
				for (ReqCtrlListDataItem reqCtrlListDataItem : reqCtrlList) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(BeCPGModel.PROP_RCL_REQ_TYPE, reqCtrlListDataItem.getReqType());
					properties.put(BeCPGModel.PROP_RCL_REQ_MESSAGE, reqCtrlListDataItem.getReqMessage());

					if (filesToUpdate.contains(reqCtrlListDataItem.getNodeRef())) {
						// update
						linkNodeRef = reqCtrlListDataItem.getNodeRef();
						nodeService.setProperties(linkNodeRef, properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(reqCtrlListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), BeCPGModel.TYPE_REQCTRLLIST, properties);
						linkNodeRef = childAssocRef.getChildRef();
						reqCtrlListDataItem.setNodeRef(childAssocRef.getChildRef());
					}

					// sources
					associationService.update(linkNodeRef, BeCPGModel.ASSOC_RCL_SOURCES, reqCtrlListDataItem.getSources());
				}
			}
		}
	}

	/**
	 * Create/Update process.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param processList
	 *            the process list
	 * @throws InvalidTypeException
	 *             the invalid type exception
	 */
	private void createProcessList(NodeRef listContainerNodeRef, List<ProcessListDataItem> processList) throws InvalidTypeException {
		if (listContainerNodeRef != null) {
			NodeRef processListNodeRef = entityListDAO.getList(listContainerNodeRef, MPMModel.TYPE_PROCESSLIST);

			if (processList == null) {
				// delete existing list
				if (processListNodeRef != null)
					nodeService.deleteNode(processListNodeRef);
			} else {

				// process list, create if needed
				if (processListNodeRef == null) {
					processListNodeRef = entityListDAO.createList(listContainerNodeRef, MPMModel.TYPE_PROCESSLIST);
				}

				List<NodeRef> listItemNodeRefs = listItems(processListNodeRef, MPMModel.TYPE_PROCESSLIST);

				// create temp list
				List<NodeRef> processListToTreat = new ArrayList<NodeRef>();
				for (ProcessListDataItem processListDataItem : processList) {
					processListToTreat.add(processListDataItem.getNodeRef());
				}

				// remove deleted nodes
				List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
				for (NodeRef listItemNodeRef : listItemNodeRefs) {

					if (!processListToTreat.contains(listItemNodeRef)) {
						// delete
						nodeService.deleteNode(listItemNodeRef);
					} else {
						filesToUpdate.add(listItemNodeRef);
					}
				}

				// update or create nodes
				NodeRef linkNodeRef = null;
				for (ProcessListDataItem processListDataItem : processList) {
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(MPMModel.PROP_PL_QTY, processListDataItem.getQty());
					properties.put(MPMModel.PROP_PL_QTY_RESOURCE, processListDataItem.getQtyResource());
					properties.put(MPMModel.PROP_PL_RATE_RESOURCE, processListDataItem.getRateResource());
					properties.put(MPMModel.PROP_PL_YIELD, processListDataItem.getYield());
					properties.put(MPMModel.PROP_PL_RATE_PROCESS, processListDataItem.getRateProcess());
					properties.put(MPMModel.PROP_PL_RATE_PRODUCT, processListDataItem.getRateProduct());

					if (filesToUpdate.contains(processListDataItem.getNodeRef())) {
						// update
						linkNodeRef = processListDataItem.getNodeRef();
						nodeService.setProperties(linkNodeRef, properties);
					} else {
						// create
						ChildAssociationRef childAssocRef = nodeService.createNode(processListNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), MPMModel.TYPE_PROCESSLIST, properties);
						linkNodeRef = childAssocRef.getChildRef();
						processListDataItem.setNodeRef(childAssocRef.getChildRef());
					}

					// process
					associationService.update(linkNodeRef, MPMModel.ASSOC_PL_STEP, processListDataItem.getStep());

					// product
					associationService.update(linkNodeRef, MPMModel.ASSOC_PL_PRODUCT, processListDataItem.getProduct());

					// resource
					associationService.update(linkNodeRef, MPMModel.ASSOC_PL_RESOURCE, processListDataItem.getResource());
				}
			}
		}
	}

	private String getCostDetailsKey(NodeRef cost, NodeRef source) {

		return String.format(KEY_COST_DETAILS, cost, source);
	}

	/*
	 * List the list items
	 */
	private List<NodeRef> listItems(NodeRef parentNodeRef, QName listItemType) {

		Set<QName> searchTypeQNames = new HashSet<QName>(1);
		searchTypeQNames.add(listItemType);

		// Do the query
		List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(parentNodeRef, searchTypeQNames);
		List<NodeRef> result = new ArrayList<NodeRef>(childAssocRefs.size());
		for (ChildAssociationRef assocRef : childAssocRefs) {

			result.add(assocRef.getChildRef());
		}
		// Done
		return result;
	}

}
