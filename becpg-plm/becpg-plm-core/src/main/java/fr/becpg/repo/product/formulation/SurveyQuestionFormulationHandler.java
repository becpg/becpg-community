package fr.becpg.repo.product.formulation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.data.SurveyList;
import fr.becpg.repo.survey.data.SurveyQuestion;

/**
 * <p>
 * PackagingMaterialFormulationHandler class.
 * </p>
 *
 * @author Alexandre Masanes
 * @version $Id: $Id
 */
public class SurveyQuestionFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(SurveyQuestionFormulationHandler.class);

	private NodeService nodeService;

	private AlfrescoRepository<BeCPGDataObject> alfrescoRepository;

	private AssociationService associationService;

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService}
	 *                    object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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
		final List<NodeRef> packMaterialListDataItemNodeRefs = CollectionUtils
				.emptyIfNull(formulatedProduct.getPackMaterialList()).stream().map(PackMaterialListDataItem::getNodeRef)
				.collect(Collectors.toList());
		final NodeRef hierarchyNodeRef = ObjectUtils.defaultIfNull(formulatedProduct.getHierarchy2(),
				formulatedProduct.getHierarchy1());
		final QName qName = nodeService.getType(formulatedProduct.getNodeRef());
		final Predicate<SurveyQuestion> qNameFilter = surveyQuestion -> CollectionUtils
				.isEmpty(surveyQuestion.getFsLinkedTypes()) || surveyQuestion.getFsLinkedTypes().contains(qName);
		final Set<SurveyQuestion> surveyQuestions = new HashSet<>();
		final List<SurveyList> surveyLists = formulatedProduct.getSurveyList();
		logger.debug("SurveyQuestionFormulationHandler::process() <- " +formulatedProduct.getNodeRef());
		for (final NodeRef packMaterialListDataItemNodeRef : packMaterialListDataItemNodeRefs) { 
			final NodeRef nodeRef = associationService.getTargetAssoc(packMaterialListDataItemNodeRef, PackModel.ASSOC_PACK_MATERIAL_LIST_MATERIAL);
			associationService.getSourcesAssocs(nodeRef, SurveyModel.ASSOC_SURVEY_FS_LINKED_CHARACT_REFS).stream()
					.map(alfrescoRepository::findOne).map(SurveyQuestion.class::cast).filter(qNameFilter)
					.filter(surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedHierarchy())
							|| surveyQuestion.getFsLinkedHierarchy().contains(hierarchyNodeRef))
					.peek(surveyQuestion -> logger.debug(
							String.format("Found SurveyQuestion %s matching PackMaterialListDataItem criteria",
									surveyQuestion.getNodeRef())))
					.forEach(surveyQuestions::add);
		}
		if (hierarchyNodeRef != null) {
			associationService.getSourcesAssocs(hierarchyNodeRef, SurveyModel.ASSOC_SURVEY_FS_LINKED_HIERARCHY).stream()
					.map(alfrescoRepository::findOne).map(SurveyQuestion.class::cast).filter(qNameFilter)
					.filter(surveyQuestion -> CollectionUtils.isEmpty(surveyQuestion.getFsLinkedCharactRefs())
							|| surveyQuestion.getFsLinkedCharactRefs().stream()
									.anyMatch(packMaterialListDataItemNodeRefs::contains))
					.peek(surveyQuestion -> logger.debug(String.format(
							"Found SurveyQuestion %s matching Hierarchy criteria", surveyQuestion.getNodeRef())))
					.forEach(surveyQuestions::add);
		}
		List.copyOf(surveyLists).stream()
				.filter(surveyList -> Optional.of((SurveyQuestion) alfrescoRepository.findOne(surveyList.getQuestion()))
						.map(surveyQuestion -> (CollectionUtils.isNotEmpty(surveyQuestion.getFsLinkedCharactRefs())
								|| CollectionUtils.isNotEmpty(surveyQuestion.getFsLinkedHierarchy()))
								&& !surveyQuestions.contains(surveyQuestion))
						.get())
				.peek(surveyList -> logger.debug(String.format("Deleting SurveyList %s with SurveyQuestion %s",
						surveyList.getNodeRef(), surveyList.getQuestion())))
				.forEach(surveyLists::remove);
		surveyQuestions.stream().map(SurveyQuestion::getNodeRef)
				.filter(Predicate
						.not(surveyLists.stream().map(SurveyList::getQuestion).collect(Collectors.toList())::contains))
				.map(SurveyList::new)
				.peek(surveyList -> logger
						.debug(String.format("Creating SurveyList with SurveyQuestion %s", surveyList.getQuestion())))
				.forEach(surveyLists::add);
		return true;
	}
}
