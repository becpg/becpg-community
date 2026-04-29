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

        private final Map<NodeRef, Double> allAllergens = new HashMap<>();

        private final Map<NodeRef, Double> inVolAllergens = new HashMap<>();

        private final Map<NodeRef, Double> allInVolAllergens = new HashMap<>();

        private final Map<NodeRef, Double> inVolAllergensProcess = new HashMap<>();

        private final Map<NodeRef, Double> allInVolAllergensProcess = new HashMap<>();

        private final Map<NodeRef, Double> inVolAllergensRawMaterial = new HashMap<>();

        private final Map<NodeRef, Double> allInVolAllergensRawMaterial = new HashMap<>();

        /**
         * <p>Getter for the voluntary major allergen map.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getAllergens() {
            return allergens;
        }

        /**
         * <p>Getter for the full voluntary allergen map.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getAllAllergens() {
            return allAllergens;
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
         * <p>Getter for the full involuntary allergen map.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getAllInVolAllergens() {
            return allInVolAllergens;
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
         * <p>Getter for the full involuntary process allergen map.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getAllInVolAllergensProcess() {
            return allInVolAllergensProcess;
        }

        /**
         * <p>Getter for involuntary allergens coming from a raw material source.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getInVolAllergensRawMaterial() {
            return inVolAllergensRawMaterial;
        }

        /**
         * <p>Getter for the full involuntary raw material allergen map.</p>
         *
         * @return a {@link java.util.Map} object
         */
        public Map<NodeRef, Double> getAllInVolAllergensRawMaterial() {
            return allInVolAllergensRawMaterial;
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
            if (Boolean.TRUE.equals(item.getVoluntary())) {
                accumulate(maps.allAllergens, allergen.getNodeRef(), item.getQtyPerc());
                if (AllergenType.Major.toString().equals(allergen.getAllergenType())) {
                    accumulate(maps.allergens, allergen.getNodeRef(), item.getQtyPerc());
                }
            } else if (Boolean.TRUE.equals(item.getInVoluntary())) {
                accumulate(maps.allInVolAllergens, allergen.getNodeRef(), item.getQtyPerc());
                if (AllergenType.Major.toString().equals(allergen.getAllergenType())) {
                    accumulate(maps.inVolAllergens, allergen.getNodeRef(), item.getQtyPerc());
                }

                if (item.getInVoluntarySources() != null) {
                    for (NodeRef source : item.getInVoluntarySources()) {
                        QName sourceType = nodeService.getType(source);

                        if (PLMModel.TYPE_RAWMATERIAL.equals(sourceType)) {
                            accumulate(maps.allInVolAllergensRawMaterial, allergen.getNodeRef(), item.getQtyPerc());
                            if (AllergenType.Major.toString().equals(allergen.getAllergenType())) {
                                accumulate(maps.inVolAllergensRawMaterial, allergen.getNodeRef(), item.getQtyPerc());
                            }
                        } else if (PLMModel.TYPE_RESOURCEPRODUCT.equals(sourceType)) {
                            accumulate(maps.allInVolAllergensProcess, allergen.getNodeRef(), item.getQtyPerc());
                            if (AllergenType.Major.toString().equals(allergen.getAllergenType())) {
                                accumulate(maps.inVolAllergensProcess, allergen.getNodeRef(), item.getQtyPerc());
                            }
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
     * Renders a flat, separator-delimited, de-duplicated string of allergen legal
     * names for the requested locale. Intended for the voluntary allergen list or
     * any flat listing that does not need the grouped involuntary substitution.
     *
     * @param allergens     an ordered {@link java.util.Collection} of allergen nodeRefs
     * @param locale        a {@link java.util.Locale} object
     * @param separator     the separator between allergen names
     * @param mlNodeService the ML-aware node service
     * @return a rendered {@link java.lang.String} (may be empty, never {@code null})
     */
    public static String renderAllergens(Collection<NodeRef> allergens, Locale locale, String separator, NodeService mlNodeService) {

        if ((allergens == null) || allergens.isEmpty()) {
            return "";
        }

        Set<String> rendered = new LinkedHashSet<>();

        for (NodeRef allergen : allergens) {
            String name = getAllergenName(allergen, locale, mlNodeService);
            if ((name != null) && !name.isEmpty()) {
                rendered.add(name);
            }
        }

        return String.join(separator, rendered);
    }

    /**
     * Renders the involuntary / traces allergen list applying the grouping rule
     * carried by {@code bcpg:allergenInvoluntaryOtherLegalName}.
     *
     * <p>For each allergen category (an allergen with a non-empty
     * {@code bcpg:allergenSubset}) <b>declared as voluntary</b> on the current
     * product, if at least one of its children is present in
     * {@code involuntaryAllergens} <b>and</b>
     * {@code bcpg:allergenInvoluntaryOtherLegalName} provides a non-blank value
     * for the requested locale, the grouped label is output once and those
     * children are hidden from the involuntary list. In every other case the
     * involuntary children are rendered individually via their
     * {@code bcpg:legalName} (default behaviour).</p>
     *
     * <p>The order is stable: grouped labels (in voluntary-set iteration order)
     * first, then the remaining involuntary children in the order given by
     * {@code involuntaryAllergens}.</p>
     *
     * @param involuntaryAllergens an ordered {@link java.util.Collection} of involuntary allergen nodeRefs
     * @param voluntaryAllergens   the full voluntary allergen set of the product (used to detect voluntary categories)
     * @param locale               a {@link java.util.Locale} object
     * @param separator            the separator between allergen names
     * @param mlNodeService        the ML-aware node service
     * @param associationService   a {@link fr.becpg.repo.helper.AssociationService} object
     * @return a rendered {@link java.lang.String} (may be empty, never {@code null})
     */
    public static String renderInvoluntaryAllergens(Collection<NodeRef> involuntaryAllergens, Collection<NodeRef> voluntaryAllergens,
            Locale locale, String separator, NodeService mlNodeService, AssociationService associationService) {

        return renderInvoluntaryAllergens(involuntaryAllergens, involuntaryAllergens, voluntaryAllergens, locale, separator, mlNodeService,
                associationService);
    }

    /**
     * Renders the involuntary / traces allergen list using a full involuntary set
     * for grouped substitution detection while keeping the display list unchanged.
     *
     * @param involuntaryAllergens        an ordered {@link java.util.Collection} of involuntary allergen nodeRefs to render individually
     * @param groupingInvoluntaryAllergens the full involuntary allergen set used to detect grouped substitutions
     * @param voluntaryAllergens          the full voluntary allergen set of the product (used to detect voluntary categories)
     * @param locale                      a {@link java.util.Locale} object
     * @param separator                   the separator between allergen names
     * @param mlNodeService               the ML-aware node service
     * @param associationService          a {@link fr.becpg.repo.helper.AssociationService} object
     * @return a rendered {@link java.lang.String} (may be empty, never {@code null})
     */
    public static String renderInvoluntaryAllergens(Collection<NodeRef> involuntaryAllergens, Collection<NodeRef> groupingInvoluntaryAllergens,
            Collection<NodeRef> voluntaryAllergens, Locale locale, String separator, NodeService mlNodeService, AssociationService associationService) {

        if (((involuntaryAllergens == null) || involuntaryAllergens.isEmpty())
                && ((groupingInvoluntaryAllergens == null) || groupingInvoluntaryAllergens.isEmpty())) {
            return "";
        }

        Set<NodeRef> involuntarySet = new LinkedHashSet<>();
        if (involuntaryAllergens != null) {
            involuntarySet.addAll(involuntaryAllergens);
        }
        if (groupingInvoluntaryAllergens != null) {
            involuntarySet.addAll(groupingInvoluntaryAllergens);
        }
        Set<NodeRef> consumed = new LinkedHashSet<>();
        LinkedHashSet<String> rendered = new LinkedHashSet<>();

        if ((voluntaryAllergens != null) && !voluntaryAllergens.isEmpty()) {
            Set<NodeRef> candidateCategories = new LinkedHashSet<>();
            for (NodeRef voluntary : voluntaryAllergens) {
                List<NodeRef> children = associationService.getTargetAssocs(voluntary, PLMModel.ASSOC_ALLERGENSUBSETS);
                if ((children != null) && !children.isEmpty()) {
                    candidateCategories.add(voluntary);
                }
                List<NodeRef> parents = associationService.getSourcesAssocs(voluntary, PLMModel.ASSOC_ALLERGENSUBSETS);
                if (parents != null) {
                    candidateCategories.addAll(parents);
                }
            }

            for (NodeRef category : candidateCategories) {
                List<NodeRef> children = associationService.getTargetAssocs(category, PLMModel.ASSOC_ALLERGENSUBSETS);
                if ((children == null) || children.isEmpty()) {
                    continue;
                }

                Set<NodeRef> involuntaryChildren = new LinkedHashSet<>();
                for (NodeRef child : children) {
                    if (involuntarySet.contains(child)) {
                        involuntaryChildren.add(child);
                    }
                }
                if (involuntaryChildren.isEmpty()) {
                    continue;
                }

                MLText othersLegalName = (MLText) mlNodeService.getProperty(category, PLMModel.PROP_ALLERGEN_INVOLUNTARY_OTHER_LEGAL_NAME);
                String localized = MLTextHelper.getClosestValue(othersLegalName, locale);

                if ((localized == null) || localized.isBlank()) {
                    continue;
                }

                rendered.add(localized);
                consumed.addAll(involuntaryChildren);
            }
        }

        if (involuntaryAllergens == null) {
            return String.join(separator, rendered);
        }

        for (NodeRef allergen : involuntaryAllergens) {
            if (consumed.contains(allergen)) {
                continue;
            }
            String name = getAllergenName(allergen, locale, mlNodeService);
            if ((name != null) && !name.isEmpty()) {
                rendered.add(name);
            }
        }

        return String.join(separator, rendered);
    }

}
