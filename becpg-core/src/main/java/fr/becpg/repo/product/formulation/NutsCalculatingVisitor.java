/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.NutGroup;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * The Class NutsCalculatingVisitor.
 *
 * @author querephi
 */
public class NutsCalculatingVisitor extends AbstractCalculatingVisitor implements ProductVisitor {
	
	/** The Constant UNIT_PER100G. */
	public static final String UNIT_PER100G = "/100g";
	
	/** The Constant UNIT_PER100ML. */
	public static final String UNIT_PER100ML = "/100mL";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(NutsCalculatingVisitor.class);
	
	@Override
	public ProductData visit(ProductData formulatedProduct) throws FormulateException{
		logger.debug("Nuts calculating visitor");
		
		Map<NodeRef, SimpleListDataItem> simpleListMap = getFormulatedList(formulatedProduct);

		if(simpleListMap != null){
		
			List<NutListDataItem> dataList = new ArrayList<NutListDataItem>();
			
			for(SimpleListDataItem sl : simpleListMap.values()){
				
				NutListDataItem nutListDataItem = new NutListDataItem(sl);
				nutListDataItem.setGroup((String)nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_NUTGROUP));								
				nutListDataItem.setUnit(calculateUnit(formulatedProduct.getUnit(), (String)nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_NUTUNIT)));
				dataList.add(nutListDataItem);
			}
		
			//sort		
			List<NutListDataItem> nutListSorted = sort(dataList);
			
			formulatedProduct.setNutList(nutListSorted);
		}						
						
		return formulatedProduct;
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
		
		if(ProductUnit.L.equals(productUnit)){
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
	private List<NutListDataItem> sort(List<NutListDataItem> nutList){
			
		Collections.sort(nutList, new Comparator<NutListDataItem>(){
        	
			final int BEFORE = -1;
    	    final int EQUAL = 0;
    	    final int AFTER = 1;	
			
            @Override
			public int compare(NutListDataItem n1, NutListDataItem n2){

            	if(n1!=null && n2!=null){
            		if(n1.equals(n2)){
            			return EQUAL;
            		}
            		
                int comp = AFTER;
            		 
            	  NutGroup o1NutGroup = NutGroup.parse(n1.getGroup());
            	  NutGroup o2NutGroup = NutGroup.parse(n2.getGroup());
           
            	  //Compare with enum declaration order level
            	  comp = o1NutGroup.compareTo(o2NutGroup);
            		
            		if(EQUAL == comp){
            			String nutName1 = (String)nodeService.getProperty(n1.getNut(), ContentModel.PROP_NAME);
                    	String nutName2 = (String)nodeService.getProperty(n2.getNut(), ContentModel.PROP_NAME);
                    	
                    	return nutName1.compareTo(nutName2);
            		}
            		
            		return comp;
            		
            	} else if(n1 == null && n2 !=null){
            		return AFTER;
            	} else if(n2 == null && n1!=null){
            		return BEFORE;
            	}	else {
            		return EQUAL;
            	}            	
            }
        });
        
        return nutList;
	}
	
	@Override
	protected QName getDataListVisited(){
		
		return BeCPGModel.TYPE_NUTLIST;
	}

	@Override
	protected boolean isCharactFormulated(NodeRef scNodeRef){
		return true;
	}
}
