package fr.becpg.repo.product.formulation;

import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.search.BeCPGSearchService;
import fr.becpg.repo.variant.filters.VariantFilters;

@Service
public class CompositionCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final String MESSAGE_RM_WATER = "message.formulate.rawmaterial.water";	
	private static final String KEY_RM_WATER = "RMWater";	
	
	private static Log logger = LogFactory.getLog(CompositionCalculatingFormulationHandler.class);
	
	private NodeService nodeService;
	
	private BeCPGSearchService beCPGSearchService;
	
	private BeCPGCacheService beCPGCacheService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");		
		
		// no compo => no formulation
		if(!formulatedProduct.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)){			
			logger.debug("no compo => no formulation");
			return true;
		}
		
		//Take in account net weight
		if(formulatedProduct.getQty() != null && formulatedProduct.getUnit() != null){
			if(ProductUnit.g.equals(formulatedProduct.getUnit())){
				formulatedProduct.setNetWeight(formulatedProduct.getQty() / 1000);
			}
			else if(ProductUnit.kg.equals(formulatedProduct.getUnit())){
				formulatedProduct.setNetWeight(formulatedProduct.getQty());
			}
		}
		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct.getNodeRef(), nodeService);			
		
		// calculate on every item
		Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL));
		visitQtyChildren(formulatedProduct, netWeight, compositeAll);
		visitYieldChildren(formulatedProduct, netWeight, compositeAll);
		
		Composite<CompoListDataItem> compositeDefaultVariant = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT));
		
		// Yield		
		Double qtyUsed = calculateQtyUsedBeforeProcess(compositeDefaultVariant);
		if(qtyUsed != null && qtyUsed != 0d){
			formulatedProduct.setYield(100 * netWeight / qtyUsed);
		}	
		
		Double netVolume = FormulationHelper.getNetVolume(formulatedProduct.getNodeRef(), nodeService);
		if(netVolume != null){
			Double calculatedVolume = calculateVolumeFromChildren(compositeDefaultVariant);
			if(calculatedVolume != 0d){
				formulatedProduct.setYieldVolume(100 * netVolume / calculatedVolume);
			}			
		}
		
		return true;
	}
	
	private void visitQtyChildren(ProductData formulatedProduct, Double parentQty, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					
			
			// qty and sub formula qty are defined and not equal to 0
			if(parentQty != null && !parentQty.equals(0d)){
				
				Double qtySubFormula = FormulationHelper.getQtySubFormula(component.getData(), nodeService);
				logger.debug("qtySubFormula: " + qtySubFormula + " parentQty: " + parentQty);
				if(qtySubFormula != null){
					
					Double qty = null;
					// take in account percentage
					if(component.getData().getCompoListUnit() != null && 
							component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){
						qtySubFormula = qtySubFormula * parentQty / 100;
					}
					
					// Take in account yield that is defined on component	
					Double yield = FormulationHelper.DEFAULT_YIELD;					
					if(component.isLeaf() && component.getData().getYieldPerc() != null && component.getData().getYieldPerc() != 0d){
						
						yield = component.getData().getYieldPerc();							
						Double qtyWater = qtySubFormula * (1 - FormulationHelper.DEFAULT_YIELD / yield);							
						CompoListDataItem waterCompoListDataItem = new CompoListDataItem(null, 
												composite.getData(), 
												qtyWater, null,
												component.getData().getCompoListUnit(), 
												component.getData().getLossPerc(), 
												DeclarationType.Declare, 
												getWaterRawMaterial());
						
						waterCompoListDataItem.setTransient(true);
						formulatedProduct.getCompoListView().getCompoList().add(waterCompoListDataItem);
					}
					qty = qtySubFormula * 100 / yield;			
					
					component.getData().setQty(qty);					
				}
			}
			
			// calculate volume ?			
			Double volume = FormulationHelper.getNetVolume(component.getData(), nodeService); 
			component.getData().getExtraProperties().put(BeCPGModel.PROP_COMPOLIST_VOLUME, volume);
			
			// calculate children
			if(!component.isLeaf()){
				
				// take in account percentage
				if(component.getData().getCompoListUnit() != null && 
						component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){	
					
					visitQtyChildren(formulatedProduct, parentQty, component);
					
					// no yield but calculate % of composite
					Double compositePerc = 0d;
					for(Composite<CompoListDataItem> child : component.getChildren()){	
						compositePerc += child.getData().getQtySubFormula();
					}
					component.getData().setQtySubFormula(compositePerc);
					component.getData().setQty(compositePerc * parentQty / 100);
				}
				else{
					
					visitQtyChildren(formulatedProduct, component.getData().getQty(),component);					
				}				
			}			
		}
	}

	private void visitYieldChildren(ProductData formulatedProduct, Double parentQty, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					
			
			// calculate children
			if(!component.isLeaf()){
				
				// take in account percentage
				if(component.getData().getCompoListUnit() != null && 
						component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){	
					
					visitYieldChildren(formulatedProduct, parentQty, component);
					component.getData().setYieldPerc(null);
				}
				else{
					visitYieldChildren(formulatedProduct, component.getData().getQty(),component);
					
					// Yield				
					component.getData().setYieldPerc(calculateYield(component));
				}				
			}			
		}		
	}
		
	private Double calculateYield(Composite<CompoListDataItem> composite) throws FormulateException{
		
		Double yieldPerc = 100d;
		
		// qty Used in the sub formula
		Double qtyUsed = 0d;				
		for(Composite<CompoListDataItem> component : composite.getChildren()){
			
			Double qty = FormulationHelper.getQty(component.getData());
			if(qty != null){
				// water can be taken in account on Raw Material
				Double yield = component.isLeaf() && component.getData().getYieldPerc() != null ? component.getData().getYieldPerc() : FormulationHelper.DEFAULT_YIELD;
				qtyUsed += qty * yield / 100;
			}
		}
		
		// qty after process
		Double qtyAfterProcess = FormulationHelper.getQtySubFormula(composite.getData(), nodeService);		
		if(qtyAfterProcess != null && qtyAfterProcess != 0 && qtyUsed != 0){
			yieldPerc = qtyAfterProcess / qtyUsed * 100;
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("component: " + nodeService.getProperty(composite.getData().getProduct(),  ContentModel.PROP_NAME) + 
					" qtyAfterProcess: " + qtyAfterProcess + " - qtyUsed: " + qtyUsed + " yieldPerc: " + yieldPerc);
		}
		
		return yieldPerc;
	}	
	
	private Double calculateQtyUsedBeforeProcess(Composite<CompoListDataItem> composite) throws FormulateException{				
		
		Double qty = 0d;
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){						
			
			if(!component.isLeaf()){
				// calculate children
				qty += calculateQtyUsedBeforeProcess(component);
			}else{
				qty += FormulationHelper.getQty(component.getData());
			}
		}
		
		return qty;
	}
	
	private NodeRef getWaterRawMaterial(){
		
		return beCPGCacheService.getFromCache(CompositionCalculatingFormulationHandler.class.getName(), KEY_RM_WATER , new BeCPGCacheDataProviderCallBack<NodeRef>() {

			@Override
			public NodeRef getData() {
								
				List<NodeRef> resultSet = null;
				String waterIngName = I18NUtil.getMessage(MESSAGE_RM_WATER, Locale.getDefault());
				
				String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_RAWMATERIAL)) + 
						LuceneHelper.mandatory(LuceneHelper.getCondEqualValue(ContentModel.PROP_NAME, waterIngName));
				resultSet = beCPGSearchService.search(query, null, RepoConsts.MAX_RESULTS_SINGLE_VALUE, SearchService.LANGUAGE_LUCENE);
				
				if (!resultSet.isEmpty()) {
					return resultSet.get(0);
				}
				
				logger.error("Failed to find water raw material. name: " + waterIngName);
				return null;
			}			
		});
	}
	
	private Double calculateVolumeFromChildren(Composite<CompoListDataItem> composite) throws FormulateException{				
		
		Double volume = 0d;
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){						
			
			Double value = null;
			
			if(!component.isLeaf()){
				// calculate children
				value = calculateVolumeFromChildren(component);
				component.getData().getExtraProperties().put(BeCPGModel.PROP_COMPOLIST_VOLUME, value);				
				
			}else{
				value = (Double)component.getData().getExtraProperties().get(BeCPGModel.PROP_COMPOLIST_VOLUME);
				value = value != null ? value : 0d;
			}			
			volume += value;
		}		
		return volume;
	}
	
}
