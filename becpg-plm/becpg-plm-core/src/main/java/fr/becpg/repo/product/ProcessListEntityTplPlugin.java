package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.MPMModel;
import fr.becpg.repo.entity.EntityTplPlugin;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.RepositoryEntity;

@Service
public class ProcessListEntityTplPlugin implements EntityTplPlugin {

	private static Log logger = LogFactory.getLog(ProcessListEntityTplPlugin.class);

	@Autowired
	private NodeService nodeService;

	@Override
	public void synchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef) {

	}

	@Override
	public boolean shouldSynchronizeDataList(RepositoryEntity entity, QName dataListQName) {
		boolean ret = (entity instanceof ResourceProductData) && MPMModel.TYPE_PROCESSLIST.equals(dataListQName);

		if (logger.isDebugEnabled()) {
			logger.debug("Should synchronize entity : " + ret + " " + dataListQName);
		}

		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T extends RepositoryEntity> void synchronizeDataList(RepositoryEntity entity, List<T> dataListItems, List<T> tplDataListItems) {

		if (logger.isDebugEnabled()) {
			logger.debug("Synchronize process list for " + entity.getName());
		}

		for (Iterator iterator = dataListItems.iterator(); iterator.hasNext();) {
			ProcessListDataItem sl = (ProcessListDataItem) iterator.next();
			boolean isFound = false;
			for (T tplDataListItem : tplDataListItems) {
				ProcessListDataItem plLItem = (ProcessListDataItem) tplDataListItem;
				if (Objects.equals(sl.getProduct(), plLItem.getProduct()) && Objects.equals(sl.getResource(), plLItem.getResource())
						&& Objects.equals(sl.getStep(), plLItem.getStep())) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				iterator.remove();
			}

		}

		for (T tplDataListItem : tplDataListItems) {
			ProcessListDataItem plLItem = (ProcessListDataItem) tplDataListItem;
			boolean isFound = false;
			for (ProcessListDataItem sl : (List<ProcessListDataItem>) dataListItems) {
				if (Objects.equals(sl.getProduct(), plLItem.getProduct()) && Objects.equals(sl.getResource(), plLItem.getResource())
						&& Objects.equals(sl.getStep(), plLItem.getStep())) {
					if (tplDataListItem.getNodeRef() != null) {
						Map<QName, Serializable> props = nodeService.getProperties(tplDataListItem.getNodeRef());
						for (Map.Entry<QName, Serializable> prop : props.entrySet()) {
							if (!prop.getKey().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
									&& !prop.getKey().getNamespaceURI().equals(NamespaceService.CONTENT_MODEL_1_0_URI)) {
								sl.getExtraProperties().put(prop.getKey(), prop.getValue());
							}
						}

					}
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				if (tplDataListItem.getNodeRef() != null) {
					Map<QName, Serializable> props = nodeService.getProperties(tplDataListItem.getNodeRef());

					for (Map.Entry<QName, Serializable> prop : props.entrySet()) {
						if (!prop.getKey().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
								&& !prop.getKey().getNamespaceURI().equals(NamespaceService.CONTENT_MODEL_1_0_URI)) {
							tplDataListItem.getExtraProperties().put(prop.getKey(), prop.getValue());
						}
					}
				}
				tplDataListItem.setNodeRef(null);
				tplDataListItem.setParentNodeRef(null);
				dataListItems.add(tplDataListItem);
			}
		}

	}

}
