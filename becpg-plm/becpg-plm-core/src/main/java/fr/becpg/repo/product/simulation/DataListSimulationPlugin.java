package fr.becpg.repo.product.simulation;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.simulation.EntitySimulationPlugin;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Allow to simulation composition items
 * 
 * @author matthieu
 *
 */
@Service
public class DataListSimulationPlugin implements EntitySimulationPlugin {

	private static Log logger = LogFactory.getLog(DataListSimulationPlugin.class);

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Override
	public boolean accept(String simulationMode) {
		return EntitySimulationPlugin.DATALIST_MODE.equals(simulationMode);
	}

	@Override
	public List<NodeRef> simulateNodeRefs(NodeRef entityNodeRef, List<NodeRef> dataListItemsNodeRefs) {
		recurSimule(entityNodeRef, null, dataListItemsNodeRefs);
		return null;
	}

	private NodeRef recurSimule(NodeRef entityNodeRef, CompositionDataItem dataListItem, List<NodeRef> dataListItemsNodeRefs) {

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

	private NodeRef createSimulationNodeRef(NodeRef entityNodeRef, NodeRef parentRef) {
		return entityVersionService.createBranch(entityNodeRef, parentRef);
	}

}
