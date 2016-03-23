package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * 
 * @author steven
 *
 */
public class ReqCtrlWebScript extends AbstractProductWebscript {

	private static final Log logger = LogFactory.getLog(ReqCtrlWebScript.class);

	AlfrescoRepository<ProductData> alfrescoRepository;

	NodeService nodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		NodeRef productNodeRef = getProductNodeRef(req);

		ProductData product = alfrescoRepository.findOne(productNodeRef);
		StopWatch watch = null;

		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		//Fetch query params
		String type = req.getParameter("type");
		String dataType = req.getParameter("dataType");
		String view = req.getParameter("view");

		//Actual filters
		RequirementType rType;
		RequirementDataType rDataType;

		Map<String, Map<String, Integer>> counts = new HashMap<String, Map<String, Integer>>();
		//Try to match requirement type. Default is null (no filter)
		try {
			rType = RequirementType.valueOf(type);
		} catch(Exception e){
			//either IllegalArgumentException or NPE
			rType = null;
		}

		//Try to match requirement data type. Default is null (no filter)
		try {
			rDataType = RequirementDataType.valueOf(dataType);
		} catch(Exception e){
			//either IllegalArgumentException or NPE
			rDataType = null;
		}

		//fetches correct list to find rclDataItem in
		List<ReqCtrlListDataItem> ctrlList;
		if(logger.isDebugEnabled()){
			logger.debug("Called view: "+view);
		}
		if(view != null && view.equals("processList")){

			ctrlList = product.getProcessListView().getReqCtrlList();
		} else if(view != null && view.equals("packagingList")){
			ctrlList = product.getPackagingListView().getReqCtrlList();
		} else {
			ctrlList = product.getCompoListView().getReqCtrlList();
		}

		for(ReqCtrlListDataItem item : ctrlList){
			//Filtering on reqType and reqDataType
			if(rType != null && item.getReqType() != (rType)){
				continue;
			}
			if(rDataType != null && item.getReqDataType() != null && item.getReqDataType() != (rDataType)){
				continue;
			}

			//sets key in case resDataType would be null
			RequirementDataType key;
			if(item.getReqDataType() == null){
				key = RequirementDataType.Nutrient;
			} else {
				key = item.getReqDataType();
			}

			//Filter passed, adding count		
			if(counts.containsKey(key.toString())){					
				Map<String, Integer> currentCount = counts.get(key.toString());
				if(currentCount == null){
					currentCount = new HashMap<String, Integer>();
				}

				if(currentCount.containsKey(item.getReqType().toString())){
					currentCount.put(item.getReqType().toString(), currentCount.get(item.getReqType().toString())+1);
				} else {
					currentCount.put(item.getReqType().toString(), 1);
				}
			} else {
				//this dataType was not found before, adding it
				Map<String, Integer> newMap = new HashMap<String, Integer>();
				newMap.put(item.getReqType().toString(), 1);
				counts.put(key.toString(), newMap);
			}
		}

		try {			
			//puts each count of rclDataItems in ret, mapped with proper key 
			List<JSONObject> rclSortingArray = new ArrayList<JSONObject>();
			JSONObject ret = new JSONObject();
			for(String dt : counts.keySet()){				
				Map<String, Integer> currentCount = counts.get(dt);

				JSONObject rclValues = new JSONObject();

				for(String key : currentCount.keySet()){
					rclValues.put(key, currentCount.get(key));
				}
				JSONObject rclValuesMapping = new JSONObject();
				rclValuesMapping.put(dt, rclValues);
				rclSortingArray.add(rclValuesMapping);
			}

			rclSortingArray.sort(new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					//sort on keys (fbd > all)
					
					try {
						JSONObject o1Values = o1.getJSONObject((String) o1.keys().next());
						JSONObject o2Values = o2.getJSONObject((String) o2.keys().next());
//						if(logger.isDebugEnabled()){
//							logger.debug("Comparing "+o1+"(has Fbd: "+o1Values.has("Forbidden")+") and "+o2+" ("+o2Values.has("Forbidden")+")");
//						}
						
						//TODO check if we only need forbidden to sort, or other sorting criterias are relevant
						if((o1Values.has("Forbidden") && !(o2Values.has("Forbidden")))
								|| (o1Values.has("Forbidden") && o2Values.has("Forbidden") && o1Values.getInt("Forbidden") >= o2Values.getInt("Forbidden"))){
							if(logger.isDebugEnabled()){
								logger.debug(o1+" (o1) > "+o2+" (o2)");
							}
							return -1;
						} else {
							if(logger.isDebugEnabled()){
								logger.debug(o2+" (o2) > "+o1+" (o1)");
							}
							return 1;
						}
					} catch (JSONException e){
						if(logger.isDebugEnabled()){
							logger.debug("JSONException, returning equals");
						}
						return 0;
					}
				}
			});
			
			if(logger.isDebugEnabled()){
				logger.debug("Sorted rclNumber: "+rclSortingArray);
			}

			//might be null if product has never been formulated, if not put it in res
			if(product.getEntityScore() != null){
				JSONObject scores = new JSONObject(product.getEntityScore());
				ret.put("rclNumber", rclSortingArray.toArray());
				ret.put("scores", scores);
				if(logger.isDebugEnabled()){
					logger.debug("ret : "+ret);
					logger.debug("scores="+scores);
				}
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

		if(logger.isDebugEnabled()){
			assert watch != null;
			watch.stop();
			logger.debug("ReqCtrlWebScript : "+this.getClass().getName()+" takes " + watch.getTotalTimeSeconds() + " seconds");
		}
	}

}
