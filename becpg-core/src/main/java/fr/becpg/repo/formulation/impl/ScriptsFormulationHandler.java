package fr.becpg.repo.formulation.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>ScriptsFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ScriptsFormulationHandler extends FormulationBaseHandler<FormulatedEntity> {

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private ScriptService scriptService;

	private SpelFormulaService formulaService;

	private ContentService contentService;

	private AssociationService associationService;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>scriptService</code>.</p>
	 *
	 * @param scriptService a {@link org.alfresco.service.cmr.repository.ScriptService} object.
	 */
	public void setScriptService(ScriptService scriptService) {
		this.scriptService = scriptService;
	}

	/**
	 * <p>Setter for the field <code>formulaService</code>.</p>
	 *
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object.
	 */
	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>logger</code>.</p>
	 *
	 * @param logger a {@link org.apache.commons.logging.Log} object.
	 */
	public static void setLogger(Log logger) {
		ScriptsFormulationHandler.logger = logger;
	}

	private static Log logger = LogFactory.getLog(ScriptsFormulationHandler.class);

	/** {@inheritDoc} */
	@Override
	public boolean process(FormulatedEntity entity) {

		if ((entity.getFormulatedEntityTpl() != null) && !entity.getFormulatedEntityTpl().equals(entity.getNodeRef())) {

			NodeRef scriptNode = associationService.getTargetAssoc(entity.getFormulatedEntityTpl(), BeCPGModel.ASSOC_ENTITY_TPL_SCRIPT);

			if ((scriptNode != null) && nodeService.exists(scriptNode)
					&& nodeService.getPath(scriptNode).toPrefixString(namespaceService).startsWith(RepoConsts.SCRIPTS_FULL_PATH)) {

				String scriptName = (String) nodeService.getProperty(scriptNode, ContentModel.PROP_NAME);

				if (logger.isDebugEnabled()) {
					logger.debug("Found script template to run:" + scriptName);
				}

				try {
					if (scriptName.endsWith(".spel")) {
						ExpressionParser parser = formulaService.getSpelParser();
						StandardEvaluationContext context = formulaService.createEntitySpelContext(entity);
						ContentReader reader = contentService.getReader(scriptNode, ContentModel.PROP_CONTENT);

						String[] formulas = SpelHelper.formatMTFormulas(reader.getContentString());
						for (String formula : formulas) {
							Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);
							if (varFormulaMatcher.matches()) {
								Expression exp = parser.parseExpression(varFormulaMatcher.group(2));
								context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
							} else {

								Expression expression = parser.parseExpression(formula);
								Object result = expression.getValue(context);
								if (logger.isDebugEnabled()) {
									logger.debug("Formula: " + formula);
									logger.debug("Expression " + expression + " returned " + result);
								}

							}
						}
					} else {
						String userName = AuthenticationUtil.getFullyAuthenticatedUser();

						Map<String, Object> model = new HashMap<>();
						model.put("currentUser", userName);
						model.put("entity", entity);

						scriptService.executeScript(scriptNode, ContentModel.PROP_CONTENT, model);

					}
				} catch (Exception e) {
					Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
					if (validCause != null) {
						throw (RuntimeException) validCause;
					}

					if (entity instanceof ReportableEntity) {
						((ReportableEntity) entity)
								.addError(MLTextHelper.getI18NMessage("message.formulate.script.error", scriptName, e.getLocalizedMessage()));
						logger.debug("Error running script : " + e.getMessage(), e);
					} else {
						logger.error("Error running script : " + e.getMessage(), e);
					}
				}
			}

		}

		return true;
	}

}
