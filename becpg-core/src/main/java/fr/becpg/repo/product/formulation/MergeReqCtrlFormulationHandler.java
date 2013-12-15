package fr.becpg.repo.product.formulation;

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
	}
}
