package fr.becpg.repo.product.requirement;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

public abstract class SimpleListRequirementScanner<T extends SimpleListDataItem> extends AbstractRequirementScanner<T> {

	private static final String MESSAGE_UNDEFINED_VALUE = "message.formulate.undefined.value";



	private static Log logger = LogFactory.getLog(SimpleListRequirementScanner.class);
	
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
		List<ReqCtrlListDataItem> ret = new LinkedList<>();

		if (getDataListVisited(formulatedProduct) != null && !getDataListVisited(formulatedProduct).isEmpty() ) {
			extractRequirements(specifications).forEach(specDataItem -> {
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
								String message = I18NUtil.getMessage(getSpecErrorMessageKey(),
										nodeService.getProperty(listDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME),
										(listDataItem.getValue() != null ? listDataItem.getValue() : I18NUtil.getMessage(MESSAGE_UNDEFINED_VALUE)),
										(minMaxSpecValueDataItem.getMini() != null
												? NumberFormat.getInstance(Locale.getDefault()).format(minMaxSpecValueDataItem.getMini()) + "<= "
												: ""),
										(minMaxSpecValueDataItem.getMaxi() != null
												? " <=" + NumberFormat.getInstance(Locale.getDefault()).format(minMaxSpecValueDataItem.getMaxi())
												: ""));
								ret.add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message,
										listDataItem.getCharactNodeRef(), new ArrayList<NodeRef>(), RequirementDataType.Specification));

							}
						}

					}
				});
			});
		}

		return ret;
	}


	protected abstract String getSpecErrorMessageKey();


	
//	private List<T> extractRequirements(ProductData formulatedProduct) {
//		List<T> ret = new ArrayList<>();
//		if (formulatedProduct.getProductSpecifications() != null) {
//			for (ProductSpecificationData specification : formulatedProduct.getProductSpecifications()) {
//				mergeRequirements(ret, extractRequirements(specification));
//				if (getDataListVisited(specification) != null) {
//					mergeRequirements(ret, getDataListVisited(specification));
//				}
//			}
//		}
//
//		// if this spec has a datalist, merge it with the rest. Only applies to
//		// specs
//		if ((getDataListVisited(formulatedProduct) != null) && !getDataListVisited(formulatedProduct).isEmpty()
//				&& (formulatedProduct instanceof ProductSpecificationData)) {
//			if (logger.isTraceEnabled()) {
//				logger.trace("formulatedProduct (c=" + formulatedProduct.getClass().getName() + ") has a dataList, visiting it)");
//			}
//			mergeRequirements(ret, getDataListVisited(formulatedProduct));
//		}
//
//		return ret;
//	}

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
