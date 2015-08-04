/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProcessListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.spel.SpelHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class CostCalculatingVisitor.
 *
 * @author querephi
 */
public class CostsCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<CostListDataItem> {

	public static final Double DEFAULT_LOSS_RATIO = 0d;

	/** The logger. */
	private static final Log logger = LogFactory.getLog(CostsCalculatingFormulationHandler.class);

	private EntityTplService entityTplService;

	private FormulaService formulaService;
	
	private PackagingHelper packagingHelper;
	
	private AlfrescoRepository<ProductData> alfrescoRepositoryProductData;

	public void setFormulaService(FormulaService formulaService) {
		this.formulaService = formulaService;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setPackagingHelper(PackagingHelper packagingHelper) {
		this.packagingHelper = packagingHelper;
	}

	public void setAlfrescoRepositoryProductData(AlfrescoRepository<ProductData> alfrescoRepositoryProductData) {
		this.alfrescoRepositoryProductData = alfrescoRepositoryProductData;
	}

	@Override
	protected Class<CostListDataItem> getInstanceClass() {

		return CostListDataItem.class;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("Cost calculating visitor");

		// no formulation
		if ((formulatedProduct.getCostList()==null)
				 &&	!alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_COSTLIST)) {
			logger.debug("no formulation");
			return true;
		}

		if (formulatedProduct.getCostList() == null) {
			formulatedProduct.setCostList(new LinkedList<CostListDataItem>());
		}		
		
		if(formulatedProduct.getDefaultVariantPackagingData() == null){
			formulatedProduct.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(formulatedProduct));
		}
		
