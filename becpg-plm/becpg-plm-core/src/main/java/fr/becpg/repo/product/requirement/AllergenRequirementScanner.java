package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public class AllergenRequirementScanner extends AbstractRequirementScanner<AllergenListDataItem> {

	public static final String MESSAGE_FORBIDDEN_ALLERGEN = "message.formulate.allergen.forbidden";

	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
		List<ReqCtrlListDataItem> ret = new LinkedList<>();

		if (formulatedProduct.getAllergenList() != null && !formulatedProduct.getAllergenList().isEmpty()) {
			extractRequirements(specifications).forEach(specDataItem -> {
				formulatedProduct.getAllergenList().forEach(listDataItem -> {
					if (listDataItem.getAllergen().equals(specDataItem.getAllergen())) {
						if ((listDataItem.getInVoluntary() || listDataItem.getVoluntary())) {

							boolean isAllergenAllowed = false;
							if (specDataItem.getVoluntary() && listDataItem.getVoluntary()) {
								isAllergenAllowed = true;
							} else if (specDataItem.getInVoluntary() && listDataItem.getInVoluntary()) {
								isAllergenAllowed = true;
							}

							if (!isAllergenAllowed) {
								String message = I18NUtil.getMessage(MESSAGE_FORBIDDEN_ALLERGEN, extractName(listDataItem.getAllergen()));
								ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message,
										listDataItem.getAllergen(), new ArrayList<NodeRef>(), RequirementDataType.Specification);
								rclDataItem.getSources().addAll(listDataItem.getVoluntarySources());
								rclDataItem.getSources().addAll(listDataItem.getInVoluntarySources());
								ret.add(rclDataItem);
							}
						}
					}
				});
			});
		}

		return ret;
	}


	protected void mergeRequirements(List<AllergenListDataItem> ret, List<AllergenListDataItem> toAdd) {
		toAdd.forEach(item -> {
			if (item.getAllergen() != null) {
				boolean isFound = false;
				for (AllergenListDataItem sl : ret) {
					if (item.getAllergen().equals(sl.getAllergen())) {
						isFound = true;

						// if one value is true, set to true
						if (Boolean.TRUE.equals(sl.getVoluntary()) && Boolean.FALSE.equals(item.getVoluntary())) {
							sl.setVoluntary(Boolean.FALSE);
						}

						if (Boolean.TRUE.equals(sl.getInVoluntary()) && Boolean.FALSE.equals(item.getInVoluntary())) {
							sl.setInVoluntary(Boolean.FALSE);
						}

						if ((sl.getQtyPerc() != null) && (item.getQtyPerc() != null)) {
							sl.setQtyPerc(Math.min(sl.getQtyPerc(), item.getQtyPerc()));
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

	private String extractName(NodeRef charactRef) {
		return (String) nodeService.getProperty(charactRef, BeCPGModel.PROP_CHARACT_NAME);
	}


	protected List<AllergenListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getAllergenList()!=null ?  partProduct.getAllergenList() : new ArrayList<>();
	}

	
}
