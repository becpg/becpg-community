package fr.becpg.repo.product.requirement;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
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

	private static final String MESSAGE_UNDEFINED_VALUE = "message.formulate.undefined.value";

	private static Log logger = LogFactory.getLog(SimpleListRequirementScanner.class);

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
		List<ReqCtrlListDataItem> ret = new LinkedList<>();

		if ((getDataListVisited(formulatedProduct) != null) && !getDataListVisited(formulatedProduct).isEmpty()) {

			for (Map.Entry<ProductSpecificationData, List<T>> entry : extractRequirements(specifications).entrySet()) {
				List<T> requirements = entry.getValue();
				ProductSpecificationData specification = entry.getKey();

				requirements.forEach(specDataItem -> {
					getDataListVisited(formulatedProduct).forEach(listDataItem -> {
						if (specDataItem instanceof MinMaxValueDataItem) {
							if (specDataItem.getCharactNodeRef().equals(listDataItem.getCharactNodeRef())) {
								boolean isCharactAllowed = true;
								MinMaxValueDataItem minMaxSpecValueDataItem = (MinMaxValueDataItem) specDataItem;
								if ((specDataItem.getValue() != null) && !specDataItem.getValue().equals(listDataItem.getValue())) {
									isCharactAllowed = false;
								}

								if (minMaxSpecValueDataItem.getMini() != null) {
									if ((listDataItem.getValue() == null) || (listDataItem.getValue() < minMaxSpecValueDataItem.getMini())) {
										isCharactAllowed = false;
									}
								}

								if (minMaxSpecValueDataItem.getMaxi() != null) {
									if ((listDataItem.getValue() == null) || (listDataItem.getValue() > minMaxSpecValueDataItem.getMaxi())) {
										isCharactAllowed = false;
									}
								}

								if (!isCharactAllowed) {
									MLText message = MLTextHelper
											.getI18NMessage(getSpecErrorMessageKey(),
													mlNodeService.getProperty(listDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME),
													(listDataItem.getValue() != null ? listDataItem.getValue()
															: MLTextHelper.getI18NMessage(MESSAGE_UNDEFINED_VALUE)),
													MLTextHelper.createMLTextI18N((l) -> {
														return (minMaxSpecValueDataItem.getMini() != null
																? NumberFormat.getInstance(l).format(minMaxSpecValueDataItem.getMini()) + "<= "
																: "");
													}), MLTextHelper.createMLTextI18N((l) -> {
														return (minMaxSpecValueDataItem.getMaxi() != null
																? " <=" + NumberFormat.getInstance(l).format(minMaxSpecValueDataItem.getMaxi())
																: "");
													}));

									ReqCtrlListDataItem reqCtrl = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message,
											listDataItem.getCharactNodeRef(), new ArrayList<NodeRef>(), RequirementDataType.Specification);

									if (specification.getRegulatoryCode() != null && !specification.getRegulatoryCode().isBlank()) {
										reqCtrl.setRegulatoryCode(specification.getRegulatoryCode());
									} else {
										reqCtrl.setRegulatoryCode(specification.getName());
									}

									ret.add(reqCtrl);

								}
							}

						}
					});
				});
			}
		}

		return ret;
	}

	/**
	 * <p>getSpecErrorMessageKey.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected abstract String getSpecErrorMessageKey();

	/** {@inheritDoc} */
	@Override
	protected void mergeRequirements(List<T> ret, List<T> toAdd) {
		toAdd.forEach(item -> {
			if (item.getCharactNodeRef() != null) {
				boolean isFound = false;
				for (SimpleListDataItem sl : ret) {
					if (item.getCharactNodeRef().equals(sl.getCharactNodeRef())) {
						isFound = true;
						if ((sl instanceof MinMaxValueDataItem) && (item instanceof MinMaxValueDataItem)) {
							MinMaxValueDataItem castSl = (MinMaxValueDataItem) sl;
							MinMaxValueDataItem castItem = (MinMaxValueDataItem) item;
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

}
