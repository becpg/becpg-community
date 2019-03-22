/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.LinkedList;
import java.util.List;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.requirement.RequirementScanner;

/**
 * The Class ProductSpecificationsFormulationHandler.
 *
 * @author matthieu
 */
public class ProductSpecificationsFormulationHandler extends FormulationBaseHandler<ProductData> {

	List<RequirementScanner> requirementScanners = new LinkedList<>();
	
	
	public void setRequirementScanners(List<RequirementScanner> requirementScanners) {
		this.requirementScanners = requirementScanners;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		if (formulatedProduct.getReqCtrlList() == null) {
			formulatedProduct.setReqCtrlList(new LinkedList<>());
		}

		for (RequirementScanner scanner : requirementScanners) {
			formulatedProduct.getReqCtrlList().addAll(scanner.checkRequirements(formulatedProduct, formulatedProduct.getProductSpecifications()));
		}

		return true;

	}

}
