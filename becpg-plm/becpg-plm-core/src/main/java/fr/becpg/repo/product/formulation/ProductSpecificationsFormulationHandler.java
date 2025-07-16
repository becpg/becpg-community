/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.SpecCompatibilityModes;
import fr.becpg.repo.product.data.productList.SpecCompatibilityDataItem;
import fr.becpg.repo.product.requirement.RequirementScanner;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * The Class ProductSpecificationsFormulationHandler.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProductSpecificationsFormulationHandler extends FormulationBaseHandler<ProductData> {

	private List<RequirementScanner> requirementScanners = new LinkedList<>();

	private static Log logger = LogFactory.getLog(ProductSpecificationsFormulationHandler.class);

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private TransactionService transactionService;

	private BehaviourFilter policyBehaviourFilter;

	private NodeService nodeService;
	
	private AssociationService associationService;
	
	private BatchQueueService batchQueueService;
	
	/**
	 * <p>Setter for the field <code>batchQueueService</code>.</p>
	 *
	 * @param batchQueueService a {@link fr.becpg.repo.batch.BatchQueueService} object
	 */
	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}
	
	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>
	 * Setter for the field <code>policyBehaviourFilter</code>.
	 * </p>
	 *
	 * @param policyBehaviourFilter
	 *            a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>
	 * Setter for the field <code>alfrescoRepository</code>.
	 * </p>
	 *
	 * @param alfrescoRepository
	 *            a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>
	 * Setter for the field <code>transactionService</code>.
	 * </p>
	 *
	 * @param transactionService
	 *            a {@link org.alfresco.service.transaction.TransactionService}
	 *            object.
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * <p>
	 * Setter for the field <code>requirementScanners</code>.
	 * </p>
	 *
	 * @param requirementScanners
	 *            a {@link java.util.List} object.
	 */
	public void setRequirementScanners(List<RequirementScanner> requirementScanners) {
		this.requirementScanners = requirementScanners;
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	private Set<NodeRef> lockedSpecification = Collections.synchronizedSet(new HashSet<>());

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct)  {

		if (formulatedProduct instanceof ProductSpecificationData) {
			if (!FormulationService.FAST_FORMULATION_CHAINID.equals(formulatedProduct.getFormulationChainId()) && (SpecCompatibilityModes.Automatic
					.toString().equals(nodeService.getProperty(formulatedProduct.getNodeRef(), PLMModel.PROP_SPEC_COMPATIBILITY_TEST_MODE))
					|| SpecCompatibilityModes.Manual.toString()
							.equals(nodeService.getProperty(formulatedProduct.getNodeRef(), PLMModel.PROP_SPEC_COMPATIBILITY_TEST_MODE)))

			) {
				if (!lockedSpecification.contains(formulatedProduct.getNodeRef())) {
					try {
						lockedSpecification.add(formulatedProduct.getNodeRef());
						ProductSpecificationData specificationData = (ProductSpecificationData) formulatedProduct;
						if (alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_SPEC_COMPATIBILTY_LIST)) {
							
							StopWatch stopWatch = new StopWatch();
							stopWatch.start();
							StringBuilder logs = new StringBuilder( "Start formulate specification at " + Calendar.getInstance().getTime().toString() + ":\n");
							
							Set<NodeRef> toUpdateProducts = ConcurrentHashMap.newKeySet();
							Set<NodeRef> toSkipProducts = ConcurrentHashMap.newKeySet();
							List<NodeRef> productNodeRefs = getProductNodeRefs((ProductSpecificationData) formulatedProduct);
							logs.append( "- found " + productNodeRefs.size() + " products to test specification on\n");
							if (logger.isDebugEnabled()) {
								logger.debug(logs.toString());
							}
							String specName = specificationData.getName();
							Date specFormulatedDate = specificationData.getFormulatedDate();
							Date specModifiedDate = specificationData.getModifiedDate();
							
							BatchInfo batchInfo = new BatchInfo("productSpecificationFormulation", "becpg.batch.productSpecification.formulation", specName);
							batchInfo.setWorkerThreads(1);
							BatchStep<NodeRef> batchStep = new BatchStep<>();
							
							AtomicBoolean formulationCommitted = new AtomicBoolean(false);
							
							TransactionSupportUtil.bindListener(new TransactionListenerAdapter() {
								@Override
								public void afterCommit() {
									formulationCommitted.set(true);
								}
							}, 0);
							
							batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(Collections.synchronizedList(productNodeRefs)));
							batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
								
								public void process(NodeRef productNodeRef) throws Throwable {
									
									try {
										
										policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
										
										ProductSpecificationData productSpecificationData = (ProductSpecificationData) alfrescoRepository.findOne(formulatedProduct.getNodeRef());
										
										Date formulatedDate = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_FORMULATED_DATE);
										
										if ((formulatedDate == null) || (specFormulatedDate == null)
												|| ((specModifiedDate != null) && (specModifiedDate.getTime() > specFormulatedDate.getTime()))
												|| (formulatedDate.getTime() > specFormulatedDate.getTime())) {
											
											ProductData productData = alfrescoRepository.findOne(productNodeRef);
											
											for (RequirementScanner scanner : requirementScanners) {
												
												StringBuilder reqDetails = null;
												
												for (RequirementListDataItem reqCtrlListDataItem : scanner.checkRequirements(productData,
														Arrays.asList((ProductSpecificationData) formulatedProduct))) {
													if (RequirementType.Forbidden.equals(reqCtrlListDataItem.getReqType())
															&& RequirementDataType.Specification.equals(reqCtrlListDataItem.getReqDataType())) {
														if (reqDetails == null) {
															reqDetails = new StringBuilder(reqCtrlListDataItem.getReqMessage());
														} else {
															reqDetails.append(RepoConsts.LABEL_SEPARATOR);
															reqDetails.append(reqCtrlListDataItem.getReqMessage());
														}
													}
												}
												if (reqDetails != null) {
													if (logger.isDebugEnabled()) {
														logger.debug("Adding Forbidden for " + productNodeRef);
													}
													
													toUpdateProducts.add(productNodeRef);
													productSpecificationData.getSpecCompatibilityList().add(new SpecCompatibilityDataItem(
															RequirementType.Forbidden, reqDetails.toString(), productNodeRef));
												}
											}
										} else {
											logger.trace("Skipping productNodeRef: " + productNodeRef);
											toSkipProducts.add(productNodeRef);
										}
										alfrescoRepository.save(productSpecificationData);
									} finally {
										policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
									}
								}
							});
							
							batchStep.setBatchStepListener(new BatchStepAdapter() {
								
								@Override
								public void beforeStep() {
									int loops = 0;
									while (!formulationCommitted.get() && loops < 40) {
										loops++;
										try {
											if (logger.isDebugEnabled()) {
												logger.debug("Waiting for formulation transaction commit...");
											}
											Thread.sleep(500);
										} catch (InterruptedException e) {
											logger.error("Thread interrupted", e);
											Thread.currentThread().interrupt();
										}
									}
									if (!formulationCommitted.get()) {
										logger.warn("Formulation is taking too long, start the batch anyway");
									}
								}
								
								@Override
								public void afterStep() {
									
									try {
										policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
										
										ProductSpecificationData productSpecificationData = (ProductSpecificationData) alfrescoRepository.findOne(formulatedProduct.getNodeRef());
										productSpecificationData.getSpecCompatibilityList().removeIf(p -> !toSkipProducts.contains(p.getSourceItem()));
										
										stopWatch.stop();
										
										logs.append( "- found " + toUpdateProducts.size() + " new forbidden products,\n");
										logs.append( "- found " + toSkipProducts.size() + " products to skip,\n");
										logs.append( "batch formulation end in " + stopWatch.getTotalTimeSeconds() + "s at " + Calendar.getInstance().getTime().toString()
												+ "\n");
										productSpecificationData.setSpecCompatibilityLog(logs.toString());
										alfrescoRepository.save(productSpecificationData);
									} finally {
										policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
									}
									
								}
							});
							
							batchQueueService.queueBatch(batchInfo, List.of(batchStep));
							
						} else {
							logger.debug("No change unit list");
						}
						
					} finally {
						lockedSpecification.remove(formulatedProduct.getNodeRef());
					}

				} else {
					throw new FormulateException("Product specification " + formulatedProduct.getName() + " is already being formulated");
				}
			} else {
				formulatedProduct.setUpdateFormulatedDate(false);
			}
		} else {

			if (formulatedProduct.getReqCtrlList() == null) {
				formulatedProduct.setReqCtrlList(new LinkedList<>());
			}

			for (RequirementScanner scanner : requirementScanners) {
				if (logger.isDebugEnabled()) {
					logger.debug("Running RequirementScanner: " + scanner.getClass().getName());
				}

				formulatedProduct.getReqCtrlList().addAll(scanner.checkRequirements(formulatedProduct, formulatedProduct.getProductSpecifications()));
			}
			
		}

		return true;

	}

	/**
	 * <p>
	 * run.
	 * </p>
	 *
	 * @return a boolean.
	 */
	public boolean run() {
		try {
			return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				for (NodeRef productSpecificationNodeRef : BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_PRODUCT_SPECIFICATION)
						.excludeDefaults().list()) {
					if (SpecCompatibilityModes.Automatic.toString()
							.equals(nodeService.getProperty(productSpecificationNodeRef, PLMModel.PROP_SPEC_COMPATIBILITY_TEST_MODE))) {

						ProductData formulatedProduct = alfrescoRepository.findOne(productSpecificationNodeRef);

						StopWatch stopWatch = null;
						if (logger.isDebugEnabled()) {
							stopWatch = new StopWatch();
							stopWatch.start();
							logger.debug("Start formulate specification :" + formulatedProduct.getName());
						}
						boolean ret = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

							policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
							try {
								if (process(formulatedProduct)) {
									formulatedProduct.setFormulatedDate(new Date());
									alfrescoRepository.save(formulatedProduct);
									return true;
								} else {
									return false;
								}
							} catch (FormulateException e) {
								logger.error(e, e);
								return false;
							} finally {
								policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
							}
						}, false, true);

						if (logger.isDebugEnabled() && stopWatch != null) {
							stopWatch.stop();
							logger.debug("Formulate specification :" + formulatedProduct.getName() + " in " + stopWatch.getTotalTimeSeconds()
									+ "s success: " + ret);
						}

					}
				}

				return true;

			}, false, true);

		} catch (Exception e) {
			logger.error(e, e);
			return false;
		}

	}

	private List<NodeRef> getProductNodeRefs(ProductSpecificationData formulatedProduct) {

		if ((formulatedProduct.getSpecCompatibilityTpls() != null) && !formulatedProduct.getSpecCompatibilityTpls().isEmpty()) {
			List<NodeRef> ret = new ArrayList<>();
			for (NodeRef tplNodeRef : formulatedProduct.getSpecCompatibilityTpls()) {
				List<NodeRef> assocRefs = associationService.getSourcesAssocs(tplNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);

				for (NodeRef assocRef : assocRefs) {
					if (!nodeService.hasAspect(assocRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
							&& !tplNodeRef.equals(assocRef)) {
						ret.add(assocRef);
					}
				}

			}

			return ret;
		}

		return BeCPGQueryBuilder.createQuery().inType(PLMModel.TYPE_FINISHEDPRODUCT).inType(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
				.inType(PLMModel.TYPE_RAWMATERIAL).excludeDefaults().maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();
	}

}
