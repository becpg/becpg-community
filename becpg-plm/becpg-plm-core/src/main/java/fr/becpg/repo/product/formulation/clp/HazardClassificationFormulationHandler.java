/*
 *
 */
package fr.becpg.repo.product.formulation.clp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GHSModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.HazardClassificationListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>HazardClassificationFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class HazardClassificationFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final String DATABASES_FOLDER = "/app:company_home/cm:System/cm:CLPDatabases";
	private static final String MISSING_CHARACTS_MSG = "message.clp.missingCharacts";
	private static final String FORMULA_ERROR_MSG = "message.clp.formula.error";
	private static final String HAZARD_STATEMENT_NOT_FOUND_MSG = "message.clp.hazardStatement.notFound";
	private static final String PICTOGRAM_NOT_FOUND_MSG = "message.clp.pictogram.notFound";

	private static final Log logger = LogFactory.getLog(HazardClassificationFormulationHandler.class);

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private FileFolderService fileFolderService;

	private ContentService contentService;

	private NodeService nodeService;

	private Repository repositoryHelper;

	private SpelFormulaService formulaService;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>fileFolderService</code>.</p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>repositoryHelper</code>.</p>
	 *
	 * @param repositoryHelper a {@link org.alfresco.repo.model.Repository} object
	 */
	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	/**
	 * <p>Setter for the field <code>formulaService</code>.</p>
	 *
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object
	 */
	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return a {@link java.lang.Class} object
	 */
	protected Class<HazardClassificationListDataItem> getInstanceClass() {
		return HazardClassificationListDataItem.class;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {
		if (accept(formulatedProduct)) {

			logger.debug("CLP calculating visitor");

			if (formulatedProduct.getHcList() == null) {
				formulatedProduct.setHcList(new LinkedList<>());
			}

			List<HazardClassificationListDataItem> retainNodes = new ArrayList<>();

			for (HazardClassificationListDataItem hazardClassificationListDataItem : formulatedProduct.getHcList()) {
				if (Boolean.TRUE.equals(hazardClassificationListDataItem.getIsManual())) {
					retainNodes.add(hazardClassificationListDataItem);
				}
			}

			Map<String, Double> clpQuantities = new HashMap<>();
			Map<String, Double> maxQuantities = new HashMap<>();
			Map<String, Map<IngItem, Double>> details = new HashMap<>();

			populateHazardQuantities(formulatedProduct, clpQuantities, maxQuantities, details);

			Map<String, NodeRef> missingCharacts = new HashMap<>();

			HazardClassificationFormulaContext formulaContext = new HazardClassificationFormulaContext(formulatedProduct, clpQuantities,
					maxQuantities, details, findPhysicoValue(formulatedProduct, HazardClassificationFormulaContext.BOILING_POINT, missingCharacts),
					findPhysicoValue(formulatedProduct, HazardClassificationFormulaContext.FLASH_POINT, missingCharacts),
					findPhysicoValue(formulatedProduct, HazardClassificationFormulaContext.HYDROCARBON_PERC, missingCharacts));

			if (!missingCharacts.isEmpty()) {
				formulatedProduct.getReqCtrlList().add(ReqCtrlListDataItem.forbidden().withMessage(MLTextHelper.getI18NMessage(MISSING_CHARACTS_MSG))
						.ofDataType(RequirementDataType.Physicochem).withSources(new ArrayList<>(missingCharacts.values())));
			}

			StandardEvaluationContext context = formulaService.createCustomSpelContext(formulatedProduct, formulaContext);

			try (CSVReader csvReader = getCSVReaderFromNodeRef(getCLPDatabase())) {
				processCSVData(formulatedProduct, retainNodes, context, csvReader);
			} catch (IOException e) {
				logger.error("Error reading CLP database CSV", e);
				return false;
			}

			formulatedProduct.getHcList().retainAll(retainNodes);
		}
		return true;
	}

	private void processCSVData(ProductData formulatedProduct, List<HazardClassificationListDataItem> retainNodes, StandardEvaluationContext context,
			CSVReader csvReader) throws IOException {
		String[] data;
		Set<String> matchedHPhrases = new HashSet<>();

		while ((data = csvReader.readNext()) != null) {
			if (data.length >= 6) {
				String hazardCode = data[1];
				String hazardClassCode = data[2];
				String pictogramCode = data[3];
				String signalWord = data[4];
				String regulatoryText = data[5];
				String formula = data[6];
				String detailFormula = data[7];

				if (!matchedHPhrases.contains(hazardCode)) {
					try {
						Expression exp = new SpelExpressionParser().parseExpression(formula);
						if (Boolean.TRUE.equals(exp.getValue(context, Boolean.class))) {

							NodeRef hazardStatement = findHazardStatement(hazardCode);
							if (hazardStatement == null) {
								formulatedProduct.getReqCtrlList().add(ReqCtrlListDataItem.forbidden()
										.withMessage(MLTextHelper.getI18NMessage(HAZARD_STATEMENT_NOT_FOUND_MSG, hazardCode))
										.withSources(Arrays.asList(formulatedProduct.getNodeRef())).ofDataType(RequirementDataType.Formulation));
							} else {

								NodeRef pictogram = null;
								if (pictogramCode != null && !pictogramCode.isBlank()) {
									pictogram = findPictogram(pictogramCode);
									if (pictogram == null) {
										formulatedProduct.getReqCtrlList()
												.add(ReqCtrlListDataItem.forbidden()
														.withMessage(MLTextHelper.getI18NMessage(PICTOGRAM_NOT_FOUND_MSG, pictogramCode))
														.withSources(Arrays.asList(formulatedProduct.getNodeRef()))
														.ofDataType(RequirementDataType.Formulation));
									}
								}

								HazardClassificationListDataItem newClp = findOrCreateHazardClassificationListDataItem(formulatedProduct,
										HazardClassificationListDataItem.build().withHazardStatement(hazardStatement)
												.withHazardClassCode(hazardClassCode).withPictogram(pictogram).withSignalWord(signalWord)
												.withRegulatoryText(regulatoryText)
												.withDetail(evaluateDetailFormula(formulatedProduct, context, detailFormula)));

								retainNodes.add(newClp);
								matchedHPhrases.add(hazardCode);
							}

						}

					} catch (Exception e) {
						handleFormulaError(formulatedProduct, formula, e);
					}
				}
			}
		}

	}

	private HazardClassificationListDataItem findOrCreateHazardClassificationListDataItem(ProductData formulatedProduct,
			HazardClassificationListDataItem newClp) {

		// Find the matching hazard classification list item, if it exists
		Optional<HazardClassificationListDataItem> existingItem = formulatedProduct.getHcList().stream()
				.filter(h -> h.getHazardStatement().equals(newClp.getHazardStatement())).findFirst();

		// If no match is found, return the new item
		if (existingItem.isEmpty()) {

			formulatedProduct.getHcList().add(newClp);

			return newClp;
		}

		HazardClassificationListDataItem ret = existingItem.get();

		// Return the existing item if it's manually set; otherwise, merge with the new one
		return Boolean.TRUE.equals(ret.getIsManual()) ? ret : ret.merge(newClp);
	}

	private void handleFormulaError(ProductData formulatedProduct, String formula, Exception e) {
		Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
		if (validCause != null) {
			throw (RuntimeException) validCause;
		}

		formulatedProduct.getReqCtrlList()
				.add(ReqCtrlListDataItem.info().withMessage(MLTextHelper.getI18NMessage(FORMULA_ERROR_MSG, formula, e.getLocalizedMessage()))
						.withSources(Arrays.asList(formulatedProduct.getNodeRef())).ofDataType(RequirementDataType.Formulation));

		logger.warn("Error in CLP formula : - " + formula);
		if (logger.isTraceEnabled()) {

			logger.trace(e, e);

		}

	}

	private void populateHazardQuantities(ProductData formulatedProduct, Map<String, Double> clpQuantities, Map<String, Double> maxQuantities,
			Map<String, Map<IngItem, Double>> details) {
		if (formulatedProduct.getIngList() != null) {
			for (IngListDataItem ing : formulatedProduct.getIngList()) {

				Double quantityPercentage = ing.getQtyPerc();

				IngItem ingItem = (IngItem) alfrescoRepository.findOne(ing.getIng());

				String clpClassifications = (String) nodeService.getProperty(ing.getIng(), GHSModel.PROP_SDS_HAZARD_CLASSIFICATIONS);
				Double toxicityAcuteOral = (Double) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_ING_TOX_ACUTE_ORAL);
				if ((toxicityAcuteOral != null) && (toxicityAcuteOral != 0)) {
					clpQuantities.merge(HazardClassificationFormulaContext.ETA_VO, quantityPercentage / toxicityAcuteOral, Double::sum);
				}

				Double toxicityAcuteDemal = (Double) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_ING_TOX_ACUTE_DERMAL);
				if ((toxicityAcuteDemal != null) && (toxicityAcuteDemal != 0)) {
					clpQuantities.merge(HazardClassificationFormulaContext.ETA_VC, quantityPercentage / toxicityAcuteDemal, Double::sum);
				}

				Double toxicityAcuteInhalation = (Double) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_ING_TOX_ACUTE_INHALATION);
				String toxicityAcuteInhalationType = (String) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_ING_TOX_ACUTE_INHALATION_TYPE);
				if ((toxicityAcuteInhalation != null) && (toxicityAcuteInhalation != 0) && toxicityAcuteInhalationType != null
						&& !toxicityAcuteInhalationType.isBlank()) {
					clpQuantities.merge(HazardClassificationFormulaContext.etaType(toxicityAcuteInhalationType),
							quantityPercentage / toxicityAcuteInhalation, Double::sum);
				}

				Double mFactor = (Double) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_ING_TOX_AQUATIC_MFACTOR);
				Boolean superSensitizing = (Boolean) nodeService.getProperty(ing.getIng(), BeCPGModel.PROP_ING_TOX_IS_SUPER_SENSITIZING);

				if ((clpClassifications != null) && !clpClassifications.isEmpty()) {
					String[] classifications = clpClassifications.split(",");
					for (String classification : classifications) {
						String[] parts = classification.split(":", 2);
						String hazardClassCode = null;
						String hazardStatement = null;
						if (parts.length > 1) {
							hazardClassCode = parts[0].trim();
							hazardStatement = parts[1].trim();
							clpQuantities.merge(classification, quantityPercentage, Double::sum);
							maxQuantities.merge(classification, quantityPercentage, Double::max);
							addToDetails(details, classification, ingItem, quantityPercentage);

							if (mFactor != null) {
								clpQuantities.merge("M:" + classification, quantityPercentage * mFactor, Double::sum);
								maxQuantities.merge("M:" + classification, quantityPercentage * mFactor, Double::max);
								addToDetails(details, "M:" + classification, ingItem, quantityPercentage * mFactor);
							}
							if (Boolean.TRUE.equals(superSensitizing)) {
								clpQuantities.merge("S:" + classification, quantityPercentage, Double::sum);
								maxQuantities.merge("S:" + classification, quantityPercentage, Double::max);
								addToDetails(details, "S:" + classification, ingItem, quantityPercentage);
							}

						} else {
							hazardStatement = parts[0].trim();
						}
						clpQuantities.merge(hazardStatement, quantityPercentage, Double::sum);
						maxQuantities.merge(hazardStatement, quantityPercentage, Double::max);
						addToDetails(details, hazardStatement, ingItem, quantityPercentage);

						if (hazardClassCode != null) {
							maxQuantities.merge(hazardClassCode, quantityPercentage, Double::sum);
							maxQuantities.merge(hazardClassCode, quantityPercentage, Double::max);
							addToDetails(details, hazardClassCode, ingItem, quantityPercentage);
						}
						if (mFactor != null) {
							clpQuantities.merge("M:" + hazardStatement, quantityPercentage * mFactor, Double::sum);
							maxQuantities.merge("M:" + hazardStatement, quantityPercentage * mFactor, Double::max);
							addToDetails(details, "M:" + hazardStatement, ingItem, quantityPercentage * mFactor);
						}
						if (Boolean.TRUE.equals(superSensitizing)) {
							clpQuantities.merge("S:" + hazardStatement, quantityPercentage, Double::sum);
							maxQuantities.merge("S:" + hazardStatement, quantityPercentage, Double::max);
							addToDetails(details, "S:" + hazardStatement, ingItem, quantityPercentage);
						}
					}
				}
			}
		}
	}

	private String evaluateDetailFormula(ProductData formulatedProduct, StandardEvaluationContext context, String formula) {
		if ((formula == null) || formula.isBlank()) {
			return null;
		}

		try {
			Expression exp = new SpelExpressionParser().parseExpression(formula);
			return exp.getValue(context, String.class);
		} catch (Exception e) {
			handleFormulaError(formulatedProduct, formula, e);
			return null;
		}
	}

	private NodeRef findPictogram(String pictogramCode) {
		return BeCPGQueryBuilder.createQuery().ofType(GHSModel.TYPE_PICTOGRAM).andPropEquals(GHSModel.PROP_PICTOGRAM_CODE, pictogramCode).inDB()
				.singleValue();
	}

	private NodeRef findHazardStatement(String hazardCode) {
		return BeCPGQueryBuilder.createQuery().ofType(GHSModel.TYPE_HAZARD_STATEMENT).andPropEquals(GHSModel.PROP_HAZARD_CODE, hazardCode).inDB()
				.singleValue();
	}

	private void addToDetails(Map<String, Map<IngItem, Double>> details, String key, IngItem ingItem, Double quantityPercentage) {
		if (ingItem != null) {
			Map<IngItem, Double> casNumbersDetail = details.getOrDefault(key, new HashMap<>());
			casNumbersDetail.merge(ingItem, quantityPercentage, Double::sum);
			details.put(key, casNumbersDetail);
		}
	}

	private Double findPhysicoValue(ProductData entity, String physicoCode, Map<String, NodeRef> missingCharacts) {
		PhysicoChemListDataItem physicoListItem = null;
		if (entity.getPhysicoChemList() != null) {
			for (PhysicoChemListDataItem physico : entity.getPhysicoChemList()) {

				if (physicoCode.equals(nodeService.getProperty(physico.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_CODE))) {

					for (ReqCtrlListDataItem reqCtrl : entity.getReqCtrlList()) {
						if (RequirementType.Forbidden.equals(reqCtrl.getReqType()) && RequirementDataType.Physicochem.equals(reqCtrl.getReqDataType())
								&& physico.getPhysicoChem().equals(reqCtrl.getCharact())) {
							missingCharacts.put(physicoCode, reqCtrl.getCharact());
							break;
						}
					}

					physicoListItem = physico;
				}
			}

			if (physicoListItem != null) {
				Double value = physicoListItem.getValue();

				if (value == null) {
					missingCharacts.put(physicoCode, physicoListItem.getPhysicoChem());
				}
				return value;
			}
		}

		return null;
	}

	private boolean accept(ProductData formulatedProduct) {
		// Reject if the product contains the ASPECT_ENTITY_TPL aspect or is an instance of ProductSpecificationData
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || formulatedProduct instanceof ProductSpecificationData) {
			return false;
		}

		// Reject if either ingList or hazard classification list is missing
		boolean hasIngredientList = alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLIST);
		boolean hasHazardClassificationList = alfrescoRepository.hasDataList(formulatedProduct, GHSModel.TYPE_HAZARD_CLASSIFICATION_LIST);
		return (hasIngredientList && hasHazardClassificationList);

	}

	/**
	 * <p>getCLPDatabase.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getCLPDatabase() {
		NodeRef dbFolderNR = BeCPGQueryBuilder.createQuery().inDB().selectNodeByPath(repositoryHelper.getCompanyHome(), DATABASES_FOLDER);

		if ((dbFolderNR == null) || fileFolderService.listFiles(dbFolderNR).isEmpty()) {
			logger.error("CLP Database not found");
			return null;
		}

		return fileFolderService.listFiles(dbFolderNR).get(0).getNodeRef();
	}

	private CSVReader getCSVReaderFromNodeRef(NodeRef file) {
		ContentReader fileReader = contentService.getReader(file, ContentModel.PROP_CONTENT);
		return new CSVReader(new InputStreamReader(fileReader.getContentInputStream()), ';', '"', 1);
	}

}
