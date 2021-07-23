package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public interface ScorableEntity extends ReportableEntity {

	String getEntityScore();

	List<ReqCtrlListDataItem>  getReqCtrlList();

	void setEntityScore(String string);

	List<AbstractProductDataView> getViews();

}
