package fr.becpg.repo.product.formulation;

import java.util.Date;
import java.util.List;

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
import fr.becpg.repo.entity.version.EntityVersionServiceImpl;
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

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");		
		
		// no compo => no formulation
		if(!formulatedProduct.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)){			
			logger.debug("no compo => no formulation");
			return true;
		}
		
		//Take in account net weight
		Double netWeight;
		if(formulatedProduct.getUnit() == ProductUnit.P){
			netWeight = formulatedProduct.getDensity();
		}
		else{
			Double qty = FormulationHelper.getQty(formulatedProduct);
			Double density = FormulationHelper.getDensity(formulatedProduct);
			netWeight = qty * density;
		}		
					
		// calculate on every item
		Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL));
		visitQtyChildren(formulatedProduct, netWeight, netWeight, compositeAll);
		visitYieldChildren(formulatedProduct, netWeight, netWeight, compositeAll);
		
		// Yield
		Double qtyUsed = calculateQtyUsedBeforeProcess(CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)));
		if(qtyUsed != null && qtyUsed != 0d){
			formulatedProduct.setYield(100 * netWeight / qtyUsed);
		}			
		
		return true;
	}
	
	private void visitQtyChildren(ProductData formulatedProduct, Double parentQty, Double qtyAfterProcess, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					
			
			// qty and sub formula qty are defined and not equal to 0
			if(parentQty != null && qtyAfterProcess != null && !qtyAfterProcess.equals(0d)){
				
				Double qtySubFormula = component.getData().getQtySubFormula();
				logger.debug("qtySubFormula: " + qtySubFormula + " qtyAfterProcess: " + qtyAfterProcess + " parentQty: " + parentQty);
				if(qtySubFormula != null){
					
					Double qty = null;
					// take in account percentage
					if(component.getData().getCompoListUnit() != null && 
							component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){
						qty = qtySubFormula * qtyAfterProcess / 100;
					}
					else{
											
						Double qtyComponent = qtySubFormula * parentQty / qtyAfterProcess;
						Double yield = FormulationHelper.DEFAULT_YIELD;
						
						// Yield that is defined on component
						if(component.isLeaf() && component.getData().getYieldPerc() != null){
							
							yield = component.getData().getYieldPerc();							
							Double qtyWater = qtyComponent * (1 - FormulationHelper.DEFAULT_YIELD / yield);
							logger.info("###qtyWater: " + qtyWater);							
							CompoListDataItem waterCompoListDataItem = new CompoListDataItem(null, 
													composite.getData(), 
													qtyWater, null, null, 
													component.getData().getCompoListUnit(), 
													component.getData().getLossPerc(), 
													DeclarationType.Declare, 
													getWaterRawMaterial());
							
							waterCompoListDataItem.setTransient(true);
							formulatedProduct.getCompoListView().getCompoList().add(waterCompoListDataItem);
						}
						qty = qtyComponent * 100 / yield;
					}					
					
					component.getData().setQty(qty);									
				}
			}	
			
			// calculate children
			if(!component.isLeaf()){
				
				// take in account percentage
				if(component.getData().getCompoListUnit() != null && 
						component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){	
					
					visitQtyChildren(formulatedProduct, parentQty, qtyAfterProcess, component);
					
					// no yield but calculate % of composite
					Double compositePerc = 0d;
					for(Composite<CompoListDataItem> child : component.getChildren()){	
						compositePerc += child.getData().getQtySubFormula();
					}
					component.getData().setQtySubFormula(compositePerc);
					component.getData().setQty(compositePerc * parentQty / 100);
				}
				else{			
					
					// has been modified from UI ?
					// then update qtySubFormula of components
					Date compositeModifiedDate = (Date)nodeService.getProperty(component.getData().getNodeRef(), ContentModel.PROP_MODIFIED);
					boolean compositeYieldModified = true;
					for(Composite<CompoListDataItem> child : component.getChildren()){
						Date childModifiedDate = (Date)nodeService.getProperty(child.getData().getNodeRef(), ContentModel.PROP_MODIFIED);
						logger.info("###compositeModifiedDate: " + compositeModifiedDate);
						logger.info("###childModifiedDate: " + childModifiedDate);
						if(compositeModifiedDate != null && compositeModifiedDate.before(childModifiedDate)){
							compositeYieldModified = false;
						}
					}
					Double dbYieldPerc = component.getData().getYieldPerc();
					Double calcultedYieldPerc = calculateYield(component);
					if(compositeYieldModified && dbYieldPerc != null && dbYieldPerc != 0d && !calcultedYieldPerc.equals(dbYieldPerc)){
					
						Double ratio =  calcultedYieldPerc / dbYieldPerc;
						for(Composite<CompoListDataItem> child : component.getChildren()){
							Double dbQtySubFormula = child.getData().getQtySubFormula();
							Double qtySubFormula = dbQtySubFormula != null ? dbQtySubFormula * ratio : null;
							logger.debug("Yield has been modified from UI, dbQtySubFormula: " + dbQtySubFormula + " qtySubFormula: " + qtySubFormula);
							child.getData().setQtySubFormula(qtySubFormula);
						}
					}
					
					Double afterProcess = component.getData().getQtyAfterProcess() != null ?component.getData().getQtyAfterProcess() : component.getData().getQtySubFormula();
					visitQtyChildren(formulatedProduct, component.getData().getQty(), afterProcess,component);					
				}				
			}			
		}
	}

	private void visitYieldChildren(ProductData formulatedProduct, Double parentQty, Double qtyAfterProcess, Composite<CompoListDataItem> composite) throws FormulateException{				
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					
			
			// calculate children
			if(!component.isLeaf()){
				
				// take in account percentage
				if(component.getData().getCompoListUnit() != null && 
						component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){	
					
					visitYieldChildren(formulatedProduct, parentQty, qtyAfterProcess, component);
					component.getData().setYieldPerc(null);
				}
				else{
					Double afterProcess = component.getData().getQtyAfterProcess() != null ?component.getData().getQtyAfterProcess() : component.getData().getQtySubFormula();
					visitYieldChildren(formulatedProduct, component.getData().getQty(), afterProcess,component);
					
					// Yield				
					component.getData().setYieldPerc(calculateYield(component));
				}				
			}			
		}		
	}
	
