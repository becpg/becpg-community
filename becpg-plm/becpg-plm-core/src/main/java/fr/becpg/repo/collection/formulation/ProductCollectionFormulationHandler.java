package fr.becpg.repo.collection.formulation;

import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.collection.data.ProductCollectionData;
import fr.becpg.repo.collection.data.list.CollectionPriceListDataItem;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulationBaseHandler;

public class ProductCollectionFormulationHandler extends FormulationBaseHandler<ProductCollectionData> {

	private NodeService nodeService;
	
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public boolean process(ProductCollectionData productCollection) {

		for (CollectionPriceListDataItem priceListItem : productCollection.getPriceList()) {
			calculateProfitabilityAndPrice(priceListItem);
		}

		Composite<CollectionPriceListDataItem> compositePrice = CompositeHelper.getHierarchicalCompoList(productCollection.getPriceList());
		calculatePriceParentValue(compositePrice);

		return true;
	}

	private void calculateProfitabilityAndPrice(CollectionPriceListDataItem priceListItem) {

		Double taxRate = null;
		if (priceListItem.getDutyFeeTax() != null) {
			taxRate = (Double) nodeService.getProperty(priceListItem.getDutyFeeTax(), GS1Model.PROP_DUTY_FEE_TAX_RATE);
		}

		if (priceListItem.getPriceTaxIncl() != null) {
			if (taxRate != null) {
				//Taux TVA (en %) x Prix TTC (en €) / (1 + Taux TVA (en %) )
				Double price = (taxRate * priceListItem.getPriceTaxIncl()) / (1 + taxRate);

				priceListItem.setDutyFeeTaxAmount(priceListItem.getPriceTaxIncl() - price);
				priceListItem.setPrice(price);

			} else {
				priceListItem.setPrice(priceListItem.getPriceTaxIncl());
			}
		}

		Double unitTotalCost = null;
		if (priceListItem.getProduct() != null) {
			unitTotalCost = (Double) nodeService.getProperty(priceListItem.getProduct(), PLMModel.PROP_UNIT_TOTAL_COST);
		}

		if (unitTotalCost != null) {
			priceListItem.setUnitTotalCost(unitTotalCost);
		} else {
			priceListItem.setUnitTotalCost(null);
		}

		if ((priceListItem.getPrice() != null) && (priceListItem.getUnitTotalCost() != null)) {

			// profitability
			double profit = priceListItem.getPrice() - priceListItem.getUnitTotalCost();
			Double profitabilityRatio = priceListItem.getPrice() / (100 * profit);

			priceListItem.setProfitabilityRatio(profitabilityRatio);

		} else {
			priceListItem.setProfitabilityRatio(null);
		}

	}

	private void calculatePriceParentValue(Composite<CollectionPriceListDataItem> parent) {
		Double price = 0d;
		Double priceTaxIncl = 0d;
		if (!parent.isLeaf()) {
			for (Composite<CollectionPriceListDataItem> component : parent.getChildren()) {
				calculatePriceParentValue(component);
				price += component.getData().getPrice() != null ? component.getData().getPrice() : 0d;
				priceTaxIncl += component.getData().getPriceTaxIncl() != null ? component.getData().getPriceTaxIncl() : 0d;
			}
			if (!parent.isRoot()) {
				parent.getData().setPrice(price);
				parent.getData().setPriceTaxIncl(priceTaxIncl);
			}
		}

	}

}
