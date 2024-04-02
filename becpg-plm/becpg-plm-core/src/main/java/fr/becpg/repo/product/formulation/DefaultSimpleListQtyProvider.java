package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.variant.model.VariantData;

public class DefaultSimpleListQtyProvider implements SimpleListQtyProvider {
	
		protected ProductData formulatedProduct;
		
		public DefaultSimpleListQtyProvider(ProductData formulatedProduct) {
			super();
			this.formulatedProduct = formulatedProduct;
		}

		@Override
		public Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
			return FormulationHelper.getQtyInKg(compoListDataItem);
		}
		
		@Override
		public Double getVolume(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
			return FormulationHelper.getNetVolume(compoListDataItem, componentProduct);
		}

		@Override
		public Double getNetWeight(VariantData variant) {
			return FormulationHelper.getNetWeight(formulatedProduct, variant, FormulationHelper.DEFAULT_NET_WEIGHT);
		}

		@Override
		public Double getNetQty(VariantData variant) {
			return  FormulationHelper.getNetQtyInLorKg(formulatedProduct ,variant , FormulationHelper.DEFAULT_NET_WEIGHT);
		}

		@Override
		public Boolean omitElement(CompoListDataItem compoListDataItem) {
			return DeclarationType.Omit.equals(compoListDataItem.getDeclType());
		}

		@Override
		public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
			return  FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem, componentProduct);
		}

		@Override
		public Double getQty(ProcessListDataItem processListDataItem, VariantData variant) {
			return FormulationHelper.getQty(formulatedProduct, variant , processListDataItem);
		}


		
		
}
