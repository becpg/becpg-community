package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RegulatoryEntityItem;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.LabelClaimFormulationHandler;

/**
 * <p>ClaimRequirementScanner class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ClaimRequirementScanner extends AbstractRequirementScanner<LabelClaimListDataItem> {

	/** Constant <code>MESSAGE_NOT_CLAIM="message.formulate.labelClaim.notClaimed"</code> */
	public static final String MESSAGE_NOT_CLAIM = "message.formulate.labelClaim.notClaimed";

	private static Log logger = LogFactory.getLog(ClaimRequirementScanner.class);

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
		List<ReqCtrlListDataItem> ret = new LinkedList<>();

		if (getDataListVisited(formulatedProduct) != null) {

			for (Map.Entry<ProductSpecificationData, List<LabelClaimListDataItem>> entry : extractRequirements(specifications).entrySet()) {
				List<LabelClaimListDataItem> requirements = entry.getValue();
				ProductSpecificationData specification = entry.getKey();

				Map<LabelClaimListDataItem, Boolean> specLabelClaimsVisitedMap = new HashMap<>();

				requirements.forEach(extracedSpecDataItem -> specLabelClaimsVisitedMap.put(extracedSpecDataItem, false));

				specLabelClaimsVisitedMap.keySet().forEach(specDataItem -> getDataListVisited(formulatedProduct).forEach(listDataItem -> {
					if (listDataItem.getLabelClaim().equals(specDataItem.getLabelClaim())) {
						if (logger.isDebugEnabled()) {
							logger.debug(extractName(specDataItem.getLabelClaim()) + " has been visited");
						}
						specLabelClaimsVisitedMap.put(specDataItem, true);

						boolean isForbidden = false;

						if ((specDataItem.getLabelClaimValue() != null) && !specDataItem.getLabelClaimValue().isEmpty()) {
							if ((listDataItem.getLabelClaimValue() == null) || listDataItem.getLabelClaimValue().isEmpty()) {
								isForbidden = true;
							} else if (!LabelClaimListDataItem.VALUE_NA.equals(listDataItem.getLabelClaimValue())) {
								if ((LabelClaimListDataItem.VALUE_TRUE.equals(specDataItem.getLabelClaimValue())
										&& !LabelClaimListDataItem.VALUE_TRUE.equals(listDataItem.getLabelClaimValue()))
										|| (LabelClaimListDataItem.VALUE_FALSE.equals(specDataItem.getLabelClaimValue())
												&& !LabelClaimListDataItem.VALUE_FALSE.equals(listDataItem.getLabelClaimValue()))
										|| (LabelClaimListDataItem.VALUE_SUITABLE.equals(specDataItem.getLabelClaimValue())
												&& !LabelClaimListDataItem.VALUE_SUITABLE.equals(listDataItem.getLabelClaimValue()))
										|| (LabelClaimListDataItem.VALUE_CERTIFIED.equals(specDataItem.getLabelClaimValue())
												&& !LabelClaimListDataItem.VALUE_CERTIFIED.equals(listDataItem.getLabelClaimValue()))) {
									isForbidden = true;
								}
							}
							if (isForbidden || Boolean.TRUE.equals(addInfoReqCtrl)) {
								MLText message = MLTextHelper.getI18NMessage(MESSAGE_NOT_CLAIM, extractName(listDataItem.getLabelClaim()),
										extractClaimValue(specDataItem.getLabelClaimValue()));

								String regulatoryId = extractRegulatoryId(specDataItem, specification);

								RequirementType reqType = isForbidden ? RequirementType.Info : RequirementType.Forbidden;

								if (!isForbidden && specDataItem.getRegulatoryType() != null) {
									reqType = specDataItem.getRegulatoryType();
								}
								if (specDataItem.getRegulatoryMessage() != null) {
									message = specDataItem.getRegulatoryMessage();
								}

								ReqCtrlListDataItem reqCtrl = ReqCtrlListDataItem.build().ofType(reqType).withMessage(message)
										.withCharact(listDataItem.getLabelClaim()).ofDataType(RequirementDataType.Specification)
										.withRegulatoryCode(regulatoryId);

								ret.add(reqCtrl);
							}
						}

					}
				}));

				// check that all the labelClaim in specs have been visited in
				// product
				specLabelClaimsVisitedMap.keySet().forEach(specDataItem -> {
					if (Boolean.FALSE.equals(specLabelClaimsVisitedMap.get(specDataItem))) {
						if (logger.isDebugEnabled()) {
							logger.debug(extractName(specDataItem.getLabelClaim()) + " was not found, raising rclDataItem for spec");
						}
						addMissingLabelClaim(ret, specification, specDataItem);
					}
				});
			}
		}
		return ret;
	}

	private MLText extractClaimValue(String labelClaimValue) {
		return MLTextHelper.getI18NMessage("message.formulate.labelClaim.value." + labelClaimValue);
	}

	private void addMissingLabelClaim(List<ReqCtrlListDataItem> ret, ProductSpecificationData specification, LabelClaimListDataItem labelClaim) {
		MLText message = MLTextHelper.getI18NMessage(LabelClaimFormulationHandler.MESSAGE_MISSING_CLAIM, extractName(labelClaim.getLabelClaim()));

		ret.add(ReqCtrlListDataItem.forbidden().withMessage(message).withCharact(labelClaim.getLabelClaim())
				.ofDataType(RequirementDataType.Specification)
				.withRegulatoryCode((specification.getRegulatoryCode() != null) && !specification.getRegulatoryCode().isBlank()
						? specification.getRegulatoryCode()
						: specification.getName()));
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	protected List<LabelClaimListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getLabelClaimList() != null ? partProduct.getLabelClaimList() : new ArrayList<>();
	}

}
