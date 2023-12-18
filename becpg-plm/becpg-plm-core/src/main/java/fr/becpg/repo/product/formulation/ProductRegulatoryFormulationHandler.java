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
import fr.becpg.repo.product.data.constraints.RegulatoryResult;
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
		}

		return true;

	}

	private void computeRegulatoryResults(RegulatoryListDataItem regulatoryListItem, List<ReqCtrlListDataItem> reqCtrlList) {
		List<String> regulatoryIds = extractRegulatoryIds(regulatoryListItem);
		for (String regulatoryId : regulatoryIds) {
			List<ReqCtrlListDataItem> matchingRequirements = reqCtrlList.stream().filter(req -> regulatoryId.equals(req.getRegulatoryCode())).collect(Collectors.toList());
			ReqCtrlListDataItem maximumDosageRequirement = getMaximumDosageRequirement(matchingRequirements);
			if (maximumDosageRequirement != null) {
				regulatoryListItem.setRegulatoryResult(RegulatoryResult.PROHIBITED);
				regulatoryListItem.setLimitingIngredient(maximumDosageRequirement.getCharact());
				regulatoryListItem.setMaximumDosage(maximumDosageRequirement.getReqMaxQty());
			} else {
				regulatoryListItem.setLimitingIngredient(null);
				regulatoryListItem.setMaximumDosage(null);
				regulatoryListItem.setRegulatoryResult(RegulatoryResult.PERMITTED);
			}
		}
	}
	
	private List<String> extractRegulatoryIds(RegulatoryListDataItem regulatoryListItem) {
		List<String> regulatoryIds = new ArrayList<>();
		if (regulatoryListItem.getRegulatoryCountries() != null && regulatoryListItem.getRegulatoryUsages() != null) {
			for (NodeRef country : regulatoryListItem.getRegulatoryCountries()) {
				String countryCode = (String) nodeService.getProperty(country, PLMModel.PROP_REGULATORY_CODE);
				for (NodeRef usage : regulatoryListItem.getRegulatoryUsages()) {
					String usageCode = (String) nodeService.getProperty(usage, PLMModel.PROP_REGULATORY_CODE);
					regulatoryIds.add(countryCode + " - " + usageCode);
				}
			}
		}
		return regulatoryIds;
	}
	
	private ReqCtrlListDataItem getMaximumDosageRequirement(List<ReqCtrlListDataItem> reqList) {
		double minValue = Double.POSITIVE_INFINITY;
		ReqCtrlListDataItem maximumDosageRequirement = null;
		for (ReqCtrlListDataItem req : reqList) {
			if (RequirementType.Forbidden.equals(req.getReqType()) && req.getReqMaxQty() != null && req.getReqMaxQty() < minValue) {
				maximumDosageRequirement = req;
				minValue = req.getReqMaxQty();
			}
		}
		return maximumDosageRequirement;
	}

}
