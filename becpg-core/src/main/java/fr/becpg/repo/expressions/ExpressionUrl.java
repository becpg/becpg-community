package fr.becpg.repo.expressions;

/**
 * Param used in advanced deliverable URL
 * <code>
 *  - /share/page/wizard?id=wizard-id&amp;destination={nodeRef}
 *  - /share/page/wizard?id=supplier-mp&amp;nodeRef={pjt:projectEntity}
 *  - content:{pjt:projectEntity|xpath:./cm:Documents/*}
 *  </code>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ExpressionUrl {
	
	private ExpressionUrl() {
		//Do Nothing
	}

	/** Constant <code>CONTENT_URL_PREFIX="content:"</code> */
	public static final  String CONTENT_URL_PREFIX = "content:";
	/** Constant <code>NODEREF_URL_PARAM="nodeRef"</code> */
	public static final  String NODEREF_URL_PARAM = "nodeRef";
	/** Constant <code>XPATH_URL_PREFIX="xpath:"</code> */
	public static final  String XPATH_URL_PREFIX = "xpath:";
	
	/** Constant <code>TASK_URL_PARAM="task"</code> */
	public static final  String TASK_URL_PARAM = "task";
}
