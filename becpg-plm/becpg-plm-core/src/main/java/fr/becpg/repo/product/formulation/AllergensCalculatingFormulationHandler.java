/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.AllergenType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.model.VariantDataItem;

/**
 * The Class AllergensCalculatingVisitor.
 * 
 * @author querephi
 */
@Service
public class AllergensCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** The logger. */
	private static Log logger = LogFactory.getLog(AllergensCalculatingFormulationHandler.class);

	protected AlfrescoRepository<AllergenListDataItem> alfrescoRepository;
	
	protected NodeService nodeService;

	public void setAlfrescoRepository(AlfrescoRepository<AllergenListDataItem> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Start AllergensCalculatingVisitor");

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE) && !formulatedProduct.hasProcessListEl(EffectiveFilters.EFFECTIVE)) {
			logger.debug("no compo => no formulation");
			return true;
		}
		

		Set<NodeRef> visitedProducts = new HashSet<NodeRef>();
		List<AllergenListDataItem> retainNodes = new ArrayList<AllergenListDataItem>();
				
		if(formulatedProduct.getAllergenList()!=null){
			for(AllergenListDataItem a : formulatedProduct.getAllergenList()){
				if(a.getIsManual()!= null && a.getIsManual()){
					//manuel
					retainNodes.add(a);
				}
				else{
					//reset
					a.setVoluntary(false);
					a.setInVoluntary(false);
					a.getVoluntarySources().clear();
					a.getInVoluntarySources().clear();
				}
			}
		}
		
		
		
		// compoList
		for (CompoListDataItem compoItem : formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE)) {

			NodeRef part = compoItem.getProduct();
			if (!visitedProducts.contains(part)) {
				visitPart(compoItem, part, formulatedProduct.getAllergenList(), retainNodes);
				visitedProducts.add(part);
			}
		}

		
		
		// process
		if (formulatedProduct.hasProcessListEl(EffectiveFilters.EFFECTIVE)) {
			for (ProcessListDataItem processItem : formulatedProduct.getProcessList(EffectiveFilters.EFFECTIVE)) {

				NodeRef resource = processItem.getResource();
				if (resource != null && !visitedProducts.contains(resource)) {
					// TODO : resource is not a product => il faudrait déplacer
					// les méthodes loadAllergenList ailleurs que dans
					visitPart(processItem, resource, formulatedProduct.getAllergenList(), retainNodes);
					visitedProducts.add(resource);
				}
			}
		}
		
		
		formulatedProduct.getAllergenList().retainAll(retainNodes);
		
		
		//sort
		sort(formulatedProduct.getAllergenList());
		
		return true;
	}

	/**
	 * Visit part.
	 * 
	 * @param part
	 *            the part
	 * @param allergenMap
	 *            the allergen map
	 */
	private void visitPart(VariantDataItem variantDataItem, NodeRef part, List<AllergenListDataItem> allergenList, List<AllergenListDataItem> retainNodes) {

	
		List<AllergenListDataItem> allergenListDataItems = alfrescoRepository.loadDataList(part, BeCPGModel.TYPE_ALLERGENLIST, BeCPGModel.TYPE_ALLERGENLIST);

		
		for (AllergenListDataItem allergenListDataItem : allergenListDataItems) {
			
			
			// Look for alllergen
			NodeRef allergenNodeRef = allergenListDataItem.getAllergen();
			if (allergenNodeRef != null) {
				
				AllergenListDataItem newAllergenListDataItem = findAllergenListDataItem(allergenList, allergenNodeRef);

				if (newAllergenListDataItem == null) {
					newAllergenListDataItem = new AllergenListDataItem();
					newAllergenListDataItem.setAllergen(allergenNodeRef);
					allergenList.add(newAllergenListDataItem);
				}

				if(!retainNodes.contains(newAllergenListDataItem)){
					//Reset existing variants
					newAllergenListDataItem.setVariants(null);
					retainNodes.add(newAllergenListDataItem);
				}				

				if(newAllergenListDataItem.getIsManual() == null || !newAllergenListDataItem.getIsManual()){
					// Define voluntary presence
					if (allergenListDataItem.getVoluntary()) {
						newAllergenListDataItem.setVoluntary(true);
					}

					// Define involuntary
					if (allergenListDataItem.getInVoluntary()) {
						newAllergenListDataItem.setInVoluntary(true);
					}

					// Define voluntary, add it when : not present and vol
					if (allergenListDataItem.getVoluntary()) {
						// is it raw material ?
						if (allergenListDataItem.getVoluntarySources().isEmpty()) {
							if (!newAllergenListDataItem.getVoluntarySources().contains(part)) {
								newAllergenListDataItem.getVoluntarySources().add(part);
							}
						} else {
							for (NodeRef p : allergenListDataItem.getVoluntarySources()) {
								if (!newAllergenListDataItem.getVoluntarySources().contains(p)) {
									newAllergenListDataItem.getVoluntarySources().add(p);
								}
							}
						}
					}

					// Define invol, add it when : not present and inVol
					if (allergenListDataItem.getInVoluntary()) {
						// is it raw material ?
						if (allergenListDataItem.getInVoluntarySources().isEmpty()) {
							if (!newAllergenListDataItem.getInVoluntarySources().contains(part)) {
								newAllergenListDataItem.getInVoluntarySources().add(part);
							}
						} else {
							for (NodeRef p : allergenListDataItem.getInVoluntarySources()) {
								if (!newAllergenListDataItem.getInVoluntarySources().contains(p)) {
									newAllergenListDataItem.getInVoluntarySources().add(p);
								}
							}
						}
					}
					
					
					//Add variants if it adds an allergen
					if(variantDataItem.getVariants()!=null && (allergenListDataItem.getVoluntary() || allergenListDataItem.getInVoluntary())){
						if(newAllergenListDataItem.getVariants()==null){
							newAllergenListDataItem.setVariants(new ArrayList<NodeRef>());
						}
						
						for(NodeRef variant : variantDataItem.getVariants()){
							if(!newAllergenListDataItem.getVariants().contains(variant)){
								newAllergenListDataItem.getVariants().add(variant);
							}
						}						
					}
				}				
			}
		}
	}
	
	private AllergenListDataItem findAllergenListDataItem(List<AllergenListDataItem> allergenList, NodeRef allergenNodeRef){
		if(allergenNodeRef != null && allergenList!=null){
			for(AllergenListDataItem a : allergenList){
				if(allergenNodeRef.equals(a.getAllergen())){
					return a;
				}
			}
		}
		return null;		
	}
	
	/**
	 * Sort allergens by type and name.
	 *
	 * @param costList the cost list
	 * @return the list
	 */
	protected void sort(List<AllergenListDataItem> allergenList){
			
		Collections.sort(allergenList, new Comparator<AllergenListDataItem>(){
			
			final int BEFORE = -1;
    	    final int EQUAL = 0;
    	    final int AFTER = 1;	
        	
            @Override
			public int compare(AllergenListDataItem a1, AllergenListDataItem a2){
            	
            	int comp = EQUAL;
            	String type1 = (String)nodeService.getProperty(a1.getAllergen(), BeCPGModel.PROP_ALLERGEN_TYPE);
            	String type2 = (String)nodeService.getProperty(a2.getAllergen(), BeCPGModel.PROP_ALLERGEN_TYPE);
            	
            	if(type1 == null){
            		logger.warn("AllergenType is null for " + a1.getAllergen());            		
            	}
            	else if(type2 == null){
            		logger.warn("AllergenType is null for " + a2.getAllergen());            		
            	}
            	else{
            		
            		comp = type1.compareTo(type2);
            		
            		if(EQUAL == comp){
            			
            			String allergenName1 = (String)nodeService.getProperty(a1.getAllergen(), ContentModel.PROP_NAME);
                    	String allergenName2 = (String)nodeService.getProperty(a2.getAllergen(), ContentModel.PROP_NAME);
                    	
                    	comp = allergenName1.compareTo(allergenName2);  
            		}
            		else{
            			
            			if(AllergenType.Major.toString().equals(type1)){
            				comp = BEFORE;
            			}
            			else{
            				comp = AFTER;
            			}
            		}
            	}            	
            	
            	return comp;
            }

        });  
		
		int i=1;
		for(AllergenListDataItem al : allergenList){
			al.setSort(i);
			i++;
		}
	}
}
