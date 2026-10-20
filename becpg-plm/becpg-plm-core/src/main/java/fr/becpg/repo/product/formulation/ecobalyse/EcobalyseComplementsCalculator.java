/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.ecobalyse;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
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

	/** Farming practices are read from the life cycle list, their codes share this prefix */
	private static final String PRACTICE_PREFIX = "AP_";

	/** Constant <code>HEDGES="AP_HEDGES"</code> */
	private static final String HEDGES = "AP_HEDGES";

	/** Constant <code>PLOT_SIZE="AP_PLOT_SIZE"</code> */
	private static final String PLOT_SIZE = "AP_PLOT_SIZE";

	/** Constant <code>CROP_DIVERSITY="AP_CROP_DIVERSITY"</code> */
	private static final String CROP_DIVERSITY = "AP_CROP_DIVERSITY";

	/** Constant <code>PERMANENT_PASTURE="AP_PERMANENT_PASTURE"</code> */
	private static final String PERMANENT_PASTURE = "AP_PERMANENT_PASTURE";

	private NodeService nodeService;

	private EcobalyseIngredientService ecobalyseIngredientService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

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

		Optional<Double> services = servicesPerKg(compoItem.getProduct());

		if (services.isEmpty()) {
			return null;
		}

		Double quantity = FormulationHelper.getQtyInKg(compoItem);

		if ((quantity == null) || (quantity == 0d)) {
			return null;
		}

		return (services.get() * quantity) / netWeight;
	}

	/**
	 * Ecosystemic services of a component per kilogram, from its Ecobalyse code first, then
	 * from its LCA list when the customer documents its own supply rather than a reference one.
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.Optional} object, empty when the component is not mapped
	 */
	private Optional<Double> servicesPerKg(NodeRef productNodeRef) {
		String code = (String) nodeService.getProperty(productNodeRef, PLMModel.PROP_ECOBALYSE_CODE);

		Optional<EcobalyseIngredient> ingredient = ecobalyseIngredientService.findByCode(code);

		return ingredient.isPresent() ? Optional.of(ingredient.get().totalEcosystemicServices()) : lifeCycleServices(productNodeRef);
	}

	/**
	 * <p>Ecosystemic services read from the LCA list of a component.</p>
	 *
	 * <p>A component that already carries complements, a semi-finished product formulated
	 * from its own components, is taken as such: its practices only cover the components
	 * documented by practices and would drop the ones mapped by a code. Otherwise the
	 * practices themselves switch the complements on, no qualifier has to be entered beside
	 * them.</p>
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.Optional} object, empty when the component carries neither
	 */
	private Optional<Double> lifeCycleServices(NodeRef productNodeRef) {
		Map<String, Double> indicators = complementIndicators(productNodeRef);

		if (indicators.containsKey(COMPLEMENTS_LCA_CODE)) {
			return Optional.of(-indicators.get(COMPLEMENTS_LCA_CODE));
		}

		if (indicators.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(sum(indicators.get(CROP_DIVERSITY)) + sum(indicators.get(HEDGES)) + sum(indicators.get(PERMANENT_PASTURE))
				+ sum(indicators.get(PLOT_SIZE)));
	}

	/**
	 * <p>sum.</p>
	 *
	 * @param value a {@link java.lang.Double} object
	 * @return the value, 0 when missing
	 */
	private double sum(Double value) {
		return value != null ? value : 0d;
	}

	/**
	 * <p>Farming practices and complements of a component, read from its LCA list.</p>
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return the values by indicator code, never null
	 */
	private Map<String, Double> complementIndicators(NodeRef productNodeRef) {
		Map<String, Double> values = new HashMap<>();

		ProductData component = (ProductData) alfrescoRepository.findOne(productNodeRef);
		if ((component == null) || (component.getLcaList() == null)) {
			return values;
		}

		for (LCAListDataItem line : component.getLcaList()) {
			if ((line.getLca() == null) || (line.getValue() == null)) {
				continue;
			}

			String code = (String) nodeService.getProperty(line.getLca(), PLMModel.PROP_LCA_CODE);
			if ((code != null) && (code.startsWith(PRACTICE_PREFIX) || COMPLEMENTS_LCA_CODE.equals(code))) {
				values.put(code, line.getValue());
			}
		}

		return values;
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
