package fr.becpg.repo.product.formulation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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
				.emptyIfNull(formulatedProduct.getPackMaterialList()).stream().map(PackMaterialListDataItem::getCharactNodeRef)
				.distinct()
				.toList();
		final NodeRef hierarchyNodeRef = ObjectUtils.defaultIfNull(formulatedProduct.getHierarchy2(),
				formulatedProduct.getHierarchy1());
		final QName qName = getTypeQName(formulatedProduct);
		final Predicate<SurveyQuestion> qNameFilter = surveyQuestion -> CollectionUtils
				.isEmpty(surveyQuestion.getFsLinkedTypes()) || surveyQuestion.getFsLinkedTypes().contains(qName);
		final Set<SurveyQuestion> surveyQuestions = new HashSet<>();
		final List<SurveyList> surveyLists = formulatedProduct.getSurveyList();
		logger.debug("SurveyQuestionFormulationHandler::process() <- " + formulatedProduct.getNodeRef());
		for (final NodeRef nodeRef : packMaterialListCharactNodeRefs) {
			for (final NodeRef surveyQuestionNodeRef : associationService.getSourcesAssocs(nodeRef, SurveyModel.ASSOC_SURVEY_FS_LINKED_CHARACT_REFS)) {
				final SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository.findOne(surveyQuestionNodeRef);
				if (qNameFilter.test(surveyQuestion) && (CollectionUtils.isEmpty(surveyQuestion.getFsLinkedHierarchy())
						|| surveyQuestion.getFsLinkedHierarchy().contains(hierarchyNodeRef))) {
					logger.debug(
							String.format("Found SurveyQuestion %s matching PackMaterialListDataItem criteria",
									surveyQuestion.getNodeRef()));
					surveyQuestions.add(surveyQuestion);
				}
			}
		}
		if (hierarchyNodeRef != null) {
			for (final NodeRef surveyQuestionNodeRef : associationService.getSourcesAssocs(hierarchyNodeRef, SurveyModel.ASSOC_SURVEY_FS_LINKED_CHARACT_REFS)) {
				final SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository.findOne(surveyQuestionNodeRef);
				if (qNameFilter.test(surveyQuestion)
						&& (CollectionUtils.isEmpty(surveyQuestion.getFsLinkedCharactRefs())
								|| surveyQuestion.getFsLinkedCharactRefs().stream()
										.anyMatch(packMaterialListCharactNodeRefs::contains))) {
					logger.debug(
							String.format("Found SurveyQuestion %s matching Hierarchy criteria",
									surveyQuestion.getNodeRef()));
					surveyQuestions.add(surveyQuestion);
				}
			}
		}
		for (final SurveyList surveyList : List.copyOf(surveyLists)) {
			final SurveyQuestion surveyQuestion = (SurveyQuestion) alfrescoRepository.findOne(surveyList.getQuestion());
			if ((CollectionUtils.isNotEmpty(surveyQuestion.getFsLinkedCharactRefs())
					|| CollectionUtils.isNotEmpty(surveyQuestion.getFsLinkedHierarchy()))
					&& !surveyQuestions.contains(surveyQuestion)) {
				logger.debug(String.format("Deleting SurveyList %s with SurveyQuestion %s", surveyList.getNodeRef(),
						surveyList.getQuestion()));
				surveyLists.remove(surveyList);
			}
		}
		for (final SurveyQuestion surveyQuestion : surveyQuestions) {
			final NodeRef surveyQuestionNodeRef = surveyQuestion.getNodeRef();
			if (surveyLists.stream().map(SurveyList::getQuestion).noneMatch(surveyQuestionNodeRef::equals)) {
				logger.debug(String.format("Creating SurveyList with SurveyQuestion %s", surveyQuestionNodeRef));
				surveyLists.add(new SurveyList(surveyQuestionNodeRef));
			}
		}
		return true;
	}
	
	private QName getTypeQName(ProductData formulatedProduct) {
		if (formulatedProduct.getClass().isAnnotationPresent(AlfType.class)
				&& formulatedProduct.getClass().isAnnotationPresent(AlfQname.class)) {
			return QName.createQName(formulatedProduct.getClass().getDeclaredAnnotation(AlfQname.class).qname(),
					namespaceService);
		}
		return null;
	}
}
