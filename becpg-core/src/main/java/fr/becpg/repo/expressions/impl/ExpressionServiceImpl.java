package fr.becpg.repo.expressions.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>ExpressionServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
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
	
	private static final String ML_PREFIX = "ml_";

	@Autowired
	protected NodeService nodeService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	protected NodeService mlNodeService;

	@Autowired
	protected NamespaceService namespaceService;

	@Autowired
	private EntityDictionaryService dictionaryService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	/** {@inheritDoc} */
	@Override
	public  String extractExpr(NodeRef nodeRef, String exprFormat) {
		return extractExpr(nodeRef,null, exprFormat);
	}

	/** {@inheritDoc} */
	@Override
	public  String extractExpr(NodeRef nodeRef, NodeRef docNodeRef, String exprFormat) {
		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(exprFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					replacement = extractPropText(nodeRef, docNodeRef, propQnameAlt);
					if ((replacement != null) && !replacement.isEmpty()) {
						break;
					}
				}

			} else {
				replacement = extractPropText(nodeRef, docNodeRef, propQname);
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

		}
		patternMatcher.appendTail(sb);
		return sb.toString();
		
	}

	private  String extractPropText(NodeRef nodeRef, NodeRef docNodeRef, String propQname) {
		NodeRef nodeToExtract = nodeRef;

		if (propQname.startsWith("doc_")) {
			nodeToExtract = docNodeRef;
		}
		propQname = propQname.replace("doc_", "");
		
		
		return extractPropText(nodeToExtract, propQname);
	}
	
	
	@SuppressWarnings("unchecked")
	private String extractPropText(NodeRef nodeRef, String propQname) {
		String ret = "";
		if (propQname.startsWith(ML_PREFIX)) {
			MLText tmp = (MLText) mlNodeService.getProperty(nodeRef, QName.createQName(propQname.substring(3), namespaceService));
			return MLTextHelper.getClosestValue(tmp, I18NUtil.getContentLocale());
		}
		QName qname = QName.createQName(propQname, namespaceService);
		if ((nodeRef != null) && (qname != null)) {
			if (dictionaryService.getAssociation(qname) != null) {
				NodeRef assoc = associationService.getTargetAssoc(nodeRef, qname);
				if (assoc != null) {
					ret = attributeExtractorService.extractPropName(assoc);
				}
			} else {
				Serializable value = nodeService.getProperty(nodeRef, QName.createQName(propQname, namespaceService));
				if (value instanceof List) {
					return ((List<String>) value).stream().collect(Collectors.joining(","));
				} else if (value != null) {

					ret = String.valueOf(value);
				}
			}
		}
		return ret;
	}
	
	/** {@inheritDoc} */
	@Override
	public String extractExpr(JSONObject object, String exprFormat) {
		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(exprFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					replacement = extractPropText(object, propQnameAlt);
					if ((replacement != null) && !replacement.isEmpty()) {
						break;
					}
				}

			} else {
				replacement = extractPropText(object, propQname);
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

		}
		patternMatcher.appendTail(sb);
		return sb.toString();
	}
	
	private String extractPropText(JSONObject object, String propQname) {
		return object.has(propQname) ? object.getString(propQname) : "";
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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
