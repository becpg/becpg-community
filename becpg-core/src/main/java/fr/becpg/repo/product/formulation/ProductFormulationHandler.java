package fr.becpg.repo.product.formulation;

import java.util.ArrayList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
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
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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
		
		if ((productData.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) ||
		(productData.hasPackagingListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) ||
		(productData.hasProcessListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT))) {			
			
			checkMissingProperties(productData);			
			
			// Continue
			return true;
		}
		// Skip formulation
		return false;

	}

	@SuppressWarnings("unchecked")
	private void checkMissingProperties(ProductData formulatedProduct){
		
		checkFormulatedProduct(formulatedProduct);
		
		for(CompoListDataItem c : formulatedProduct.getCompoList()){	
			if(c.getCompoListUnit() == null){
				addMessingReq(formulatedProduct, null, MESSAGE_WRONG_UNIT);
			}
			else{
				checkCompositionItem(formulatedProduct, c.getProduct(), c);
			}			
		}
		
		for(PackagingListDataItem p : formulatedProduct.getPackagingList()){	
			if(p.getPackagingListUnit() == null){
				addMessingReq(formulatedProduct, null, MESSAGE_WRONG_UNIT);
			}
			else{
				checkPackagingItem(formulatedProduct, p);
			}
			
		}
	}
	
	private void checkFormulatedProduct(ProductData formulatedProduct){
		
		Double qty = FormulationHelper.getProductQty(formulatedProduct.getNodeRef(), nodeService);
		if(qty == null || qty.equals(0d)){
			addMessingReq(formulatedProduct, formulatedProduct.getNodeRef(), MESSAGE_MISSING_QTY);
		}
		
		checkCompositionItem(formulatedProduct, formulatedProduct.getNodeRef(), null);
	}
	
	private void checkCompositionItem(ProductData formulatedProduct, NodeRef productNodeRef, CompoListDataItem c){
				
		if(!BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT.isMatch(nodeService.getType(productNodeRef))){
			
			ProductUnit productUnit = FormulationHelper.getProductUnit(productNodeRef, nodeService);
			if(productUnit == null){	
				addMessingReq(formulatedProduct, productNodeRef, MESSAGE_MISSING_UNIT);
			}			
			
			if(c != null){
				
				if(FormulationHelper.isCompoUnitP(c.getCompoListUnit())){
					Double netWeight = FormulationHelper.getNetWeight(productNodeRef, nodeService);
					if(netWeight == null || netWeight.equals(0d)){								
						addMessingReq(formulatedProduct, productNodeRef, MESSAGE_MISSING_NET_WEIGHT);
					}
				}
				
				boolean shouldUseLiter = FormulationHelper.isProductUnitLiter(productUnit);
				boolean useLiter = FormulationHelper.isCompoUnitLiter(c.getCompoListUnit());
				
				if(shouldUseLiter && !useLiter || !shouldUseLiter && useLiter){
					addMessingReq(formulatedProduct, productNodeRef, MESSAGE_WRONG_UNIT);
				}
				
				Double overrunPerc = (Double) nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_COMPOLIST_OVERRUN_PERC);
				if(FormulationHelper.isProductUnitLiter(productUnit) || overrunPerc != null){
					Double density = FormulationHelper.getDensity(productNodeRef, nodeService);
					if(density == null || density.equals(0d)){
						addMessingReq(formulatedProduct, productNodeRef, MESSAGE_MISSING_DENSITY);
					}
				}				
			}
		}		
	}	
	
	private void checkPackagingItem(ProductData formulatedProduct, PackagingListDataItem p){
		NodeRef productNodeRef = p.getProduct();
		ProductUnit productUnit = FormulationHelper.getProductUnit(productNodeRef, nodeService);
		if(productUnit == null){	
			addMessingReq(formulatedProduct, productNodeRef, MESSAGE_MISSING_UNIT);
		}
		else{			
			if((p.getPackagingListUnit().equals(PackagingListUnit.kg) || p.getPackagingListUnit().equals(PackagingListUnit.g)) &&
					!FormulationHelper.isProductUnitKg(productUnit)){						
				addMessingReq(formulatedProduct, productNodeRef, MESSAGE_WRONG_UNIT);
			}
			
			if((p.getPackagingListUnit().equals(PackagingListUnit.P) || p.getPackagingListUnit().equals(PackagingListUnit.PP)) &&
					!productUnit.equals(ProductUnit.P)){						
				addMessingReq(formulatedProduct, productNodeRef, MESSAGE_WRONG_UNIT);
			}
			
			if((p.getPackagingListUnit().equals(PackagingListUnit.m) && !productUnit.equals(ProductUnit.m)) ||
					(p.getPackagingListUnit().equals(PackagingListUnit.m2) && !productUnit.equals(ProductUnit.m2))){						
				addMessingReq(formulatedProduct, productNodeRef, MESSAGE_WRONG_UNIT);
			}
		}
		
		Double tare = FormulationHelper.getTareInKg(productNodeRef, nodeService);
		if(tare == null){
			addMessingReq(formulatedProduct, productNodeRef, MESSAGE_MISSING_TARE);
		}
	}
		
	private void addMessingReq(ProductData formulatedProduct, NodeRef sourceNodeRef, String reqMsg){
		String message = I18NUtil.getMessage(reqMsg);
		ArrayList<NodeRef> sources = new ArrayList<NodeRef>(1);
		if(sourceNodeRef!=null){
			sources.add(sourceNodeRef);
		}			
		formulatedProduct.getCompoListView().getReqCtrlList().add(new ReqCtrlListDataItem(null,  RequirementType.Forbidden, message, sources));
	}
}
