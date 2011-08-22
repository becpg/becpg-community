/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

// TODO: Auto-generated Javadoc
/**
 * The Class AllergensCalculatingVisitor.
 *
 * @author querephi
 */
public class AllergensCalculatingVisitor implements ProductVisitor {
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AllergensCalculatingVisitor.class);
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	/**
	 * Sets the product dao.
	 *
	 * @param productDAO the new product dao
	 */
	public void setProductDAO(ProductDAO productDAO){
		this.productDAO = productDAO;
	}	

//	@Override
//	public RawMaterialData visit(RawMaterialData rawMaterialData) {		
//		return (RawMaterialData)visitProduct(rawMaterialData);		
//	}
//
//	@Override
//	public PackagingMaterialData visit(PackagingMaterialData packagingMaterialData) {		
//		//Nothing to do
//		return packagingMaterialData;
//	}
//
//	@Override
//	public SemiFinishedProductData visit(SemiFinishedProductData semiFinishedProductData) {		
//		return (SemiFinishedProductData)visitProduct(semiFinishedProductData);		
//	}
//
//	@Override
//	public FinishedProductData visit(FinishedProductData finishedProductData) {
//		//Nothing to do		
//		return finishedProductData;
//	}
//
//	@Override
//	public LocalSemiFinishedProduct visit(LocalSemiFinishedProduct localSemiFinishedProductData) {		
//		//Nothing to do
//		return localSemiFinishedProductData;
//	}	
	
	/* (non-Javadoc)
 * @see fr.becpg.repo.product.ProductVisitor#visit(fr.becpg.repo.food.ProductData)
 */
@Override
	public ProductData visit(ProductData formulatedProduct){
		
		logger.debug("Start AllergensCalculatingVisitor");
		
		// no compo => no formulation
		if(formulatedProduct.getCompoList() == null){			
			logger.debug("no compo => no formulation");
			return formulatedProduct;
		}
		
		Set<NodeRef> visitedProducts = new HashSet<NodeRef>();
		Map<NodeRef, AllergenListDataItem> allergenMap = new HashMap<NodeRef, AllergenListDataItem>();
		
		for(CompoListDataItem compoItem : formulatedProduct.getCompoList()){
					
			NodeRef part = compoItem.getProduct();
			if(!visitedProducts.contains(part)){				
				logger.debug("visitPart: " + part);				
				visitPart(part, allergenMap);
				visitedProducts.add(part);
			}				
		}
		
		List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>(allergenMap.values());		
		formulatedProduct.setAllergenList(allergenList);
		logger.debug("product Visited, allergens size: " +allergenList.size());
		return formulatedProduct;
	}

	/**
	 * Visit part.
	 *
	 * @param part the part
	 * @param allergenMap the allergen map
	 */
	private void visitPart(NodeRef part, Map<NodeRef, AllergenListDataItem> allergenMap){		
		
		Collection<QName> dataLists = new ArrayList<QName>();		
		dataLists.add(BeCPGModel.TYPE_ALLERGENLIST);
		ProductData productData = productDAO.find(part, dataLists);
		
		if(productData.getAllergenList() == null){
			return;
		}
		
		for(AllergenListDataItem allergenListDataItem : productData.getAllergenList()){			
			
			//Look for alllergen
			NodeRef allergenNodeRef = allergenListDataItem.getAllergen();
			AllergenListDataItem newAllergenListDataItem = allergenMap.get(allergenNodeRef);
			
			if(newAllergenListDataItem == null){
				newAllergenListDataItem =new AllergenListDataItem();
				newAllergenListDataItem.setAllergen(allergenNodeRef);
				allergenMap.put(allergenNodeRef, newAllergenListDataItem);
			}									

			//Define voluntary presence
			if(allergenListDataItem.getVoluntary()){
				newAllergenListDataItem.setVoluntary(true);
			}
			
			//Define involuntary
			if(allergenListDataItem.getInVoluntary()){
				newAllergenListDataItem.setInVoluntary(true);
			}
			
			//Define voluntary, add it when : not present and vol
			if(allergenListDataItem.getVoluntary()){
				//is it raw material ?
				if(allergenListDataItem.getVoluntarySources().size() == 0){
					if(!newAllergenListDataItem.getVoluntarySources().contains(part)){
						newAllergenListDataItem.getVoluntarySources().add(part);
					}
				}
				else{
					for(NodeRef p : allergenListDataItem.getVoluntarySources()){
						if(!newAllergenListDataItem.getVoluntarySources().contains(p)){
							newAllergenListDataItem.getVoluntarySources().add(p);
						}
					}
				}
			}
			
			//Define invol, add it when : not present and inVol
			if(allergenListDataItem.getInVoluntary()){
				//is it raw material ?
				if(allergenListDataItem.getInVoluntarySources().size() == 0){
					if(!newAllergenListDataItem.getInVoluntarySources().contains(part)){
						newAllergenListDataItem.getInVoluntarySources().add(part);
					}
				}
				else{
					for(NodeRef p : allergenListDataItem.getInVoluntarySources()){
						if(!newAllergenListDataItem.getInVoluntarySources().contains(p)){
							newAllergenListDataItem.getInVoluntarySources().add(p);
						}
					}
				}				
			}
		}
	}
		
}
