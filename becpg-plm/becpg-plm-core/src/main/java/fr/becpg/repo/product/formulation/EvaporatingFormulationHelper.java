/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.formulation.labeling.EvaporatedDataItem;

/**
 * Helper class for evaporation calculations in formulation.
 * Provides common evaporation logic used by both ingredient list and labeling formulation handlers.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EvaporatingFormulationHelper {

    private static final Log logger = LogFactory.getLog(EvaporatingFormulationHelper.class);

    private EvaporatingFormulationHelper() {
        // Private constructor
    }

    /**
     * Checks if a product has evaporation data (water aspect or evaporation rate).
     *
     * @param productNodeRef the product node reference
     * @param nodeService the node service
     * @return true if the product has evaporation data
     */
    public static boolean hasEvaporationData(NodeRef productNodeRef, NodeService nodeService) {
        return nodeService.hasAspect(productNodeRef, PLMModel.ASPECT_WATER)
                || (nodeService.getProperty(productNodeRef, PLMModel.PROP_EVAPORATED_RATE) != null);
    }

    /**
     * Gets the evaporation rate for a product, defaulting to 100% if not specified.
     *
     * @param productNodeRef the product node reference
     * @param nodeService the node service
     * @return the evaporation rate (default 100%)
     */
    public static Double getEvaporateRate(NodeRef productNodeRef, NodeService nodeService) {
        Double evaporateRate = (Double) nodeService.getProperty(productNodeRef, PLMModel.PROP_EVAPORATED_RATE);
        return evaporateRate != null ? evaporateRate : 100d;
    }

    /**
     * Calculates the maximum evaporable quantity based on quantity with yield and evaporation rate.
     *
     * @param qtyWithYield the quantity with yield
     * @param evaporateRate the evaporation rate percentage
     * @return the maximum evaporable quantity, or 0 if qtyWithYield is null
     */
    public static Double calculateMaxEvaporableQty(Double qtyWithYield, Double evaporateRate) {
        if ((qtyWithYield == null) || (evaporateRate == null)) {
            return 0d;
        }
        return (qtyWithYield * evaporateRate) / 100d;
    }

    /**
     * Calculates the maximum evaporable volume based on volume with yield and evaporation rate.
     *
     * @param volumeWithYield the volume with yield
     * @param evaporateRate the evaporation rate percentage
     * @return the maximum evaporable volume, or 0 if volumeWithYield is null
     */
    public static Double calculateMaxEvaporableVolume(Double volumeWithYield, Double evaporateRate) {
        if ((volumeWithYield == null) || (evaporateRate == null)) {
            return 0d;
        }
        return (volumeWithYield * evaporateRate) / 100d;
    }

    /**
     * Applies evaporation to a set of items based on yield and evaporation rates.
     * 
     * @param <T> the type of items being processed
     * @param evaporatingQty the total quantity to evaporate
     * @param evaporatedDataItems the set of items with evaporation data
     * @param getQtyWithYield function to get quantity with yield from an item
     * @param setQtyWithYield function to set quantity with yield on an item
     * @param matchItem function to match an evaporated data item to an item in the collection
     * @param getItemName function to get the name of an item (for logging)
     * @param getMaxEvaporable function to get the max evaporable limit from EvaporatedDataItem (null if no limit)
     * @return the remaining quantity that was not evaporated
     */
    public static <T> Double applyEvaporation(Double evaporatingQty, Set<EvaporatedDataItem> evaporatedDataItems,
            Function<T, Double> getQtyWithYield, BiConsumer<T, Double> setQtyWithYield,
            Function<NodeRef, T> matchItem, Function<T, String> getItemName, Function<EvaporatedDataItem, Double> getMaxEvaporable) {

        if ((evaporatingQty == null) || (evaporatingQty <= 0d) || evaporatedDataItems.isEmpty()) {
            return evaporatingQty;
        }

        AtomicReference<Double> remainingQty = new AtomicReference<>(evaporatingQty);

        // 1. Evaporate ingredients with 100% rate first
        Set<EvaporatedDataItem> fullEvaporationItems = evaporatedDataItems.stream()
                .filter(item -> (item.getRate() != null) && (item.getRate() == 100d)).collect(Collectors.toSet());

        remainingQty.set(processEvaporation(remainingQty.get(), fullEvaporationItems, null,
                getQtyWithYield, setQtyWithYield, matchItem, getItemName, getMaxEvaporable));

        if (logger.isTraceEnabled()) {
            logger.trace("- REMAINING evaporation after 100% items: " + remainingQty.get());
        }

        // 2. Distribute remaining evaporation proportionally
        Set<EvaporatedDataItem> remainingItems = evaporatedDataItems.stream()
                .filter(item -> !fullEvaporationItems.contains(item)).collect(Collectors.toSet());

        Double totalRate = remainingItems.stream().mapToDouble(EvaporatedDataItem::getRate).sum();

        remainingQty.set(processEvaporation(remainingQty.get(), remainingItems, totalRate,
                getQtyWithYield, setQtyWithYield, matchItem, getItemName, getMaxEvaporable));

        // 3. If not all has been evaporated, remove from the first item with 100% evaporation
        // (only when not limiting max evaporation)
        if ((remainingQty.get() > 0.000001) && !fullEvaporationItems.isEmpty() && (getMaxEvaporable == null)) {
            EvaporatedDataItem evaporatedDataItem = fullEvaporationItems.iterator().next();
            T item = matchItem.apply(evaporatedDataItem.getProductNodeRef());

            if ((item != null) && (getQtyWithYield.apply(item) != null)) {
                setQtyWithYield.accept(item, getQtyWithYield.apply(item) - remainingQty.get());

                if (logger.isTraceEnabled()) {
                    logger.trace("- STILL REMAINING evaporation removed from first: " + remainingQty.get());
                }
            }
        }

        return remainingQty.get();
    }

    /**
     * Process evaporation for a set of items with similar evaporation characteristics.
     * 
     * @param <T> the type of items being processed
     * @param evaporatingQty the quantity to evaporate
     * @param items the evaporated data items to process
     * @param totalRate the sum of all evaporation rates (null for 100% items)
     * @param getQtyWithYield function to get quantity with yield from an item
     * @param setQtyWithYield function to set quantity with yield on an item
     * @param matchItem function to match an evaporated data item to an item in the collection
     * @param getItemName function to get the name of an item (for logging)
     * @param getMaxEvaporable function to get the max evaporable limit from EvaporatedDataItem (null if no limit)
     * @return the remaining quantity that was not evaporated
     */
    private static <T> Double processEvaporation(Double evaporatingQty, Set<EvaporatedDataItem> items,
            Double totalRate, Function<T, Double> getQtyWithYield,
            BiConsumer<T, Double> setQtyWithYield, Function<NodeRef, T> matchItem, Function<T, String> getItemName, Function<EvaporatedDataItem, Double> getMaxEvaporable) {

        if ((evaporatingQty == null) || (evaporatingQty <= 0d)) {
            return evaporatingQty;
        }

        // Step 1: Calculate available water for each item based on rate and quantity
        Map<EvaporatedDataItem, EvapData<T>> evaporationDataMap = new HashMap<>();
        double totalAvailableWater = 0d;

        for (EvaporatedDataItem evaporatedDataItem : items) {
            T item = matchItem.apply(evaporatedDataItem.getProductNodeRef());

            if (item != null) {
                Double rate = evaporatedDataItem.getRate() != null ? evaporatedDataItem.getRate() : 100d;
                
                // Skip items with 0% evaporation rate
                if (rate == 0d) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipping evaporation for item with 0% rate: " + getItemName.apply(item));
                    }
                    continue;
                }

                Double qtyWithYield = getQtyWithYield.apply(item);

                if ((qtyWithYield != null) && (qtyWithYield > 0d)) {
                    Double maxEvapQty = (qtyWithYield * rate) / 100d;
                    
                    // Limit max evaporation if limit function is provided
                    if (getMaxEvaporable != null) {
                        Double maxLimit = getMaxEvaporable.apply(evaporatedDataItem);
                        if (maxLimit != null) {
                            maxEvapQty = Math.min(maxEvapQty, maxLimit);
                        }
                    }
                    
                    evaporationDataMap.put(evaporatedDataItem, new EvapData<>(item, rate, maxEvapQty, qtyWithYield));
                    totalAvailableWater += maxEvapQty;
                }
            }
        }

        // Step 2: Distribute evaporation based on available water rather than just rates
        // IMPORTANT: Save the original evaporatingQty at the start of this phase
        // All items in THIS phase should use the SAME base value (matching labeling behavior)
        Double evaporatingQtyAtStart = evaporatingQty;
        
        if (!evaporationDataMap.isEmpty() && (totalAvailableWater > 0d)) {
            for (Map.Entry<EvaporatedDataItem, EvapData<T>> entry : evaporationDataMap.entrySet()) {
                EvapData<T> evapData = entry.getValue();

                // Use FormulationHelper to calculate proportional evaporation
                // Use evaporatingQtyAtStart (not the accumulating evaporatingQty) for all items in this phase
                Double evaporatedQty = FormulationHelper.calculateProportionalEvaporation(
                        evaporatingQtyAtStart, evapData.rate, evapData.maxEvapQty, totalRate, totalAvailableWater);

                setQtyWithYield.accept(evapData.item, evapData.qtyWithYield - evaporatedQty);

                if (logger.isDebugEnabled()) {
                    logger.debug("Apply evaporation qty " + evaporatedQty + " (max: " + evapData.maxEvapQty 
                            + ") on " + getItemName.apply(evapData.item) + " - remaining: "
                            + getQtyWithYield.apply(evapData.item));
                }

                evaporatingQty -= evaporatedQty;
            }
        }

        return evaporatingQty;
    }

    /**
     * Helper class to store evaporation data for any type of item.
     * 
     * @param <T> the type of item being processed
     */
    private static class EvapData<T>
    {
        final T item;
        final Double rate;
        final Double maxEvapQty;
        final Double qtyWithYield;

        EvapData(T item, Double rate, Double maxEvapQty, Double qtyWithYield)
        {
            this.item = item;
            this.rate = rate;
            this.maxEvapQty = maxEvapQty;
            this.qtyWithYield = qtyWithYield;
        }
    }
}
