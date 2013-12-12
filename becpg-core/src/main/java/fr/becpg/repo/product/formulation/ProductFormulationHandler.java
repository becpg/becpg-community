package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.ProductService;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.variant.filters.VariantFilters;

@Service
public class ProductFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final String MESSAGE_MISSING_NET_WEIGHT = "message.formulate.missing.netWeight";
	private static final String MESSAGE_MISSING_QTY = "message.formulate.missing.qty";
	private static final String MESSAGE_MISSING_UNIT = "message.formulate.missing.unit";
	private static final String MESSAGE_MISSING_DENSITY = "message.formulate.missing.density";
	private static final String MESSAGE_WRONG_UNIT = "message.formulate.wrong.unit";
	private static final String MESSAGE_MISSING_TARE = "message.formulate.missing.tare";
	
	protected static Log logger = LogFactory.getLog(ProductFormulationHandler.class);
	
	private NodeService nodeService;
	
	private ProductService productService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}



	@Override
	public boolean process(ProductData productData) throws FormulateException {

		//First Reset 			
		if(productData.getCompoListView()!=null && productData.getCompoListView().getReqCtrlList()!=null){
			productData.getCompoListView().getReqCtrlList().clear();
		}		
		if(productData.getPackagingListView()!=null && productData.getPackagingListView().getReqCtrlList()!=null){
			productData.getPackagingListView().getReqCtrlList().clear();
		}		
		if(productData.getProcessListView()!=null && productData.getProcessListView().getReqCtrlList()!=null){
			productData.getProcessListView().getReqCtrlList().clear();
		}
		if(productData.getPackagingListView()!=null && productData.getPackagingListView().getReqCtrlList()!=null){
			productData.getPackagingListView().getReqCtrlList().clear();
		}
		if(productData.getProcessListView()!=null && productData.getProcessListView().getReqCtrlList()!=null){
			productData.getProcessListView().getReqCtrlList().clear();
		}
		
		
		if ((productData.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) ||
		(productData.hasPackagingListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) ||
		(productData.hasProcessListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT))) {		
			
			checkShouldFormulateComponents(productData);
			
			checkMissingProperties(productData);			
			
			// Continue
			return true;
		}
		// Skip formulation
		return false;

	}

	private void checkShouldFormulateComponents(ProductData formulatedProduct) throws FormulateException {
		for(CompoListDataItem c : formulatedProduct.getCompoList()){	
			if(productService.shouldFormulate(c.getProduct())){
				productService.formulate(c.getProduct());
			}
			
		}
		
		for(PackagingListDataItem p : formulatedProduct.getPackagingList()){	
			if(productService.shouldFormulate(p.getProduct())){
				productService.formulate(p.getProduct());
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	private void checkMissingProperties(ProductData formulatedProduct){
		
		checkFormulatedProduct(formulatedProduct);
		
		for(CompoListDataItem c : formulatedProduct.getCompoList()){	
			if(c.getCompoListUnit() == null){
				addMessingReq(formulatedProduct.getCompoListView().getReqCtrlList(), null, MESSAGE_WRONG_UNIT);
			}
			else{
				checkCompositionItem(formulatedProduct.getCompoListView().getReqCtrlList(), c.getProduct(), c);
			}			
		}
		
		for(PackagingListDataItem p : formulatedProduct.getPackagingList()){	
			if(p.getPackagingListUnit() == null){
				addMessingReq(formulatedProduct.getCompoListView().getReqCtrlList(), null, MESSAGE_WRONG_UNIT);
			}
			else{
				checkPackagingItem(formulatedProduct.getPackagingListView().getReqCtrlList(), p);
			}
			
		}
	}
	
	private void checkFormulatedProduct(ProductData formulatedProduct){
		
		NodeRef productNodeRef = formulatedProduct.getNodeRef();
		List<ReqCtrlListDataItem> reqCtrlList = null;
		if(BeCPGModel.TYPE_PACKAGINGKIT.isMatch(nodeService.getType(productNodeRef))){
			reqCtrlList = formulatedProduct.getPackagingListView().getReqCtrlList();
		}
		else{
			Double qty = FormulationHelper.getProductQty(productNodeRef, nodeService);
			if(qty == null || qty.equals(0d)){
				addMessingReq(formulatedProduct.getCompoListView().getReqCtrlList(), productNodeRef, MESSAGE_MISSING_QTY);
			}			
			checkNetWeight(formulatedProduct.getCompoListView().getReqCtrlList(), productNodeRef);
			reqCtrlList = formulatedProduct.getCompoListView().getReqCtrlList();
		}
		
		ProductUnit productUnit = FormulationHelper.getProductUnit(productNodeRef, nodeService);
		if(productUnit == null){	
			addMessingReq(reqCtrlList, productNodeRef, MESSAGE_MISSING_UNIT);
		}
	}
	
	private void checkCompositionItem(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef productNodeRef, CompoListDataItem c){
				
		if(!BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT.isMatch(nodeService.getType(productNodeRef))){
			
			ProductUnit productUnit = FormulationHelper.getProductUnit(productNodeRef, nodeService);
			if(c != null){
				
				if(FormulationHelper.isCompoUnitP(c.getCompoListUnit())){
					checkNetWeight(reqCtrlListDataItem, productNodeRef);
				}
				
				boolean shouldUseLiter = FormulationHelper.isProductUnitLiter(productUnit);
				boolean useLiter = FormulationHelper.isCompoUnitLiter(c.getCompoListUnit());
				
				if(shouldUseLiter && !useLiter || !shouldUseLiter && useLiter){
					addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT);
				}
				
				Double overrunPerc = (Double) nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_COMPOLIST_OVERRUN_PERC);
				if(FormulationHelper.isProductUnitLiter(productUnit) || overrunPerc != null){
					Double density = FormulationHelper.getDensity(productNodeRef, nodeService);
					if(density == null || density.equals(0d)){
						addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_DENSITY);
					}
				}				
			}
		}		
	}	
	
	private void checkNetWeight(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef productNodeRef){
		Double netWeight = FormulationHelper.getNetWeight(productNodeRef, nodeService,null);
		if(netWeight == null || netWeight.equals(0d)){								
			addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_NET_WEIGHT);
		}
	}
	
	private void checkPackagingItem(List<ReqCtrlListDataItem> reqCtrlListDataItem, PackagingListDataItem p){
		NodeRef productNodeRef = p.getProduct();
		ProductUnit productUnit = FormulationHelper.getProductUnit(productNodeRef, nodeService);
		if(productUnit == null){	
			addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_UNIT);
		}
		else{			
			if((p.getPackagingListUnit().equals(PackagingListUnit.kg) || p.getPackagingListUnit().equals(PackagingListUnit.g)) &&
					!FormulationHelper.isProductUnitKg(productUnit)){						
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT);
			}
			
			if((p.getPackagingListUnit().equals(PackagingListUnit.P) || p.getPackagingListUnit().equals(PackagingListUnit.PP)) &&
					!productUnit.equals(ProductUnit.P)){						
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT);
			}
			
			if((p.getPackagingListUnit().equals(PackagingListUnit.m) && !productUnit.equals(ProductUnit.m)) ||
					(p.getPackagingListUnit().equals(PackagingListUnit.m2) && !productUnit.equals(ProductUnit.m2))){						
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT);
			}
		}
		
		if(!nodeService.hasAspect(p.getProduct(), PackModel.ASPECT_PALLET)){
			Double tare = FormulationHelper.getTareInKg(productNodeRef, nodeService);
			if(tare == null){
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_TARE);
			}
		}		
	}
		
	private void addMessingReq(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef sourceNodeRef, String reqMsg){
		String message = I18NUtil.getMessage(reqMsg);
		ArrayList<NodeRef> sources = new ArrayList<NodeRef>(1);
		if(sourceNodeRef!=null){
			sources.add(sourceNodeRef);
		}			
		reqCtrlListDataItem.add(new ReqCtrlListDataItem(null,  RequirementType.Forbidden, message, sources));
	}
}
