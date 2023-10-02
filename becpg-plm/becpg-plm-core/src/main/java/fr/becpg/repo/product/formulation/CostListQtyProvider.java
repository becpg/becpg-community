package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.variant.model.VariantData;

public class CostListQtyProvider implements SimpleListQtyProvider {

		ProductData formulatedProduct;


		public CostListQtyProvider (ProductData formulatedProduct) {
			this.formulatedProduct = formulatedProduct;
		}


		@Override
		public Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
			return FormulationHelper.getQtyForCost(compoListDataItem, parentLossRatio, componentProduct, CostsCalculatingFormulationHandler.keepProductUnit());
		}

		@Override
		public Double getVolume(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
			return getQty(compoListDataItem, parentLossRatio, componentProduct);
		}


		@Override
		public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
			return  FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem,componentProduct);
		}

		@Override
		public Double getQty(ProcessListDataItem processListDataItem, VariantData variant) {
			return  FormulationHelper.getQtyForCost(formulatedProduct, variant, processListDataItem);
		}


		@Override
		public Double getNetWeight( VariantData variant) {
			return FormulationHelper.getNetQtyForCost(formulatedProduct,variant);
		}

		@Override
		public Double getNetQty( VariantData variant) {
			return  FormulationHelper.getNetQtyForCost(formulatedProduct,variant);
		}

		@Override
		public Boolean omitElement(CompoListDataItem compoListDataItem) {
			return false;
		}




}
