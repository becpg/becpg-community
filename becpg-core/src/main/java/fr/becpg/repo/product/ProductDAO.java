/*
 * 
 */
package fr.becpg.repo.product;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.product.data.BaseObject;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.SimpleCharactDataItem;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProductDAO.
 *
 * @author querephi
 */
public interface ProductDAO {

	/**
	 * Creates the.
	 *
	 * @param parentNodeRef the parent node ref
	 * @param productData the product data
	 * @param dataLists the data lists
	 * @return the node ref
	 */
	public NodeRef create(NodeRef parentNodeRef, ProductData productData, Collection<QName> dataLists);
	
	/**
	 * Update.
	 *
	 * @param productNodeRef the product node ref
	 * @param productData the product data
	 * @param dataLists the data lists
	 */
	public void update(NodeRef productNodeRef, ProductData productData, Collection<QName> dataLists);	
	
	/**
	 * Find.
	 *
	 * @param productNodeRef the product node ref
	 * @param dataLists the data lists
	 * @return the product data
	 */
	public ProductData find(NodeRef productNodeRef, Collection<QName> dataLists);
	
	/**
	 * Delete.
	 *
	 * @param productNodeRef the product node ref
	 */
	public void delete(NodeRef productNodeRef);
	
	public void createCostList(NodeRef listContainerNodeRef, List<CostListDataItem> costList);
	public void createCostListItem(NodeRef listNodeRef, CostListDataItem costListDataItem, Map<NodeRef, NodeRef> filesToUpdate, Integer sortIndex);
	
	public List<AllergenListDataItem> loadAllergenList(NodeRef listContainerNodeRef);
	public AllergenListDataItem loadAllergenListItem(NodeRef listItemNodeRef);
	public List<CostListDataItem> loadCostList(NodeRef listContainerNodeRef);
	public CostListDataItem loadCostListItem(NodeRef listItemNodeRef);
	public List<IngListDataItem> loadIngList(NodeRef listContainerNodeRef);
	public IngListDataItem loadIngListItem(NodeRef listItemNodeRef);
	public List<NutListDataItem> loadNutList(NodeRef listContainerNodeRef);
	public NutListDataItem loadNutListItem(NodeRef listItemNodeRef);
	public List<IngLabelingListDataItem> loadIngLabelingList(NodeRef listContainerNodeRef);
	public IngLabelingListDataItem loadIngLabelingListItem(NodeRef listItemNodeRef);
	public List<DynamicCharactListItem> loadDynamicCharactList(NodeRef listContainerNodeRef);
	public DynamicCharactListItem loadDynamicCharactListItem(NodeRef listItemNodeRef);

	public List<? extends SimpleCharactDataItem> loadList(NodeRef productNodeRef, QName dataList);

	public BaseObject loadItemByType(NodeRef dataListItem, QName dataListType);
}
