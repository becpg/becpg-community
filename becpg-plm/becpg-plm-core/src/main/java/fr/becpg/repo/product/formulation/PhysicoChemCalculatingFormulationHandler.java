/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * The Class PhysicoChemCalculatingVisitor.
 *
 * @author querephi
 */
public class PhysicoChemCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<PhysicoChemListDataItem> {

	private static Log logger = LogFactory.getLog(PhysicoChemCalculatingFormulationHandler.class);

	@Override
	protected Class<PhysicoChemListDataItem> getInstanceClass() {
		
		return PhysicoChemListDataItem.class;
	}
	
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {	
		logger.debug("Physico chemical calculating visitor");
		
		formulateSimpleList(formulatedProduct, formulatedProduct.getPhysicoChemList());

		return true;
	}

	@Override
	protected QName getDataListVisited(){
		
		return PLMModel.TYPE_PHYSICOCHEMLIST;
	}

	@Override
	protected boolean isCharactFormulated(SimpleListDataItem sl ){
		if(!super.isCharactFormulated(sl)){
			return false;
		}
		Boolean isFormulated = (Boolean)nodeService.getProperty(sl.getCharactNodeRef(), PLMModel.PROP_PHYSICO_CHEM_FORMULATED); 
		return isFormulated != null ? isFormulated.booleanValue() : false;
	}
	
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType){
		return getMandatoryCharactsFromList(formulatedProduct.getPhysicoChemList());
	}
}
