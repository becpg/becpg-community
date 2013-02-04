/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.filters.EffectiveFilters;

/**
 * The Class AllergensCalculatingVisitor.
 * 
 * @author querephi
 */
public class AllergensCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** The logger. */
	private static Log logger = LogFactory.getLog(AllergensCalculatingFormulationHandler.class);

	protected AlfrescoRepository<ProductData> alfrescoRepository;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Start AllergensCalculatingVisitor");

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE)) {
			logger.debug("no compo => no formulation");
			return true;
		}

		Set<NodeRef> visitedProducts = new HashSet<NodeRef>();
		List<AllergenListDataItem> retainNodes = new ArrayList<AllergenListDataItem>();
		
		//manuel
		if(formulatedProduct.getAllergenList()!=null){
			for(AllergenListDataItem a : formulatedProduct.getAllergenList()){
				if(a.getIsManual()!= null && a.getIsManual()){
					retainNodes.add(a);
				}
			}
		}
		
		// compoList
		for (CompoListDataItem compoItem : formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE)) {

			NodeRef part = compoItem.getProduct();
			if (!visitedProducts.contains(part)) {
				visitPart(part, formulatedProduct.getAllergenList(), retainNodes);
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
					visitPart(resource, formulatedProduct.getAllergenList(), retainNodes);
					visitedProducts.add(resource);
				}
			}
		}

		logger.debug("###allergen size: " + formulatedProduct.getAllergenList().size() + " retainNodes: " + retainNodes.size());		
		formulatedProduct.getAllergenList().retainAll(retainNodes);
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
	private void visitPart(NodeRef part, List<AllergenListDataItem> allergenList, List<AllergenListDataItem> retainNodes) {

		ProductData productData = (ProductData) alfrescoRepository.findOne(part);

		if (productData.getAllergenList() == null) {
			return;
		}

		for (AllergenListDataItem allergenListDataItem : productData.getAllergenList()) {

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
						if (allergenListDataItem.getVoluntarySources().size() == 0) {
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
						if (allergenListDataItem.getInVoluntarySources().size() == 0) {
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
}
