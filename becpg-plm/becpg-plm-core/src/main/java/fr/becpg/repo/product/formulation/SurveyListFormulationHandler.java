package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	private static final byte NB_OF_SURVEY_LISTS = 4;
	
	private static final String SURVEY_LIST_BASE_NAME = SurveyModel.TYPE_SURVEY_LIST.getLocalName();
	
	public static final List<String> SURVEY_LIST_NAMES;
	
	static {
		final List<String> surveyListNames = new ArrayList<>(NB_OF_SURVEY_LISTS);
		for (byte i = 0; i < NB_OF_SURVEY_LISTS; ++i) {
			surveyListNames.add(SURVEY_LIST_BASE_NAME + (i == 0 ? "" : "@" + i));
		}
		SURVEY_LIST_NAMES = Collections.unmodifiableList(surveyListNames);
	}

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

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {
		final List<NodeRef> packMaterialListCharactNodeRefs = CollectionUtils
				.emptyIfNull(formulatedProduct.getPackMaterialList()).stream()
				.map(PackMaterialListDataItem::getCharactNodeRef).distinct().toList();
		final NodeRef hierarchyNodeRef = ObjectUtils.defaultIfNull(formulatedProduct.getHierarchy2(),
				formulatedProduct.getHierarchy1());
		final QName qName = getTypeQName(formulatedProduct);
		final Map<String, QName> qNameCache = new HashMap<>();
		// NO type criterion OR type criteria INCLUDES formulated product's type
		final Predicate<SurveyQuestion> qNameFilter = surveyQuestion -> CollectionUtils
				.isEmpty(surveyQuestion.getFsLinkedTypes())
				|| surveyQuestion.getFsLinkedTypes().stream().map(typeName -> qNameCache.computeIfAbsent(typeName,
						__ -> QName.createQName(typeName, namespaceService))).anyMatch(qName::equals);
		final Set<SurveyQuestion> surveyQuestions = new HashSet<>();
		final Map<String, List<SurveyListDataItem>> namesSurveyLists = new HashMap<>(NB_OF_SURVEY_LISTS, 1);
		// is the formulation launched from a unit test ?
		final boolean transientEntity = formulatedProduct.getNodeRef() == null;
		for (final String surveyListName : SURVEY_LIST_NAMES) {
			namesSurveyLists.put(surveyListName, (SURVEY_LIST_BASE_NAME.equals(surveyListName)
					? formulatedProduct.getSurveyList()
					: alfrescoRepository.loadDataList(formulatedProduct.getNodeRef(), surveyListName, SurveyModel.TYPE_SURVEY_LIST)
							.stream().map(SurveyListDataItem.class::cast)
							.collect(Collectors.toCollection(ArrayList::new))));
			// if we're in a test context we only add the default survey list to the map
			if (transientEntity) break;
		}
		logger.debug("SurveyQuestionFormulationHandler::process() <- " + formulatedProduct.getNodeRef());
		for (final NodeRef nodeRef : packMaterialListCharactNodeRefs) {
			for (final NodeRef surveyQuestionNodeRef : associationService.getSourcesAssocs(nodeRef,
					SurveyModel.ASSOC_SURVEY_FS_LINKED_CHARACT_REFS)) {
				final SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository
						.findOne(surveyQuestionNodeRef);
				if (qNameFilter.test(surveyQuestion) && (CollectionUtils.isEmpty(surveyQuestion.getFsLinkedHierarchy())
						|| surveyQuestion.getFsLinkedHierarchy().contains(hierarchyNodeRef))) {
					logger.debug(String.format("Found SurveyQuestion %s matching PackMaterialListDataItem criteria",
							surveyQuestion.getNodeRef()));
					surveyQuestions.add(surveyQuestion);
				}
			}
		}
		if (hierarchyNodeRef != null) {
			for (final NodeRef surveyQuestionNodeRef : associationService.getSourcesAssocs(hierarchyNodeRef,
					SurveyModel.ASSOC_SURVEY_FS_LINKED_HIERARCHY)) {
				final SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository
						.findOne(surveyQuestionNodeRef);
				if (qNameFilter.test(surveyQuestion)
						&& (CollectionUtils.isEmpty(surveyQuestion.getFsLinkedCharactRefs())
								|| surveyQuestion.getFsLinkedCharactRefs().stream()
										.anyMatch(packMaterialListCharactNodeRefs::contains))) {
					logger.debug(String.format("Found SurveyQuestion %s matching Hierarchy criteria",
							surveyQuestion.getNodeRef()));
					surveyQuestions.add(surveyQuestion);
				}
			}
		}
		for (final SurveyQuestion surveyQuestion : surveyQuestions) {
			final NodeRef surveyQuestionNodeRef = surveyQuestion.getNodeRef();
			final String fsSurveyListName = surveyQuestion.getFsSurveyListName();
			if (namesSurveyLists.containsKey(fsSurveyListName)) {
				final List<SurveyListDataItem> surveyLists = namesSurveyLists.get(fsSurveyListName);
				if (surveyLists.stream().map(SurveyListDataItem::getQuestion).noneMatch(surveyQuestionNodeRef::equals)) {
					logger.debug(String.format("Creating SurveyList with SurveyQuestion %s into %s",
							surveyQuestionNodeRef, fsSurveyListName));
					surveyLists.add(new SurveyListDataItem(surveyQuestionNodeRef, true));
				}
			}
		}
		// if we're not in a test context we apply the update of the additional survey lists to the repository
		if (!transientEntity) {
			final NodeRef dataListContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(formulatedProduct);
			namesSurveyLists.entrySet().stream()
					.filter(nameSurveyLists -> !SURVEY_LIST_BASE_NAME.equals(nameSurveyLists.getKey()))
					.forEach(nameSurveyLists -> alfrescoRepository.saveDataList(dataListContainerNodeRef,
							SurveyModel.TYPE_SURVEY_LIST, nameSurveyLists.getKey(), nameSurveyLists.getValue()));
		}
		return true;
	}
	
	private QName getTypeQName(ProductData formulatedProduct) {
		final Class<?> clazz = formulatedProduct.getClass();
		if (clazz.isAnnotationPresent(AlfType.class) && clazz.isAnnotationPresent(AlfQname.class)) {
			return QName.createQName(clazz.getDeclaredAnnotation(AlfQname.class).qname(), namespaceService);
		}
		return null;
	}
}