package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public class ClaimRequirementScanner extends AbstractRequirementScanner<LabelClaimListDataItem> {

	public static final String MESSAGE_NOT_CLAIM = "message.formulate.labelClaim.notClaimed";

	private static Log logger = LogFactory.getLog(ClaimRequirementScanner.class);

	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
		List<ReqCtrlListDataItem> ret = new LinkedList<>();

		if (getDataListVisited(formulatedProduct) != null) {
			Map<LabelClaimListDataItem, Boolean> specLabelClaimsVisitedMap = new HashMap<>();
			extractRequirements(formulatedProduct).forEach(extracedSpecDataItem -> {
				specLabelClaimsVisitedMap.put(extracedSpecDataItem, false);
			});

			specLabelClaimsVisitedMap.keySet().forEach(specDataItem -> {
				getDataListVisited(formulatedProduct).forEach(listDataItem -> {
					if (listDataItem.getLabelClaim().equals(specDataItem.getLabelClaim())) {
						if (logger.isDebugEnabled()) {
							logger.debug(extractName(specDataItem.getLabelClaim()) + " has been visited");
						}
						specLabelClaimsVisitedMap.put(specDataItem, true);
						if (Boolean.TRUE.equals(specDataItem.getIsClaimed()) && (!Boolean.TRUE
								.equals(listDataItem.getIsClaimed() || !LabelClaimListDataItem.VALUE_NA.equals(listDataItem.getLabelClaimValue())))) {
							addSpecificationUnclaimedLabelClaim(formulatedProduct, listDataItem);
						} else if (LabelClaimListDataItem.VALUE_SUITABLE.equals(listDataItem.getLabelClaimValue()) && (!Boolean.TRUE.equals(
								listDataItem.getIsClaimed() || !LabelClaimListDataItem.VALUE_SUITABLE.equals(listDataItem.getLabelClaimValue())))) {
							addSpecificationUnclaimedLabelClaim(formulatedProduct, listDataItem);
						}

					}
				});
			});

			// check that all the labelClaim in specs have been visited in
			// product
			specLabelClaimsVisitedMap.keySet().forEach(specDataItem -> {
				if (Boolean.FALSE.equals(specLabelClaimsVisitedMap.get(specDataItem))) {
					if (logger.isDebugEnabled()) {
						logger.debug(extractName(specDataItem.getLabelClaim()) + " was not found, raising rclDataItem for spec");
					}
					addSpecificationUnclaimedLabelClaim(formulatedProduct, specDataItem);
				}
			});
		}

		return ret;
	}

	private void addSpecificationUnclaimedLabelClaim(ProductData formulatedProduct, LabelClaimListDataItem labelClaim) {
		String message = I18NUtil.getMessage(MESSAGE_NOT_CLAIM, extractName(labelClaim.getLabelClaim()));
		formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, labelClaim.getLabelClaim(),
				new ArrayList<NodeRef>(), RequirementDataType.Specification));
	}

	private List<LabelClaimListDataItem> extractRequirements(ProductData formulatedProduct) {
		List<LabelClaimListDataItem> ret = new ArrayList<>();
		if (formulatedProduct.getProductSpecifications() != null) {
			for (ProductSpecificationData specification : formulatedProduct.getProductSpecifications()) {
				mergeRequirements(ret, extractRequirements(specification));
				if (getDataListVisited(specification) != null) {
					mergeRequirements(ret, getDataListVisited(specification));
				}
			}
		}
		return ret;
	}

	@Override
	protected void mergeRequirements(List<LabelClaimListDataItem> ret, List<LabelClaimListDataItem> toAdd) {
		toAdd.forEach(item -> {
			if (item.getLabelClaim() != null) {
				boolean isFound = false;
				for (LabelClaimListDataItem sl : ret) {
					if (item.getLabelClaim().equals(sl.getLabelClaim())) {
						isFound = true;
						if (Boolean.FALSE.equals(sl.getIsClaimed()) && Boolean.TRUE.equals(item.getIsClaimed())) {
							sl.setIsClaimed(true);
						}
						break;
					}
				}
				if (!isFound) {
					ret.add(item);
				}
			}
		});
	}

	@Override
	protected List<LabelClaimListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getLabelClaimList();
	}

	private String extractName(NodeRef labelClaim) {
		return (String) nodeService.getProperty(labelClaim, BeCPGModel.PROP_CHARACT_NAME);
	}

}
