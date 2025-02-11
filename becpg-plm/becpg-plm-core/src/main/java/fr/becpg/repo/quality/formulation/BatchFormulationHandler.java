package fr.becpg.repo.quality.formulation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.QualityModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.StockType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.quality.data.dataList.AllocationListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>BatchFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BatchFormulationHandler extends FormulationBaseHandler<BatchData> {

	private static Log logger = LogFactory.getLog(BatchFormulationHandler.class);

	private static final String MESSAGE_MISSING_STOCK = "message.formulate.missing.stock";

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(BatchData batchData) {

		if ((batchData.getProduct() != null) && batchData.hasCompoListEl()) {

			boolean isVolume = false;

			if ((batchData.getUnit() != null)) {
				isVolume = batchData.getUnit().isVolume();
			}

			ReqCtrlListDataItem reqCtrl = null;
			Map<NodeRef, Double> rawMaterials = new HashMap<>();
			for (CompoListDataItem compoList : batchData.getCompoList()) {
				NodeRef productNodeRef = compoList.getProduct();
				if ((productNodeRef != null) && !shouldOmit(compoList)) {

					Double qty = FormulationHelper.getQtyInKg(compoList);

					if (qty != null) {

						ProductData subProductData = (ProductData) alfrescoRepository.findOne(productNodeRef);

						Double lossPerc = FormulationHelper.calculateLossPerc(0d, FormulationHelper.getComponentLossPerc(subProductData, compoList));

						if (!(subProductData instanceof LocalSemiFinishedProductData) && accept(batchData, subProductData)) {
							if (!shouldDeclare(compoList) || (subProductData instanceof RawMaterialData)) {

								Double rmQty = rawMaterials.get(productNodeRef);
								if (rmQty == null) {
									rmQty = 0d;
								}
								rmQty += FormulationHelper.getQtyWithLoss(qty, lossPerc);
								rawMaterials.put(productNodeRef, rmQty);
							} else {
								extractRawMaterials(subProductData, rawMaterials, qty, lossPerc);
							}
						}
					}
				}
			}

			// sort
			List<Map.Entry<NodeRef, Double>> sortedRawMaterials = new LinkedList<>(rawMaterials.entrySet());
			Collections.sort(sortedRawMaterials, (r1, r2) -> r2.getValue().compareTo(r1.getValue()));

			if ((batchData.getAllocationList() != null) && alfrescoRepository.hasDataList(batchData, QualityModel.TYPE_BATCH_ALLOCATION_LIST)) {

				List<AllocationListDataItem> toRetain = new LinkedList<>();

				boolean canApply = true;
				int sort = 0;
				for (Map.Entry<NodeRef, Double> entry : sortedRawMaterials) {

					AllocationListDataItem item = batchData.getAllocationList().stream().filter(a -> entry.getKey().equals(a.getProduct()))
							.findFirst().orElse(null);

					if (item == null) {
						item = new AllocationListDataItem();
						item.setProduct(entry.getKey());
						batchData.getAllocationList().add(item);
					}

					item.setSort(sort++);

					toRetain.add(item);

					//Revert batch order
					if (SystemState.Valid.equals(item.getState()) && SystemState.Simulation.equals(batchData.getState())) {
						item.setStockListItems(increaseInventory(item.getStockListItems(), item.getBatchQty(), item.getUnit()));
						item.setState(SystemState.Simulation);
					}

					if (!SystemState.Valid.equals(item.getState())) {
						ProductData rawMaterialData = (ProductData) alfrescoRepository.findOne(entry.getKey());
						Double totalStockInKgOrL = computeTotalStock(batchData, rawMaterialData, item.getStockListItems());

						ProductUnit unit = isVolume ? ProductUnit.L : ProductUnit.kg;
						Double value = entry.getValue();
						if ((value < 0.0001) && !isVolume) {
							value = entry.getValue() * 1000000;
							unit = ProductUnit.mg;
						} else if (value < 1) {
							value = entry.getValue() * 1000;
							unit = isVolume ? ProductUnit.mL : ProductUnit.g;
						}

						item.setBatchQty(value);
						item.setUnit(unit);
						item.setState(SystemState.Simulation);

						if (logger.isDebugEnabled()) {
							logger.debug("Compare asked qty " + entry.getValue() + " vs stock " + totalStockInKgOrL);
						}

						if (totalStockInKgOrL < entry.getValue()) {
							item.setState(SystemState.Refused);

							if (reqCtrl == null) {
								reqCtrl = ReqCtrlListDataItem.forbidden().withMessage(MLTextHelper.getI18NMessage(MESSAGE_MISSING_STOCK))
										.ofDataType(RequirementDataType.Formulation);

								batchData.getReqCtrlList().add(reqCtrl);
							}

							reqCtrl.addSource(entry.getKey());

							canApply = false;
						} else {
							item.setState(SystemState.Simulation);
						}

					} else {
						canApply = false;

					}

				}

				batchData.getAllocationList().retainAll(toRetain);

				if (canApply && SystemState.Valid.equals(batchData.getState())) {

					for (AllocationListDataItem item : batchData.getAllocationList()) {
						ProductData rawMaterialData = (ProductData) alfrescoRepository.findOne(item.getProduct());
						item.setStockListItems(
								decreaseInventory(batchData, rawMaterialData, item.getStockListItems(), item.getBatchQty(), item.getUnit()));
						item.setState(SystemState.Valid);
					}

				}
			}

		}

		return true;
	}

	private List<NodeRef> increaseInventory(List<NodeRef> stockListItems, Double batchQty, ProductUnit batchUnit) {
		List<NodeRef> stockRefs = new ArrayList<>();
		if ((stockListItems != null) && !stockListItems.isEmpty()) {
			StockListDataItem item = (StockListDataItem) alfrescoRepository.findOne(stockListItems.get(0));

			if ((batchUnit != null) && (batchUnit.isVolume() || batchUnit.isWeight())) {
				batchQty = batchQty / batchUnit.getUnitFactor();
			}

			ProductUnit stockUnit = item.getUnit();
			if (stockUnit == null) {
				stockUnit = ProductUnit.kg;
			}

			Double newQty = item.getBatchQty() + (batchQty * stockUnit.getUnitFactor());
			item.setBatchQty(newQty);
			alfrescoRepository.save(item);
			stockRefs.add(item.getNodeRef());
		}
		return stockRefs;
	}

	private List<NodeRef> decreaseInventory(BatchData batchData, ProductData rawMaterialData, List<NodeRef> stockListItems, Double batchQty,
			ProductUnit batchUnit) {
		List<NodeRef> stockRefs = new ArrayList<>();

		if ((batchUnit != null) && (batchUnit.isVolume() || batchUnit.isWeight())) {
			batchQty = batchQty / batchUnit.getUnitFactor();
		}

		List<StockListDataItem> filteredStockList = extractFilteredStockList(batchData, rawMaterialData, stockListItems);

		for (StockListDataItem item : filteredStockList) {

			if (batchQty > 0) {
				ProductUnit stockUnit = item.getUnit() != null ? item.getUnit() : ProductUnit.kg;

				// Calculate the new quantity and update batchQty
				Double newQty = Math.max(item.getBatchQty() - (batchQty * stockUnit.getUnitFactor()), 0);
				batchQty -= (item.getBatchQty() / stockUnit.getUnitFactor());
				item.setBatchQty(newQty);
				alfrescoRepository.save(item);

				// Add the node reference
				stockRefs.add(item.getNodeRef());
			}
		}

		return stockRefs;
	}

	/**
	 * <p>extractFilteredStockList.</p>
	 *
	 * @param batchData a {@link fr.becpg.repo.quality.data.BatchData} object
	 * @param rawMaterialData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param stockListItems a {@link java.util.List} object
	 * @return a {@link java.util.List} object
	 */
	public List<StockListDataItem> extractFilteredStockList(BatchData batchData, ProductData rawMaterialData, List<NodeRef> stockListItems) {
		List<StockListDataItem> tmp = extractStockList(batchData, rawMaterialData);

		// Sort the list by 'useByDate', with null values treated as Long.MAX_VALUE
		tmp.sort((item1, item2) -> {
			Long time1 = (item1.getUseByDate() != null) ? item1.getUseByDate().getTime() : Long.MAX_VALUE;
			Long time2 = (item2.getUseByDate() != null) ? item2.getUseByDate().getTime() : Long.MAX_VALUE;
			return time1.compareTo(time2);
		});

		// Filter the list based on stockListItems
		return tmp.stream().filter(item -> ((stockListItems != null && !stockListItems.isEmpty() && stockListItems.contains(item.getNodeRef()))
				|| accept(batchData, item))).toList();
	}

	private List<StockListDataItem> extractStockList(BatchData batchData, ProductData rawMaterialData) {
		List<StockListDataItem> ret = new ArrayList<>();

		if (rawMaterialData.isGeneric() && rawMaterialData.hasCompoListEl()) {
			for (CompoListDataItem compoList : rawMaterialData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				NodeRef productNodeRef = compoList.getProduct();
				if ((productNodeRef != null) && !shouldOmit(compoList)) {
					ProductData subProductData = (ProductData) alfrescoRepository.findOne(productNodeRef);
					if (accept(batchData, subProductData)) {
						ret.addAll(extractStockList(batchData, subProductData));
					}
				}
			}
		} else {
			ret.addAll(rawMaterialData.getStockList());
		}

		return ret;
	}

	private boolean shouldOmit(CompoListDataItem compoList) {
		return StockType.Omit.equals(compoList.getStockType())
				|| (compoList.getStockType() == null && DeclarationType.Omit.equals(compoList.getDeclType()));
	}

	private boolean shouldDeclare(CompoListDataItem compoList) {
		return StockType.Components.equals(compoList.getStockType())
				|| (compoList.getStockType() == null && DeclarationType.Declare.equals(compoList.getDeclType()));

	}

	private boolean accept(BatchData batchData, StockListDataItem item) {
		return isMatch(batchData.getPlants(), item.getPlants()) && isMatch(batchData.getLaboratories(), item.getLaboratories())
				&& hasValidBatchQty(item) && isNotRefused(item) && isUseByDateValid(item);
	}

	private boolean accept(BatchData batchData, ProductData subProductData) {
		return isMatch(batchData.getPlants(), subProductData.getPlants());
	}

	private boolean isMatch(List<NodeRef> batchList, List<NodeRef> itemList) {
		return (batchList == null) || (itemList == null) || batchList.isEmpty() || itemList.isEmpty()
				|| batchList.stream().anyMatch(itemList::contains);
	}

	private boolean hasValidBatchQty(StockListDataItem item) {
		return (item.getBatchQty() != null) && (item.getBatchQty() > 0);
	}

	private boolean isNotRefused(StockListDataItem item) {
		return !SystemState.Refused.equals(item.getState());
	}

	private boolean isUseByDateValid(StockListDataItem item) {
		return (item.getUseByDate() == null) || (item.getUseByDate().getTime() >= Calendar.getInstance().getTimeInMillis());
	}

	private Double computeTotalStock(BatchData batchData, ProductData rawMaterialData, List<NodeRef> stockListItems) {
		double total = 0d;

		List<StockListDataItem> tmp = new ArrayList<>();

		if ((stockListItems != null) && !stockListItems.isEmpty()) {
			for (NodeRef item : stockListItems) {
				tmp.add((StockListDataItem) alfrescoRepository.findOne(item));
			}
		} else {
			tmp = extractStockList(batchData, rawMaterialData);
		}

		for (StockListDataItem item : tmp) {
			if (accept(batchData, item)) {
				Double qty = item.getBatchQty();
				if (qty != null) {
					if (item.getUnit() != null) {
						if ((item.getUnit().isVolume() || item.getUnit().isWeight())) {
							qty = qty / item.getUnit().getUnitFactor();
						} else if (item.getUnit().isP()) {
							qty = qty * FormulationHelper.getNetWeight(rawMaterialData, FormulationHelper.DEFAULT_NET_WEIGHT);
						}
					}

					total += qty;
				}
			}

		}
		return total;
	}

	private void extractRawMaterials(ProductData productData, Map<NodeRef, Double> rawMaterials, Double parentQty, Double parentLossPerc) {

		for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			NodeRef productNodeRef = compoList.getProduct();
			if ((productNodeRef != null) && !shouldOmit(compoList)) {

				Double qty = FormulationHelper.getQtyInKg(compoList);
				Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
				if (logger.isDebugEnabled()) {
					logger.debug("Get rawMaterial " + productData.getName() + "qty: " + qty + " netWeight " + netWeight + " parentQty " + parentQty);
				}
				if ((qty != null) && (netWeight != 0d)) {
					qty = (parentQty * qty * FormulationHelper.getYield(compoList)) / (100 * netWeight);

					ProductData subProductData = (ProductData) alfrescoRepository.findOne(productNodeRef);

					Double lossPerc = FormulationHelper.calculateLossPerc(parentLossPerc,
							FormulationHelper.getComponentLossPerc(subProductData, compoList));

					if (subProductData instanceof RawMaterialData) {

						Double rmQty = rawMaterials.get(productNodeRef);
						if (rmQty == null) {
							rmQty = 0d;
						}
						rmQty += FormulationHelper.getQtyWithLoss(qty, lossPerc);
						rawMaterials.put(productNodeRef, rmQty);
					} else if (!(subProductData instanceof LocalSemiFinishedProductData)) {
						extractRawMaterials(subProductData, rawMaterials, qty, lossPerc);
					}
				}
			}
		}

	}

}
