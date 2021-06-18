package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;
import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>Abstract AbstractExprNameExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractExprNameExtractor implements AttributeExtractorPlugin {

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

	/**
	 * <p>extractExpr.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param exprFormat a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String extractExpr(NodeRef nodeRef, String exprFormat) {
		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(exprFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					replacement = extractPropText(nodeRef, propQnameAlt);
					if ((replacement != null) && !replacement.isEmpty()) {
						break;
					}
				}

			} else {
				replacement = extractPropText(nodeRef, propQname);
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

		}
		patternMatcher.appendTail(sb);
		return sb.toString();
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
	public Integer getPriority() {
		return 0;
	}

}
