package fr.becpg.repo.expressions.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.repository.RepositoryEntity;

@Service("expressionService")
public class ExpressionServiceImpl implements ExpressionService {

	private Log logger = LogFactory.getLog(ExpressionServiceImpl.class);

	private static final Pattern jsPattern = Pattern.compile("^js\\((.*)\\)$");
	private static final Pattern spelPattern = Pattern.compile("^spel\\((.*)\\)$");

	private static final String DEBUG_MESG = "Eval: %s";

	private static final String HELPER_JS = "<import resource=\"classpath:/beCPG/rules/helpers.js\">\n";

	@Autowired
	private ScriptService scriptService;

	@Autowired
	private ServiceRegistry serviceRegistry;

	@Autowired
	private SpelFormulaService spelFormulaService;

	@Override
	public <T extends RepositoryEntity> Object eval(String condition, T formulatedEntity) {

		Matcher match = jsPattern.matcher(condition);
		try {
			if (match.matches()) {

				if (logger.isDebugEnabled()) {
					logger.debug(String.format(DEBUG_MESG, match.group(1)));
				}

				Map<String, Object> model = new HashMap<>();
				model.put("entity", new ActivitiScriptNode(formulatedEntity.getNodeRef(), serviceRegistry));

				return scriptService.executeScriptString(HELPER_JS + match.group(1), model);

			} else {
				match = spelPattern.matcher(condition);
				if (match.matches()) {
					StandardEvaluationContext context = spelFormulaService.createEntitySpelContext(formulatedEntity);

					if (context != null) {

						if (logger.isDebugEnabled()) {
							logger.debug(String.format(DEBUG_MESG, match.group(1)));
						}

						ExpressionParser parser = spelFormulaService.getSpelParser();
						Expression expression = parser.parseExpression(match.group(1));

						return (expression.getValue(context));

					}

				}
			}

		} catch (Exception e) {
			logger.error("Failed to excute :" + match.group(1), e);
			if (formulatedEntity instanceof ReportableEntity) {
				((ReportableEntity) formulatedEntity).addError("Unable to parse catalog formula " + match.group(1));
			}

			return null;
		}

		return condition;

	}

	@Override
	public Object eval(String condition, List<NodeRef> nodeRefs) {

		Matcher match = jsPattern.matcher(condition);

		if (match.matches()) {

			try {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format(DEBUG_MESG, match.group(1)));
				}

				Map<String, Object> model = new HashMap<>();
				model.put("items", nodeRefs.stream().map(n -> new ActivitiScriptNode(n, serviceRegistry)).toArray(ScriptNode[]::new));

				return scriptService.executeScriptString(HELPER_JS + match.group(1), model);

			} catch (Exception e) {
				logger.error("Failed to excute :" + match.group(1), e);
			}
		} else {
			match = spelPattern.matcher(condition);
			if (match.matches()) {

				logger.warn("Spel not supported in context");
			}
		}

		return condition;
	}

}
