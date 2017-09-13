package fr.becpg.repo.simulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("simulationService")
public class SimulationServiceImpl implements SimulationService {

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	@Qualifier("ecoAsyncThreadPool")
	private ThreadPoolExecutor threadExecuter;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private NodeService nodeService;

	private static Log logger = LogFactory.getLog(SimulationServiceImpl.class);

	@Override
	public void createBudget(NodeRef destNodeRef, SystemState state) {

		Runnable command = new AsyncCreateBudgetCommand(destNodeRef, state, AuthenticationUtil.getFullyAuthenticatedUser());
		if (!threadExecuter.getQueue().contains(command)) {
			threadExecuter.execute(command);
		} else {
			logger.warn("AsyncCreateBudgetCommand job already in queue for " + destNodeRef);
			logger.info("AsyncCreateBudgetCommand active task size " + threadExecuter.getActiveCount());
			logger.info("AsyncCreateBudgetCommand queue size " + threadExecuter.getTaskCount());
		}

	}

	/**
	 * Pour le budget :
	 * 
	 * Création d'un site budget Création d'une action pour créer le budget
	 * 
	 * Création d'un dossier année dans le site budget Création des branches des
	 * PF et SF validé dans le dossier années Mise à jour des codes ERP (
	 * ERP-BUD2017)
	 * 
	 * Les cadences sont ensuite réimporté sur les éléments budget, les coûts
	 * N+1 sont importés sur les MP et emballage Les nomenclatures et PF/SF
	 * budget sont mis à jour SyncToX3 est coché
	 * 
	 * Les nouveaux articles budgets sont créé dans X3 Les nomenclatures des
	 * articles existants sont mises à jour avec une effectivité au 1er janvier
	 * de l'année budget
	 * 
	 */

	private class AsyncCreateBudgetCommand implements Runnable {

		private final NodeRef destNodeRef;
		private SystemState state = SystemState.Valid;
		private final String userName;

		public AsyncCreateBudgetCommand(NodeRef destNodeRef, SystemState state, String userName) {
			super();
			this.destNodeRef = destNodeRef;
			this.state = state;
			this.userName = userName;
		}

		@Override
		public void run() {
			try {

				AuthenticationUtil.runAs(() -> {

					List<NodeRef> productNodeRefs = transactionService.getRetryingTransactionHelper()
							.doInTransaction(
									() -> BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_FINISHEDPRODUCT)
											.andPropEquals(PLMModel.PROP_PRODUCT_STATE, state.toString()).excludeDefaults().inDB().list(),
									false, true);

					for (NodeRef product : productNodeRefs) {

						transactionService.getRetryingTransactionHelper().doInTransaction(() -> simuleChild(destNodeRef, product, new HashMap<>()),
								false, true);

					}

					return destNodeRef;

				}, userName);

			} catch (Exception e) {
				if (e instanceof ConcurrencyFailureException) {
					throw (ConcurrencyFailureException) e;
				}
				logger.error("Unable to create budget ", e);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + getOuterType().hashCode();
			result = (prime * result) + ((destNodeRef == null) ? 0 : destNodeRef.hashCode());
			result = (prime * result) + ((state == null) ? 0 : state.hashCode());
			result = (prime * result) + ((userName == null) ? 0 : userName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			AsyncCreateBudgetCommand other = (AsyncCreateBudgetCommand) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (destNodeRef == null) {
				if (other.destNodeRef != null) {
					return false;
				}
			} else if (!destNodeRef.equals(other.destNodeRef)) {
				return false;
			}
			if (state != other.state) {
				return false;
			}
			if (userName == null) {
				if (other.userName != null) {
					return false;
				}
			} else if (!userName.equals(other.userName)) {
				return false;
			}
			return true;
		}

		private AsyncCreateBudgetCommand getOuterType() {
			return AsyncCreateBudgetCommand.this;
		}

	}

	private NodeRef simuleChild(NodeRef destNodeRef, NodeRef productNodeRef, Map<NodeRef, NodeRef> visitedNodes) {
		QName type = nodeService.getType(productNodeRef);
		if (PLMModel.TYPE_FINISHEDPRODUCT.equals(type) || PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(type)) {
			NodeRef productBranchNodeRef = visitedNodes.get(productNodeRef);
			if (productBranchNodeRef == null) {
				productBranchNodeRef = entityVersionService.createBranch(productNodeRef, destNodeRef);
				visitedNodes.put(productNodeRef, productBranchNodeRef);
			}

			ProductData productData = alfrescoRepository.findOne(productNodeRef);

			if (productData.getCompoList() != null) {
				for (CompositionDataItem item : productData.getCompoList()) {
					NodeRef simulationNodeRef = simuleChild(destNodeRef, item.getComponent(), visitedNodes);
					if ((simulationNodeRef != null) && !simulationNodeRef.equals(item.getComponent())) {
						associationService.update(item.getNodeRef(), item.getComponentAssocName(), simulationNodeRef);
					}
				}
			}

			return productBranchNodeRef;
		}

		return null;
	}

	@Override
	public NodeRef recurSimule(NodeRef entityNodeRef, CompositionDataItem dataListItem, List<NodeRef> dataListItemsNodeRefs) {

		NodeRef parentNodeRef = dataListItem != null ? dataListItem.getComponent() : entityNodeRef;

		ProductData productData = alfrescoRepository.findOne(parentNodeRef);

		if (productData.getCompoList() != null) {

			for (AbstractProductDataView view : productData.getViews()) {
				for (CompositionDataItem item : view.getMainDataList()) {

					NodeRef simulationNodeRef = recurSimule(entityNodeRef, item, dataListItemsNodeRefs);
					if (simulationNodeRef != null) {
						if (dataListItem == null) {
							logger.debug("Update root " + productData.getName());
							associationService.update(item.getNodeRef(), item.getComponentAssocName(), simulationNodeRef);
						} else {
							NodeRef parentSimulationNodeRef = createSimulationNodeRef(parentNodeRef,
									nodeService.getPrimaryParent(entityNodeRef).getParentRef());
							ProductData newProductData = alfrescoRepository.findOne(parentSimulationNodeRef);
							logger.debug("Create new SF " + newProductData.getName());

							for (AbstractProductDataView newView : newProductData.getViews()) {
								if (newView.getClass().getName().equals(view.getClass().getName())) {
									for (CompositionDataItem newItem : newView.getMainDataList()) {
										NodeRef origNodeRef = associationService.getTargetAssoc(newItem.getNodeRef(), ContentModel.ASSOC_ORIGINAL);
										if ((origNodeRef != null) && origNodeRef.equals(item.getNodeRef())) {
											associationService.update(newItem.getNodeRef(), item.getComponentAssocName(), simulationNodeRef);
											logger.debug("Update new SF " + newProductData.getName());
											return newProductData.getNodeRef();
										}
									}
								}
							}
						}
					}
				}
			}

		}

		if ((dataListItem != null) && dataListItemsNodeRefs.contains(dataListItem.getNodeRef())) {
			logger.debug("Found item to simulate:" + dataListItem.getNodeRef());
			return createSimulationNodeRef(dataListItem.getComponent(), nodeService.getPrimaryParent(entityNodeRef).getParentRef());
		}

		return null;

	}

	@Override
	public NodeRef createSimulationNodeRef(NodeRef entityNodeRef, NodeRef parentRef) {
		return entityVersionService.createBranch(entityNodeRef, parentRef);
	}

}
