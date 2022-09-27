package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;

public class CostListQtyProvider implements SimpleListQtyProvider {

		ProductData formulatedProduct;
		Double netQty;


		public CostListQtyProvider (ProductData formulatedProduct) {
			this.formulatedProduct = formulatedProduct;
			this.netQty = FormulationHelper.getNetQtyForCost(formulatedProduct);
		}


		@Override
		public Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
			return FormulationHelper.getQtyForCost(compoListDataItem, parentLossRatio, componentProduct, CostsCalculatingFormulationHandler.keepProductUnit);
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
		public Double getQty(ProcessListDataItem processListDataItem) {
			return  FormulationHelper.getQtyForCost(formulatedProduct, processListDataItem);
		}


		@Override
		public Double getNetWeight() {
			return netQty;
		}

		@Override
		public Double getNetQty() {
			return  netQty;
		}

		@Override
		public Boolean omitElement(CompoListDataItem compoListDataItem) {
			return false;
		}




}
