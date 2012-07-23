package fr.becpg.repo.listvalue;


import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

@Service
public class CompoListValuePlugin extends EntityListValuePlugin {

	private static Log logger = LogFactory.getLog(CompoListValuePlugin.class);

	private static final String SOURCE_TYPE_COMPOLIST_PARENT_LEVEL = "compoListParentLevel";
	
	private MultiLevelDataListService multiLevelDataListService;
	
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_COMPOLIST_PARENT_LEVEL };
	}


	public void setMultiLevelDataListService(MultiLevelDataListService multiLevelDataListService) {
		this.multiLevelDataListService = multiLevelDataListService;
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
			
			// need to load assoc so we use the MultiLevelDataListService
			MultiLevelListData mlld = multiLevelDataListService.getMultiLevelListData(dataListFilter);
			
			NodeRef itemId = null;
			@SuppressWarnings("unchecked")
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				if (extras.get("itemId") != null) {
					itemId = new NodeRef((String) extras.get("itemId"));
				}				
			}
			
			List<ListValueEntry> result = getParentsLevel(mlld, query, itemId);			
			
			return new ListValuePage(result, pageNum, pageSize, null);			
		}
		return null;
	}

	private List<ListValueEntry> getParentsLevel(MultiLevelListData mlld, String query, NodeRef itemId) {

		List<ListValueEntry> result = new ArrayList<ListValueEntry>();

		if (mlld != null) {

			for (Map.Entry<NodeRef, MultiLevelListData> kv : mlld.getTree().entrySet()) {
				
				NodeRef productNodeRef = kv.getValue().getEntityNodeRef();
				logger.debug("productNodeRef: "+ productNodeRef);
				
				// avoid cycle: when editing an item, cannot select itself as parent
				if(itemId != null && itemId.equals(kv.getKey())){
					continue;
				}

				if (nodeService.getType(productNodeRef).isMatch(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {

					boolean addNode = false;
					String productName = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
					logger.debug("productName: "+ productName + " - query: " + query);
					
					if (!query.isEmpty()) {
						
						if (productName != null) {
							if(isQueryMath(query,productName)){
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
					result.addAll(getParentsLevel(kv.getValue(), query, itemId));
				}
			}
		}

		return result;
	}


	
	boolean isQueryMath(String query, String productName) {

		if (query != null) {
		
			if(SUFFIX_ALL.equals(query)){
				return true;
			}
			
			Analyzer analyzer = getTextAnalyzer();

			if (logger.isDebugEnabled()) {
				logger.debug("Using analyzer : " + analyzer.getClass().getName());
			}
			TokenStream querySource = null;
			Reader queryReader = null;
			TokenStream productNameSource = null;
			Reader productNameReader = null;
			try {

				queryReader = new StringReader(query);
				productNameReader = new StringReader(productName);
				querySource = analyzer.tokenStream(null, queryReader);
				productNameSource = analyzer.tokenStream(null, productNameReader);
				
				Token reusableToken = new Token();
				boolean match = true;
				while ((reusableToken = querySource.next(reusableToken)) != null) {
					Token tmpToken = new Token();
					while ((tmpToken = productNameSource.next(tmpToken)) != null) {
						match = false;
						if(logger.isDebugEnabled()){
							logger.debug("Test StartWith : "+reusableToken.term()+" with "+tmpToken.term());
						}
						
						if(tmpToken.term().startsWith(reusableToken.term())){
							match = true;
							break;
						}
					}	
					if(!match){
						break;
					}
				}
				querySource.reset();
				productNameSource.reset();
				return match;
			} catch (Exception e) {
				logger.error(e, e);
			} finally {

				try {
					if (querySource != null) {
						querySource.close();
					}
					if (productNameSource != null) {
						productNameSource.close();
					}
					
				} catch (IOException e) {
					// Nothing todo here
					logger.error(e, e);
				}

			}

		}

		return false;
	}
	
	
	
	
}
