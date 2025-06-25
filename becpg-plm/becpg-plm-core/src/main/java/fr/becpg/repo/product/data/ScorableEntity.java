package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.entity.catalog.CataloguableEntity;
import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * <p>ScorableEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ScorableEntity extends CataloguableEntity, ReportableEntity {

	/**
	 * <p>getReqCtrlList.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	List<ReqCtrlListDataItem> getReqCtrlList();

	/**
	 * <p>getViews.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	List<AbstractProductDataView> getViews();

}
