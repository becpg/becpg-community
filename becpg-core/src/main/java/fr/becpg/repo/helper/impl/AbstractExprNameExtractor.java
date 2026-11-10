package fr.becpg.repo.helper.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

/**
 * <p>Abstract AbstractExprNameExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractExprNameExtractor implements AttributeExtractorPlugin {

	private static final String CSS_CLASS_SEPARATOR = " ";

	private static final String CSS_ID_SEPARATOR = "-";

	@Autowired
	protected NodeService nodeService;

	@Autowired
	protected NamespaceService namespaceService;

	@Autowired
	protected ExpressionService expressionService;


	/**
	 * <p>extractExpr.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param exprFormat a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public  String extractExpr(NodeRef nodeRef, String exprFormat) {
		return clean(expressionService.extractExpr(nodeRef,null, exprFormat));
	}
	
	/**
	 * <p>clean.</p>
	 *
	 * @param expr a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	private String clean(String expr) {
		return expr != null ? expr.replaceFirst("^ - ", "").replaceFirst(" - $", "").trim() : expr;
	}



	/**
	 * Builds the CSS classes carrying the custom icon of an entity: the class of the template the entity was created
	 * from and, when the entity is itself a template, the class of its own icon.
	 *
	 * @param typeCss the CSS class of the entity type
	 * @param nodeRef the entity
	 * @param entityTplRef the template the entity refers to, possibly null
	 * @return the CSS classes, each one preceded by a space, or an empty string
	 */
	protected String templateCssClasses(String typeCss, NodeRef nodeRef, NodeRef entityTplRef) {
		StringBuilder cssClasses = new StringBuilder();
		if (entityTplRef != null) {
			cssClasses.append(templateCssClass(typeCss, entityTplRef));
		}
		if (!nodeRef.equals(entityTplRef) && nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
			cssClasses.append(templateCssClass(typeCss, nodeRef));
		}
		return cssClasses.toString();
	}

	private String templateCssClass(String typeCss, NodeRef templateNodeRef) {
		return CSS_CLASS_SEPARATOR + typeCss + CSS_ID_SEPARATOR + templateNodeRef.getId();
	}

	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return MEDIUM_PRIORITY;
	}

}
