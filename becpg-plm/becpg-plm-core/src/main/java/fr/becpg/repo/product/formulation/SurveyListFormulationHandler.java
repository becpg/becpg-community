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
import java.util.stream.Stream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
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

	private NamespaceService namespaceService;

	private AlfrescoRepository<BeCPGDataObject> alfrescoRepository;

	private AssociationService associationService;

	/**
	 * <p>
	 * Setter for the field <code>namespaceService</code>.
	 * </p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService}
	 *                    object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>
	 * Setter for the field <code>alfrescoRepository</code>.
	 * </p>
	 *
	 * @param alfrescoRepository a
	 *                           {@link fr.becpg.repo.repository.AlfrescoRepository}
	 *                           object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<BeCPGDataObject> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>
	 * Setter for the field <code>associationService</code>.
	 * </p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService}
	 *                           object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public boolean process(ProductData formulatedProduct) {
		// Extract key data from the formulated product.
		final List<NodeRef> packMaterialListCharactNodeRefs = getPackMaterialListCharactNodeRefs(formulatedProduct);
		final NodeRef hierarchyNodeRef = getHierarchyNodeRef(formulatedProduct);
		final QName productTypeQName = getTypeQName(formulatedProduct);
		final List<NodeRef> subsidiaryRefs = formulatedProduct.getSubsidiaryRefs();
		final List<NodeRef> plants = formulatedProduct.getPlants();
		final Set<SurveyQuestion> surveyQuestions = new HashSet<>();

		logger.debug("SurveyQuestionFormulationHandler::process() <- " + formulatedProduct.getNodeRef());

		// Create a predicate that checks the product type against the question's linked types.

		// Build criteria for linking survey questions.
		final Map<Criterion, List<? extends Serializable>> criteriaNodeRefs = buildCriteriaNodeRefs(packMaterialListCharactNodeRefs, hierarchyNodeRef, productTypeQName,
				subsidiaryRefs != null ? subsidiaryRefs : new ArrayList<>(), plants != null ? plants : new ArrayList<>());

		// Process associations based on the criteria.
		processSurveyQuestionAssociations(criteriaNodeRefs, surveyQuestions);

		// Update the survey lists for the formulated product.
		updateSurveyLists(formulatedProduct, surveyQuestions);

		return true;
	}

	/**
	 * Extracts the list of character node references from the PackMaterialList.
	 */
	private List<NodeRef> getPackMaterialListCharactNodeRefs(ProductData formulatedProduct) {
		return CollectionUtils.emptyIfNull(formulatedProduct.getPackMaterialList()).stream().map(PackMaterialListDataItem::getCharactNodeRef)
				.distinct().toList();
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
	private Map<Criterion, List<? extends Serializable>> buildCriteriaNodeRefs(List<NodeRef> packMaterialListCharactNodeRefs, NodeRef hierarchyNodeRef,
			QName productTypeQName, List<NodeRef> subsidiaryRefs, List<NodeRef> plants) {
		final Map<String, QName> qNameCache = new HashMap<>();
		return Map.of(
				new Criterion(true, SurveyModel.ASSOC_SURVEY_FS_LINKED_CHARACT_REFS, "PackMaterialListDataItem",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedCharactRefs())
								|| surveyQuestion.getFsLinkedCharactRefs().stream().anyMatch(packMaterialListCharactNodeRefs::contains)),
					packMaterialListCharactNodeRefs,
				new Criterion(true, SurveyModel.ASSOC_SURVEY_FS_LINKED_HIERARCHY, "Hierarchy",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedHierarchy())
								|| surveyQuestion.getFsLinkedHierarchy().contains(hierarchyNodeRef)),
					hierarchyNodeRef != null ? Collections.singletonList(hierarchyNodeRef) : Collections.emptyList(),
				new Criterion(false, SurveyModel.PROP_SURVEY_FS_LINKED_TYPE, "Type",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedTypes())
								|| surveyQuestion.getFsLinkedTypes().stream()
										.map(typeName -> qNameCache.computeIfAbsent(typeName,
												q -> QName.createQName(typeName, namespaceService)))
										.anyMatch(productTypeQName::equals)),
						Collections.singletonList(productTypeQName.toPrefixString()),
				new Criterion(true, BeCPGModel.ASSOC_SUBSIDIARY_REF, "SubsidiaryRefs",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getSubsidiaryRefs())
								|| !Collections.disjoint(surveyQuestion.getSubsidiaryRefs(), subsidiaryRefs)),
					subsidiaryRefs, 
				new Criterion(true, BeCPGModel.ASSOC_PLANTS, "Plants", surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getPlants())
						|| !Collections.disjoint(surveyQuestion.getPlants(), plants)),
					plants
		);
	}

	/**
	 * Iterates over the criteria and, for each association found, checks if the corresponding
	 * survey question meets all the criteria. If so, the question is added to the surveyQuestions set.
	 */
	@SuppressWarnings("unchecked")
	private void processSurveyQuestionAssociations(Map<Criterion, List<? extends Serializable>> criteriaNodeRefs,
			Set<SurveyQuestion> surveyQuestions) {
		for (final Entry<Criterion, List<? extends Serializable>> entry : criteriaNodeRefs.entrySet()) {
			final Criterion criterion = entry.getKey();
			final Stream<NodeRef> nodeRefStream;
			if (criterion.assoc()) {
				nodeRefStream = ((List<NodeRef>) CollectionUtils.emptyIfNull(entry.getValue())).stream()
						.flatMap(criterionNodeRef -> associationService
								.getSourcesAssocs(criterionNodeRef, criterion.qName()).stream());
			} else {
				nodeRefStream = BeCPGQueryBuilder.createQuery().ofType(SurveyModel.TYPE_SURVEY_QUESTION)
						.andPropEquals(criterion.qName(), entry.getValue().get(0).toString())
						.inDB().list().stream();
			}
			nodeRefStream.forEach(surveyQuestionNodeRef -> {
				final Object found = alfrescoRepository.findOne(surveyQuestionNodeRef);
				if (found instanceof SurveyQuestion surveyQuestion) {
					// Check that the survey question passes the type filter and all other criteria.
					boolean criteriaMatch = criteriaNodeRefs.keySet().stream().map(Criterion::filter)
							.filter(filter -> !filter.equals(criterion.filter())).allMatch(filter -> filter.test(surveyQuestion));
					if (criteriaMatch) {
						logger.debug(String.format("Found SurveyQuestion %s matching %s criteria", surveyQuestion.getNodeRef(),
								criterion.displayedName()));
						surveyQuestions.add(surveyQuestion);
					}
				}
			});
		}
	}

	/**
	 * Updates the survey lists for the formulated product by adding any missing survey questions.
	 * If not in a transient context, the data is saved to the repository.
	 */
	private void updateSurveyLists(ProductData formulatedProduct, Set<SurveyQuestion> surveyQuestions) {
		final Map<String, List<SurveyListDataItem>> namesSurveyLists = SurveyableEntityHelper.getNamesSurveyLists(alfrescoRepository,
				formulatedProduct);
		for (final SurveyQuestion surveyQuestion : surveyQuestions) {
			final NodeRef surveyQuestionNodeRef = surveyQuestion.getNodeRef();
			final String fsSurveyListName = surveyQuestion.getFsSurveyListName();
			if (namesSurveyLists.containsKey(fsSurveyListName)) {
				final List<SurveyListDataItem> surveyLists = namesSurveyLists.computeIfAbsent(fsSurveyListName, unused -> { 
					final List<SurveyListDataItem> empty = new ArrayList<>();
					if (SurveyableEntityHelper.isDefault(fsSurveyListName)) {
						formulatedProduct.setSurveyList(empty);
					}
					return empty;
				});
				boolean alreadyPresent = surveyLists.stream().map(SurveyListDataItem::getQuestion)
								.anyMatch(nodeRef -> nodeRef.equals(surveyQuestionNodeRef));
				if (!alreadyPresent) {
					logger.debug(String.format("Creating SurveyList with SurveyQuestion %s into %s", surveyQuestionNodeRef, fsSurveyListName));
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
					.forEach(entry -> alfrescoRepository.saveDataList(dataListContainerNodeRef, SurveyModel.TYPE_SURVEY_LIST, entry.getKey(),
							entry.getValue()));
		}
	}

	/**
	 * A simple record to group the citerion type (FALSE = prop, TRUE = assoc), QName, a display name, a predicate filter and a transformer together.
	 */
	private record Criterion(boolean assoc, QName qName, String displayedName, Predicate<SurveyQuestion> filter) {
	}

	private QName getTypeQName(ProductData formulatedProduct) {
		final Class<?> clazz = formulatedProduct.getClass();
		if (clazz.isAnnotationPresent(AlfType.class) && clazz.isAnnotationPresent(AlfQname.class)) {
			return QName.createQName(clazz.getDeclaredAnnotation(AlfQname.class).qname(), namespaceService);
		}
		return null;
	}
}
