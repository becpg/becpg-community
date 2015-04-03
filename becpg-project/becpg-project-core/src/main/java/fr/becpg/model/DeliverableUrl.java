package fr.becpg.model;

/**
 * Param used in advanced deliverable URL
 *  - /share/page/wizard?id=wizard-id&destination={nodeRef}
 *  - /share/page/wizard?id=supplier-mp&nodeRef={pjt:projectEntity}
 *  - content:{pjt:projectEntity|xpath:./cm:Documents/child[contains('Fiche Technique Client')]}
 * @author matthieu
 *
 */
public class DeliverableUrl {

	public final static String CONTENT_URL_PREFIX = "content:";
	public final static String NODEREF_URL_PARAM = "nodeRef";
	public final static String XPATH_URL_PREFIX = "xpath:";
	
}
