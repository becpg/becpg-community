package fr.becpg.repo.product.formulation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;

/**
 * Merge ReqCtrlListDataItem to avoid duplication of items and sort them
 * @author quere
 *
 */
@Service
public class MergeReqCtrlFormulationHandler extends FormulationBaseHandler<ProductData> {

	protected static Log logger = LogFactory.getLog(MergeReqCtrlFormulationHandler.class);

	@Override
	public boolean process(ProductData productData) throws FormulateException {
		
		mergeReqCtrlList(productData.getCompoListView().getReqCtrlList());
		mergeReqCtrlList(productData.getPackagingListView().getReqCtrlList());
		mergeReqCtrlList(productData.getProcessListView().getReqCtrlList());
		
		return true;
	}

	private void mergeReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList){
		
		Map<String, ReqCtrlListDataItem> dbReqCtrlList = new HashMap<>();
		Map<String, ReqCtrlListDataItem> newReqCtrlList = new HashMap<>();
		
		for(ReqCtrlListDataItem r : reqCtrlList){
			if(r.getNodeRef() != null){
				dbReqCtrlList.put(r.getReqMessage(), r);
			}
			else{
				newReqCtrlList.put(r.getReqMessage(), r);
			}
		}		

		for(Map.Entry<String, ReqCtrlListDataItem> dbKV : dbReqCtrlList.entrySet()){
			if(!newReqCtrlList.containsKey(dbKV.getKey())){
				// remove
				reqCtrlList.remove(dbKV.getValue());
			}
			else{
				// update
				ReqCtrlListDataItem newReqCtrlListDataItem = newReqCtrlList.get(dbKV.getKey());
				dbKV.getValue().setReqType(newReqCtrlListDataItem.getReqType());
				dbKV.getValue().setSources(newReqCtrlListDataItem.getSources());
				reqCtrlList.remove(newReqCtrlListDataItem);		
			}
		}
		
		//sort
		sort(reqCtrlList);
	}
	
	/**
	 * Sort by type
	 *
	 */
	private void sort(List<ReqCtrlListDataItem> reqCtrlList){
		
		Collections.sort(reqCtrlList, new Comparator<ReqCtrlListDataItem>(){
       	
		final int BEFORE = -1;
   	    final int EQUAL = 0;
   	    final int AFTER = 1;	
			
			@Override
			public int compare(ReqCtrlListDataItem r1, ReqCtrlListDataItem r2) {
				
				if (r1.getReqType() != null && r1.getReqType() != null) {
					if (r1.getReqType().equals(r2.getReqType())) {
						return EQUAL;
					}
					else if(r1.getReqType().equals(RequirementType.Forbidden)){
						return BEFORE;
					}
					else if(r2.getReqType().equals(RequirementType.Forbidden)){
						return AFTER;
					}
					else if(r1.getReqType().equals(RequirementType.Tolerated)){
						return BEFORE;
					}
					else{
						return AFTER;
					}
				}
				else if(r1.getReqType() != null){
					return BEFORE;
				}
				else if(r2.getReqType() != null){
					return AFTER;
				}
				
				return EQUAL;
			}
       });  
		
		int i = 0;
		for(ReqCtrlListDataItem r : reqCtrlList){
			r.setSort(i);
			i++;
		}
	}
}
