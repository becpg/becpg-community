package fr.becpg.model;

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
public class DeliverableUrl {

	/** Constant <code>CONTENT_URL_PREFIX="content:"</code> */
	public final static String CONTENT_URL_PREFIX = "content:";
	/** Constant <code>NODEREF_URL_PARAM="nodeRef"</code> */
	public final static String NODEREF_URL_PARAM = "nodeRef";
	/** Constant <code>XPATH_URL_PREFIX="xpath:"</code> */
	public final static String XPATH_URL_PREFIX = "xpath:";
	
	/** Constant <code>TASK_URL_PARAM="task"</code> */
	public final static String TASK_URL_PARAM = "task";
}