//	private void visitChildren(ProductData formulatedProduct, Double parentQty, Double qtyAfterProcess, Composite<CompoListDataItem> composite) throws FormulateException{				
//		
//		// we cannot modify composite.getChildren()
//		Map<Composite<CompoListDataItem>, List<CompoListDataItem>> compositesWithWater = new HashMap<Composite<CompoListDataItem>, List<CompoListDataItem>>();
//		
//		for(Composite<CompoListDataItem> component : composite.getChildren()){					
//			
//			// qty and sub formula qty are defined and not equal to 0
//			if(parentQty != null && qtyAfterProcess != null && !qtyAfterProcess.equals(0d)){
//				
//				Double qtySubFormula = component.getData().getQtySubFormula();
//				logger.debug("qtySubFormula: " + qtySubFormula + " qtyAfterProcess: " + qtyAfterProcess + " parentQty: " + parentQty);
//				if(qtySubFormula != null){
//					
//					Double qty = null;
//					// take in account percentage
//					if(component.getData().getCompoListUnit() != null && 
//							component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){
//						qty = qtySubFormula * qtyAfterProcess / 100;
//					}
//					else{
//											
//						Double qtyComponent = qtySubFormula * parentQty / qtyAfterProcess;
//						Double yield = FormulationHelper.DEFAULT_YIELD;
//						
//						// Yield that is defined on component
//						if(component.isLeaf() && component.getData().getYieldPerc() != null){
//							
//							yield = component.getData().getYieldPerc();							
//							Double qtyWater = qtyComponent * (1 - FormulationHelper.DEFAULT_YIELD / yield);
//							logger.info("###qtyWater: " + qtyWater);							
//							CompoListDataItem waterCompoListDataItem = new CompoListDataItem(null, 
//													composite.getData(), 
//													qtyWater, null, null, 
//													component.getData().getCompoListUnit(), 
//													component.getData().getLossPerc(), 
//													DeclarationType.Declare, 
//													getWaterRawMaterial());
//							
//							waterCompoListDataItem.setTransient(true);
//							formulatedProduct.getCompoListView().getCompoList().add(waterCompoListDataItem);
//							
////							List<CompoListDataItem> compoListDataItems = compositesWithWater.get(composite);
////							if(compoListDataItems == null){
////								compoListDataItems = new ArrayList<CompoListDataItem>();
////								compositesWithWater.put(composite, compoListDataItems);
////							}							
////							compoListDataItems.add(waterCompoListDataItem);
//						}
//						qty = qtyComponent * 100 / yield;
//					}					
//					
//					component.getData().setQty(qty);									
//				}
//			}	
//			
//			// calculate children
//			if(!component.isLeaf()){
//				
//				// take in account percentage
//				if(component.getData().getCompoListUnit() != null && 
//						component.getData().getCompoListUnit().equals(CompoListUnit.Perc)){	
//					
//					visitChildren(formulatedProduct, parentQty, qtyAfterProcess, component);
//					
//					// no yield but calculate % of composite
//					Double compositePerc = 0d;
//					for(Composite<CompoListDataItem> child : component.getChildren()){	
//						compositePerc += child.getData().getQtySubFormula();
//					}
//					component.getData().setQtySubFormula(compositePerc);
//					component.getData().setQty(compositePerc * parentQty / 100);
//					component.getData().setYieldPerc(null);
//				}
//				else{
//
//					
//					// has been modified from UI ?
//					// then update qtySubFormula of components
//					Double dbYieldPerc = component.getData().getYieldPerc();
//					Double calcultedYieldPerc = calculateYield(component);
//					if(dbYieldPerc != null && dbYieldPerc != 0d && !calcultedYieldPerc.equals(dbYieldPerc)){
//												
//						Double ratio =  calcultedYieldPerc / dbYieldPerc;
//						for(Composite<CompoListDataItem> child : component.getChildren()){
//							Double dbQtySubFormula = child.getData().getQtySubFormula();
//							Double qtySubFormula = dbQtySubFormula != null ? dbQtySubFormula * ratio : null;
//							logger.debug("Yield has been modified from UI, dbQtySubFormula: " + dbQtySubFormula + " qtySubFormula: " + qtySubFormula);
//							child.getData().setQtySubFormula(qtySubFormula);
//						}
//					}
//														
//					Double afterProcess = component.getData().getQtyAfterProcess() != null ?component.getData().getQtyAfterProcess() : component.getData().getQtySubFormula();
//					visitChildren(formulatedProduct, component.getData().getQty(), afterProcess,component);
//					
//					// Yield				
//					component.getData().setYieldPerc(calculateYield(component));
//				}				
//			}			
//		}
//		
////		for(Map.Entry<Composite<CompoListDataItem>, List<CompoListDataItem>> kv : compositesWithWater.entrySet()){
////			for(CompoListDataItem c : kv.getValue()){
////				logger.info("### add compoList item for water: " + c);
////				kv.getKey().addChild(new Composite<CompoListDataItem>(c));
////			}			
////		}
//	}
	
	private Double calculateYield(Composite<CompoListDataItem> composite) throws FormulateException{
		
		Double yieldPerc = 100d;
		
		// qty Used in the sub formula
		Double qtyUsed = 0d;				
		for(Composite<CompoListDataItem> component : composite.getChildren()){
			
			Double qty = FormulationHelper.getQtySubFormula(component.getData(), nodeService);
			if(qty != null){
				qtyUsed += qty;
			}
		}
		
		// qty after process
		Double qtyAfterProcess = FormulationHelper.getQtyAfterProcess(composite.getData(), nodeService);
		logger.debug("qtyAfterProcess: " + qtyAfterProcess + " - qtyUsed: " + qtyUsed);
		if(qtyAfterProcess != 0 && qtyUsed != 0){
			yieldPerc = qtyAfterProcess / qtyUsed * 100;
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
				qty += FormulationHelper.getQty(component.getData(), nodeService);
			}
		}
		
		return qty;
	}
	
	private NodeRef getWaterRawMaterial(){
		
		return beCPGCacheService.getFromCache(EntityVersionServiceImpl.class.getName(), KEY_RM_WATER , new BeCPGCacheDataProviderCallBack<NodeRef>() {

			@Override
			public NodeRef getData() {
								
				List<NodeRef> resultSet = null;
				String waterIngName = I18NUtil.getMessage(MESSAGE_RM_WATER);
				
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
	
}
