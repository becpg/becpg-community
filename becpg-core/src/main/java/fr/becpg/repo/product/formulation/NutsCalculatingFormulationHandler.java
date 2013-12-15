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
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.NutListDataItem;

/**
 * The Class NutsCalculatingVisitor.
 *
 * @author querephi
 */
@Service
public class NutsCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<NutListDataItem> {
	
	/** The Constant UNIT_PER100G. */
	public static final String UNIT_PER100G = "/100g";
	
	/** The Constant UNIT_PER100ML. */
	public static final String UNIT_PER100ML = "/100mL";
	
	public static final String NUT_FORMULATED = I18NUtil.getMessage("message.formulate.nut.formulated");
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(NutsCalculatingFormulationHandler.class);
	
	@Override
	protected Class<NutListDataItem> getInstanceClass() {
		return NutListDataItem.class;
	}
	
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("Nuts calculating visitor");
		
		formulateSimpleList(formulatedProduct, formulatedProduct.getNutList());

		if(formulatedProduct.getNutList() != null){
		
			for(NutListDataItem n : formulatedProduct.getNutList()){
				
				n.setGroup((String)nodeService.getProperty(n.getNut(), BeCPGModel.PROP_NUTGROUP));				
				n.setUnit(calculateUnit(formulatedProduct.getUnit(), (String)nodeService.getProperty(n.getNut(), BeCPGModel.PROP_NUTUNIT)));
				
				if(formulatedProduct.getServingSize() != null && n.getValue() != null){
					double valuePerserving = n.getValue() * formulatedProduct.getServingSize() / 100;
					n.setValuePerServing(valuePerserving);
					Double gda = (Double)nodeService.getProperty(n.getNut(), BeCPGModel.PROP_NUTGDA);
					if(gda != null && gda != 0d){
						n.setGdaPerc(100 * n.getValuePerServing()/gda);
					}
				}
				else{
					n.setValuePerServing(null);
					n.setGdaPerc(null);
				}
				
				if(isCharactFormulated(n)){
					n.setMethod(NUT_FORMULATED);
				}
			}		
		
		}
		
		return true;
	}

	/**
	 * Calculate the nutListUnit
	 * @param productUnit
	 * @param nutUnit
	 * @return
	 */
	public static String calculateUnit(ProductUnit productUnit, String nutUnit){
		
		return nutUnit += calculateSuffixUnit(productUnit);
	}
	
	/**
	 * Calculate the suffix of nutListUnit
	 * @param productUnit
	 * @return
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit){
		
		if(ProductUnit.L.equals(productUnit) || ProductUnit.mL.equals(productUnit)){
			return UNIT_PER100ML;
		}				
		else{
			return UNIT_PER100G;
		}		
	}	
	
	@Override
	protected QName getDataListVisited(){
		
		return BeCPGModel.TYPE_NUTLIST;
	}

	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType){		
		return getMandatoryCharactsFromList(formulatedProduct.getNutList());
	}
}