		if(formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)) || formulatedProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
				|| formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))){
			formulateSimpleList(formulatedProduct, formulatedProduct.getCostList());
			
			// simulation: take in account cost of components defined on formulated product
			calculateSimulationCosts(formulatedProduct);
		}		
		
		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = formulaService.createEvaluationContext(formulatedProduct);

		computeCostsList(formulatedProduct, parser, context);

		if (formulatedProduct.getCostList() != null) {
			for (CostListDataItem c : formulatedProduct.getCostList()) {				
				c.setUnit(calculateUnit(formulatedProduct.getUnit(), 
						(String)nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTCURRENCY), 
						(Boolean)nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED)));
				
				if (transientFormulation) {
					c.setTransient(true);
				}
			}
		}
		
		Composite<CostListDataItem> composite = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCostList());
		calculateParentCost(formulatedProduct, composite);
		
		// profitability
		calculateProfitability(formulatedProduct);

		return true;
	}

	
	//Merge with nutList
	@Deprecated
	private void computeCostsList(ProductData productData, ExpressionParser parser, StandardEvaluationContext context) {
		
		if (productData.getCostList() != null) {
			for (CostListDataItem costListDataItem : productData.getCostList()) {
				String error = null;
				costListDataItem.setIsFormulated(false);
				costListDataItem.setErrorLog(null);
				if ((costListDataItem.getIsManual() == null || !costListDataItem.getIsManual()) && costListDataItem.getCost() != null) {

					String formula = (String) nodeService.getProperty(costListDataItem.getCost(), PLMModel.PROP_COST_FORMULA);
					if (formula != null && formula.length() > 0) {
						try {
							costListDataItem.setIsFormulated(true);							
							formula = SpelHelper.formatFormula(formula);

							Expression exp = parser.parseExpression(formula);
							Object ret = exp.getValue(context);
							if (ret instanceof Double) {
								costListDataItem.setValue((Double) ret);

								if (formula.contains(".value")) {
									try {
										exp = parser.parseExpression(formula.replace(".value", ".mini"));
										costListDataItem.setMini((Double) exp.getValue(context));
										exp = parser.parseExpression(formula.replace(".value", ".maxi"));
										costListDataItem.setMaxi((Double) exp.getValue(context));
									} catch (Exception e) {
										costListDataItem.setMaxi(null);
										costListDataItem.setMini(null);
										if (logger.isDebugEnabled()) {
											logger.debug("Error in formula :" + formula, e);
										}
									}
								}

							} else {
								error = I18NUtil.getMessage("message.formulate.formula.incorrect.type.double",
										Locale.getDefault());
							}

						} catch (Exception e) {
							error = e.getLocalizedMessage();
							if (logger.isDebugEnabled()) {
								logger.debug("Error in formula :" + SpelHelper.formatFormula(formula), e);
							}
						}
					}
				}

				if (error != null) {
					costListDataItem.setValue(null);
					costListDataItem.setErrorLog(error);
					String message = I18NUtil.getMessage("message.formulate.costList.error", Locale.getDefault(),
							nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME), error);
					productData.getCompoListView().getReqCtrlList()
							.add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, costListDataItem.getCost(), new ArrayList<NodeRef>()));
				}

			}
		}

	}

	@Override
	protected void visitChildren(ProductData formulatedProduct, List<CostListDataItem> costList) throws FormulateException {

		Double netQty;
		if (formulatedProduct instanceof PackagingKitData) {
			netQty = FormulationHelper.QTY_FOR_PIECE;
		} else if (formulatedProduct instanceof ResourceProductData) {
			netQty = FormulationHelper.QTY_FOR_PIECE;
		} else {
			if (ProductUnit.P.equals(formulatedProduct.getUnit())) {
				netQty = FormulationHelper.QTY_FOR_PIECE;
			} else {
				netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
			}
		}

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			/*
			 * Composition
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts1 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RAWMATERIAL);

			Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(
					Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())));
			visitCompoListChildren(formulatedProduct, composite, costList, DEFAULT_LOSS_RATIO, netQty, mandatoryCharacts1);

			addReqCtrlList(formulatedProduct.getCompoListView().getReqCtrlList(), mandatoryCharacts1);

		}

		if (formulatedProduct.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			/*
			 * PackagingList
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts2 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_PACKAGINGMATERIAL);

			for (PackagingListDataItem packagingListDataItem : formulatedProduct.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
					new VariantFilters<>()))) {
				Double qty = FormulationHelper.getQtyForCost(packagingListDataItem);
				
				// secondary on packagingKit with pallet aspect -> nothing
				// tertiary on packagingKit with pallet aspect -> divide by boxesPerPallet
				// secondary on finishedProduct (if it's not packagingKit with pallet aspect) -> divide by productPerBoxes
				// tertiary on finishedProduct (if it's not packagingKit with pallet aspect) -> divide by productPerBoxes * boxesPerPallet				
				PackagingLevel packagingLevel = packagingListDataItem.getPkgLevel(); 
				if(packagingLevel != null){
					if(formulatedProduct instanceof PackagingKitData && 
							formulatedProduct.getAspects().contains(PackModel.ASPECT_PALLET)){
						if(packagingLevel.equals(PackagingLevel.Tertiary)){
							Integer nbByPalet = (Integer) nodeService.getProperty(formulatedProduct.getNodeRef(), PackModel.PROP_PALLET_BOXES_PER_PALLET);
							if (nbByPalet != null && nbByPalet > 0) {
								qty = qty / nbByPalet;
							}
						}						
					}
					else if((nodeService.hasAspect(packagingListDataItem.getProduct(), PackModel.ASPECT_PALLET) && 
							PackagingLevel.Secondary.equals(packagingListDataItem.getPkgLevel()) && 
							PackagingListUnit.PP.equals(packagingListDataItem.getPackagingListUnit()) && 
							PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(packagingListDataItem.getProduct()))) == false &&
							formulatedProduct.getDefaultVariantPackagingData() != null){
						if(packagingLevel.equals(PackagingLevel.Secondary)){
							if(formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null &&
								formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != 0d){
								logger.debug("qty : " + qty + " product per boxes " + formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes());
								qty = qty / formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes();
							}							
						}
						else if(packagingLevel.equals(PackagingLevel.Tertiary)){
							if(formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null &&
								formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != 0d &&
								formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet() != null &&
								formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet() != 0d){								
								logger.debug("qty : " + qty + " product per boxes " + formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() + " boxes per pallet " + formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet());
								qty = qty / (formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() * formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet());
							}
						}
					}
				}
				
				visitPart(packagingListDataItem.getProduct(), costList, qty, null, netQty, mandatoryCharacts2, null,  false);
			}

			addReqCtrlList(formulatedProduct.getPackagingListView().getReqCtrlList(), mandatoryCharacts2);
		}

		if (formulatedProduct.hasProcessListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			/*
			 * ProcessList
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts3 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RESOURCEPRODUCT);
			for (ProcessListDataItem processListDataItem : formulatedProduct.getProcessList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
					new VariantFilters<>()))) {

				Double qty = FormulationHelper.getQty(formulatedProduct, processListDataItem);
				if (processListDataItem.getResource() != null && qty != null) {
					if (ProcessListUnit.P.equals(processListDataItem.getUnit())) {
						netQty = FormulationHelper.QTY_FOR_PIECE;
					}

					visitPart(processListDataItem.getResource(), costList, qty, null, netQty, mandatoryCharacts3, null, false);
				}
			}

			addReqCtrlList(formulatedProduct.getProcessListView().getReqCtrlList(), mandatoryCharacts3);
		}

	}

	private void visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite, List<CostListDataItem> costList,
			Double parentLossRatio, Double netQty, Map<NodeRef, List<NodeRef>> mandatoryCharacts) throws FormulateException {

		Map<NodeRef, Double> totalQtiesValue = new HashMap<>();
		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			if (!component.isLeaf()) {

				// take in account the loss perc
				Double lossPerc = component.getData().getLossPerc() != null ? component.getData().getLossPerc() : 0d;
				Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);
				if (logger.isDebugEnabled()) {
					logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
				}

				// calculate children
				Composite<CompoListDataItem> c = component;
				visitCompoListChildren(formulatedProduct, c, costList, newLossPerc, netQty, mandatoryCharacts);
			} else {
				CompoListDataItem compoListDataItem = component.getData();
				Double qty = FormulationHelper.getQtyForCost(compoListDataItem, 
						parentLossRatio,
						ProductUnit.getUnit((String)nodeService.getProperty(compoListDataItem.getProduct(), PLMModel.PROP_PRODUCT_UNIT)));
				visitPart(compoListDataItem.getProduct(), costList, qty, null, netQty, mandatoryCharacts, totalQtiesValue, formulatedProduct instanceof RawMaterialData);				
			}
		}		
		//Case Generic MP
		if( formulatedProduct instanceof RawMaterialData){
			formulateGenericRawMaterial(costList, totalQtiesValue, netQty);
		}
	}

	@Override
	protected QName getDataListVisited() {
		return PLMModel.TYPE_COSTLIST;
	}

	/**
	 * Calculate the costListUnit
	 * 
	 * @param productUnit
	 * @param costUnit
	 * @return
	 */
	public static String calculateUnit(ProductUnit productUnit, String costUnit, Boolean isFixed) {

		if (isFixed != null && isFixed) {
			return costUnit;
		}
		else{
			return costUnit + calculateSuffixUnit(productUnit);
		}		
	}

	/**
	 * Calculate the suffix of the costListUnit
	 * 
	 * @param productUnit
	 * @return
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit) {
		if (productUnit == null || productUnit.equals(ProductUnit.kg) || productUnit.equals(ProductUnit.g)) {
			return UNIT_SEPARATOR + ProductUnit.kg;
		} else if (productUnit.equals(ProductUnit.L) || productUnit.equals(ProductUnit.mL)) {
			return UNIT_SEPARATOR + ProductUnit.L;
		} else {
			return UNIT_SEPARATOR + productUnit.toString();
		}
	}

	private void calculateProfitability(ProductData formulatedProduct) {

		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, 1d);
		Double unitTotalVariableCost = 0d;//for 1 product
		Double unitTotalFixedCost = 0d;

		for (CostListDataItem c : formulatedProduct.getCostList()) {

			Boolean isFixed = (Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED);
			Double costPerProduct = null;				
			
			if (c.getValue() != null) {
				if (isFixed != null && isFixed == Boolean.TRUE) {
					unitTotalFixedCost += c.getValue();
					if(formulatedProduct.getProjectedQty() != null && !formulatedProduct.getProjectedQty().equals(0)){
						costPerProduct = c.getValue() / formulatedProduct.getProjectedQty();
					}												
					
				} else if(FormulationHelper.isProductUnitP(formulatedProduct.getUnit())){
					costPerProduct = c.getValue();
				} else{
					costPerProduct = netQty * c.getValue();
				}
			}
			
			c.setValuePerProduct(null);
			if(costPerProduct != null){
				if(c.getDepthLevel() != null && c.getDepthLevel() == 1){
					unitTotalVariableCost += costPerProduct;
				}				
				if(formulatedProduct instanceof FinishedProductData){
					c.setValuePerProduct(costPerProduct);
				}
			}
		}

		if(formulatedProduct instanceof FinishedProductData){
			formulatedProduct.setUnitTotalCost(unitTotalVariableCost);
		}		
		else{
			// €/Kg, €/L or €/P
			formulatedProduct.setUnitTotalCost(unitTotalVariableCost / netQty);
		}

		if (formulatedProduct.getUnitPrice() != null && formulatedProduct.getUnitTotalCost() != null) {

			// profitability
			Double profit = formulatedProduct.getUnitPrice() - formulatedProduct.getUnitTotalCost();
			Double profitability = 100 * profit / formulatedProduct.getUnitPrice();
			logger.debug("profitability: " + profitability);
			formulatedProduct.setProfitability(profitability);

			// breakEven
			if (profit > 0) {

				Long breakEven = Math.round(unitTotalFixedCost / profit);
				formulatedProduct.setBreakEven(breakEven);
			} else {
				formulatedProduct.setBreakEven(null);
			}

		} else {
			formulatedProduct.setProfitability(null);
		}
	}

	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {

		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>();

		NodeRef entityTplNodeRef = entityTplService.getEntityTpl(componentType);

		if (entityTplNodeRef != null) {

			List<CostListDataItem> costList = alfrescoRepository.loadDataList(entityTplNodeRef, PLMModel.TYPE_COSTLIST, PLMModel.TYPE_COSTLIST);

			for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
				for (CostListDataItem c : costList) {
					if (c.getCost() != null && c.getCost().equals(costListDataItem.getCost())) {
						mandatoryCharacts.put(c.getCost(), new ArrayList<NodeRef>());
						break;
					}
				}
			}
		}
		return mandatoryCharacts;
	}
	
	@Override
	protected void copyProductTemplateList(ProductData formulatedProduct, List<CostListDataItem> simpleListDataList){
		//TODO : manage multiple plants
		NodeRef plantNodeRef = formulatedProduct.getPlants().isEmpty() ? null : formulatedProduct.getPlants().get(0);
		
		List<CostListDataItem> templateCostLists = new ArrayList<>();
		if (formulatedProduct.getEntityTpl() != null && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			templateCostLists.addAll(formulatedProduct.getEntityTpl().getCostList());
		}
		if(formulatedProduct.getClients() !=null){
			for(ClientData client : formulatedProduct.getClients()){
				templateCostLists.addAll(client.getCostList());
			}
		}
				
		for(CostListDataItem templateCostList : templateCostLists){
			boolean addCost = true;
			for(CostListDataItem costList : simpleListDataList){
				//plants
				if(templateCostList.getPlants().isEmpty() || templateCostList.getPlants().contains(plantNodeRef)){
					//same cost
					if(costList.getCost() != null && costList.getCost().equals(templateCostList.getCost())){						
						//manual
						if(templateCostList.getIsManual() == null || !templateCostList.getIsManual()){
							copyTemplateCost(formulatedProduct, templateCostList, costList);
						}
						addCost = false;
						break;
					}
				}
				else{
					addCost = false;
				}					
			}
			if(addCost){
				templateCostList.setNodeRef(null);
				templateCostList.setParentNodeRef(null);
				copyTemplateCost(formulatedProduct, templateCostList, templateCostList);
				simpleListDataList.add(templateCostList);
			}
		}
	}
	
	private void copyTemplateCost(ProductData formulatedProduct, CostListDataItem templateCostList, CostListDataItem costList){
		
		if(logger.isDebugEnabled()){
			logger.debug("copy cost " + nodeService.getProperty(templateCostList.getCost(), ContentModel.PROP_NAME) + " unit " + templateCostList.getUnit() +
					" PackagingData " + formulatedProduct.getDefaultVariantPackagingData());
		}
		boolean isCalculated = false;
		
		if(formulatedProduct.getUnit() != null && templateCostList.getUnit() != null){
			if(!templateCostList.getUnit().endsWith(formulatedProduct.getUnit().toString())){	
				if(FormulationHelper.isProductUnitP(formulatedProduct.getUnit())){
					if(templateCostList.getUnit().endsWith("kg") || templateCostList.getUnit().endsWith("L")){
						calculateValues(templateCostList, costList, false, FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						isCalculated = true;
					}
					else if(templateCostList.getUnit().endsWith("Pal")){
						if(formulatedProduct.getDefaultVariantPackagingData() != null 
								&& formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null 
								&& formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet() != null){												
							calculateValues(templateCostList, costList, true, (double)formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() * formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet());
						}
						isCalculated = true;
					}
				}
				else if(FormulationHelper.isProductUnitKg(formulatedProduct.getUnit()) || FormulationHelper.isProductUnitLiter(formulatedProduct.getUnit())){
					if(templateCostList.getUnit().endsWith("P")){
						calculateValues(templateCostList, costList, true, FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						isCalculated = true;
					}
					else if(templateCostList.getUnit().endsWith("Pal")){
						if(formulatedProduct.getDefaultVariantPackagingData() != null 
								&& formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null 
								&& formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet() != null){
							calculateValues(templateCostList, costList, true, (double)formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() * formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet() * FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));							
						}						
						isCalculated = true;
					}
				}				
			}
		}	
		
		if(!isCalculated){
			calculateValues(templateCostList, costList, null, null);
		}
	}
	
	private void calculateValues(CostListDataItem templateCostList, CostListDataItem costList, Boolean divide, Double qty){
		
		if(logger.isDebugEnabled()){
			logger.debug("calculateValues " + nodeService.getProperty(templateCostList.getCost(), ContentModel.PROP_NAME));
		}
		
		Double value = templateCostList.getValue();
		Double maxi = templateCostList.getMaxi();
		Double previousValue = templateCostList.getPreviousValue();
		Double futureValue = templateCostList.getFutureValue();
		
		if(divide != null && qty != null){
			if(divide){
				value = divide(value, qty);
				maxi = divide(maxi, qty);
				previousValue = divide(previousValue, qty);
				futureValue = divide(futureValue, qty); 
			}
			else{
				value = multiply(value, qty);
				maxi = multiply(maxi, qty);
				previousValue = multiply(previousValue, qty);
				futureValue = multiply(futureValue, qty); 
			}
		}		
		
		if(value != null){
			costList.setValue(value);
		}
		if(maxi != null){
			costList.setMaxi(maxi);
		}
		if(previousValue != null){
			costList.setPreviousValue(previousValue);
		}
		if(futureValue != null){
			costList.setFutureValue(futureValue);
		}					
	}
	
	private Double divide(Double a, Double b){
		if(a != null && b != null && b != 0d){
			return a/b;
		}
		return null;
	}
	
	private Double multiply(Double a, Double b){
		if(a!=null && b!=null){
			return a*b;
		}
		return null;
	}
	
	private void calculateParentCost(ProductData formulatedProduct, Composite<CostListDataItem> composite) {

		Double value = 0d;
		Double maxi = 0d;
		Double previousValue = 0d;
		Double futureValue = 0d;
		for (Composite<CostListDataItem> component : composite.getChildren()) {
			calculateParentCost(formulatedProduct, component);
			CostListDataItem costListDataItem = component.getData();			
			if(costListDataItem.getComponentNodeRef() != null){
				return;
			}
			if (costListDataItem.getValue() != null) {
				value += costListDataItem.getValue();
			}
			if (costListDataItem.getMaxi() != null) {
				maxi += costListDataItem.getMaxi();
			}
			if (costListDataItem.getPreviousValue() != null) {
				previousValue += costListDataItem.getPreviousValue();
			}
			if (costListDataItem.getFutureValue() != null) {
				futureValue += costListDataItem.getFutureValue();
			}
		}
		if (!composite.isRoot()) {			
			if (!composite.isLeaf()) {
				composite.getData().setValue(value);
				composite.getData().setMaxi(maxi);
				composite.getData().setPreviousValue(previousValue);
				composite.getData().setFutureValue(futureValue);
			}
		}
	}
	
	private double getCompoListQty(ProductData productData, NodeRef componentNodeRef, double parentQty) {
		double totalQty = 0d;
		for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			NodeRef productNodeRef = compoList.getProduct();			
			Double qty = FormulationHelper.getQtyForCost(compoList, 
									0d,
									ProductUnit.getUnit((String)nodeService.getProperty(productNodeRef, PLMModel.PROP_PRODUCT_UNIT)));
			if (logger.isDebugEnabled()) {
				logger.debug("Get component " + nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME) + "qty: " + qty + " recipeQtyUsed "
						+ productData.getRecipeQtyUsed());
			}
			if (qty != null && productData.getRecipeQtyUsed() != null && productData.getRecipeQtyUsed() != 0d) {
				qty = parentQty * qty / productData.getRecipeQtyUsed();

				if (productNodeRef.equals(componentNodeRef)) {
					totalQty += qty;	
				} else {
					totalQty += getCompoListQty(alfrescoRepositoryProductData.findOne(productNodeRef), componentNodeRef, qty);
				}
			}
		}
		return totalQty;
	}
	
	private double getPackagingListQty(ProductData productData, NodeRef componentNodeRef) {
		double totalQty = 0d;
		for (PackagingListDataItem packList : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			NodeRef productNodeRef = packList.getProduct();			
			Double qty = FormulationHelper.getQtyForCost(packList);
			if (logger.isDebugEnabled()) {
				logger.debug("Get component " + nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME) + "qty: " + qty);
			}
			if (productNodeRef.equals(componentNodeRef)) {
				totalQty += qty;
			}
		}
		return totalQty;
	}
	
	private void calculateSimulationCosts(ProductData formulatedProduct){
		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		for (CostListDataItem c : formulatedProduct.getCostList()) {				
			if(c.getComponentNodeRef() != null && c.getParent() != null){
				Double qtyComponent;
				ProductData componentData = alfrescoRepositoryProductData.findOne(c.getComponentNodeRef());
				if(componentData instanceof PackagingMaterialData){
					qtyComponent = getPackagingListQty(formulatedProduct, c.getComponentNodeRef());
				}
				else{
					qtyComponent = getCompoListQty(formulatedProduct, c.getComponentNodeRef(), formulatedProduct.getRecipeQtyUsed());					
				}
				for(CostListDataItem c2 : componentData.getCostList()){
					if(c2.getCost().equals(c.getParent().getCost()) && c.getSimulatedValue() != null){						
						if(logger.isDebugEnabled()){
							logger.debug("add simulationCost " + "c2 value " + c2.getValue() + "c simulated value " + c.getSimulatedValue() + " qty component " + qtyComponent + " netQty " + netQty);
						}			
						c.setValue((c.getSimulatedValue() - c2.getValue()) * qtyComponent / netQty);
						c.getParent().setValue(c.getParent().getValue() + c.getValue());
						break;
					}
				}				
			}
		}		
	}	
}
