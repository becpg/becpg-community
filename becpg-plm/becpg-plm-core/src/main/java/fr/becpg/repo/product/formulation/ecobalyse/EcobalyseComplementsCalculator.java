/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.ecobalyse;

import java.util.List;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Computes the non-LCA complements of the Ecobalyse environmental cost.
 *
 * <p>Ecobalyse publishes the ecosystemic services per kilogram of ingredient, already
 * carrying the thresholds and the weightings of the method, so this handler only has to
 * scale them by the quantity used and to sum them. The result lands in the LCA list as the
 * {@code BIODIVERSITY_COMPLEMENTS} indicator, which the environmental cost then aggregates
 * like any other indicator.</p>
 *
 * <p>A complement is a bonus: it is published as a negative contribution, which is how
 * Ecobalyse subtracts it from the cost.</p>
 *
 * @author matthieu
 */
public class EcobalyseComplementsCalculator {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(EcobalyseComplementsCalculator.class);

	/** Constant <code>COMPLEMENTS_LCA_CODE="BIODIVERSITY_COMPLEMENTS"</code> */
	public static final String COMPLEMENTS_LCA_CODE = "BIODIVERSITY_COMPLEMENTS";

	private NodeService nodeService;

	private EcobalyseIngredientService ecobalyseIngredientService;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>ecobalyseIngredientService</code>.</p>
	 *
	 * @param ecobalyseIngredientService a {@link fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredientService} object
	 */
	public void setEcobalyseIngredientService(EcobalyseIngredientService ecobalyseIngredientService) {
		this.ecobalyseIngredientService = ecobalyseIngredientService;
	}

	/**
	 * Adds the complements of the product to its LCA list.
	 *
	 * <p>Called by the LCA handler once the list has been rebuilt from the composition,
	 * because a line that no component backs would otherwise be cleaned away.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	public void addComplements(ProductData formulatedProduct) {
		if (!accept(formulatedProduct)) {
			return;
		}

		Double complements = computeComplements(formulatedProduct);

		if (complements != null) {
			publishComplements(formulatedProduct, complements);
		}
	}

	/**
	 * <p>accept.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a boolean
	 */
	private boolean accept(ProductData formulatedProduct) {
		return !formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) && (formulatedProduct.getCompoList() != null)
				&& (formulatedProduct.getLcaList() != null);
	}

	/**
	 * Sums what every component brings, in points for the whole product.
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return the complements, or null when no component is mapped to Ecobalyse
	 */
	private Double computeComplements(ProductData formulatedProduct) {
		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);

		if ((netWeight == null) || (netWeight == 0d)) {
			return null;
		}

		Double total = null;

		for (CompoListDataItem compoItem : formulatedProduct.getCompoList()) {
			Double contribution = componentComplements(compoItem, netWeight);
			if (contribution != null) {
				total = (total == null ? 0d : total) + contribution;
			}
		}

		return total;
	}

	/**
	 * <p>componentComplements.</p>
	 *
	 * @param compoItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 * @param netWeight the net weight of the formulated product
	 * @return the complements of the component, or null when it is not mapped
	 */
	private Double componentComplements(CompoListDataItem compoItem, Double netWeight) {
		if (compoItem.getProduct() == null) {
			return null;
		}

		Optional<EcobalyseIngredient> ingredient = findIngredient(compoItem.getProduct());

		if (ingredient.isEmpty()) {
			return null;
		}

		Double quantity = FormulationHelper.getQtyInKg(compoItem);

		if ((quantity == null) || (quantity == 0d)) {
			return null;
		}

		return (ingredient.get().totalEcosystemicServices() * quantity) / netWeight;
	}

	/**
	 * A component is mapped either by its Ecobalyse code, or by the farming practices
	 * entered on it when the customer documents its own supply rather than a reference one.
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<EcobalyseIngredient> findIngredient(NodeRef productNodeRef) {
		String code = (String) nodeService.getProperty(productNodeRef, PLMModel.PROP_ECOBALYSE_CODE);

		Optional<EcobalyseIngredient> ingredient = ecobalyseIngredientService.findByCode(code);

		return ingredient.isPresent() ? ingredient : fromAgriculturalPractices(productNodeRef);
	}

	/**
	 * <p>fromAgriculturalPractices.</p>
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.Optional} object
	 */
	private Optional<EcobalyseIngredient> fromAgriculturalPractices(NodeRef productNodeRef) {
		if (!nodeService.hasAspect(productNodeRef, PLMModel.ASPECT_AGRICULTURAL_PRACTICE)) {
			return Optional.empty();
		}

		EcobalyseIngredient practices = EcobalyseIngredient.builder()
				.withIdentity(productNodeRef.getId(), null, (String) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_CHARACT_NAME))
				.withOrigin((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_AP_CROP_GROUP),
						(String) nodeService.getProperty(productNodeRef, PLMModel.PROP_AP_SCENARIO), null)
				.withLandOccupation((Double) nodeService.getProperty(productNodeRef, PLMModel.PROP_AP_LAND_OCCUPATION))
				.withEcosystemicServices((Double) nodeService.getProperty(productNodeRef, PLMModel.PROP_AP_CROP_DIVERSITY),
						(Double) nodeService.getProperty(productNodeRef, PLMModel.PROP_AP_HEDGES),
						(Double) nodeService.getProperty(productNodeRef, PLMModel.PROP_AP_PERMANENT_PASTURE),
						(Double) nodeService.getProperty(productNodeRef, PLMModel.PROP_AP_PLOT_SIZE))
				.build();

		return Optional.of(practices);
	}

	/**
	 * <p>Publishes the complements as a negative contribution of the LCA list.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param complements the complements of the product, in points
	 */
	private void publishComplements(ProductData formulatedProduct, Double complements) {
		NodeRef complementsLca = findComplementsIndicator();

		if (complementsLca == null) {
			logger.debug("No " + COMPLEMENTS_LCA_CODE + " indicator in the repository, skipping the Ecobalyse complements");
			return;
		}

		LCAListDataItem item = findOrCreateItem(formulatedProduct, complementsLca);

		item.setValue(-complements);

		if (logger.isDebugEnabled()) {
			logger.debug("Ecobalyse complements of " + formulatedProduct.getName() + ": " + -complements);
		}
	}

	/**
	 * <p>findComplementsIndicator.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private NodeRef findComplementsIndicator() {
		List<NodeRef> found = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_LCA)
				.andPropEquals(PLMModel.PROP_LCA_CODE, COMPLEMENTS_LCA_CODE).list();

		return ((found == null) || found.isEmpty()) ? null : found.get(0);
	}

	/**
	 * <p>findOrCreateItem.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param complementsLca a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.data.productList.LCAListDataItem} object
	 */
	private LCAListDataItem findOrCreateItem(ProductData formulatedProduct, NodeRef complementsLca) {
		for (LCAListDataItem item : formulatedProduct.getLcaList()) {
			if (complementsLca.equals(item.getLca())) {
				return item;
			}
		}

		LCAListDataItem item = new LCAListDataItem();
		item.setLca(complementsLca);
		formulatedProduct.getLcaList().add(item);

		return item;
	}

}
