package fr.becpg.repo.formulation.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.AssociationService;

public class ScriptsFormulationHandler extends FormulationBaseHandler<FormulatedEntity> {

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private ScriptService scriptService;

	private SpelFormulaService formulaService;

	private ContentService contentService;

	private AssociationService associationService;
	

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setScriptService(ScriptService scriptService) {
		this.scriptService = scriptService;
	}

	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public static void setLogger(Log logger) {
		ScriptsFormulationHandler.logger = logger;
	}

	private static Log logger = LogFactory.getLog(ScriptsFormulationHandler.class);

	@Override
	public boolean process(FormulatedEntity entity) throws FormulateException {

		if ((entity.getFormulatedEntityTpl() != null) && !entity.getFormulatedEntityTpl().equals(entity.getNodeRef())) {

			NodeRef scriptNode = associationService.getTargetAssoc(entity.getFormulatedEntityTpl(), BeCPGModel.PROP_ENTITY_TPL_SCRIPT);

			if ((scriptNode != null) && nodeService.exists(scriptNode)
					&& nodeService.getPath(scriptNode).toPrefixString(namespaceService).startsWith(RepoConsts.SCRIPTS_FULL_PATH)) {

				logger.debug("Found script template to run");

				if (((String) nodeService.getProperty(scriptNode, ContentModel.PROP_NAME)).endsWith(".spel")) {

					ExpressionParser parser = new SpelExpressionParser();
					StandardEvaluationContext context = formulaService.createEntitySpelContext(entity);
					ContentReader reader = contentService.getReader(scriptNode, ContentModel.PROP_CONTENT);
					

					String[] formulas = SpelHelper.formatMTFormulas(reader.getContentString());
					for (String formula : formulas) {

						Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);
						if (varFormulaMatcher.matches()) {
							Expression exp = parser.parseExpression(varFormulaMatcher.group(2));
							context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
						} else {
							parser.parseExpression(formula);
						}
					}
				} else {

					String userName = AuthenticationUtil.getFullyAuthenticatedUser();

					Map<String, Object> model = new HashMap<>();
					model.put("currentUser", userName);
					model.put("entity", entity);

					scriptService.executeScript(scriptNode, ContentModel.PROP_CONTENT, model);
				}
			}

		}

		return true;
	}

}
