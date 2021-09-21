package fr.becpg.repo.quality.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import com.ibm.icu.util.Calendar;

import fr.becpg.model.PLMModel;
import fr.becpg.model.QualityModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.quality.data.dataList.AllocationListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

public class BatchFormulationHandler extends FormulationBaseHandler<BatchData> {

	private static Log logger = LogFactory.getLog(BatchFormulationHandler.class);

	private static final String MESSAGE_MISSING_STOCK = "message.formulate.missing.stock";

	private AlfrescoRepository<ProductData> alfrescoRepository;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

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
				if ((productNodeRef != null) && !DeclarationType.Omit.equals(compoList.getDeclType())) {

					Double qty = FormulationHelper.getQtyInKg(compoList);

					if (qty != null) {
						ProductData subProductData = alfrescoRepository.findOne(productNodeRef);
						if (!(subProductData instanceof LocalSemiFinishedProductData)) {
							if (!DeclarationType.Declare.equals(compoList.getDeclType()) || (subProductData instanceof RawMaterialData)) {

								Double rmQty = rawMaterials.get(productNodeRef);
								if (rmQty == null) {
									rmQty = 0d;
								}
								rmQty += qty;
								rawMaterials.put(productNodeRef, rmQty);
							} else {
								extractRawMaterials(subProductData, rawMaterials, qty);
							}
						}
					}
				}
			}

			// sort
			List<Map.Entry<NodeRef, Double>> sortedRawMaterials = new LinkedList<>(rawMaterials.entrySet());
			Collections.sort(sortedRawMaterials, (r1, r2) -> r2.getValue().compareTo(r1.getValue()));
			
			if(batchData.getAllocationList()!=null && alfrescoRepository.hasDataList(batchData, QualityModel.TYPE_BATCH_ALLOCATION_LIST)) {

				List<AllocationListDataItem> toRetain = new LinkedList<>();
	
				boolean canApply = true;
				int sort = 0;
				for (Map.Entry<NodeRef, Double> entry : sortedRawMaterials) {
	
					AllocationListDataItem item = batchData.getAllocationList().stream().filter(a -> entry.getKey().equals(a.getProduct())).findFirst()
							.orElse(null);
	
					if (item == null) {
						item = new AllocationListDataItem();
						item.setProduct(entry.getKey());
						batchData.getAllocationList().add(item);
					}
	
					item.setSort(sort++);
	
					toRetain.add(item);
	
					if (!SystemState.Valid.equals(item.getState())) {
						ProductData rawMaterialData = alfrescoRepository.findOne(entry.getKey());
						Double totalStockInKgOrL = computeTotalStock(rawMaterialData);
	
						item.setBatchQty(entry.getValue());
						item.setUnit(isVolume ? ProductUnit.L : ProductUnit.kg);
						item.setState(SystemState.Simulation);
	
						if (logger.isDebugEnabled()) {
							logger.debug("Compare asked qty " + entry.getValue() + " vs stock " + totalStockInKgOrL);
						}
	
						if (totalStockInKgOrL < entry.getValue()) {
							item.setState(SystemState.Refused);
	
							if (reqCtrl == null) {
								reqCtrl = new ReqCtrlListDataItem(null, RequirementType.Forbidden, new MLText(I18NUtil.getMessage(MESSAGE_MISSING_STOCK)),
										null, new ArrayList<>(), RequirementDataType.Formulation);
								batchData.getReqCtrlList().add(reqCtrl);
							}
	
							if (!reqCtrl.getSources().contains(entry.getKey())) {
								reqCtrl.getSources().add(entry.getKey());
							}
	
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
						ProductData rawMaterialData = alfrescoRepository.findOne(item.getProduct());
						item.setStockListItems(decreaseInventory(rawMaterialData, item.getBatchQty(), item.getUnit()));
						item.setState(SystemState.Valid);
					}
	
				}
			}

		}
			

		return true;
	}

	private List<NodeRef> decreaseInventory(ProductData rawMaterialData, Double batchQty, ProductUnit batchUnit) {
		List<NodeRef> stockRefs = new ArrayList<>();

		if ((batchUnit != null) && (batchUnit.isVolume() || batchUnit.isWeight())) {
			batchQty = batchQty / batchUnit.getUnitFactor();
		}

		for (StockListDataItem item : rawMaterialData.getStockList()) {

			if (batchQty <= 0) {
				break;
			}

			if (accept(item)) {

				ProductUnit stockUnit = item.getUnit();
				if (stockUnit == null) {
					stockUnit = ProductUnit.kg;
				}

				Double newQty = Math.max(item.getBatchQty() - (batchQty * stockUnit.getUnitFactor()), 0);
				batchQty -= (item.getBatchQty() / stockUnit.getUnitFactor());
				item.setBatchQty(newQty);

				stockRefs.add(item.getNodeRef());
			}

		}

		alfrescoRepository.save(rawMaterialData);

		return stockRefs;
	}

	private boolean accept(StockListDataItem item) {
		return (item.getBatchQty() != null) && (item.getBatchQty() > 0) && !SystemState.Refused.equals(item.getState())
				&& ((item.getUseByDate() == null) || (item.getUseByDate().getTime() >= Calendar.getInstance().getTimeInMillis()));
	}

	private Double computeTotalStock(ProductData rawMaterialData) {
		double total = 0d;
		for (StockListDataItem item : rawMaterialData.getStockList()) {
			if (accept(item)) {
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

	private void extractRawMaterials(ProductData productData, Map<NodeRef, Double> rawMaterials, Double parentQty) {

		for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			NodeRef productNodeRef = compoList.getProduct();
			if ((productNodeRef != null) && !DeclarationType.Omit.equals(compoList.getDeclType())) {

				Double qty = FormulationHelper.getQtyInKg(compoList);
				Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
				if (logger.isDebugEnabled()) {
					logger.debug("Get rawMaterial " + productData.getName() + "qty: " + qty + " netWeight " + netWeight + " parentQty " + parentQty);
				}
				if ((qty != null) && (netWeight != 0d)) {
					qty = (parentQty * qty * FormulationHelper.getYield(compoList)) / (100 * netWeight);

					ProductData subProductData = alfrescoRepository.findOne(productNodeRef);

					if (subProductData instanceof RawMaterialData) {

						Double rmQty = rawMaterials.get(productNodeRef);
						if (rmQty == null) {
							rmQty = 0d;
						}
						rmQty += qty;
						rawMaterials.put(productNodeRef, rmQty);
					} else if (!(subProductData instanceof LocalSemiFinishedProductData)) {
						extractRawMaterials(subProductData, rawMaterials, qty);
					}
				}
			}
		}

	}

}
