package fr.becpg.repo.product.helper;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	
	private static Log logger = LogFactory.getLog(AllocationHelper.class);
	
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

		for (CompoListDataItem compoList : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			NodeRef productNodeRef = compoList.getProduct();
			if ((productNodeRef != null) && !DeclarationType.Omit.equals(compoList.getDeclType())) {
				ProductData componentProductData = (ProductData) alfrescoRepository.findOne(productNodeRef);
				
				Double qty = FormulationHelper.getQtyInKg(compoList);
				Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
				if (logger.isDebugEnabled()) {
					logger.debug("Get rawMaterial " + componentProductData.getName() + "qty: " + qty + " netWeight "
							+ netWeight + " parentQty " + parentQty);
				}
				if ((qty != null) && (netWeight != 0d)) {
					qty = (parentQty * qty * FormulationHelper.getYield(compoList)) / (100 * netWeight);

					if (componentProductData.isRawMaterial()) {
						Double rmQty = allocations.get(productNodeRef);
						if (rmQty == null) {
							rmQty = 0d;
						}
						rmQty += qty;
						allocations.put(productNodeRef, rmQty);
					} else if (!componentProductData.isLocalSemiFinished()) {
						extractAllocations(componentProductData, allocations, qty , alfrescoRepository);
					}
				}
			}
		}

		return allocations;
	}

	
}
