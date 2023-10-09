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


	public  String extractExpr(NodeRef nodeRef, String exprFormat) {
		return expressionService.extractExpr(nodeRef,null, exprFormat);
	}
	

	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return 0;
	}

}
