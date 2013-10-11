/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
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
import fr.becpg.repo.product.data.productList.NutGroup;
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
		
		//sort
		sort(formulatedProduct.getNutList());
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
	
	/**
	 * Sort nuts by group and name.
	 *
	 * @param nutList the nut list
	 * @return the list
	 */
	@Override
	protected void sort(List<NutListDataItem> nutList){
		
		Collections.sort(nutList, new Comparator<NutListDataItem>(){
        	
			final int BEFORE = -1;
    	    final int EQUAL = 0;
    	    final int AFTER = 1;	
			
			@Override
			public int compare(NutListDataItem n1, NutListDataItem n2) {

				if (n1 != null && n2 != null) {
					if (n1.equals(n2)) {
						return EQUAL;
					}

					int comp = AFTER;

					NutGroup o1NutGroup = NutGroup.parse(n1.getGroup());
					NutGroup o2NutGroup = NutGroup.parse(n2.getGroup());

					// Compare with enum declaration order level
					comp = o1NutGroup.compareTo(o2NutGroup);

					if (EQUAL == comp) {
						String nutName1 = (String) nodeService.getProperty(n1.getNut(), ContentModel.PROP_NAME);
						String nutName2 = (String) nodeService.getProperty(n2.getNut(), ContentModel.PROP_NAME);

						return nutName1.compareTo(nutName2);
					}

					return comp;

				} else if (n1 == null && n2 != null) {
					return AFTER;
				} else if (n2 == null && n1 != null) {
					return BEFORE;
				} else {
					return EQUAL;
				}
			}
        });  
		
		int i = 0;
		for(NutListDataItem n : nutList){
			n.setSort(i);
			i++;
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
