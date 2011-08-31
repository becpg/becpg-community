/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.sort.NutListDataItemDecorator;
import fr.becpg.repo.product.data.productList.sort.NutListSortComparator;

// TODO: Auto-generated Javadoc
/**
 * The Class NutsCalculatingVisitor.
 *
 * @author querephi
 */
public class NutsCalculatingVisitor implements ProductVisitor {
	
	/** The Constant QTY_FOR_PIECE. */
	public static final float QTY_FOR_PIECE = 1f;
	
	/** The Constant DEFAULT_DENSITY. */
	public static final float DEFAULT_DENSITY = 1f;
	
	/** The Constant UNIT_PER100G. */
	public static final String UNIT_PER100G = "/100g";
	
	/** The Constant UNIT_PER100ML. */
	public static final String UNIT_PER100ML = "/100mL";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(NutsCalculatingVisitor.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the product dao.
	 *
	 * @param productDAO the new product dao
	 */
	public void setProductDAO(ProductDAO productDAO){
		this.productDAO = productDAO;
	}

//	@Override
//	public FinishedProductData visit(FinishedProductData finishedProductData) {
//		visitProduct(finishedProductData);		
//	}
//
//	@Override
//	public RawMaterialData visit(RawMaterialData rawMaterialData) {
//		//Nothing to do
//	}
//
//	@Override
//	public PackagingMaterialData visit(PackagingMaterialData packagingMaterialData) {
//		//Nothing to do
//	}
//
//	@Override
//	public void visit(SemiFinishedProductData semiFinishedProductData) {
//		visitProduct(semiFinishedProductData);
//	}
//
//	@Override
//	public void visit(LocalSemiFinishedProduct localSemiFinishedProductData) {
//		//Nothing to do		
//	}
	
	/* (non-Javadoc)
 * @see fr.becpg.repo.product.ProductVisitor#visit(fr.becpg.repo.food.ProductData)
 */
@Override
	public ProductData visit(ProductData formulatedProduct){
		logger.debug("Nuts calculating visitor");
		
		// no compo => no formulation
		if(formulatedProduct.getCompoList() == null){			
			logger.debug("no compo => no formulation");
			return formulatedProduct;
		}
		
		// init nutMap with dbValues
		Map<NodeRef, NutListDataItem> nutMap = new HashMap<NodeRef, NutListDataItem>();
		if(formulatedProduct.getNutList() != null){			
			for(NutListDataItem nl : formulatedProduct.getNutList()){
				// reset value
				nl.setValue(0f);
				nutMap.put(nl.getNut(), nl);
			}
		}
		
		for(CompoListDataItem compoItem : formulatedProduct.getCompoList()){			
			visitPart(formulatedProduct, compoItem, nutMap);
		}
		
		List<NutListDataItem> nutList = new ArrayList<NutListDataItem>(nutMap.values());
		
		//Take in account net weight
		Float qty = (formulatedProduct.getUnit() != ProductUnit.P) ? formulatedProduct.getQty():QTY_FOR_PIECE; //unit => qty == 1
		Float density = (formulatedProduct.getDensity() != null) ? formulatedProduct.getDensity():DEFAULT_DENSITY; //density is null => 1
		Float netWeight = qty * density;
		for(NutListDataItem n : nutList){
			
			if(n.getValue() != null)
				n.setValue(n.getValue() / netWeight);			
		}
				
		//sort		
		List<NutListDataItem> nutListSorted = sort(nutList); 
		

		for(NutListDataItem n : nutList)
			logger.debug("unsorted list: " + (String)nodeService.getProperty(n.getNut(), ContentModel.PROP_NAME));
		
		for(NutListDataItem n : nutListSorted)
        	logger.debug((String)nodeService.getProperty(n.getNut(), ContentModel.PROP_NAME) + " - " + n.getGroup());
        
		
		formulatedProduct.setNutList(nutListSorted);		
		return formulatedProduct;
	}

	/**
	 * Visit part.
	 *
	 * @param formulatedProduct the formulated product
	 * @param compoListDataItem the compo list data item
	 * @param nutMap the nut map
	 */
	private void visitPart(ProductData formulatedProduct, CompoListDataItem compoListDataItem,  Map<NodeRef, NutListDataItem> nutMap){
		
		Collection<QName> dataLists = new ArrayList<QName>();		
		dataLists.add(BeCPGModel.TYPE_NUTLIST);			
		ProductData productData = productDAO.find(compoListDataItem.getProduct(), dataLists);
		
		if(productData.getNutList() == null){
			return;
		}
		
		for(NutListDataItem nutListDataItem : productData.getNutList()){			
			
			//Look for nut
			NodeRef nutNodeRef = nutListDataItem.getNut();
			NutListDataItem newNutListDataItem = nutMap.get(nutNodeRef);
			
			if(newNutListDataItem == null){
				newNutListDataItem =new NutListDataItem();
				newNutListDataItem.setNut(nutNodeRef);				
				newNutListDataItem.setGroup((String)nodeService.getProperty(nutNodeRef, BeCPGModel.PROP_NUTGROUP));
				
				
				String unit = calculateUnit(formulatedProduct.getUnit(), (String)nodeService.getProperty(nutNodeRef, BeCPGModel.PROP_NUTUNIT));				
				newNutListDataItem.setUnit(unit);
				nutMap.put(nutNodeRef, newNutListDataItem);
			}									
			
			//Calculate value
			Float newValue = newNutListDataItem.getValue();
			Float qty = FormulationHelper.getQty(compoListDataItem);
			Float density = (productData.getDensity() != null) ? productData.getDensity():DEFAULT_DENSITY; //density is null => 1
			Float value = nutListDataItem.getValue();
			
			if(qty != null && value != null){
				
				Float valueToAdd = density * qty * value;
				if(newValue != null){
					newValue += valueToAdd;
				}
				else{
					newValue = valueToAdd;
				}
				
				//logger.debug(String.format("calcul: '%s' - qty: '%f' -nutValue: '%f - valueToAdd: '%f'", productData.getName(), qty, value, valueToAdd));
			}			
			
			//logger.debug(String.format("productData: '%s' - Nut: '%s' - oldValue: '%f - newValue: '%f'", productData.getName(), (String)nodeService.getProperty(nutNodeRef, ContentModel.PROP_NAME), value, newValue));
			newNutListDataItem.setValue(newValue);
		}
	}
		
	/**
	 * S.
	 *
	 * @param nutList the nut list
	 * @return the list
	 */
	private List<NutListDataItem> sort(List<NutListDataItem> nutList){
		
		List<NutListDataItemDecorator> nutListDecorated = new ArrayList<NutListDataItemDecorator>();
		for(NutListDataItem nutListDataItem : nutList){
			NutListDataItemDecorator d = new NutListDataItemDecorator();
			d.setNutListDataItem(nutListDataItem);
			d.setNutName((String)nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME));			
			nutListDecorated.add(d);
		}
        Collections.sort(nutListDecorated, new NutListSortComparator());
       
        List<NutListDataItem> nutListSorted = new ArrayList<NutListDataItem>();
        for(NutListDataItemDecorator n : nutListDecorated)
        	nutListSorted.add(n.getNutListDataItem());
                
        return nutListSorted;
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
}
