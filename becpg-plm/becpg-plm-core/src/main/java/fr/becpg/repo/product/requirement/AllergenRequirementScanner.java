package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * <p>AllergenRequirementScanner class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AllergenRequirementScanner extends AbstractRequirementScanner<AllergenListDataItem> {

	/** Constant <code>MESSAGE_FORBIDDEN_ALLERGEN="message.formulate.allergen.forbidden"</code> */
	public static final String MESSAGE_FORBIDDEN_ALLERGEN = "message.formulate.allergen.forbidden";

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {
		List<ReqCtrlListDataItem> ret = new LinkedList<>();

		if ((formulatedProduct.getAllergenList() != null) && !formulatedProduct.getAllergenList().isEmpty()) {

			for (Map.Entry<ProductSpecificationData, List<AllergenListDataItem>> entry : extractRequirements(specifications).entrySet()) {
				List<AllergenListDataItem> requirements = entry.getValue();
				ProductSpecificationData specification = entry.getKey();

				requirements.forEach(specDataItem -> {
					formulatedProduct.getAllergenList().forEach(listDataItem -> {
						if (listDataItem.getAllergen() != null && listDataItem.getAllergen().equals(specDataItem.getAllergen())
								&& ((Boolean.TRUE.equals(listDataItem.getInVoluntary()) || Boolean.TRUE.equals(listDataItem.getVoluntary())))) {

							boolean isAllergenAllowed = false;
							if ((Boolean.TRUE.equals(specDataItem.getVoluntary()) && Boolean.TRUE.equals(listDataItem.getVoluntary()))
									|| (Boolean.TRUE.equals(specDataItem.getInVoluntary()) && Boolean.TRUE.equals(listDataItem.getInVoluntary()))) {
								isAllergenAllowed = true;
							}

							if (!isAllergenAllowed || Boolean.TRUE.equals(addInfoReqCtrl)) {
								MLText message = MLTextHelper.getI18NMessage(MESSAGE_FORBIDDEN_ALLERGEN, extractName(listDataItem.getAllergen()));
								
								RequirementType reqType= isAllergenAllowed ? RequirementType.Info : RequirementType.Forbidden;
								
								if (!isAllergenAllowed && specDataItem.getRegulatoryType() != null) {
									reqType = specDataItem.getRegulatoryType();
								}
								if (specDataItem.getRegulatoryMessage() != null) {
									message = specDataItem.getRegulatoryMessage();
								}
								
								ReqCtrlListDataItem rclDataItem = ReqCtrlListDataItem.build()
										.ofType(reqType).withMessage(message)
										.withCharact(listDataItem.getAllergen()).ofDataType(RequirementDataType.Specification)
										.withSources(Stream.of(listDataItem.getVoluntarySources(), listDataItem.getInVoluntarySources())
												.flatMap(List::stream).distinct().toList())
										.withRegulatoryCode(extractRegulatoryId(specDataItem, specification));

								ret.add(rclDataItem);
							}
						}
					});
				});
			}
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
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

	/** {@inheritDoc} */
	@Override
	protected List<AllergenListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getAllergenList() != null ? partProduct.getAllergenList() : new ArrayList<>();
	}

}
