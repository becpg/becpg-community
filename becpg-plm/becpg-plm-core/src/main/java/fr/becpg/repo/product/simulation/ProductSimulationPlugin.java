package fr.becpg.repo.product.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.google.common.collect.Lists;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.simulation.EntitySimulationPlugin;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;

@Service
public class ProductSimulationPlugin implements EntitySimulationPlugin {

	private static Log logger = LogFactory.getLog(ProductSimulationPlugin.class);

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private TransactionService transactionService;

	@Override
	public boolean accept(String simulationMode) {
		return EntitySimulationPlugin.RECUR_MODE.equals(simulationMode);
	}

	@Override
	public List<NodeRef> simulateNodeRefs(NodeRef destNodeRef, List<NodeRef> nodeRefs) {

		Map<NodeRef, NodeRef> cache = new HashMap<>();

		StopWatch watch = null;
		if (logger.isInfoEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		L2CacheSupport.doInCacheContext(() -> {
			for (final List<NodeRef> subList : Lists.partition(nodeRefs, 5)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					for (final NodeRef entityNodeRef : subList) {
						simuleChild(destNodeRef, entityNodeRef, cache, true);
					}
					return null;

				}, false, true);

			}

		}, false, true);

		if (logger.isInfoEnabled()) {
			watch.stop();
			logger.info("Simulate batch takes " + watch.getTotalTimeSeconds() + " seconds");
		}

		return new ArrayList<>(cache.values());
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

	private NodeRef simuleChild(NodeRef destNodeRef, NodeRef productNodeRef, Map<NodeRef, NodeRef> visitedNodes, boolean firstLevel) {
		QName type = nodeService.getType(productNodeRef);
		if (PLMModel.TYPE_FINISHEDPRODUCT.equals(type) || PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(type) || firstLevel) {
			NodeRef productBranchNodeRef = visitedNodes.get(productNodeRef);
			if (productBranchNodeRef == null) {
				productBranchNodeRef = entityVersionService.createBranch(productNodeRef, destNodeRef);

				String oldErpCode = (String) nodeService.getProperty(productNodeRef, PLMModel.PROP_ERP_CODE);
				if ((oldErpCode != null) && !oldErpCode.isEmpty()) {
					String destName = (String) nodeService.getProperty(destNodeRef, ContentModel.PROP_NAME);
					nodeService.setProperty(productBranchNodeRef, PLMModel.PROP_ERP_CODE, destName + oldErpCode);
				}
				
				logger.debug("Create simulation node for: "+ nodeService.getProperty(productBranchNodeRef,  ContentModel.PROP_NAME));
				
				visitedNodes.put(productNodeRef, productBranchNodeRef);
			} else {
				logger.debug("Found simulation node in cache: "+ nodeService.getProperty(productBranchNodeRef,  ContentModel.PROP_NAME));
			}
			if (PLMModel.TYPE_FINISHEDPRODUCT.equals(type) || PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(type)) {

				ProductData productData = alfrescoRepository.findOne(productBranchNodeRef);

				logger.debug(" -- Visit simulation node:  " + productData.getName() + " " + productData.getErpCode());

				if (productData.getCompoList() != null) {
					for (CompositionDataItem item : productData.getCompoList()) {
						NodeRef simulationNodeRef = simuleChild(destNodeRef, item.getComponent(), visitedNodes, false);
						if ((simulationNodeRef != null) && !simulationNodeRef.equals(item.getComponent())) {
							associationService.update(item.getNodeRef(), item.getComponentAssocName(), simulationNodeRef);
						}
					}
				}
			}

			return productBranchNodeRef;
		}

		return null;
	}

}
