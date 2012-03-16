package fr.becpg.repo.entity.datalist.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;

/**
 * 
 * @author matthieu
 *
 */
public class WUsedExtractor extends SimpleExtractor {

	//TODO bad dependency !!!!!!! Should not depend on product
	private ProductService productService;

	
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination, boolean hasWriteAccess) {

		//TODO filter with query filter
		
		PaginatedExtractedItems ret = new PaginatedExtractedItems(pagination.getPageSize());

		if (BeCPGModel.TYPE_COMPOLIST.equals(dataListFilter.getDataType())) {

			List<CompoListDataItem> wUsedList = productService.getWUsedCompoList(dataListFilter.getEntityNodeRef());

			wUsedList = pagination.paginate(wUsedList);

			for (CompoListDataItem compoListDataItem : wUsedList) {
				ret.getItems().add(extract(compoListDataItem));
			}

		} else if (BeCPGModel.TYPE_PACKAGINGLIST.equals(dataListFilter.getDataType())) {

			List<PackagingListDataItem> wUsedList = productService.getWUsedPackagingList(dataListFilter.getEntityNodeRef());

			wUsedList = pagination.paginate(wUsedList);

			for (PackagingListDataItem packagingListDataItem : wUsedList) {
				ret.getItems().add(extract(packagingListDataItem));
			}

		}
		ret.setFullListSize(pagination.getFullListSize());
		return ret;
		
	}

//TODO should be done based on metadataFields
	private Map<String, Object> extract(PackagingListDataItem wUsedItem) {
		
		Map<String, Object> itemData = new HashMap<String, Object>(1);
		
		Map<String, Object> ret = new HashMap<String, Object>(4);
		ret.put("assoc_bcpg_packagingListProduct",getPropertyValue(wUsedItem.getProduct()));
		ret.put("prop_bcpg_packagingListQty",getPropertyValue(wUsedItem.getQty()));
		ret.put("prop_bcpg_packagingListUnit",getPropertyValue(wUsedItem.getPackagingListUnit()));
		ret.put("prop_bcpg_packagingListDeclType",getPropertyValue(wUsedItem.getPkgLevel()));
		itemData.put(PROP_NODEDATA, ret);
		itemData.put(PROP_ACTIONSET, "");
		
		
		Map<String, Object> permissions = new HashMap<String, Object>(1);
		Map<String, Boolean> userAccess = new HashMap<String, Boolean>(3);
		permissions.put("userAccess", userAccess);
		userAccess.put("delete", false);
		userAccess.put("create", false);
		userAccess.put("edit", false);
		
		itemData.put(PROP_PERMISSIONS, permissions);
		
		itemData.put(PROP_ACTIONLABELS, new HashMap<String, Object>());
		return itemData;
	}


	private Map<String, Object> extract(CompoListDataItem wUsedItem) {
		Map<String, Object> itemData = new HashMap<String, Object>(1);
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("prop_bcpg_depthLevel",getPropertyValue(wUsedItem.getDepthLevel()));
		ret.put("assoc_bcpg_compoListProduct",getPropertyValue(wUsedItem.getProduct()));
		ret.put("prop_bcpg_compoListQty",getPropertyValue(wUsedItem.getQty()));
		ret.put("prop_bcpg_compoListQtySubFormula",getPropertyValue(wUsedItem.getQtySubFormula()));
		ret.put("prop_bcpg_compoListQtyAfterProcess",getPropertyValue(wUsedItem.getQtyAfterProcess()));
		ret.put("prop_bcpg_compoListUnit",getPropertyValue(wUsedItem.getCompoListUnit()));
		ret.put("prop_bcpg_compoListLossPerc",getPropertyValue(wUsedItem.getLossPerc()));
		ret.put("prop_bcpg_compoListDeclGrp",getPropertyValue(wUsedItem.getDeclGrp()));
		ret.put("prop_bcpg_compoListDeclType",getPropertyValue(wUsedItem.getDeclType()));
		itemData.put(PROP_NODEDATA, ret);
		itemData.put(PROP_ACTIONSET, "");
		
		
		Map<String, Object> permissions = new HashMap<String, Object>(1);
		Map<String, Boolean> userAccess = new HashMap<String, Boolean>(3);
		permissions.put("userAccess", userAccess);
		userAccess.put("delete", false);
		userAccess.put("create", false);
		userAccess.put("edit", false);
		
		itemData.put(PROP_PERMISSIONS, permissions);
		
		itemData.put(PROP_ACTIONLABELS, new HashMap<String, Object>());
		return itemData;
	}
	
	
//TODO move that to propertyservice
	private Map<String, Object> getPropertyValue(Object o) {
		Map<String, Object> ret = new HashMap<String, Object>();
		
			ret.put("value", o);
			String displayValue = o!=null ? o.toString() : "";
			if(o instanceof Float){
				PropertyFormats propertyFormats = attributeExtractorService.getPropertyFormats();
				if (propertyFormats.getDecimalFormat() != null) {
					displayValue = propertyFormats.getDecimalFormat().format(o);
				}
			} else if(o instanceof NodeRef){
				QName type = nodeService.getType((NodeRef)o);
				if (type != null) {
					ret.put("metadata", attributeExtractorService.extractMetadata(type, (NodeRef)o));
				}
				displayValue = (String) nodeService.getProperty((NodeRef)o, ContentModel.PROP_NAME);
			}
			ret.put("displayValue", displayValue);
		return ret;
	}

}
