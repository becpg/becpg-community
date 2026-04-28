/*
 *
 */
package fr.becpg.repo.product.formulation.spel;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.formulation.spel.CustomSpelFunctions;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.helper.AllergenHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>Custom SPEL helper accessible as {@code @product}.</p>
 *
 * <p>Exposes allergen accessors and renderers on top of {@link AllergenHelper}
 * so that labeling rules, Excel report formulas and any other SPEL context can
 * share a single implementation. Rendering honours the current locale
 * ({@link org.springframework.extensions.surf.util.I18NUtil#getLocale()}) and
 * substitutes grouped category labels via
 * {@code bcpg:allergenOthersLegalName} for involuntary lists.</p>
 *
 * <p>Usage examples:</p>
 * <pre>
 *   @product.allergens()                              // voluntary allergen nodeRefs on the current entity
 *   @product.involuntaryAllergens(entity)             // involuntary allergen nodeRefs on the given product
 *   @product.renderAllergens()                        // comma-separated voluntary labels
 *   @product.renderInvoluntaryAllergens(" / ")        // custom separator
 *   @product.renderInvoluntaryAllergenInProcess()
 *   @product.renderInvoluntaryInRawMaterial()
 * </pre>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProductSpelFunctions implements CustomSpelFunctions {

    private static final Log logger = LogFactory.getLog(ProductSpelFunctions.class);

    @Autowired
    private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

    @Autowired
    private NodeService nodeService;

    @Autowired
    @Qualifier("mlAwareNodeService")
    private NodeService mlNodeService;

    @Autowired
    private AssociationService associationService;

    /** {@inheritDoc} */
    @Override
    public boolean match(String beanName) {
        return "product".equals(beanName);
    }

    /** {@inheritDoc} */
    @Override
    public Object create(RepositoryEntity repositoryEntity) {
        return new ProductSpelFunctionsWrapper(repositoryEntity);
    }

    /**
     * Wrapper actually exposed to SPEL as {@code @product}.
     */
    public class ProductSpelFunctionsWrapper {

        private final RepositoryEntity entity;

        ProductSpelFunctionsWrapper(RepositoryEntity entity) {
            this.entity = entity;
        }

        /**
         * <p>Voluntary major allergens on the current entity.</p>
         *
         * @return a {@link java.util.List} of allergen {@link org.alfresco.service.cmr.repository.NodeRef}
         */
        public List<NodeRef> allergens() {
            return allergens(entity);
        }

        /**
         * <p>Voluntary major allergens on the given product.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @return a {@link java.util.List} of allergen {@link org.alfresco.service.cmr.repository.NodeRef}
         */
        public List<NodeRef> allergens(RepositoryEntity target) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return sortedList(AllergenHelper.sorted(maps.getAllergens()));
        }

        /**
         * <p>Involuntary (traces) major allergens on the current entity.</p>
         *
         * @return a {@link java.util.List} of allergen {@link org.alfresco.service.cmr.repository.NodeRef}
         */
        public List<NodeRef> involuntaryAllergens() {
            return involuntaryAllergens(entity);
        }

        /**
         * <p>Involuntary (traces) major allergens on the given product.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @return a {@link java.util.List} of allergen {@link org.alfresco.service.cmr.repository.NodeRef}
         */
        public List<NodeRef> involuntaryAllergens(RepositoryEntity target) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return sortedList(AllergenHelper.sorted(maps.getInVolAllergens()));
        }

        /**
         * <p>Involuntary allergens whose source is a process (resource).</p>
         *
         * @return a {@link java.util.List} of allergen {@link org.alfresco.service.cmr.repository.NodeRef}
         */
        public List<NodeRef> involuntaryAllergenInProcess() {
            return involuntaryAllergenInProcess(entity);
        }

        /**
         * <p>Involuntary allergens whose source is a process on the given product.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @return a {@link java.util.List} of allergen {@link org.alfresco.service.cmr.repository.NodeRef}
         */
        public List<NodeRef> involuntaryAllergenInProcess(RepositoryEntity target) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return sortedList(AllergenHelper.sorted(maps.getInVolAllergensProcess()));
        }

        /**
         * <p>Involuntary allergens whose source is a raw material.</p>
         *
         * @return a {@link java.util.List} of allergen {@link org.alfresco.service.cmr.repository.NodeRef}
         */
        public List<NodeRef> involuntaryInRawMaterial() {
            return involuntaryInRawMaterial(entity);
        }

        /**
         * <p>Involuntary allergens whose source is a raw material on the given product.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @return a {@link java.util.List} of allergen {@link org.alfresco.service.cmr.repository.NodeRef}
         */
        public List<NodeRef> involuntaryInRawMaterial(RepositoryEntity target) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return sortedList(AllergenHelper.sorted(maps.getInVolAllergensRawMaterial()));
        }

        /**
         * <p>Renders voluntary allergens for the current locale using the default separator.</p>
         *
         * @return a {@link java.lang.String} object
         */
        public String renderAllergens() {
            return renderAllergens(RepoConsts.LABEL_SEPARATOR);
        }

        /**
         * <p>Renders voluntary allergens for the current locale using the given separator.</p>
         *
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderAllergens(String separator) {
            return renderAllergens(entity, separator);
        }

        /**
         * <p>Renders voluntary allergens of the given product.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderAllergens(RepositoryEntity target, String separator) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return AllergenHelper.renderAllergens(AllergenHelper.sorted(maps.getAllergens()), locale(), safe(separator), mlNodeService);
        }

        /**
         * <p>Renders voluntary allergens of the given product, forcing the locale.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @param localeCode the target locale (e.g. {@code "fr"}, {@code "en"}, {@code "fr_CA"})
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderAllergensForLocale(RepositoryEntity target, String localeCode, String separator) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return AllergenHelper.renderAllergens(AllergenHelper.sorted(maps.getAllergens()), parse(localeCode), safe(separator), mlNodeService);
        }

        /**
         * <p>Renders voluntary allergens on the current entity, forcing the locale.</p>
         *
         * @param localeCode the target locale (e.g. {@code "fr"}, {@code "en"}, {@code "fr_CA"})
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderAllergensForLocale(String localeCode, String separator) {
            return renderAllergensForLocale(entity, localeCode, separator);
        }

        /**
         * <p>Renders involuntary allergens with grouped category substitution.</p>
         *
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergens() {
            return renderInvoluntaryAllergens(RepoConsts.LABEL_SEPARATOR);
        }

        /**
         * <p>Renders involuntary allergens with a custom separator.</p>
         *
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergens(String separator) {
            return renderInvoluntaryAllergens(entity, separator);
        }

        /**
         * <p>Renders involuntary allergens of the given product.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergens(RepositoryEntity target, String separator) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return AllergenHelper.renderInvoluntaryAllergens(AllergenHelper.sorted(maps.getInVolAllergens()),
                    AllergenHelper.sorted(maps.getAllInVolAllergens()), AllergenHelper.sorted(maps.getAllAllergens()), locale(), safe(separator), mlNodeService,
                    associationService);
        }

        /**
         * <p>Renders involuntary allergens of the given product, forcing the locale.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @param localeCode the target locale (e.g. {@code "fr"}, {@code "en"}, {@code "fr_CA"})
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergensForLocale(RepositoryEntity target, String localeCode, String separator) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return AllergenHelper.renderInvoluntaryAllergens(AllergenHelper.sorted(maps.getInVolAllergens()),
                    AllergenHelper.sorted(maps.getAllInVolAllergens()), AllergenHelper.sorted(maps.getAllAllergens()), parse(localeCode), safe(separator),
                    mlNodeService, associationService);
        }

        /**
         * <p>Renders involuntary allergens on the current entity, forcing the locale.</p>
         *
         * @param localeCode the target locale (e.g. {@code "fr"}, {@code "en"}, {@code "fr_CA"})
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergensForLocale(String localeCode, String separator) {
            return renderInvoluntaryAllergensForLocale(entity, localeCode, separator);
        }

        /**
         * <p>Renders involuntary allergens present in process sources.</p>
         *
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergenInProcess() {
            return renderInvoluntaryAllergenInProcess(RepoConsts.LABEL_SEPARATOR);
        }

        /**
         * <p>Renders involuntary allergens present in process sources with a custom separator.</p>
         *
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergenInProcess(String separator) {
            return renderInvoluntaryAllergenInProcess(entity, separator);
        }

        /**
         * <p>Renders involuntary allergens present in process sources of the given product.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergenInProcess(RepositoryEntity target, String separator) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return AllergenHelper.renderInvoluntaryAllergens(AllergenHelper.sorted(maps.getInVolAllergensProcess()),
                    AllergenHelper.sorted(maps.getAllInVolAllergensProcess()), AllergenHelper.sorted(maps.getAllAllergens()), locale(), safe(separator),
                    mlNodeService, associationService);
        }

        /**
         * <p>Renders involuntary process allergens of the given product, forcing the locale.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @param localeCode the target locale (e.g. {@code "fr"}, {@code "en"}, {@code "fr_CA"})
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergenInProcessForLocale(RepositoryEntity target, String localeCode, String separator) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return AllergenHelper.renderInvoluntaryAllergens(AllergenHelper.sorted(maps.getInVolAllergensProcess()),
                    AllergenHelper.sorted(maps.getAllInVolAllergensProcess()), AllergenHelper.sorted(maps.getAllAllergens()), parse(localeCode), safe(separator),
                    mlNodeService, associationService);
        }

        /**
         * <p>Renders involuntary process allergens on the current entity, forcing the locale.</p>
         *
         * @param localeCode the target locale
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryAllergenInProcessForLocale(String localeCode, String separator) {
            return renderInvoluntaryAllergenInProcessForLocale(entity, localeCode, separator);
        }

        /**
         * <p>Renders involuntary allergens present in raw material sources.</p>
         *
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryInRawMaterial() {
            return renderInvoluntaryInRawMaterial(RepoConsts.LABEL_SEPARATOR);
        }

        /**
         * <p>Renders involuntary allergens present in raw material sources with a custom separator.</p>
         *
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryInRawMaterial(String separator) {
            return renderInvoluntaryInRawMaterial(entity, separator);
        }

        /**
         * <p>Renders involuntary allergens present in raw material sources of the given product.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryInRawMaterial(RepositoryEntity target, String separator) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return AllergenHelper.renderInvoluntaryAllergens(AllergenHelper.sorted(maps.getInVolAllergensRawMaterial()),
                    AllergenHelper.sorted(maps.getAllInVolAllergensRawMaterial()), AllergenHelper.sorted(maps.getAllAllergens()), locale(), safe(separator),
                    mlNodeService, associationService);
        }

        /**
         * <p>Renders involuntary raw material allergens of the given product, forcing the locale.</p>
         *
         * @param target a {@link fr.becpg.repo.repository.RepositoryEntity} object
         * @param localeCode the target locale (e.g. {@code "fr"}, {@code "en"}, {@code "fr_CA"})
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryInRawMaterialForLocale(RepositoryEntity target, String localeCode, String separator) {
            AllergenHelper.AllergenMaps maps = extract(target);
            return AllergenHelper.renderInvoluntaryAllergens(AllergenHelper.sorted(maps.getInVolAllergensRawMaterial()),
                    AllergenHelper.sorted(maps.getAllInVolAllergensRawMaterial()), AllergenHelper.sorted(maps.getAllAllergens()), parse(localeCode),
                    safe(separator), mlNodeService, associationService);
        }

        /**
         * <p>Renders involuntary raw material allergens on the current entity, forcing the locale.</p>
         *
         * @param localeCode the target locale
         * @param separator a {@link java.lang.String} object
         * @return a {@link java.lang.String} object
         */
        public String renderInvoluntaryInRawMaterialForLocale(String localeCode, String separator) {
            return renderInvoluntaryInRawMaterialForLocale(entity, localeCode, separator);
        }

        private AllergenHelper.AllergenMaps extract(RepositoryEntity target) {
            ProductData productData = resolveProduct(target);
            if (productData == null) {
                return new AllergenHelper.AllergenMaps();
            }
            try {
                return AllergenHelper.extract(productData, alfrescoRepository, nodeService);
            } catch (RuntimeException e) {
                logger.warn("Failed to extract allergens for " + productData.getNodeRef() + ": " + e.getMessage());
                return new AllergenHelper.AllergenMaps();
            }
        }

        private ProductData resolveProduct(RepositoryEntity target) {
            if (target instanceof ProductData product) {
                return product;
            }
            if ((target != null) && (target.getNodeRef() != null)) {
                RepositoryEntity loaded = alfrescoRepository.findOne(target.getNodeRef());
                if (loaded instanceof ProductData product) {
                    return product;
                }
            }
            return null;
        }

        private List<NodeRef> sortedList(Set<NodeRef> set) {
            if ((set == null) || set.isEmpty()) {
                return Collections.emptyList();
            }
            return List.copyOf(set);
        }

        private Locale locale() {
            Locale current = I18NUtil.getLocale();
            return (current != null) ? current : Locale.getDefault();
        }

        private Locale parse(String localeCode) {
            if ((localeCode == null) || localeCode.isBlank()) {
                return locale();
            }
            try {
                return MLTextHelper.parseLocale(localeCode);
            } catch (RuntimeException e) {
                logger.warn("Invalid locale code '" + localeCode + "', falling back on current locale: " + e.getMessage());
                return locale();
            }
        }

        private String safe(String separator) {
            return (separator != null) ? separator : RepoConsts.LABEL_SEPARATOR;
        }
    }

}
