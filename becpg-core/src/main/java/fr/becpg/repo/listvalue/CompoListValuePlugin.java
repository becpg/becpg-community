package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.listvalue.impl.AbstractBaseListValuePlugin;

public class CompoListValuePlugin extends AbstractBaseListValuePlugin {

	private static Log logger = LogFactory.getLog(CompoListValuePlugin.class);

	private static final String SOURCE_TYPE_COMPOLIST_PARENT_LEVEL = "compoListParentLevel";
	private static final String SUFFIX_ALL = "*";
	
	private MultiLevelDataListService multiLevelDataListService;
	private NodeService nodeService;

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_COMPOLIST_PARENT_LEVEL };
	}


	public void setMultiLevelDataListService(MultiLevelDataListService multiLevelDataListService) {
		this.multiLevelDataListService = multiLevelDataListService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {
		
		NodeRef entityNodeRef = new NodeRef((String)props.get(ListValueService.PROP_NODEREF));
		logger.debug("CompoListValuePlugin sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);
		
		if(sourceType.equals(SOURCE_TYPE_COMPOLIST_PARENT_LEVEL)){
			
			DataListFilter dataListFilter = new DataListFilter();
			dataListFilter.setDataType(BeCPGModel.TYPE_COMPOLIST);
			dataListFilter.setEntityNodeRef(entityNodeRef);
			
			MultiLevelListData mlld = multiLevelDataListService.getMultiLevelListData(dataListFilter);
			
			List<ListValueEntry> result = getParentsLevel(mlld, query);			
			
			return new ListValuePage(result, pageNum, pageSize, null);			
		}
		return null;
	}

	private List<ListValueEntry> getParentsLevel(MultiLevelListData mlld, String query) {

		List<ListValueEntry> result = new ArrayList<ListValueEntry>();

		if (mlld != null) {

			for (Map.Entry<NodeRef, MultiLevelListData> kv : mlld.getTree().entrySet()) {
				
				NodeRef productNodeRef = kv.getValue().getEntityNodeRef();
				logger.debug("productNodeRef: "+ productNodeRef);

				if (nodeService.getType(productNodeRef).isMatch(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {

					boolean addNode = false;
					String productName = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
					logger.debug("productName: "+ productName + " - query: " + query);
					
					if (!query.isEmpty()) {
						
						if (productName != null) {
							
							if(query.endsWith(SUFFIX_ALL)){
								query = query.substring(0, query.length()-1);
							}
							
							String name = productName.substring(0, query.length());
							if (query.equalsIgnoreCase(name)) {
								addNode = true;
							}
						}
					} else {
						addNode = true;
					}

					if (addNode) {
						logger.debug("add node productName: "+ productName);
						String state = (String )nodeService.getProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_STATE);
						result.add(new ListValueEntry(kv.getKey().toString(), productName, BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT.getLocalName()+"-"+state));
					}
				}

				if (kv.getValue() != null) {
					result.addAll(getParentsLevel(kv.getValue(), query));
				}
			}
		}

		return result;
	}

}
