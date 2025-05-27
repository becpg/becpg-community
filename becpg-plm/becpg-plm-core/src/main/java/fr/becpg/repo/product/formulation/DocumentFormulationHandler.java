package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections4.CollectionUtils;
// String utilities removed, using standard Java string operations
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
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
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.document.DocumentTypeItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>
 * DocumentFormulationHandler class.
 * </p>
 *
 * Handles the document creation and management based on DocumentTypeItem configurations.
 * Documents can be automatically generated, have validity types (none, manual, auto),
 * and have mandatory status calculated from properties or formulas.
 */
public class DocumentFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(DocumentFormulationHandler.class);

	public static final String MESSAGE_DOCUMENT_FORMULATION_ERROR = "message.formulate.document.error";

	private static final String DEFAULT_SUPPLIER_DOCUMENTS_PATH = "./SupplierDocuments/";

	private FileFolderService fileFolderService;
	private NodeService nodeService;
	private NodeService mlNodeService;
	private NamespaceService namespaceService;
	private AlfrescoRepository<DocumentTypeItem> documentTypeRepository;
	private AssociationService associationService;
	private EntityService entityService;
	private RepoService repoService;
	private ExpressionService expressionService;

	private SpelFormulaService formulaService;

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
	 * Setter for the field <code>documentTypeRepository</code>.
	 * </p>
	 *
	 * @param documentTypeRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setDocumentTypeRepository(AlfrescoRepository<DocumentTypeItem> documentTypeRepository) {
		this.documentTypeRepository = documentTypeRepository;
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
	
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setExpressionService(ExpressionService expressionService) {
		this.expressionService = expressionService;
	}

	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/**
	 * {@inheritDoc}
	 * Process method that manages documents for a product based on document types.
	 * This handles automatic document generation, calculation of mandatory status,
	 * and setting document states based on effectivity types.
	 */
	@Override
	public boolean process(ProductData productData) {
		logger.debug("DocumentFormulationHandler::process() <- " + productData.getNodeRef());

		// Map to track documents by their document type
		Map<NodeRef, NodeRef> docByType = entityService.getDocumentsByType(productData.getNodeRef());

		// Process all document types to generate documents if needed
		for (NodeRef docTypeNodeRef : BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_DOCUMENT_TYPE).inDB().list()) {
			DocumentTypeItem docTypeItem = documentTypeRepository.findOne(docTypeNodeRef);

			if ((docTypeItem.isSynchronisedDocumentType() && isDocTypeMatchProduct(productData, docTypeItem))
					&& !docByType.containsKey(docTypeNodeRef)) {
				// Create new document if it doesn't exist yet
				NodeRef docNodeRef = createDocument(productData, docTypeItem);
				if (docNodeRef != null) {
					docByType.put(docTypeNodeRef, docNodeRef);
				}
			}
		}

		// Process all documents to update their state and mandatory status
		for (Map.Entry<NodeRef, NodeRef> entry : docByType.entrySet()) {
			NodeRef docTypeNodeRef = entry.getKey();
			NodeRef docNodeRef = entry.getValue();

			DocumentTypeItem docTypeItem = documentTypeRepository.findOne(docTypeNodeRef);

			// Update document mandatory status
			boolean isMandatory = calculateDocumentIsMandatory(productData, docTypeItem);
			nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_IS_MANDATORY, isMandatory);

			// Get current document state
			String documentState = (String) nodeService.getProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_STATE);

			// Update document state and effectivity dates
			updateDocumentStateAndDates(docNodeRef, docTypeItem, documentState);
		}

		return true;
	}

	/**
	 * Create a new document for the given document type
	 */
	private NodeRef createDocument(ProductData productData, DocumentTypeItem docTypeItem) {

		// Determine the destination path
		String destPath = (docTypeItem.getDestPath() != null) && !docTypeItem.getDestPath().trim().isEmpty() ? docTypeItem.getDestPath()
				: DEFAULT_SUPPLIER_DOCUMENTS_PATH;

		NodeRef destFolder = repoService.getFolderByPath(productData.getNodeRef(), destPath);

		if (destFolder != null) {
			// Generate document name using the template format
			String documentName = generateDocumentName(productData, docTypeItem);

			// Create the document file
			FileInfo fileInfo = fileFolderService.create(destFolder, documentName, ContentModel.TYPE_CONTENT);
			NodeRef docNodeRef = fileInfo.getNodeRef();

			// Add document aspect
			Map<QName, Serializable> aspectProps = new HashMap<>();
			nodeService.addAspect(docNodeRef, BeCPGModel.ASPECT_DOCUMENT_ASPECT, aspectProps);

			// Add document type reference
			associationService.update(docNodeRef, BeCPGModel.ASSOC_DOCUMENT_TYPE_REF, docTypeItem.getNodeRef());

			// Add entity reference
			associationService.update(docNodeRef, BeCPGModel.ASSOC_DOCUMENT_ENTITY_REF, productData.getNodeRef());

			// Initialize state to Simulation
			nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_STATE, SystemState.Simulation);

			// Set initial mandatory state
			boolean isMandatory = calculateDocumentIsMandatory(productData, docTypeItem);
			nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_IS_MANDATORY, isMandatory);

			// Handle effectivity dates based on effectivity type
			updateEffectivityDates(docNodeRef, docTypeItem);

			return docNodeRef;
		}

		return null;
	}

	/**
	 * Generate document name using the template format
	 */
	private String generateDocumentName(ProductData productData, DocumentTypeItem docTypeItem) {
		String nameFormat = docTypeItem.getNameFormat();

		if ((nameFormat == null) || nameFormat.trim().isEmpty()) {
			// Default format if none specified
			return productData.getName() + " - " + docTypeItem.getName();
		}

		// Process template
		String documentName = expressionService.extractExpr(productData.getNodeRef(), docTypeItem.getNodeRef(), nameFormat);

		// Ensure we have a valid filename
		return ((documentName != null) && !documentName.trim().isEmpty()) ? documentName : (productData.getName() + " - " + docTypeItem.getName());
	}

	/**
	 * Update document state and effectivity dates based on document type settings and current state
	 */
	private void updateDocumentStateAndDates(NodeRef docNodeRef, DocumentTypeItem docTypeItem, String documentState) {
		if (DocumentTypeItem.DocumentEffectivityType.AUTO.equals(docTypeItem.getEffectivityType())) {
			// Reset dates for OnHold status
			nodeService.setProperty(docNodeRef, BeCPGModel.PROP_CM_FROM, null);
			nodeService.setProperty(docNodeRef, BeCPGModel.PROP_CM_TO, null);
		} else if (docTypeItem.getEffectivityType() == null) {
			// Set mandatory to false for Archived status
			nodeService.setProperty(docNodeRef, BeCPGModel.PROP_DOCUMENT_IS_MANDATORY, false);
		} else if (SystemState.ToValidate.toString().equals(documentState)) {
			// For ToValidate state, update effectivity dates based on effectivity type
			updateEffectivityDates(docNodeRef, docTypeItem);
		}
	}

	/**
	 * Update effectivity dates based on effectivity type
	 * Aligns with the logic from DocumentAspectPolicy
	 */
	private void updateEffectivityDates(NodeRef docNodeRef, DocumentTypeItem docTypeItem) {
		DocumentTypeItem.DocumentEffectivityType effectivityType = docTypeItem.getEffectivityType();

		if ((effectivityType == DocumentTypeItem.DocumentEffectivityType.AUTO)
				|| (effectivityType == DocumentTypeItem.DocumentEffectivityType.NONE)) {

			// Set start date to now if not set
			Date fromDate = (Date) nodeService.getProperty(docNodeRef, BeCPGModel.PROP_CM_FROM);
			if (fromDate == null) {
				fromDate = new Date();
				nodeService.setProperty(docNodeRef, BeCPGModel.PROP_CM_FROM, fromDate);
			}

			// For AUTO type, set end date based on auto expiration delay
			if (effectivityType == DocumentTypeItem.DocumentEffectivityType.AUTO) {
				Integer autoExpirationDelay = docTypeItem.getAutoExpirationDelay();
				if (autoExpirationDelay != null) {
					Calendar toDateCal = Calendar.getInstance();
					toDateCal.setTime(fromDate);
					toDateCal.add(Calendar.DAY_OF_YEAR, autoExpirationDelay);
					nodeService.setProperty(docNodeRef, BeCPGModel.PROP_CM_TO, toDateCal.getTime());
				}
			}
		}
		// For MANUAL effectivity type, do nothing as dates should be set manually
	}

	/**
	 * Calculate if document is mandatory based on document type settings
	 */
	private boolean calculateDocumentIsMandatory(ProductData productData, DocumentTypeItem docTypeItem) {
		// Check if explicitly mandatory
		if (Boolean.TRUE.equals(docTypeItem.isMandatory())) {
			return true;
		}

		ExpressionParser parser = formulaService.getSpelParser();
		StandardEvaluationContext context = formulaService.createEntitySpelContext(productData);

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
						if (ret instanceof Boolean) {
							return (Boolean) ret;
						} else {
							productData.getReqCtrlList()
									.add(ReqCtrlListDataItem.tolerated()
											.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DOCUMENT_FORMULATION_ERROR,
													mlNodeService.getProperty(docTypeItem.getNodeRef(), BeCPGModel.PROP_CHARACT_NAME),
													I18NUtil.getMessage("message.formulate.formula.incorrect.type.boolean", Locale.getDefault())))
											.withCharact(docTypeItem.getNodeRef()).ofDataType(RequirementDataType.Labelclaim));
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error evaluating document mandatory formula: " + formulaText, e);

				productData.getReqCtrlList()
						.add(ReqCtrlListDataItem.tolerated()
								.withMessage(MLTextHelper.getI18NMessage(MESSAGE_DOCUMENT_FORMULATION_ERROR,
										mlNodeService.getProperty(docTypeItem.getNodeRef(), BeCPGModel.PROP_CHARACT_NAME), e.getLocalizedMessage()))
								.withCharact(docTypeItem.getNodeRef()).ofDataType(RequirementDataType.Labelclaim));

			}
		}

		return false;
	}

	/**
	 * Check if a document type matches the product
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
			// Get product characteristics (this would need to be adapted to your specific model)
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
	 * Get product characteristics - adapt this method to match your specific model
	 */
	private List<NodeRef> getProductCharacts(ProductData productData) {
		List<NodeRef> productCharacts = new ArrayList<>();

		// Get claims
		List<NodeRef> claims = productData.getLabelClaimList().stream().filter(p -> p.getIsClaimed()).map(p -> p.getNodeRef()).toList();
		if (CollectionUtils.isNotEmpty(claims)) {
			productCharacts.addAll(claims);
		}

		// Get certifications
		List<NodeRef> certifications = associationService.getTargetAssocs(productData.getNodeRef(), PLMModel.ASSOC_SUBSIDIARY_CERTIFICATIONS);
		if (CollectionUtils.isNotEmpty(certifications)) {
			productCharacts.addAll(certifications);
		}

		// TODO Get surveys
		List<NodeRef> surveys = productData.getSurveyList().stream().map(p -> p.getQuestion()).toList();
		if (CollectionUtils.isNotEmpty(surveys)) {
			productCharacts.addAll(surveys);
		}

		return productCharacts;
	}
}