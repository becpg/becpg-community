package fr.becpg.repo.expressions.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections4.CollectionUtils;
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
import fr.becpg.repo.expressions.ExpressionUrl;
import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

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
	private static final Pattern formatPattern = Pattern.compile("^format\\((.*)\\)$");

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
	private AttributeExtractorService attributeExtractorService;

	/** {@inheritDoc} */
	@Override
	public String extractExpr(NodeRef nodeRef, String exprFormat, boolean assocName) {
		return extractExpr(nodeRef, null, exprFormat, assocName);
	}
	
	/** {@inheritDoc} */
	@Override
	public String extractExpr(NodeRef nodeRef, NodeRef docNodeRef, String exprFormat) { 
		return extractExpr(nodeRef, docNodeRef, exprFormat, true);
	}

	/** {@inheritDoc} */
	@Override
	public String extractExpr(NodeRef nodeRef, NodeRef docNodeRef, String exprFormat, boolean assocName) {
		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(exprFormat);
		StringBuffer sb = new StringBuffer();
		List<NodeRef> nodeRefs = Collections.singletonList(nodeRef);
		while (patternMatcher.find()) {
			String propQname = patternMatcher.group(1);
			List<Serializable> replacement = null;
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					if (!"nodeRef".equals(propQnameAlt)) {
						replacement = extractPropText(nodeRefs, docNodeRef, propQnameAlt, assocName);
						if (replacement.stream().allMatch(NodeRef.class::isInstance)) {
							nodeRefs = replacement.stream().map(NodeRef.class::cast).collect(Collectors.toList());
							continue;
						}
						if (!sb.isEmpty()) break;
					}
				}

			} else {
				replacement = "nodeRef".equals(propQname) ? Collections.singletonList(nodeRef)
						: extractPropText(Collections.singletonList(nodeRef), docNodeRef, propQname, assocName);
			}
			patternMatcher.appendReplacement(sb, replacement != null
					? replacement.stream().map(Object::toString).collect(Collectors.joining(",")).replace("$", "")
					: "");

		}
		patternMatcher.appendTail(sb);
		return sb.toString();
		
	}

	private List<Serializable> extractPropText(List<NodeRef> nodeRefs, NodeRef docNodeRef, String propQname, boolean assocName) {

		if (propQname.startsWith("doc_")) {
			nodeRefs = Collections.singletonList(docNodeRef);
		}
		propQname = propQname.replace("doc_", "");

		return extractPropText(nodeRefs, propQname, assocName);
	}
	
	
	@SuppressWarnings("unchecked")
	private List<Serializable> extractPropText(List<NodeRef> nodeRefs, String propQname, boolean assocName) {
		final List<Serializable> ret = new ArrayList<>();
		final String postProcessing;
		if (propQname.contains("?")) {
			final String[] propQnamePostProcessing = propQname.split("\\?");
			propQname = propQnamePostProcessing[0];
			postProcessing = propQnamePostProcessing[1];
		} else {
			postProcessing = "";
		}
		final boolean xpathUrlPrefix = propQname.startsWith(ExpressionUrl.XPATH_URL_PREFIX);
		final boolean typeExpr = propQname.startsWith("@type");
		QName qname = !(xpathUrlPrefix || typeExpr) ? QName.createQName(propQname, namespaceService) : null;
		if (CollectionUtils.isNotEmpty(nodeRefs)) {
			for (final NodeRef nodeRef : nodeRefs) {
				if (propQname.startsWith(ML_PREFIX)) {
					MLText tmp = (MLText) mlNodeService.getProperty(nodeRef, QName.createQName(propQname.substring(3), namespaceService));
					ret.add(MLTextHelper.getClosestValue(tmp, I18NUtil.getContentLocale()));
				} else if (qname != null && dictionaryService.getAssociation(qname) != null) {
					List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, qname);
					if (assocs != null) {
						assocs.stream().map(AssociationRef::getTargetRef)
								.map(targetRef -> assocName ? attributeExtractorService.extractPropName(targetRef)
										: targetRef)
								.forEach(ret::add);
					}
				} else {
					Serializable value = null;
					if (qname != null) {
						value = nodeService.getProperty(nodeRef, qname);
					} else if (propQname.startsWith(ExpressionUrl.XPATH_URL_PREFIX)) {
						value = String.valueOf(BeCPGQueryBuilder.createQuery().selectNodeByPath(
								nodeRef,
								propQname.substring(ExpressionUrl.XPATH_URL_PREFIX.length())));
					} else if (propQname.startsWith("@type")) {
						QName type = nodeService.getType(nodeRef);
						value = type != null ? type.getLocalName() : "";
					}
					final Matcher matcher = formatPattern.matcher(postProcessing);
					final String formatStr = matcher.matches() ? matcher.group(1) : null;
					if (value instanceof List) {
						Stream<Serializable> stream = ((List<Serializable>) value).stream();
						if (formatStr != null) {
							stream = stream.map(listEntry -> String.format(formatStr, listEntry));
						}
						stream.forEach(ret::add);
					} else if (value != null) {
						if (formatStr != null) {
							value = String.format(formatStr, value);
						}
						ret.add(value);
					}
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

				} else {
					match = formatPattern.matcher(condition);
					if (match.matches()) {
						if (logger.isDebugEnabled()) {
							logger.debug(String.format(DEBUG_MESG, match.group(1)));
						}
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

	@Override
	public String extractExpr(NodeRef nodeRef, String exprFormat) {
		return extractExpr(nodeRef, exprFormat, true);
	}

}
