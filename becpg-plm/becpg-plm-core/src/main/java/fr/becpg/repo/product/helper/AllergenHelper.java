/*
 *
 */
package fr.becpg.repo.product.helper;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.allergen.AllergenItem;
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>Centralizes extraction and rendering of product allergens.</p>
 *
 * <p>Used by the labeling formulation handler, the report extractor and the
 * {@code @product} SPEL functions to guarantee a single consistent
 * implementation across label rendering and Excel exports.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AllergenHelper {

    private AllergenHelper() {
        // static helper
    }

    /**
     * Container for the four allergen maps built from a {@link ProductData}.
     * Keys are allergen nodeRefs, values are accumulated quantity percentages
     * (may be {@code null} when no quantity is available).
     */
    public static class AllergenMaps {

        private final Map<NodeRef, Double> allergens = new HashMap<>();

        private final Map<NodeRef, Double> inVolAllergens = new HashMap<>();

        private final Map<NodeRef, Double> inVolAllergensProcess = new HashMap<>();

        private final Map<NodeRef, Double> inVolAllergensRawMaterial = new HashMap<>();

        /**
         * <p>Getter for the voluntary major allergen map.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getAllergens() {
            return allergens;
        }

        /**
         * <p>Getter for the involuntary major allergen map.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getInVolAllergens() {
            return inVolAllergens;
        }

        /**
         * <p>Getter for involuntary allergens coming from a process source.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getInVolAllergensProcess() {
            return inVolAllergensProcess;
        }

        /**
         * <p>Getter for involuntary allergens coming from a raw material source.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getInVolAllergensRawMaterial() {
            return inVolAllergensRawMaterial;
        }
    }

    /**
     * Builds the four allergen maps (voluntary, involuntary, process, raw material)
     * for the given product. Only allergens of type {@link AllergenType#Major} are
     * collected, matching the existing labeling behaviour.
     *
     * @param productData        the product to extract allergens from
     * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
     * @param nodeService        a {@link org.alfresco.service.cmr.repository.NodeService} object
     * @return a fully populated {@link AllergenMaps} instance
     */
    public static AllergenMaps extract(ProductData productData, AlfrescoRepository<RepositoryEntity> alfrescoRepository, NodeService nodeService) {

        AllergenMaps maps = new AllergenMaps();

        if ((productData == null) || (productData.getAllergenList() == null)) {
            return maps;
        }

        for (AllergenListDataItem item : productData.getAllergenList()) {
            if (item.getAllergen() == null) {
                continue;
            }

            AllergenItem allergen = (AllergenItem) alfrescoRepository.findOne(item.getAllergen());
            if (!AllergenType.Major.toString().equals(allergen.getAllergenType())) {
                continue;
            }

            if (Boolean.TRUE.equals(item.getVoluntary())) {
                accumulate(maps.allergens, allergen.getNodeRef(), item.getQtyPerc());
            } else if (Boolean.TRUE.equals(item.getInVoluntary())) {
                accumulate(maps.inVolAllergens, allergen.getNodeRef(), item.getQtyPerc());

                if (item.getInVoluntarySources() != null) {
                    for (NodeRef source : item.getInVoluntarySources()) {
                        QName sourceType = nodeService.getType(source);

                        if (PLMModel.TYPE_RAWMATERIAL.equals(sourceType)) {
                            accumulate(maps.inVolAllergensRawMaterial, allergen.getNodeRef(), item.getQtyPerc());
                        } else if (PLMModel.TYPE_RESOURCEPRODUCT.equals(sourceType)) {
                            accumulate(maps.inVolAllergensProcess, allergen.getNodeRef(), item.getQtyPerc());
                        }
                    }
                }
            }
        }

        return maps;
    }

    private static void accumulate(Map<NodeRef, Double> target, NodeRef allergen, Double qtyPerc) {
        Double qty = qtyPerc;

        if (target.containsKey(allergen) && (qty != null) && (target.get(allergen) != null)) {
            qty += target.get(allergen);
        }

        target.put(allergen, qty);
    }

    /**
     * Returns the parent category localized grouped label
     * ({@code bcpg:allergenOthersLegalName}) when the given child allergen has a
     * parent category (reverse {@code bcpg:allergenSubset} association) defining
     * a non-blank value for the requested locale. Returns {@code null} when no
     * grouped label applies.
     *
     * @param childAllergen      a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param locale             a {@link java.util.Locale} object
     * @param mlNodeService      the ML-aware node service
     * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
     * @return the localized grouped label or {@code null}
     */
    public static String findInvoluntaryGroupLabel(NodeRef childAllergen, Locale locale, NodeService mlNodeService,
            AssociationService associationService) {

        List<NodeRef> parents = associationService.getSourcesAssocs(childAllergen, PLMModel.ASSOC_ALLERGENSUBSETS);
        if ((parents == null) || parents.isEmpty()) {
            return null;
        }

        for (NodeRef parent : parents) {
            MLText othersLegalName = (MLText) mlNodeService.getProperty(parent, PLMModel.PROP_ALLERGEN_OTHERS_LEGAL_NAME);
            if (othersLegalName == null) {
                continue;
            }

            String localized = MLTextHelper.getClosestValue(othersLegalName, locale);
            if ((localized != null) && !localized.isBlank()) {
                return localized;
            }
        }

        return null;
    }

    /**
     * Resolves the localized name of an allergen node. Uses {@code bcpg:legalName}
     * when available and falls back on {@code bcpg:charactName}.
     *
     * @param allergen      a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param locale        a {@link java.util.Locale} object
     * @param mlNodeService the ML-aware node service
     * @return the resolved name or {@code null}
     */
    public static String getAllergenName(NodeRef allergen, Locale locale, NodeService mlNodeService) {

        MLText legalName = (MLText) mlNodeService.getProperty(allergen, BeCPGModel.PROP_LEGAL_NAME);
        String ret = MLTextHelper.getClosestValue(legalName, locale);

        if ((ret == null) || ret.isEmpty()) {
            legalName = (MLText) mlNodeService.getProperty(allergen, BeCPGModel.PROP_CHARACT_NAME);
            ret = MLTextHelper.getClosestValue(legalName, locale);
        }

        return ret;
    }

    /**
     * Returns the allergen nodeRefs from a qty-indexed map, sorted by accumulated
     * quantity ascending (nulls last). Preserves the ordering used historically
     * in {@link fr.becpg.repo.product.formulation.labeling.LabelingFormulaContext}.
     *
     * @param allergens a {@link java.util.Map} object
     * @return an ordered {@link java.util.Set} of allergen nodeRefs
     */
    public static Set<NodeRef> sorted(Map<NodeRef, Double> allergens) {
        Set<NodeRef> ret = new LinkedHashSet<>();
        allergens.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry<NodeRef, Double>::getValue, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(Map.Entry::getKey).forEach(ret::add);
        return ret;
    }

    /**
     * Renders a flat, separator-delimited string of allergen legal names.
     * When {@code involuntary} is {@code true}, children of a category defining
     * a non-blank {@code bcpg:allergenOthersLegalName} for the locale are
     * replaced by the grouped label and deduplicated.
     *
     * @param allergens          an ordered {@link java.util.Collection} of allergen nodeRefs
     * @param locale             a {@link java.util.Locale} object
     * @param involuntary        {@code true} to enable grouped substitution
     * @param separator          the separator between allergen names
     * @param mlNodeService      the ML-aware node service
     * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
     * @return a rendered {@link java.lang.String} (may be empty, never {@code null})
     */
    public static String renderAllergens(Collection<NodeRef> allergens, Locale locale, boolean involuntary, String separator,
            NodeService mlNodeService, AssociationService associationService) {

        if ((allergens == null) || allergens.isEmpty()) {
            return "";
        }

        StringBuilder ret = new StringBuilder();
        Set<String> rendered = new LinkedHashSet<>();

        for (NodeRef allergen : allergens) {
            String name = null;
            if (involuntary) {
                name = findInvoluntaryGroupLabel(allergen, locale, mlNodeService, associationService);
            }
            if (name == null) {
                name = getAllergenName(allergen, locale, mlNodeService);
            }

            if ((name == null) || name.isEmpty() || !rendered.add(name)) {
                continue;
            }

            if (ret.length() > 0) {
                ret.append(separator);
            }
            ret.append(name);
        }

        return ret.toString();
    }

}
