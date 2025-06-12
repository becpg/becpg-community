package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.document.DocumentTypeItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.helper.SurveyableEntityHelper;

/**
 * <p>
 * DocumentFormulationHandler class.
 * </p>
 *
 * <p>
 * Handles the document creation and management based on DocumentTypeItem configurations.
 * Documents can be automatically generated based on product characteristics, including label claims.
 * </p>
 *
 * <p>
 * Key features:
 * <ul>
 *   <li>Automatic document creation based on product characteristics</li>
 *   <li>Calculation of mandatory status from properties, linked characteristics, or formulas</li>
 *   <li>Special handling of label claims - claims must be marked as claimed (isClaimed=true) to make documents mandatory</li>
 *   <li>Synchronization of document states with product states</li>
 *   <li>Support for dynamic document naming through templates</li>
 * </ul>
 * </p>
 */
public class DocumentFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(DocumentFormulationHandler.class);

	public static final String MESSAGE_DOCUMENT_FORMULATION_ERROR = "message.formulate.document.error";

	private static final String DEFAULT_SUPPLIER_DOCUMENTS_PATH = "SupplierDocuments";

	private static final String MESSAGE_DOCUMENT_MANDATORY = "message.formulate.document.mandatory";

	private FileFolderService fileFolderService;
	private NodeService nodeService;
	private NodeService mlNodeService;
	private NamespaceService namespaceService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	private AssociationService associationService;
	private EntityService entityService;
	private RepoService repoService;
	private ExpressionService expressionService;
	private SpelFormulaService formulaService;
	private BehaviourFilter policyBehaviourFilter;

	/**
	 * <p>
	 * Setter for the field <code>fileFolderService</code>.
	 * </p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object.
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>namespaceService</code>.
	 * </p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>
	 * Setter for the field <code>alfrescoRepository</code>.
	 * </p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object for accessing repository entities.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>
	 * Setter for the field <code>associationService</code>.
	 * </p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>
	 * Setter for the field <code>mlNodeService</code>.
	 * </p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object for multilingual node operations.
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>entityService</code>.
	 * </p>
	 *
	 * @param entityService a {@link fr.becpg.repo.entity.EntityService} object for entity operations.
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * <p>
	 * Setter for the field <code>repoService</code>.
	 * </p>
	 *
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object for repository operations.
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	/**
	 * <p>
	 * Setter for the field <code>expressionService</code>.
	 * </p>
	 *
	 * @param expressionService a {@link fr.becpg.repo.expressions.ExpressionService} object for expression evaluation.
	 */
	public void setExpressionService(ExpressionService expressionService) {
		this.expressionService = expressionService;
	}

	/**
	 * <p>
	 * Setter for the field <code>formulaService</code>.
	 * </p>
	 *
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object for SpEL formula evaluation.
	 */
	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Process method that manages documents for a product based on document types.
	 * This method performs the following tasks:
	 * <ol>
	 *   <li>Identifies existing documents for the product</li>
	 *   <li>Creates new documents for matching document types that don't have documents yet</li>
	 *   <li>Updates the mandatory status of all documents based on document type settings</li>
	 *   <li>Updates document states to synchronize with the product state</li>
	 * </ol>
	 * </p>
	 * <p>
	 * For label claims, this process ensures that documents are only marked as mandatory
	 * when the associated claims are actually claimed (isClaimed=true). This is crucial for
	 * handling cases where products have multiple label claims with different claim statuses.
	 * </p>
	 *
	 * @param productData the product data to process documents for
	 * @return true if processing was successful, false otherwise
	 */
	@Override
	public boolean process(ProductData productData) {

		if (productData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (productData instanceof ProductSpecificationData)) {
			return true;
		}

		logger.debug("DocumentFormulationHandler::process() <- " + productData.getNodeRef());

		// Map to track documents by their document type
		Map<NodeRef, NodeRef> docByType = entityService.getDocumentsByType(productData.getNodeRef());

		// Process all document types to generate documents if needed
		for (NodeRef docTypeNodeRef : BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_DOCUMENT_TYPE).inDB().list()) {
			DocumentTypeItem docTypeItem = (DocumentTypeItem) alfrescoRepository.findOne(docTypeNodeRef);

			if ((docTypeItem.isSynchronisedDocumentType() && isDocTypeMatchProduct(productData, docTypeItem))
					&& !docByType.containsKey(docTypeNodeRef)) {
				// Create new document if it doesn't exist yet
				NodeRef docNodeRef = createDocument(productData, docTypeItem);
				if (docNodeRef != null) {
					docByType.put(docTypeNodeRef, docNodeRef);
				}
			}
		}

		ExpressionParser parser = formulaService.getSpelParser();
		StandardEvaluationContext context = formulaService.createEntitySpelContext(productData);

		// Process all documents to update their state and mandatory status
		for (Map.Entry<NodeRef, NodeRef> entry : docByType.entrySet()) {
			NodeRef docTypeNodeRef = entry.getKey();
			NodeRef docNodeRef = entry.getValue();

			DocumentTypeItem docTypeItem = (DocumentTypeItem) alfrescoRepository.findOne(docTypeNodeRef);


			boolean isMandatory = false;

			String documentState = (String) nodeService.getProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_STATE);

			//We do not delete but set mandatory to false
			if (docTypeItem.isSynchronisedDocumentType() && !isDocTypeMatchProduct(productData, docTypeItem)) {

				if (SystemState.Simulation.toString().equals(documentState)) {
					nodeService.deleteNode(docNodeRef);
					break;
				}

			} else {
				// Update document mandatory status
				isMandatory = calculateDocumentIsMandatory(productData, docTypeItem, parser, context);
			}

			nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_IS_MANDATORY, isMandatory);


			if (isMandatory && SystemState.Simulation.toString().equals(documentState)) {
				productData.getReqCtrlList()
						.add(ReqCtrlListDataItem.forbidden()
								.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DOCUMENT_MANDATORY,
										mlNodeService.getProperty(docTypeItem.getNodeRef(), BeCPGModel.PROP_CHARACT_NAME)))
								.withCharact(docTypeItem.getNodeRef()).ofDataType(RequirementDataType.Completion));
			}

			
			// Update document category
			String documentTypeCategory = (String) nodeService.getProperty(docTypeNodeRef, BeCPGModel.PROP_DOCUMENT_TYPE_CATEGORY);
			nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_CATEGORY, documentTypeCategory);

			// Add entity reference
			associationService.update(docNodeRef, BeCPGModel.ASSOC_DOCUMENT_ENTITY_REF, productData.getNodeRef());

			// Update document state and effectivity dates
			updateDocumentState(productData, docNodeRef);
		}

		return true;
	}

	/**
	 * Creates a new document for the given document type and product data.
	 * <p>
	 * This method creates a document in the destination folder specified by the document type,
	 * or uses the default supplier documents path if none is specified. It generates the document name,
	 * adds the necessary aspects and associations, and initializes the document state.
	 * </p>
	 *
	 * @param productData the product data for which to create the document
	 * @param docTypeItem the document type configuration
	 * @return the NodeRef of the created document, or null if creation failed
	 */
	private NodeRef createDocument(ProductData productData, DocumentTypeItem docTypeItem) {

		// Determine the destination path
		String destPath = (docTypeItem.getDestPath() != null) && !docTypeItem.getDestPath().trim().isEmpty() ? docTypeItem.getDestPath()
				: DEFAULT_SUPPLIER_DOCUMENTS_PATH;

		NodeRef destFolder = null;
		if (!".".equals(destPath)) {
			destFolder = repoService.getFolderByPath(productData.getNodeRef(), destPath);
		}

		if (destFolder == null) {
			destFolder = productData.getNodeRef();
		}

		// Generate document name using the template format
		String documentName = generateDocumentName(productData, docTypeItem);

		// Create the document file
		FileInfo fileInfo = fileFolderService.create(destFolder, documentName, ContentModel.TYPE_CONTENT);
		NodeRef docNodeRef = fileInfo.getNodeRef();

		try {
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			// Add document aspect
			Map<QName, Serializable> aspectProps = new HashMap<>();
			nodeService.addAspect(docNodeRef, BeCPGModel.ASPECT_DOCUMENT_ASPECT, aspectProps);

			// Add document type reference
			associationService.update(docNodeRef, BeCPGModel.ASSOC_DOCUMENT_TYPE_REF, docTypeItem.getNodeRef());

			// Initialize state to Simulation
			nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_STATE, SystemState.Simulation);
		} finally {
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
		}

		return docNodeRef;

	}

	/**
	 * Generates a document name using the template format specified in the document type.
	 * <p>
	 * The template can include placeholders that are replaced with actual values.
	 * Example: {cm:name} - {doc_bcpg:charactName}
	 * </p>
	 * <p>
	 * If no format is specified or the generated name is empty, it falls back to using
	 * the characteristic name as the document name.
	 * </p>
	 *
	 * @param productData the product data used for variable substitution
	 * @param docTypeItem the document type containing the name format template
	 * @return the generated document name
	 */
	private String generateDocumentName(ProductData productData, DocumentTypeItem docTypeItem) {
		String nameFormat = docTypeItem.getNameFormat();
		String documentName = docTypeItem.getCharactName();

		if ((nameFormat != null) && !nameFormat.isBlank()) {
			String generatedName = expressionService.extractExpr(productData.getNodeRef(), docTypeItem.getNodeRef(), nameFormat);
			if ((generatedName != null) && !generatedName.isBlank()) {
				documentName = generatedName;
			}
		}

		if (FilenameUtils.getExtension(documentName).isEmpty()) {
			documentName += ".pdf";
		}

		return documentName;
	}

	/**
	 * Updates document state based on document type settings and current state.
	 * <p>
	 * This method synchronizes the document state with the product state:
	 * - If product is Valid, the document state is set to Valid
	 * - If product is Stopped, the document state is set to Stopped
	 * - If product is Archived, the document state is set to Archived
	 * </p>
	 *
	 * @param productData the product data containing the state to synchronize with
	 * @param docNodeRef the node reference of the document to update
	 */
	private void updateDocumentState(ProductData productData, NodeRef docNodeRef) {

		// Get current document state
		String documentState = (String) nodeService.getProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_STATE);

		if (SystemState.ToValidate.toString().equals(documentState)) {
			if (SystemState.Valid.equals(productData.getState())) {
				nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_STATE, SystemState.Valid.toString());
			} else if (SystemState.Stopped.equals(productData.getState())) {
				nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_STATE, SystemState.Stopped.toString());
			} else if (SystemState.Archived.equals(productData.getState())) {
				nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_STATE, SystemState.Archived.toString());
			}
		}

	}

	/**
	 * Calculates if a document is mandatory based on document type settings.
	 *
	 * For label claims, they must be claimed (isClaimed=true) to make a document mandatory.
	 * For other characteristics, they just need to be present in the product.
	 *
	 * @param productData the product data containing characteristics
	 * @param docTypeItem the document type to check
	 * @param parser the expression parser for formula evaluation
	 * @param context the evaluation context for formula evaluation
	 * @return true if the document is mandatory, false otherwise
	 */
	private boolean calculateDocumentIsMandatory(ProductData productData, DocumentTypeItem docTypeItem, ExpressionParser parser,
			StandardEvaluationContext context) {

		// Check if explicitly mandatory
		if (Boolean.TRUE.equals(docTypeItem.getIsMandatory())) {
			return true;
		}

		// Check if there's a formula to evaluate
		String formulaText = docTypeItem.getFormula();
		if ((formulaText != null) && !formulaText.trim().isEmpty()) {
			try {
				String[] formulas = SpelHelper.formatMTFormulas(formulaText);
				for (String formula : formulas) {

					Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);
					if (varFormulaMatcher.matches()) {
						Expression exp = parser.parseExpression(varFormulaMatcher.group(2));
						context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
					} else {
						Expression exp = parser.parseExpression(formula);
						Object ret = exp.getValue(context);
						if (ret instanceof Boolean mandatory) {
							return mandatory;
						} else {
							productData.getReqCtrlList()
									.add(ReqCtrlListDataItem.tolerated()
											.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DOCUMENT_FORMULATION_ERROR,
													mlNodeService.getProperty(docTypeItem.getNodeRef(), BeCPGModel.PROP_CHARACT_NAME),
													I18NUtil.getMessage("message.formulate.formula.incorrect.type.boolean", Locale.getDefault())))
											.withCharact(docTypeItem.getNodeRef()).ofDataType(RequirementDataType.Formulation));
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error evaluating document mandatory formula: " + formulaText, e);

				productData.getReqCtrlList()
						.add(ReqCtrlListDataItem.tolerated()
								.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DOCUMENT_FORMULATION_ERROR,
										mlNodeService.getProperty(docTypeItem.getNodeRef(), BeCPGModel.PROP_CHARACT_NAME), e.getLocalizedMessage()))
								.withCharact(docTypeItem.getNodeRef()).ofDataType(RequirementDataType.Formulation));

			}
		}

		return false;
	}

	/**
	 * Checks if a document type matches the product based on multiple criteria.
	 * <p>
	 * The matching is based on the following criteria, all of which must be satisfied:
	 * <ul>
	 *   <li>Product type: The product type must match one of the linked types in the document type</li>
	 *   <li>Hierarchy: The product hierarchy must match one of the linked hierarchies in the document type</li>
	 *   <li>Characteristics: At least one of the linked characteristics must be present in the product</li>
	 *   <li>Subsidiary: If specified, at least one subsidiary must match between product and document type</li>
	 *   <li>Plants: If specified, at least one plant must match between product and document type</li>
	 * </ul>
	 * </p>
	 *
	 * @param productData the product data to check for a match
	 * @param docTypeItem the document type configuration
	 * @return true if the document type matches the product, false otherwise
	 */
	private boolean isDocTypeMatchProduct(ProductData productData, DocumentTypeItem docTypeItem) {
		// Check product type match
		List<String> linkedTypes = docTypeItem.getLinkedTypes();
		if (CollectionUtils.isNotEmpty(linkedTypes)) {
			QName productTypeQName = nodeService.getType(productData.getNodeRef());
			boolean typeMatch = false;

			for (String typeName : linkedTypes) {
				QName typeQName = QName.createQName(typeName, namespaceService);
				if (productTypeQName.equals(typeQName)) {
					typeMatch = true;
					break;
				}
			}

			if (!typeMatch) {
				return false;
			}
		}

		// Check hierarchy match
		List<NodeRef> linkedHierarchy = docTypeItem.getLinkedHierarchy();
		if (CollectionUtils.isNotEmpty(linkedHierarchy)) {
			NodeRef hierarchy1 = productData.getHierarchy1();
			NodeRef hierarchy2 = productData.getHierarchy2();

			boolean hierarchyMatch = false;
			for (NodeRef hierarchyRef : linkedHierarchy) {
				if (hierarchyRef.equals(hierarchy1) || hierarchyRef.equals(hierarchy2)) {
					hierarchyMatch = true;
					break;
				}
			}

			if (!hierarchyMatch) {
				return false;
			}
		}

		// Check charact refs match
		List<NodeRef> linkedCharactRefs = docTypeItem.getLinkedCharactRefs();
		if (CollectionUtils.isNotEmpty(linkedCharactRefs)) {
			List<NodeRef> productCharacts = getProductCharacts(productData);

			boolean charactMatch = false;
			for (NodeRef charactRef : linkedCharactRefs) {
				if (productCharacts.contains(charactRef)) {
					charactMatch = true;
					break;
				}
			}

			if (!charactMatch) {
				return false;
			}
		}

		// Check subsidiary match
		List<NodeRef> subsidiaryRefs = docTypeItem.getSubsidiaryRefs();
		if (CollectionUtils.isNotEmpty(subsidiaryRefs)) {
			List<NodeRef> productSubsidiaries = productData.getSubsidiaryRefs();

			if (CollectionUtils.isEmpty(productSubsidiaries) || Collections.disjoint(subsidiaryRefs, productSubsidiaries)) {
				return false;
			}
		}

		// Check plants match
		List<NodeRef> plants = docTypeItem.getPlants();
		if (CollectionUtils.isNotEmpty(plants)) {
			List<NodeRef> productPlants = productData.getPlants();

			if (CollectionUtils.isEmpty(productPlants) || Collections.disjoint(plants, productPlants)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Get all product characteristics for matching with document types.
	 * Note: This method returns ALL product characteristics, including ALL label claims
	 * (both claimed and unclaimed), certifications, and survey choices.
	 *
	 * @param productData the product data containing characteristics
	 * @return list of node references for all product characteristics
	 */
	private List<NodeRef> getProductCharacts(ProductData productData) {
		List<NodeRef> productCharacts = new ArrayList<>();

		// Get claims
		List<NodeRef> claims = productData.getLabelClaimList().stream()
				.filter(p -> LabelClaimListDataItem.VALUE_CERTIFIED.equals(p.getLabelClaimValue())).map(LabelClaimListDataItem::getLabelClaim)
				.toList();
		if (CollectionUtils.isNotEmpty(claims)) {
			productCharacts.addAll(claims);
		}

		//		// Get certifications
		//		List<NodeRef> certifications = associationService.getTargetAssocs(productData.getNodeRef(), PLMModel.ASSOC_SUBSIDIARY_CERTIFICATIONS);
		//		if (CollectionUtils.isNotEmpty(certifications)) {
		//			productCharacts.addAll(certifications);
		//		}

		final List<SurveyListDataItem> surveyList = SurveyableEntityHelper.getNamesSurveyLists(alfrescoRepository, productData).values().stream()
				.filter(Objects::nonNull).flatMap(List::stream).toList();

		List<NodeRef> surveys = surveyList.stream().map(SurveyListDataItem::getChoices).flatMap(List::stream).toList();
		if (CollectionUtils.isNotEmpty(surveys)) {
			productCharacts.addAll(surveys);
		}

		return productCharacts;
	}
}