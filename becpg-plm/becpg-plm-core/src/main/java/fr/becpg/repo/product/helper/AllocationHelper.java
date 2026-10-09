package fr.becpg.repo.product.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>AllocationHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AllocationHelper {
	
	/** Constant <code>logger</code> */
	private static Log logger = LogFactory.getLog(AllocationHelper.class);
	
	/**
	 * <p>Constructor for AllocationHelper.</p>
	 */
	private AllocationHelper() {
		//Private
	}

	/**
	 * <p>extractAllocations.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param allocations a {@link java.util.Map} object
	 * @param parentQty a {@link java.lang.Double} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @return a {@link java.util.Map} object
	 */
	public static Map<NodeRef, Double> extractAllocations(ProductData productData, Map<NodeRef, Double> allocations, Double parentQty,
			AlfrescoRepository<BeCPGDataObject> alfrescoRepository) {
		Extraction extraction = new Extraction(allocations, alfrescoRepository, new HashSet<>());
		extractProduct(extraction, productData, parentQty);
		return allocations;
	}

	/**
	 * What every level of the extraction shares: the allocations gathered, the repository, and
	 * the products on the path from the root to the level being read, which is how a cyclic
	 * composition is caught.
	 */
	private record Extraction(Map<NodeRef, Double> allocations, AlfrescoRepository<BeCPGDataObject> alfrescoRepository, Set<NodeRef> path) {
	}

	/**
	 * A product found again on its own path is a cyclic composition, an error of the data: it
	 * would recurse until the stack overflows, so its branch is dropped and a warning logged.
	 * The path is not a record of every product visited, a component used in several branches is
	 * counted in each of them.
	 */
	private static void extractProduct(Extraction extraction, ProductData productData, Double parentQty) {
		if ((productData.getNodeRef() != null) && !extraction.path().add(productData.getNodeRef())) {
			logger.warn("Cyclic composition, " + productData.getName() + " (" + productData.getNodeRef()
					+ ") contains itself: its allocations are left out of that branch");
			return;
		}
		try {
			List<CompoListDataItem> compoList = productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));
			if (compoList != null) {
				extractComposite(extraction, productData, parentQty, CompositeHelper.getHierarchicalCompoList(compoList));
			}
		} finally {
			extraction.path().remove(productData.getNodeRef());
		}
	}

	/**
	 * Returns a copy of the allocation map ordered by allocated quantity descending,
	 * so that the main raw material (the one with the highest allocated quantity) comes
	 * first. This allows a single representative raw material to be picked deterministically
	 * (e.g. {@code keySet().iterator().next()} in a SmartContent formula), instead of relying
	 * on the arbitrary iteration order of a {@link java.util.HashMap}.
	 *
	 * @param allocations the allocation map
	 * @return a new map ordered by allocated quantity descending, empty when the input is empty
	 */
	public static Map<NodeRef, Double> sortByAllocationDesc(Map<NodeRef, Double> allocations) {
		List<Map.Entry<NodeRef, Double>> entries = new ArrayList<>(allocations.entrySet());
		entries.sort(Comparator.comparingDouble((Map.Entry<NodeRef, Double> entry) -> entry.getValue() != null ? entry.getValue() : 0d).reversed());

		Map<NodeRef, Double> sorted = new LinkedHashMap<>();
		for (Map.Entry<NodeRef, Double> entry : entries) {
			sorted.put(entry.getKey(), entry.getValue());
		}
		return sorted;
	}

	/**
	 * Extracts raw material allocations from a hierarchical composition. Local
	 * semi-finished products are treated as grouping levels of the enclosing
	 * recipe: their children are processed in the context of the enclosing
	 * product with the same parent quantity.
	 *
	 * @param extraction the state shared by every level of the extraction
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param parentQty a {@link java.lang.Double} object
	 * @param composite a {@link fr.becpg.repo.data.hierarchicalList.Composite} object
	 */
	private static void extractComposite(Extraction extraction, ProductData productData, Double parentQty, Composite<CompoListDataItem> composite) {

		for (Composite<CompoListDataItem> child : composite.getChildren()) {
			CompoListDataItem compoList = child.getData();
			NodeRef productNodeRef = compoList.getProduct();
			if ((productNodeRef != null) && !DeclarationType.Omit.equals(compoList.getDeclType())) {
				ProductData componentProductData = (ProductData) extraction.alfrescoRepository().findOne(productNodeRef);

				if (componentProductData.isLocalSemiFinished()) {
					extractComposite(extraction, productData, parentQty, child);
					continue;
				}

				Double qty = FormulationHelper.getQtyInKg(compoList);
				Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
				if (logger.isDebugEnabled()) {
					logger.debug("Get rawMaterial " + componentProductData.getName() + "qty: " + qty + " netWeight "
							+ netWeight + " parentQty " + parentQty);
				}
				if ((qty != null) && (netWeight != 0d)) {
					qty = (parentQty * qty * FormulationHelper.getYield(compoList)) / (100 * netWeight);

					if (componentProductData.isRawMaterial()) {
						extraction.allocations().merge(productNodeRef, qty, Double::sum);
					} else if (!child.getChildren().isEmpty()) {
						extractComposite(extraction, componentProductData, qty, child);
					} else {
						extractProduct(extraction, componentProductData, qty);
					}
				}
			}
		}
	}

	
}
