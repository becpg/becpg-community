/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * The Class PhysicoChemCalculatingVisitor.
 *
 * @author querephi
 */
public class PhysicoChemCalculatingVisitor extends AbstractProductFormulationHandler {

	private static Log logger = LogFactory.getLog(PhysicoChemCalculatingVisitor.class);

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {	
		logger.debug("Physico chemical calculating visitor");
		
		Map<NodeRef, SimpleListDataItem> simpleListMap = getFormulatedList(formulatedProduct);

		if(simpleListMap != null){
			
			List<PhysicoChemListDataItem> dataList = new ArrayList<PhysicoChemListDataItem>();
			
			for(SimpleListDataItem sl : simpleListMap.values()){
				dataList.add(new PhysicoChemListDataItem(sl));
			}
			
			formulatedProduct.setPhysicoChemList(dataList);
		}						

		return true;
	}

	@Override
	protected QName getDataListVisited(){
		
		return BeCPGModel.TYPE_PHYSICOCHEMLIST;
	}

	@Override
	protected boolean isCharactFormulated(SimpleListDataItem sl ){
		if(!super.isCharactFormulated(sl)){
			return false;
		}
		Boolean isFormulated = (Boolean)nodeService.getProperty(sl.getNodeRef(), BeCPGModel.PROP_PHYSICO_CHEM_FORMULATED); 
		return isFormulated != null ? isFormulated.booleanValue() : false;
	}
}
