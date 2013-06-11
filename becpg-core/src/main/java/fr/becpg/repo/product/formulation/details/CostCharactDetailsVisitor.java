package fr.becpg.repo.product.formulation.details;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.CostsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.filters.EffectiveFilters;

@Service
public class CostCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	private static Log logger = LogFactory.getLog(CostCharactDetailsVisitor.class);
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems) throws FormulateException {

		CharactDetails ret = new CharactDetails(extractCharacts(dataListItems));
		Double netWeight = FormulationHelper.getNetWeight(productData);

		/*
		 * Calculate cost details
		 */
		if (productData.hasCompoListEl(EffectiveFilters.EFFECTIVE)) {		
			Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(productData.getCompoList(EffectiveFilters.EFFECTIVE));		
			visitCompoListChildren(productData, composite, ret, CostsCalculatingFormulationHandler.DEFAULT_LOSS_RATIO, netWeight);
		}		

		/*
		 * Calculate the costs of the packaging
		 */
		if (productData.hasPackagingListEl(EffectiveFilters.EFFECTIVE)) {
			for (PackagingListDataItem packagingListDataItem : productData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
				Double qty = FormulationHelper.getQty(packagingListDataItem);
				visitPart(packagingListDataItem.getProduct(), ret, qty, netWeight);

			}
		}

		/*
		 * Calculate the costs of the processes
		 */
		if (productData.hasProcessListEl(EffectiveFilters.EFFECTIVE)) {
			for (ProcessListDataItem processListDataItem : productData.getProcessList(EffectiveFilters.EFFECTIVE)) {
				Double qty = FormulationHelper.getQty(productData, processListDataItem);
				visitPart(processListDataItem.getResource(), ret, qty, netWeight);
			}
		}

		return ret;
	}
	
	private void visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite, CharactDetails ret, Double parentLossRatio, Double netWeight) throws FormulateException{
		
		for(Composite<CompoListDataItem> component : composite.getChildren()){					

			if(!component.isLeaf()){
				
				// take in account the loss perc			
				Double lossPerc = component.getData().getLossPerc() != null ? component.getData().getLossPerc() : 0d;
				Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);			
				if(logger.isDebugEnabled()){
					logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
				}
				
				// calculate children				
				Composite<CompoListDataItem> c = (Composite<CompoListDataItem>)component;
				visitCompoListChildren(formulatedProduct, c, ret, newLossPerc, netWeight);							
			}
			else{
				CompoListDataItem compoListDataItem = component.getData();
				Double qty = FormulationHelper.getQtyWithLost(compoListDataItem, parentLossRatio);
				visitPart(compoListDataItem.getProduct(), ret, qty, netWeight);
			}			
		}
	}
}
