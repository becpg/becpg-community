package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>DefaultSimpleListQtyProvider class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DefaultSimpleListQtyProvider implements SimpleListQtyProvider {
	
		protected ProductData formulatedProduct;
		
		/**
		 * <p>Constructor for DefaultSimpleListQtyProvider.</p>
		 *
		 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
		 */
		public DefaultSimpleListQtyProvider(ProductData formulatedProduct) {
			super();
			this.formulatedProduct = formulatedProduct;
		}

		/** {@inheritDoc} */
		@Override
		public Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
			return FormulationHelper.getQtyInKg(compoListDataItem);
		}
		
		/** {@inheritDoc} */
		@Override
		public Double getVolume(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
			return FormulationHelper.getNetVolume(compoListDataItem, componentProduct);
		}

		/** {@inheritDoc} */
		@Override
		public Double getNetWeight(VariantData variant) {
			return FormulationHelper.getNetWeight(formulatedProduct, variant, FormulationHelper.DEFAULT_NET_WEIGHT);
		}

		/** {@inheritDoc} */
		@Override
		public Double getNetQty(VariantData variant) {
			return  FormulationHelper.getNetQtyInLorKg(formulatedProduct ,variant , FormulationHelper.DEFAULT_NET_WEIGHT);
		}

		/** {@inheritDoc} */
		@Override
		public Boolean omitElement(CompoListDataItem compoListDataItem) {
			return DeclarationType.Omit.equals(compoListDataItem.getDeclType());
		}

		/** {@inheritDoc} */
		@Override
		public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
			return  FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem, componentProduct);
		}

		/** {@inheritDoc} */
		@Override
		public Double getQty(ProcessListDataItem processListDataItem, VariantData variant) {
			return FormulationHelper.getQty(formulatedProduct, variant , processListDataItem);
		}


		
		
}
