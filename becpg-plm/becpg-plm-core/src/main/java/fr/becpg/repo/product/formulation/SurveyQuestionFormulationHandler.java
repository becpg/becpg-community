package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.data.SurveyList;
import fr.becpg.repo.survey.data.SurveyQuestion;

/**
 * <p>
 * SurveyQuestionFormulationHandler class.
 * </p>
 *
 * @author Alexandre Masanes
 * @version $Id: $Id
 */
public class SurveyQuestionFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(SurveyQuestionFormulationHandler.class);

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
		final Predicate<SurveyQuestion> qNameFilter = surveyQuestion -> CollectionUtils
				.isEmpty(surveyQuestion.getFsLinkedTypes())
				|| surveyQuestion.getFsLinkedTypes().stream().map(typeName -> qNameCache.computeIfAbsent(typeName,
						__ -> QName.createQName(typeName, namespaceService))).anyMatch(qName::equals);
		final Set<SurveyQuestion> surveyQuestions = new HashSet<>();
		final Map<String, List<SurveyList>> namesSurveyLists = new HashMap<>(4, 1);
		final List<SurveyList> deletedSurveyLists = new ArrayList<>();
		final String surveyListBaseName = "surveyList";
		final QName surveyListQName = QName.createQName("survey:" + surveyListBaseName, namespaceService);
		final boolean cacheOnlyEnable = L2CacheSupport.isCacheOnlyEnable();
		for (int i = 0; i < 4; ++i) {
			final String surveyListName = surveyListBaseName + (i == 0 ? "" : "@" + i);
			namesSurveyLists.put(surveyListName, (i == 0
					? formulatedProduct.getSurveyList()
					: alfrescoRepository
							.loadDataList(formulatedProduct.getNodeRef(), surveyListName, surveyListQName).stream()
							.map(SurveyList.class::cast).collect(Collectors.toCollection(ArrayList::new))));
			if (cacheOnlyEnable) break;
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
		for (final Entry<String, List<SurveyList>> nameSurveyLists : namesSurveyLists.entrySet()) {
			final String fsSurveyListName = nameSurveyLists.getKey();
			final List<SurveyList> surveyLists = nameSurveyLists.getValue();
			for (final SurveyList surveyList : List.copyOf(surveyLists)) {
				final SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository
						.findOne(surveyList.getQuestion());
				if (surveyList.isGenerated() && (!surveyQuestions.contains(surveyQuestion)
						|| !Objects.equals(surveyQuestion.getFsSurveyListName(), fsSurveyListName))) {
					logger.debug(String.format("Deleting SurveyList %s from %s with SurveyQuestion %s",
							surveyList.getNodeRef(), fsSurveyListName, surveyList.getQuestion()));
					if (surveyListBaseName.equals(fsSurveyListName)) {
						surveyLists.remove(surveyList);
					} else {
						deletedSurveyLists.add(surveyList);
					}
				}
			}
		}
		for (final SurveyQuestion surveyQuestion : surveyQuestions) {
			final NodeRef surveyQuestionNodeRef = surveyQuestion.getNodeRef();
			final String fsSurveyListName = surveyQuestion.getFsSurveyListName();
			if (namesSurveyLists.containsKey(fsSurveyListName)) {
				final List<SurveyList> surveyLists = namesSurveyLists.get(fsSurveyListName);
				if (surveyLists.stream().map(SurveyList::getQuestion).noneMatch(surveyQuestionNodeRef::equals)) {
					logger.debug(String.format("Creating SurveyList with SurveyQuestion %s into %s",
							surveyQuestionNodeRef, fsSurveyListName));
					surveyLists.add(new SurveyList(surveyQuestionNodeRef, true));
				}
			}
		}
		if (!cacheOnlyEnable) {
			final NodeRef dataListContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(formulatedProduct);
			namesSurveyLists.entrySet().stream()
					.filter(nameSurveyLists -> !surveyListBaseName.equals(nameSurveyLists.getKey()))
					.forEach(nameSurveyLists -> alfrescoRepository.saveDataList(dataListContainerNodeRef,
							surveyListQName, nameSurveyLists.getKey(), nameSurveyLists.getValue()));
			deletedSurveyLists.stream().map(SurveyList::getNodeRef).forEach(alfrescoRepository::delete);
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
