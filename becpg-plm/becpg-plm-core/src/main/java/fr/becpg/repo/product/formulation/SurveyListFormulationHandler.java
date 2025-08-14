package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyQuestion;
import fr.becpg.repo.survey.helper.SurveyableEntityHelper;

/**
 * <p>
 * SurveyListFormulationHandler class.
 * </p>
 *
 * @author Alexandre Masanes
 * @version $Id: $Id
 */
public class SurveyListFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(SurveyListFormulationHandler.class);

	private static final String CACHE_KEY = SurveyQuestion.class.getName();
	
	private final NamespaceService namespaceService;

	private final AlfrescoRepository<BeCPGDataObject> alfrescoRepository;
	
	private final RepositoryEntityDefReader<ProductData> repositoryEntityDefReader;

	private final BeCPGCacheService beCPGCacheService;

	/**
	 * <p>
	 * Constructor.
	 * </p>
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService}
	 *                    object.
	 * @param alfrescoRepository a
	 *                           {@link fr.becpg.repo.repository.AlfrescoRepository}
	 *                           object.
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object.
	 */
	public SurveyListFormulationHandler(NamespaceService namespaceService, AlfrescoRepository<BeCPGDataObject> alfrescoRepository,
			BeCPGCacheService beCPGCacheService, RepositoryEntityDefReader<ProductData> repositoryEntityDefReader ) {
		super();
		this.namespaceService = namespaceService;
		this.alfrescoRepository = alfrescoRepository;
		this.beCPGCacheService = beCPGCacheService;
		this.repositoryEntityDefReader = repositoryEntityDefReader;
		beCPGCacheService.storeInCache(CACHE_KEY, CACHE_KEY, null);
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {
		if (accept(formulatedProduct)) {

			final Map<String,List<SurveyListDataItem>> namesSurveyLists = SurveyableEntityHelper.getNamesSurveyLists(alfrescoRepository,
					formulatedProduct);

			if (namesSurveyLists.isEmpty()) {
				return true;
			}

			// Extract key data from the formulated product.
			final List<NodeRef> packMaterialListCharactNodeRefs = getPackMaterialListCharactNodeRefs(formulatedProduct);
			final NodeRef hierarchyNodeRef = getHierarchyNodeRef(formulatedProduct);
			final QName productTypeQName = repositoryEntityDefReader.getType(formulatedProduct.getClass());
			final List<NodeRef> subsidiaryRefs = formulatedProduct.getSubsidiaryRefs();
			final List<NodeRef> plants = formulatedProduct.getPlants();
			final Set<SurveyQuestion> surveyQuestions = new HashSet<>();

			logger.debug("Starting SurveyQuestionFormulationHandler::process for " + formulatedProduct.getNodeRef());

			// Create a predicate that checks the product type against the question's linked types.

			// Build criteria for linking survey questions.
			final Map<Criterion, List<? extends Serializable>> criteriaNodeRefs = buildCriteriaNodeRefs(packMaterialListCharactNodeRefs,
					hierarchyNodeRef, productTypeQName, subsidiaryRefs != null ? subsidiaryRefs : new ArrayList<>(),
					plants != null ? plants : new ArrayList<>());

			// Process associations based on the criteria.
			processSurveyQuestionAssociations(criteriaNodeRefs, surveyQuestions);

			// Update the survey lists for the formulated product.
			updateSurveyLists(formulatedProduct, surveyQuestions, namesSurveyLists);

			logger.debug("Ending SurveyQuestionFormulationHandler::process for " + formulatedProduct.getNodeRef());
		}

		return true;
	}

	protected boolean accept(ProductData formulatedProduct) {

		boolean hasSurveyList = (formulatedProduct.getSurveyList() != null)
				&& ((formulatedProduct.getSurveyList() instanceof ArrayList)
						|| alfrescoRepository.hasDataList(formulatedProduct, SurveyModel.TYPE_SURVEY_LIST));

		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| (formulatedProduct instanceof ProductSpecificationData)) && hasSurveyList;
	}

	/**
	 * Extracts the list of character node references from the PackMaterialList.
	 */
	private List<NodeRef> getPackMaterialListCharactNodeRefs(ProductData formulatedProduct) {
		return CollectionUtils.emptyIfNull(formulatedProduct.getPackMaterialList()).stream()
				.map(PackMaterialListDataItem::getCharactNodeRef).distinct().toList();
	}

	/**
	 * Chooses the hierarchy node reference using hierarchy2 if present, otherwise hierarchy1.
	 */
	private NodeRef getHierarchyNodeRef(ProductData formulatedProduct) {
		return ObjectUtils.defaultIfNull(formulatedProduct.getHierarchy2(), formulatedProduct.getHierarchy1());
	}

	/**
	 * Builds a map of criteria to their associated node references.
	 */
	private Map<Criterion, List<? extends Serializable>> buildCriteriaNodeRefs(
			List<NodeRef> packMaterialListCharactNodeRefs, NodeRef hierarchyNodeRef, QName productTypeQName,
			List<NodeRef> subsidiaryRefs, List<NodeRef> plants) {
		final Map<String, QName> qNameCache = new HashMap<>();
		return Map.of(
				new Criterion("PackMaterialListDataItem",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedCharactRefs())
								|| surveyQuestion.getFsLinkedCharactRefs().stream().anyMatch(packMaterialListCharactNodeRefs::contains)),
				packMaterialListCharactNodeRefs,
				new Criterion("Hierarchy",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedHierarchy())
								|| surveyQuestion.getFsLinkedHierarchy().contains(hierarchyNodeRef)),
				hierarchyNodeRef != null ? Collections.singletonList(hierarchyNodeRef) : Collections.emptyList(),
				new Criterion("Type",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedTypes()) || surveyQuestion.getFsLinkedTypes().stream()
								.filter(typeName -> typeName != null && !typeName.isEmpty())
								.map(typeName -> qNameCache.computeIfAbsent(typeName,
										q -> QName.resolveToQName(namespaceService, typeName)))
								.anyMatch(productTypeQName::equals)),
				Collections.singletonList(productTypeQName.toPrefixString()),
				new Criterion("SubsidiaryRefs",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getSubsidiaryRefs())
								|| !Collections.disjoint(surveyQuestion.getSubsidiaryRefs(), subsidiaryRefs)),
				subsidiaryRefs, new Criterion("Plants", surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getPlants())
						|| !Collections.disjoint(surveyQuestion.getPlants(), plants)),
				plants);
	}

	/**
	 * Iterates over the criteria and, for each association found, checks if the corresponding
	 * survey question meets all the criteria. If so, the question is added to the surveyQuestions set.
	 */
	private void processSurveyQuestionAssociations(Map<Criterion, List<? extends Serializable>> criteriaNodeRefs,
			Set<SurveyQuestion> surveyQuestions) {
		final Set<NodeRef> surveyQuestionNodeRefs = surveyQuestions.stream().map(SurveyQuestion::getNodeRef)
				.collect(Collectors.toSet());
		final List<SurveyQuestion> allSurveyQuestions = beCPGCacheService.getFromCache(CACHE_KEY, CACHE_KEY,
				() -> BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION)
						.andPropQuery(SurveyModel.PROP_SURVEY_FS_SURVEY_LIST_NAME,
								SurveyableEntityHelper.SURVEY_LIST_BASE_NAME)
						.inDB().list().stream().map(alfrescoRepository::findOne).map(SurveyQuestion.class::cast)
						.filter(surveyQuestion -> surveyQuestion.getParent() == null).toList());
		for (final Entry<Criterion, List<? extends Serializable>> entry : criteriaNodeRefs.entrySet()) {
			final Criterion criterion = entry.getKey();
			allSurveyQuestions.stream().filter(criterion.filter())
					.filter(surveyQuestion -> !surveyQuestionNodeRefs.contains(surveyQuestion.getNodeRef())).map(SurveyQuestion.class::cast)
					.forEach(surveyQuestion -> {
						// Check that the survey question passes the type filter and all other criteria.
						boolean criteriaMatch = criteriaNodeRefs.keySet().stream().map(Criterion::filter)
								.filter(filter -> !filter.equals(criterion.filter())).allMatch(filter -> filter.test(surveyQuestion));
						if (criteriaMatch) {
							logger.debug(String.format("Found SurveyQuestion %s matching %s criteria", surveyQuestion.getNodeRef(),
									criterion.displayedName()));
							surveyQuestions.add(surveyQuestion);
						}
					});
		}
	}

	/**
	 * Updates the survey lists for the formulated product by adding any missing survey questions.
	 * If not in a transient context, the data is saved to the repository.
	 * @param namesSurveyLists
	 */
	private void updateSurveyLists(ProductData formulatedProduct, Set<SurveyQuestion> surveyQuestions,
			Map<String, List<SurveyListDataItem>> namesSurveyLists) {
		for (final SurveyQuestion surveyQuestion : surveyQuestions) {
			final NodeRef surveyQuestionNodeRef = surveyQuestion.getNodeRef();
			final String fsSurveyListName = surveyQuestion.getFsSurveyListName();
			if (namesSurveyLists.containsKey(fsSurveyListName)) {
				final List<SurveyListDataItem> surveyLists = namesSurveyLists.computeIfAbsent(fsSurveyListName,
						unused -> {
					final List<SurveyListDataItem> empty = new ArrayList<>();
					if (SurveyableEntityHelper.isDefault(fsSurveyListName)) {
						formulatedProduct.setSurveyList(empty);
					}
					return empty;
				});
				boolean alreadyPresent = surveyLists.stream().map(SurveyListDataItem::getQuestion)
						.anyMatch(nodeRef -> nodeRef.equals(surveyQuestionNodeRef));
				if (!alreadyPresent) {
					logger.debug(String.format("Creating SurveyList with SurveyQuestion %s into %s",
							surveyQuestionNodeRef, fsSurveyListName));
					final SurveyListDataItem surveyListDataItem = new SurveyListDataItem(surveyQuestionNodeRef, true);
					surveyListDataItem.setSort(surveyQuestion.getSort());
					surveyLists.add(surveyListDataItem);
				}
			}
		}

		// If we are not in a test (transient) context, save the survey lists.
		if (!SurveyableEntityHelper.isTransient(formulatedProduct)) {
			final NodeRef dataListContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(formulatedProduct);
			namesSurveyLists.entrySet().stream().filter(entry -> !SurveyableEntityHelper.isDefault(entry.getKey()))
					.forEach(entry -> alfrescoRepository.saveDataList(dataListContainerNodeRef,
							SurveyModel.TYPE_SURVEY_LIST, entry.getKey(), entry.getValue()));
		}
	}

	/**
	 * A simple record to group a display name, a predicate filter and a transformer together.
	 */
	private record Criterion(String displayedName, Predicate<SurveyQuestion> filter) {
	}

}
