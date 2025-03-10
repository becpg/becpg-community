package fr.becpg.repo.product.requirement;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RegulatoryEntityItem;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * <p>Abstract SimpleListRequirementScanner class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class SimpleListRequirementScanner<T extends SimpleListDataItem> extends AbstractRequirementScanner<T> {

	/** Constant <code>MESSAGE_UNDEFINED_VALUE="message.formulate.undefined.value"</code> */
	public static final String MESSAGE_UNDEFINED_VALUE = "message.formulate.undefined.value";

	private static Log logger = LogFactory.getLog(SimpleListRequirementScanner.class);

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
		List<ReqCtrlListDataItem> ret = new LinkedList<>();

		List<T> dataListVisited = getDataListVisited(formulatedProduct);

		if ((dataListVisited != null) && !dataListVisited.isEmpty()) {

			for (Map.Entry<ProductSpecificationData, List<T>> entry : extractRequirements(specifications).entrySet()) {
				List<T> requirements = entry.getValue();
				ProductSpecificationData specification = entry.getKey();

				requirements.forEach(specDataItem -> {
					dataListVisited.forEach(listDataItem -> {
						if ((specDataItem instanceof MinMaxValueDataItem minMaxSpecValueDataItem)
								&& specDataItem.getCharactNodeRef().equals(listDataItem.getCharactNodeRef())) {
							boolean isCharactAllowed = true;
							if ((specDataItem.getValue() != null) && !specDataItem.getValue().equals(getValue(specDataItem, listDataItem))) {
								isCharactAllowed = false;
							}

							if (minMaxSpecValueDataItem.getMini() != null) {
								if ((getValue(specDataItem, listDataItem) == null)
										|| (getValue(specDataItem, listDataItem) < minMaxSpecValueDataItem.getMini())) {
									isCharactAllowed = false;
								}
							}

							Double reqCtrlMaxQty = null;

							if (minMaxSpecValueDataItem.getMaxi() != null) {
								if ((getValue(specDataItem, listDataItem) == null)
										|| (getValue(specDataItem, listDataItem) > minMaxSpecValueDataItem.getMaxi())) {
									isCharactAllowed = false;
									if ((getValue(specDataItem, listDataItem) != null) && (getValue(specDataItem, listDataItem) != 0)) {
										reqCtrlMaxQty = (minMaxSpecValueDataItem.getMaxi() / getValue(specDataItem, listDataItem)) * 100d;
									}
								}
							}

							if (!isCharactAllowed || Boolean.TRUE.equals(addInfoReqCtrl)) {

								String keyMessage = isCharactAllowed ? getSpecInfoMessageKey(specDataItem) : getSpecErrorMessageKey(specDataItem);

								MLText message = MLTextHelper
										.getI18NMessage(keyMessage,
												mlNodeService.getProperty(listDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME),
												(getValue(specDataItem, listDataItem) != null ? getValue(specDataItem, listDataItem)
														: MLTextHelper.getI18NMessage(MESSAGE_UNDEFINED_VALUE)),
												MLTextHelper.createMLTextI18N(l -> (minMaxSpecValueDataItem.getMini() != null
														? NumberFormat.getInstance(l).format(minMaxSpecValueDataItem.getMini()) + "<= "
														: "")), MLTextHelper.createMLTextI18N(l -> (minMaxSpecValueDataItem.getMaxi() != null
														? " <=" + NumberFormat.getInstance(l).format(minMaxSpecValueDataItem.getMaxi())
														: "")));

								String regulatoryId = null;

								RequirementType reqType = isCharactAllowed ? RequirementType.Info : RequirementType.Forbidden;

								if (minMaxSpecValueDataItem instanceof RegulatoryEntityItem regulatoryEntityItem) {
									regulatoryId = extractRegulatoryId(regulatoryEntityItem, specification);
									if (!isCharactAllowed && (regulatoryEntityItem.getRegulatoryType() != null)) {
										reqType = regulatoryEntityItem.getRegulatoryType();
									}
									if ((regulatoryEntityItem.getRegulatoryMessage() != null)
											&& !MLTextHelper.isEmpty(regulatoryEntityItem.getRegulatoryMessage())) {
										message = regulatoryEntityItem.getRegulatoryMessage();
									}
								}

								if ((regulatoryId == null) || regulatoryId.isBlank()) {
									if ((specification.getRegulatoryCode() != null) && !specification.getRegulatoryCode().isBlank()) {
										regulatoryId = specification.getRegulatoryCode();
									} else {
										regulatoryId = specification.getName();
									}
								}

								ret.add(ReqCtrlListDataItem.build().ofType(reqType).withMessage(message).withCharact(listDataItem.getCharactNodeRef())
										.ofDataType(RequirementDataType.Specification).withReqMaxQty(reqCtrlMaxQty).withRegulatoryCode(regulatoryId));
							}
						}
					});
				});
			}
		}

		return ret;
	}

	/**
	 * <p>getValue.</p>
	 *
	 * @param specDataItem a T object
	 * @param listDataItem a T object
	 * @return a {@link java.lang.Double} object
	 */
	protected abstract Double getValue(T specDataItem, T listDataItem);

	/**
	 * <p>getSpecErrorMessageKey.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 * @param specDataItem a T object
	 */
	protected abstract String getSpecErrorMessageKey(T specDataItem);

	/**
	 * <p>getSpecInfoMessageKey.</p>
	 *
	 * @return a {@link java.lang.String} object
	 * @param specDataItem a T object
	 */
	protected abstract String getSpecInfoMessageKey(T specDataItem);

	/** {@inheritDoc} */
	@Override
	protected void mergeRequirements(List<T> ret, List<T> toAdd) {
		toAdd.forEach(item -> {
			if (item.getCharactNodeRef() != null) {
				boolean isFound = false;
				for (T sl : ret) {
					if (shouldMerge(item, sl)) {
						isFound = true;
						if ((sl instanceof MinMaxValueDataItem castSl) && (item instanceof MinMaxValueDataItem castItem)) {
							if (logger.isTraceEnabled()) {
								logger.trace("Merging minMax values: sl=[" + castSl.getMini() + " - " + castSl.getMaxi() + "], item=["
										+ castItem.getMini() + " - " + castItem.getMaxi() + "]");
							}
							if ((castSl.getMini() != null) && (castItem.getMini() != null)) {
								castSl.setMini(Math.max(castSl.getMini(), castItem.getMini()));
							} else if (castItem.getMini() != null) {
								castSl.setMini(castItem.getMini());
							}

							if ((castSl.getMaxi() != null) && (castItem.getMaxi() != null)) {
								castSl.setMaxi(Math.min(castSl.getMaxi(), castItem.getMaxi()));
							} else if (castItem.getMaxi() != null) {
								castSl.setMaxi(castItem.getMaxi());
							}
							if (logger.isTraceEnabled()) {
								logger.trace("Merged sl=[" + castSl.getMini() + " - " + castSl.getMaxi() + "]");
							}
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

	/**
	 * <p>shouldMerge.</p>
	 *
	 * @param item a T object
	 * @param sl a T object
	 * @return a boolean
	 */
	protected boolean shouldMerge(T item, T sl) {
		return item.getCharactNodeRef().equals(sl.getCharactNodeRef());
	}

}
