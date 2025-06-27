package fr.becpg.repo.helper.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

/**
 * <p>Abstract AbstractExprNameExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractExprNameExtractor implements AttributeExtractorPlugin {

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
	
	private String clean(String expr) {
		return expr != null ? expr.replaceFirst("^ - ", "").replaceFirst(" - $", "").trim() : expr;
	}



	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return MEDIUM_PRIORITY;
	}

}
