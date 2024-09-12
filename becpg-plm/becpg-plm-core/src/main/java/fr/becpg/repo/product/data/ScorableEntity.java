package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * <p>ScorableEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ScorableEntity extends ReportableEntity {

	/**
	 * <p>getEntityScore.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getEntityScore();

	/**
	 * <p>getReqCtrlList.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	List<ReqCtrlListDataItem>  getReqCtrlList();

	/**
	 * <p>setEntityScore.</p>
	 *
	 * @param string a {@link java.lang.String} object
	 */
	void setEntityScore(String string);

	/**
	 * <p>getViews.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	List<AbstractProductDataView> getViews();

}
