package fr.becpg.model;

/**
 * Param used in advanced deliverable URL
 * <code>
 *  - /share/page/wizard?id=wizard-id&amp;destination={nodeRef}
 *  - /share/page/wizard?id=supplier-mp&amp;nodeRef={pjt:projectEntity}
 *  - content:{pjt:projectEntity|xpath:./cm:Documents/*}
 *  </code>
 * @author matthieu
 *
 */
public class DeliverableUrl {

	public final static String CONTENT_URL_PREFIX = "content:";
	public final static String NODEREF_URL_PARAM = "nodeRef";
	public final static String XPATH_URL_PREFIX = "xpath:";
	
}
