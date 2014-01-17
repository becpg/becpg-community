package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BaseObject;

public abstract class AbstractProductDataView extends BaseObject {

	protected List<ReqCtrlListDataItem> reqCtrlList;
	protected List<DynamicCharactListItem> dynamicCharactList;
	
	public abstract List<? extends CompositionDataItem> getMainDataList();

	@DataList
	@AlfQname(qname="bcpg:reqCtrlList")
	public List<ReqCtrlListDataItem> getReqCtrlList() {
		return reqCtrlList;
	}

	public void setReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList) {
		this.reqCtrlList = reqCtrlList;
	}

	
	@DataList
	@AlfQname(qname="bcpg:dynamicCharactList")
	public List<DynamicCharactListItem> getDynamicCharactList() {
		return dynamicCharactList;
	}

	public void setDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList) {
		this.dynamicCharactList = dynamicCharactList;
	}

	
}
