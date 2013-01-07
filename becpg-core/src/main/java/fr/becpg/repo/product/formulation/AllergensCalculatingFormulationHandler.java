/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.filters.EffectiveFilters;

/**
 * The Class AllergensCalculatingVisitor.
 * 
 * @author querephi
 */
public class AllergensCalculatingFormulationHandler extends AbstractProductFormulationHandler {

	/** The logger. */
	private static Log logger = LogFactory.getLog(AllergensCalculatingFormulationHandler.class);



	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Start AllergensCalculatingVisitor");

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE)) {
			logger.debug("no compo => no formulation");
			return true;
		}

		Set<NodeRef> visitedProducts = new HashSet<NodeRef>();
		Map<NodeRef, AllergenListDataItem> allergenMap = new HashMap<NodeRef, AllergenListDataItem>();

		// compoList
			for (CompoListDataItem compoItem : formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE)) {

				NodeRef part = compoItem.getProduct();
				if (!visitedProducts.contains(part)) {
					visitPart(part, allergenMap);
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
					visitPart(resource, allergenMap);
					visitedProducts.add(resource);
				}
			}
		}

		List<AllergenListDataItem> allergenList = getListToUpdate(formulatedProduct.getNodeRef(), allergenMap);
		formulatedProduct.setAllergenList(allergenList);
		logger.debug("product Visited, allergens size: " + allergenList.size());
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
	private void visitPart(NodeRef part, Map<NodeRef, AllergenListDataItem> allergenMap) {

		ProductData productData = (ProductData) alfrescoRepository.findOne(part);

		if (productData.getAllergenList() == null) {
			return;
		}

		for (AllergenListDataItem allergenListDataItem : productData.getAllergenList()) {

			// Look for alllergen
			NodeRef allergenNodeRef = allergenListDataItem.getAllergen();
			if (allergenNodeRef != null) {
				AllergenListDataItem newAllergenListDataItem = allergenMap.get(allergenNodeRef);

				if (newAllergenListDataItem == null) {
					newAllergenListDataItem = new AllergenListDataItem();
					newAllergenListDataItem.setAllergen(allergenNodeRef);
					allergenMap.put(allergenNodeRef, newAllergenListDataItem);
				}

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

	/**
	 * Calculate allergens to update (take in account manual list items)
	 * 
	 * @param productNodeRef
	 * @param allergenMap
	 * @return
	 */
	private List<AllergenListDataItem> getListToUpdate(NodeRef productNodeRef, Map<NodeRef, AllergenListDataItem> allergenMap) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);

		if (listContainerNodeRef != null) {

			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ALLERGENLIST);

			if (listNodeRef != null) {

				List<NodeRef> manualLinks = entityListDAO.getManualListItems(listNodeRef, BeCPGModel.TYPE_ALLERGENLIST);

				for (NodeRef manualLink : manualLinks) {

					AllergenListDataItem allergenListDataItem = (AllergenListDataItem)alfrescoRepository.findOne(manualLink);
					allergenMap.put(allergenListDataItem.getAllergen(), allergenListDataItem);
				}
			}
		}

		return new ArrayList<AllergenListDataItem>(allergenMap.values());
	}

	

}
