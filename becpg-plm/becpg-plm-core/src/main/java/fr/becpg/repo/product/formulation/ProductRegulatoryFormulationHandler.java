/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RegulatoryEntity;
import fr.becpg.repo.product.data.constraints.RegulatoryResult;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public class ProductRegulatoryFormulationHandler extends FormulationBaseHandler<ProductData> {

	private NodeService nodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct)  {

		if (formulatedProduct.getRegulatoryList() != null) {
			for (RegulatoryListDataItem regulatoryListItem : formulatedProduct.getRegulatoryList()) {
				computeRegulatoryResults(regulatoryListItem, formulatedProduct.getReqCtrlList());
			}
			computeRegulatoryResults(formulatedProduct, formulatedProduct.getReqCtrlList());
		}

		return true;

	}

	private void computeRegulatoryResults(RegulatoryEntity regulatoryEntity, List<ReqCtrlListDataItem> reqCtrlList) {
		List<String> regulatoryIds = extractRegulatoryIds(regulatoryEntity);
		for (String regulatoryId : regulatoryIds) {
			List<ReqCtrlListDataItem> matchingRequirements = reqCtrlList.stream().filter(req -> regulatoryId.equals(req.getRegulatoryCode())).collect(Collectors.toList());
			if (hasError(matchingRequirements)) {
				regulatoryEntity.setRegulatoryResult(RegulatoryResult.ERROR);
				if (regulatoryEntity instanceof RegulatoryListDataItem) {
					((RegulatoryListDataItem) regulatoryEntity).setLimitingIngredient(null);
					((RegulatoryListDataItem) regulatoryEntity).setMaximumDosage(null);
				}
			} else {
				ReqCtrlListDataItem maximumDosageRequirement = getMaximumDosageRequirement(matchingRequirements);
				if (maximumDosageRequirement != null) {
					regulatoryEntity.setRegulatoryResult(RegulatoryResult.PROHIBITED);
					if (regulatoryEntity instanceof RegulatoryListDataItem) {
						((RegulatoryListDataItem) regulatoryEntity).setLimitingIngredient(maximumDosageRequirement.getCharact());
						((RegulatoryListDataItem) regulatoryEntity).setMaximumDosage(maximumDosageRequirement.getReqMaxQty());
					}
				} else {
					if (regulatoryEntity instanceof RegulatoryListDataItem) {
						((RegulatoryListDataItem) regulatoryEntity).setLimitingIngredient(null);
						((RegulatoryListDataItem) regulatoryEntity).setMaximumDosage(null);
					}
					regulatoryEntity.setRegulatoryResult(RegulatoryResult.PERMITTED);
				}
			}
		}
	}
	
	private List<String> extractRegulatoryIds(RegulatoryEntity regulatoryListItem) {
		List<String> regulatoryIds = new ArrayList<>();
		if (regulatoryListItem.getRegulatoryCountriesRef() != null && regulatoryListItem.getRegulatoryUsagesRef() != null) {
			for (NodeRef country : regulatoryListItem.getRegulatoryCountriesRef()) {
				String countryCode = (String) nodeService.getProperty(country, PLMModel.PROP_REGULATORY_CODE);
				for (NodeRef usage : regulatoryListItem.getRegulatoryUsagesRef()) {
					String usageCode = (String) nodeService.getProperty(usage, PLMModel.PROP_REGULATORY_CODE);
					regulatoryIds.add(countryCode + " - " + usageCode);
				}
			}
		}
		return regulatoryIds;
	}
	
	private boolean hasError(List<ReqCtrlListDataItem> reqList) {
		for (ReqCtrlListDataItem req : reqList) {
			if (RequirementType.Forbidden.equals(req.getReqType())
					&& RequirementDataType.Formulation.equals(req.getReqDataType())) {
				return true;
			}
		}
		return false;
	}
	
	private ReqCtrlListDataItem getMaximumDosageRequirement(List<ReqCtrlListDataItem> reqList) {
		double minValue = Double.POSITIVE_INFINITY;
		ReqCtrlListDataItem maximumDosageRequirement = null;
		for (ReqCtrlListDataItem req : reqList) {
			if (RequirementType.Forbidden.equals(req.getReqType())
					&& RequirementDataType.Specification.equals(req.getReqDataType()) && req.getReqMaxQty() != null
					&& req.getReqMaxQty() < minValue) {
				maximumDosageRequirement = req;
				minValue = req.getReqMaxQty();
			}
		}
		return maximumDosageRequirement;
	}

}
