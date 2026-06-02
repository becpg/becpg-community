package fr.becpg.repo.product.requirement;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.ToxListDataItem;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;

/**
 * <p>ToxRequirementScanner class.</p>
 */
public class ToxRequirementScanner extends AbstractRequirementScanner<ToxListDataItem> {

	private static final Log logger = LogFactory.getLog(ToxRequirementScanner.class);

	public static final String MESSAGE_TOX_NOT_IN_RANGE = "message.formulate.tox.notInRangeValue";

	public static final String MESSAGE_TOX_INFO = "message.formulate.info.tox.notInRangeValue";

	@Override
	public List<RequirementListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
		List<RequirementListDataItem> ret = new ArrayList<>();

		if (logger.isDebugEnabled()) {
			logger.debug("Scanning requirements for product: " + formulatedProduct.getName());
		}

		Map<ProductSpecificationData, List<ToxListDataItem>> requirementsMap = extractRequirements(specifications);

		boolean checkAuthorized = shouldCheckAuthorized(formulatedProduct, requirementsMap);

		if (formulatedProduct.getToxList() != null) {
			if (checkAuthorized) {
				for (ToxListDataItem listDataItem : formulatedProduct.getToxList()) {
					if (listDataItem.getTox() == null) {
						continue;
					}

					boolean authorized = false;
					for (Map.Entry<ProductSpecificationData, List<ToxListDataItem>> entry : requirementsMap.entrySet()) {
						List<ToxListDataItem> requirements = entry.getValue();
						ProductSpecificationData specification = entry.getKey();

						for (ToxListDataItem specDataItem : requirements) {
							if (RequirementType.Authorized.equals(specDataItem.getRegulatoryType())
									&& listDataItem.getTox().equals(specDataItem.getTox())
									&& checkRegulatoryUsageMatch(specDataItem, formulatedProduct)) {
								authorized = true;
								Double value = listDataItem.getValue();
								Double maxThreshold = specDataItem.getMaxi() != null ? specDataItem.getMaxi() : 100.0;
								Double minThreshold = specDataItem.getMini();

								boolean isMaxAllowed = (value == null) || (value <= maxThreshold);
								boolean isMinAllowed = (value == null) || (minThreshold == null) || (value >= minThreshold);

								boolean isToxAllowed = isMaxAllowed && isMinAllowed;

								if (logger.isDebugEnabled()) {
									logger.debug("Authorized Tox check for " + listDataItem.getTox() + ": value=" + value + 
											", range=[" + minThreshold + " - " + maxThreshold + "], allowed=" + isToxAllowed);
								}

								if (!isToxAllowed || Boolean.TRUE.equals(addInfoReqCtrl)) {
									String keyMessage = isToxAllowed ? MESSAGE_TOX_INFO : MESSAGE_TOX_NOT_IN_RANGE;
									MLText message = getScannerMessage(specDataItem, listDataItem, keyMessage, value, minThreshold, maxThreshold);
									String regulatoryId = extractRegulatoryId(specDataItem, specification);
									RequirementType reqType = isToxAllowed ? RequirementType.Info : RequirementType.Forbidden;

									if (!isToxAllowed && (specDataItem.getRegulatoryType() != null)) {
										reqType = specDataItem.getRegulatoryType();
									}

									ret.add(RequirementListDataItem.build()
											.ofType(reqType)
											.withMessage(message)
											.withCharact(listDataItem.getTox())
											.ofDataType(RequirementDataType.Specification)
											.withRegulatoryCode(regulatoryId));
								}
								break;
							}
						}
						if (authorized) {
							break;
						}
					}

					if (!authorized) {
						if (logger.isDebugEnabled()) {
							logger.debug("Tox not authorized: " + listDataItem.getTox());
						}
						MLText message = MLTextHelper.getI18NMessage(
								"message.formulate.tox.notAuthorized",
								mlNodeService.getProperty(listDataItem.getTox(), BeCPGModel.PROP_CHARACT_NAME)
						);
						ret.add(RequirementListDataItem.build()
								.ofType(RequirementType.Forbidden)
								.withMessage(message)
								.withCharact(listDataItem.getTox())
								.ofDataType(RequirementDataType.Specification)
								.withRegulatoryCode(extractRegulatoryId(null, specifications.get(0))));
					}
				}
			} else {
				for (Map.Entry<ProductSpecificationData, List<ToxListDataItem>> entry : requirementsMap.entrySet()) {
					List<ToxListDataItem> requirements = entry.getValue();
					ProductSpecificationData specification = entry.getKey();

					for (ToxListDataItem specDataItem : requirements) {
						if (checkRegulatoryUsageMatch(specDataItem, formulatedProduct)) {
							for (ToxListDataItem listDataItem : formulatedProduct.getToxList()) {
								if (specDataItem.getTox() != null && specDataItem.getTox().equals(listDataItem.getTox())) {
									Double value = listDataItem.getValue();
									Double maxThreshold = specDataItem.getMaxi() != null ? specDataItem.getMaxi() : 100.0;
									Double minThreshold = specDataItem.getMini();

									boolean isMaxAllowed = (value == null) || (value <= maxThreshold);
									boolean isMinAllowed = (value == null) || (minThreshold == null) || (value >= minThreshold);

									boolean isToxAllowed = isMaxAllowed && isMinAllowed;

									if (logger.isDebugEnabled()) {
										logger.debug("Tox check for " + listDataItem.getTox() + ": value=" + value + 
												", range=[" + minThreshold + " - " + maxThreshold + "], allowed=" + isToxAllowed);
									}

									if (!isToxAllowed || Boolean.TRUE.equals(addInfoReqCtrl)) {
										String keyMessage = isToxAllowed ? MESSAGE_TOX_INFO : MESSAGE_TOX_NOT_IN_RANGE;
										MLText message = getScannerMessage(specDataItem, listDataItem, keyMessage, value, minThreshold, maxThreshold);
										String regulatoryId = extractRegulatoryId(specDataItem, specification);
										RequirementType reqType = isToxAllowed ? RequirementType.Info : RequirementType.Forbidden;

										if (!isToxAllowed && (specDataItem.getRegulatoryType() != null)) {
											reqType = specDataItem.getRegulatoryType();
										}

										ret.add(RequirementListDataItem.build()
												.ofType(reqType)
												.withMessage(message)
												.withCharact(listDataItem.getTox())
												.ofDataType(RequirementDataType.Specification)
												.withRegulatoryCode(regulatoryId));
									}
								}
							}
						}
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Tox scan complete. Raised " + ret.size() + " requirement controls.");
		}

		return ret;
	}

	private boolean shouldCheckAuthorized(ProductData formulatedProduct, Map<ProductSpecificationData, List<ToxListDataItem>> requirementsMap) {
		boolean checkAuthorized = false;

		for (Map.Entry<ProductSpecificationData, List<ToxListDataItem>> entry : requirementsMap.entrySet()) {
			List<ToxListDataItem> requirements = entry.getValue();
			for (ToxListDataItem specDataItem : requirements) {
				if (checkRegulatoryUsageMatch(specDataItem, formulatedProduct) && RequirementType.Authorized.equals(specDataItem.getRegulatoryType())) {
					checkAuthorized = true;
				}
			}
		}
		return checkAuthorized;
	}

	private MLText getScannerMessage(ToxListDataItem specDataItem, ToxListDataItem listDataItem, String keyMessage, Double value, Double minThreshold, Double maxThreshold) {
		if (specDataItem.getRegulatoryMessage() != null && !MLTextHelper.isEmpty(specDataItem.getRegulatoryMessage())) {
			return specDataItem.getRegulatoryMessage();
		} else {
			return MLTextHelper.getI18NMessage(
					keyMessage,
					mlNodeService.getProperty(listDataItem.getTox(), BeCPGModel.PROP_CHARACT_NAME),
					MLTextHelper.createMLTextI18N(l -> NumberFormat.getInstance(l).format(value) + "%"),
					MLTextHelper.createMLTextI18N(l -> (minThreshold != null
							? NumberFormat.getInstance(l).format(minThreshold) + "%<= "
							: "")),
					MLTextHelper.createMLTextI18N(l -> (maxThreshold != null
							? " <=" + NumberFormat.getInstance(l).format(maxThreshold) + "%"
							: ""))
			);
		}
	}

	@Override
	protected List<ToxListDataItem> getDataListVisited(ProductData productData) {
		return productData.getToxList() != null ? productData.getToxList() : new ArrayList<>();
	}

	@Override
	protected void mergeRequirements(List<ToxListDataItem> ret, List<ToxListDataItem> toAdd) {
		toAdd.forEach(item -> {
			if (item.getTox() != null) {
				boolean isFound = false;
				for (ToxListDataItem sl : ret) {
					if (item.getTox().equals(sl.getTox())) {
						isFound = true;
						
						if (sl.getMini() != null && item.getMini() != null) {
							sl.setMini(Math.max(sl.getMini(), item.getMini()));
						} else if (item.getMini() != null) {
							sl.setMini(item.getMini());
						}

						if (sl.getMaxi() != null && item.getMaxi() != null) {
							sl.setMaxi(Math.min(sl.getMaxi(), item.getMaxi()));
						} else if (item.getMaxi() != null) {
							sl.setMaxi(item.getMaxi());
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

}
