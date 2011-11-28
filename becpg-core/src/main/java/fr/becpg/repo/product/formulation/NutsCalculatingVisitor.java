/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutGroup;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;

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
	
	protected static final String MSG_REQ_NOT_RESPECTED = "message.formulate.warning.requirement.nut";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(NutsCalculatingVisitor.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	private EntityListDAO entityListDAO;
	
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
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
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
	public ProductData visit(ProductData formulatedProduct) throws FormulateException{
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
		
		//Take in account net weight
		Float qty = (formulatedProduct.getUnit() != ProductUnit.P) ? formulatedProduct.getQty():QTY_FOR_PIECE; //unit => qty == 1
		Float density = (formulatedProduct.getDensity() != null) ? formulatedProduct.getDensity():DEFAULT_DENSITY; //density is null => 1
		Float netWeight = qty * density;
		for(NutListDataItem n : nutMap.values()){
			
			if(n.getValue() != null)
				n.setValue(n.getValue() / netWeight);			
		}
		
		// manual listItem
		List<NutListDataItem> nutList = getListToUpdate(formulatedProduct.getNodeRef(), nutMap);
				
		//sort		
		List<NutListDataItem> nutListSorted = sort(nutList); 

		formulatedProduct.setNutList(nutListSorted);		
		
		// check requirements
		formulatedProduct = checkReqCtrl(formulatedProduct);
				
		return formulatedProduct;
	}

	/**
	 * Visit part.
	 *
	 * @param formulatedProduct the formulated product
	 * @param compoListDataItem the compo list data item
	 * @param nutMap the nut map
	 * @throws FormulateException 
	 */
	private void visitPart(ProductData formulatedProduct, CompoListDataItem compoListDataItem,  Map<NodeRef, NutListDataItem> nutMap) throws FormulateException{
		
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
        	
            @Override
			public int compare(NutListDataItem n1, NutListDataItem n2){
            	
            	final int BEFORE = -1;
        	    final int EQUAL = 0;
        	    final int AFTER = 1;	    
        		int comparison = EQUAL;
        				
        		if(!n1.getGroup().equals(n2.getGroup())){
        			
        			NutGroup o1NutGroup = NutGroup.parse(n1.getGroup());
        			NutGroup o2NutGroup = NutGroup.parse(n2.getGroup());
        			
        			if(o1NutGroup == NutGroup.Group1){
        				comparison = BEFORE;
        			}
        			else if(o1NutGroup == NutGroup.Other){
        				comparison = AFTER;
        			}
        			else if(o2NutGroup == NutGroup.Group1){
        				comparison = AFTER;
        			}
        		}
        		else{
        			String nutName1 = (String)nodeService.getProperty(n1.getNut(), ContentModel.PROP_NAME);
                	String nutName2 = (String)nodeService.getProperty(n2.getNut(), ContentModel.PROP_NAME);
                	
        			comparison = nutName1.compareTo(nutName2);
        		}
        				
        		return comparison;                
            }

        });
        
        return nutList;
	}
	
	/**
	 * Calculate nuts to update
	 * @param productNodeRef
	 * @param costMap
	 * @return
	 */
	private List<NutListDataItem> getListToUpdate(NodeRef productNodeRef, Map<NodeRef, NutListDataItem> nutMap){
				
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
		
		if(listContainerNodeRef != null){
			
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_NUTLIST);
			
			if(listNodeRef != null){
				
				List<NodeRef> manualLinks = entityListDAO.getManualLinks(listNodeRef, BeCPGModel.TYPE_NUTLIST);
				
				for(NodeRef manualLink : manualLinks){
					
					NutListDataItem nutListDataItem = productDAO.loadNutListItem(manualLink);		    		
		    		nutMap.put(nutListDataItem.getNut(), nutListDataItem);
				}
			}
		}
		
		return new ArrayList<NutListDataItem>(nutMap.values());
	}
	
	private ProductData checkReqCtrl(ProductData formulatedProduct){
		
		for(NutListDataItem n : formulatedProduct.getNutList()){
			
			if(n.getValue() != null && (n.getMaxi() != null && n.getValue() > n.getMaxi() || n.getMini() != null && n.getValue() < n.getMini())){
				
				String msg = I18NUtil.getMessage(MSG_REQ_NOT_RESPECTED, 
						nodeService.getProperty(n.getNut(), ContentModel.PROP_NAME),
						n.getValue(),
						n.getMini(),
						n.getMaxi());
				formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, msg, null));
			}						
		}
		
		return formulatedProduct;
	}
}
