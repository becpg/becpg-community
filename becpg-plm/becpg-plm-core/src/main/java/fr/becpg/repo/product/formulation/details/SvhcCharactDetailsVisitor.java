package fr.becpg.repo.product.formulation.details;

import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.SvhcListDataItem;
import fr.becpg.repo.product.formulation.SvhcCalculatingHelper;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>SvhcCharactDetailsVisitor class.</p>
 *
 * @author matthieu
 */
@Service
public class SvhcCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	/** {@inheritDoc} */
	@Override
	protected Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		if (partProduct.isPackaging() && simpleCharact instanceof SvhcListDataItem svhcListDataItem) {
			return SvhcCalculatingHelper.extractPackagingValue(svhcListDataItem);
		}
		return super.extractValue(formulatedProduct, partProduct, simpleCharact);
	}
}
