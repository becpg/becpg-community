/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.SpecCompatibilityDataItem;
import fr.becpg.repo.product.requirement.RequirementScanner;
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

	private Set<NodeRef> lookedSpecification = Collections.synchronizedSet(new HashSet<>());

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct)  {

		if (formulatedProduct instanceof ProductSpecificationData) {
			if (!FormulationService.FAST_FORMULATION_CHAINID.equals(formulatedProduct.getFormulationChainId())) {
				if (!lookedSpecification.contains(formulatedProduct.getNodeRef())) {
					try {
						lookedSpecification.add(formulatedProduct.getNodeRef());
						ProductSpecificationData specificationData = (ProductSpecificationData) formulatedProduct;
						if (alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_SPEC_COMPATIBILTY_LIST)) {
							StopWatch stopWatch = new StopWatch();
							stopWatch.start();
							StringBuilder logs = new StringBuilder( "Start formulate specification at " + Calendar.getInstance().getTime().toString() + ":\n");

							Map<NodeRef, List<String>> toUpdate = new HashMap<>();
							Set<NodeRef> toSkipProduct = new HashSet<>();
							List<NodeRef> productNodeRefs = getProductNodeRefs((ProductSpecificationData) formulatedProduct);
							logs.append( "- found " + productNodeRefs.size() + " products to test specification on\n");

							if (logger.isDebugEnabled()) {
								logger.debug(logs.toString());
							}

							for (NodeRef productNodeRef : productNodeRefs) {

								Date formulatedDate = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_FORMULATED_DATE);

								if ((formulatedDate == null) || (specificationData.getFormulatedDate() == null)
										|| ((specificationData.getModifiedDate() != null)
												&& (specificationData.getModifiedDate().getTime() > specificationData.getFormulatedDate().getTime()))
										|| (formulatedDate.getTime() > specificationData.getFormulatedDate().getTime())) {

									transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
										ProductData productData = alfrescoRepository.findOne(productNodeRef);

										for (RequirementScanner scanner : requirementScanners) {

											StringBuilder reqDetails = null;

											for (ReqCtrlListDataItem reqCtrlListDataItem : scanner.checkRequirements(productData,
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
												
												List<String> reqList = toUpdate.computeIfAbsent(productNodeRef, k -> new ArrayList<>());
												reqList.add(reqDetails.toString());
											}
										}
										return productData;
									}, true, true);
								} else {
									logger.trace("Skipping productNodeRef: " + productNodeRef);
									toSkipProduct.add(productNodeRef);
								}
							}

							specificationData.getSpecCompatibilityList().removeIf(p -> !toSkipProduct.contains(p.getSourceItem()));

							for (Map.Entry<NodeRef, List<String>> entry : toUpdate.entrySet()) {
								for (String req :  entry.getValue()) {
									specificationData.getSpecCompatibilityList()
									.add(new SpecCompatibilityDataItem(RequirementType.Forbidden, req, entry.getKey()));
								}
							}

							stopWatch.stop();

							logs.append( "- found " + toUpdate.size() + " new forbidden products,\n");
							logs.append( "- found " + toSkipProduct.size() + " products to skip,\n");
							logs.append( "formulation end in " + stopWatch.getTotalTimeSeconds() + "s at " + Calendar.getInstance().getTime().toString()
									+ "\n");
							specificationData.setSpecCompatibilityLog(logs.toString());

						} else {
							logger.debug("No change unit list");
						}
					} finally {
						lookedSpecification.remove(formulatedProduct.getNodeRef());
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
					if (Boolean.TRUE.equals(nodeService.getProperty(productSpecificationNodeRef, PLMModel.PROP_SPEC_COMPATIBILITY_JOB_ON))) {

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

						if (logger.isDebugEnabled()) {
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
