package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>CostListQtyProvider class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CostListQtyProvider implements SimpleListQtyProvider {

		ProductData formulatedProduct;


		/**
		 * <p>Constructor for CostListQtyProvider.</p>
		 *
		 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
		 */
		public CostListQtyProvider (ProductData formulatedProduct) {
			this.formulatedProduct = formulatedProduct;
		}


		/** {@inheritDoc} */
		@Override
		public Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
			return FormulationHelper.getQtyForCost(compoListDataItem, parentLossRatio, componentProduct, CostsCalculatingFormulationHandler.keepProductUnit());
		}

		/** {@inheritDoc} */
		@Override
		public Double getVolume(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
			return getQty(compoListDataItem, parentLossRatio, componentProduct);
		}


		/** {@inheritDoc} */
		@Override
		public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
			return  FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem,componentProduct);
		}

		/** {@inheritDoc} */
		@Override
		public Double getQty(ProcessListDataItem processListDataItem, VariantData variant) {
			return  FormulationHelper.getQtyForCost(formulatedProduct, variant, processListDataItem);
		}


		/** {@inheritDoc} */
		@Override
		public Double getNetWeight( VariantData variant) {
			return FormulationHelper.getNetQtyForCost(formulatedProduct,variant);
		}

		/** {@inheritDoc} */
		@Override
		public Double getNetQty( VariantData variant) {
			return  FormulationHelper.getNetQtyForCost(formulatedProduct,variant);
		}

		/** {@inheritDoc} */
		@Override
		public Boolean omitElement(CompoListDataItem compoListDataItem) {
			return false;
		}




}
