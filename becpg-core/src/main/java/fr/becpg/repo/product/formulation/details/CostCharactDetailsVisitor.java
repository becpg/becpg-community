package fr.becpg.repo.product.formulation.details;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.data.hierarchicalList.AbstractComponent;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.CostsCalculatingVisitor;
import fr.becpg.repo.product.formulation.FormulateException;
import fr.becpg.repo.product.formulation.FormulationHelper;

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
		if (productData.getCompoList() != null && productData.getCompoList().size() > 0) {		
			Composite<CompoListDataItem> composite = CompoListDataItem.getHierarchicalCompoList(productData.getCompoList());		
			visitCompoListChildren(productData, composite, ret, CostsCalculatingVisitor.DEFAULT_LOSS_RATIO, netWeight);
		}		

		/*
		 * Calculate the costs of the packaging
		 */
		if (productData.getPackagingList() != null && productData.getPackagingList().size() > 0) {
			for (PackagingListDataItem packagingListDataItem : productData.getPackagingList()) {
				Double qty = FormulationHelper.getQty(packagingListDataItem);
				visitPart(packagingListDataItem.getProduct(), ret, qty, netWeight);

			}
		}

		/*
		 * Calculate the costs of the processes
		 */
		if (productData.getProcessList() != null && productData.getProcessList().size() > 0) {
			for (ProcessListDataItem processListDataItem : productData.getProcessList()) {
				Double qty = FormulationHelper.getQty(processListDataItem);
				visitPart(processListDataItem.getResource(), ret, qty, netWeight);
			}
		}

		return ret;
	}
	
	private void visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite, CharactDetails ret, Double parentLossRatio, Double netWeight) throws FormulateException{
		
		for(AbstractComponent<CompoListDataItem> component : composite.getChildren()){					

			if(component instanceof Composite){
				
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
				Double qty = FormulationHelper.getQtyWithLost(compoListDataItem, nodeService, parentLossRatio);
				visitPart(compoListDataItem.getProduct(), ret, qty, netWeight);
			}			
		}
	}
}
