package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

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
		final Map<String, QName> qNameCache = new HashMap<>();
		final Set<SurveyQuestion> surveyQuestions = new HashSet<>();

		logger.debug("SurveyQuestionFormulationHandler::process() <- " + formulatedProduct.getNodeRef());

		// Create a predicate that checks the product type against the question's linked types.
		final Predicate<SurveyQuestion> qNameFilter = surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedTypes()) || surveyQuestion
				.getFsLinkedTypes().stream().map(typeName -> qNameCache.computeIfAbsent(typeName, q -> QName.createQName(typeName, namespaceService)))
				.anyMatch(productTypeQName::equals);

		// Build criteria for linking survey questions.
		final Map<Criterion, List<NodeRef>> criteriaNodeRefs = buildCriteriaNodeRefs(packMaterialListCharactNodeRefs, hierarchyNodeRef,
				subsidiaryRefs != null ? subsidiaryRefs : new ArrayList<>(), plants != null ? plants : new ArrayList<>());

		// Process associations based on the criteria.
		processSurveyQuestionAssociations(criteriaNodeRefs, qNameFilter, surveyQuestions);

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
	private Map<Criterion, List<NodeRef>> buildCriteriaNodeRefs(List<NodeRef> packMaterialListCharactNodeRefs, NodeRef hierarchyNodeRef,
			List<NodeRef> subsidiaryRefs, List<NodeRef> plants) {

		return Map.of(
				new Criterion(SurveyModel.ASSOC_SURVEY_FS_LINKED_CHARACT_REFS, "PackMaterialListDataItem",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedCharactRefs())
								|| surveyQuestion.getFsLinkedCharactRefs().stream().anyMatch(packMaterialListCharactNodeRefs::contains)),
				packMaterialListCharactNodeRefs,
				new Criterion(SurveyModel.ASSOC_SURVEY_FS_LINKED_HIERARCHY, "Hierarchy",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedHierarchy())
								|| surveyQuestion.getFsLinkedHierarchy().contains(hierarchyNodeRef)),
				hierarchyNodeRef != null ? Collections.singletonList(hierarchyNodeRef) : Collections.emptyList(),
				new Criterion(BeCPGModel.ASSOC_SUBSIDIARY_REF, "SubsidiaryRefs",
						surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getSubsidiaryRefs())
								|| !Collections.disjoint(surveyQuestion.getSubsidiaryRefs(), subsidiaryRefs)),
				subsidiaryRefs, new Criterion(BeCPGModel.ASSOC_PLANTS, "Plants", surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getPlants())
						|| !Collections.disjoint(surveyQuestion.getPlants(), plants)),
				plants);
	}

	/**
	 * Iterates over the criteria and, for each association found, checks if the corresponding
	 * survey question meets all the criteria. If so, the question is added to the surveyQuestions set.
	 */
	private void processSurveyQuestionAssociations(Map<Criterion, List<NodeRef>> criteriaNodeRefs, Predicate<SurveyQuestion> qNameFilter,
			Set<SurveyQuestion> surveyQuestions) {

		for (final Entry<Criterion, List<NodeRef>> entry : criteriaNodeRefs.entrySet()) {
			final Criterion criterion = entry.getKey();
			for (final NodeRef criterionNodeRef : CollectionUtils.emptyIfNull(entry.getValue())) {
				for (final NodeRef surveyQuestionNodeRef : associationService.getSourcesAssocs(criterionNodeRef, criterion.qName())) {
					final Object found = alfrescoRepository.findOne(surveyQuestionNodeRef);
					if (found instanceof SurveyQuestion surveyQuestion) {
						// Check that the survey question passes the type filter and all other criteria.
						boolean allOtherCriteriaMatch = criteriaNodeRefs.keySet().stream().map(Criterion::filter)
								.filter(filter -> !filter.equals(criterion.filter())).allMatch(filter -> filter.test(surveyQuestion));
						if (qNameFilter.test(surveyQuestion) && allOtherCriteriaMatch) {
							logger.debug(String.format("Found SurveyQuestion %s matching %s criteria", surveyQuestion.getNodeRef(),
									criterion.displayedName()));
							surveyQuestions.add(surveyQuestion);
						}
					}
				}
			}
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
				final List<SurveyListDataItem> surveyLists = namesSurveyLists.get(fsSurveyListName);
				boolean alreadyPresent = surveyLists.stream().map(SurveyListDataItem::getQuestion)
						.anyMatch(nodeRef -> nodeRef.equals(surveyQuestionNodeRef));
				if (!alreadyPresent) {
					logger.debug(String.format("Creating SurveyList with SurveyQuestion %s into %s", surveyQuestionNodeRef, fsSurveyListName));
					surveyLists.add(new SurveyListDataItem(surveyQuestionNodeRef, true));
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
	 * A simple record to group the QName, a display name, and a predicate filter together.
	 */
	private record Criterion(QName qName, String displayedName, Predicate<SurveyQuestion> filter) {
	}

	private QName getTypeQName(ProductData formulatedProduct) {
		final Class<?> clazz = formulatedProduct.getClass();
		if (clazz.isAnnotationPresent(AlfType.class) && clazz.isAnnotationPresent(AlfQname.class)) {
			return QName.createQName(clazz.getDeclaredAnnotation(AlfQname.class).qname(), namespaceService);
		}
		return null;
	}
}
