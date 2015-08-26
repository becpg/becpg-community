package fr.becpg.repo.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

public abstract class AbstractExprNameExtractor implements AttributeExtractorPlugin{

	private final static String ML_PREFIX = "ml_";
	
	@Autowired
	protected NodeService nodeService;
	
	@Autowired
	@Qualifier("mlAwareNodeService")
	protected NodeService mlNodeService;

	@Autowired
	protected NamespaceService namespaceService;

	
	
	protected String extractExpr(NodeRef nodeRef, String exprFormat){
		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(exprFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					replacement = extractPropText(nodeRef, propQnameAlt);
					if (replacement != null && !replacement.isEmpty()) {
						break;
					}
				}

			} else {
				replacement = extractPropText(nodeRef, propQname);
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement : "");

		}
		patternMatcher.appendTail(sb);
		return sb.toString();
	}



	private String extractPropText(NodeRef nodeRef, String propQname) {
		if(propQname.startsWith(ML_PREFIX)){
		     MLText tmp = (MLText) mlNodeService.getProperty(nodeRef, QName.createQName(propQname.substring(3), namespaceService));
		     if(tmp!=null){
		    	return tmp.getClosestValue(I18NUtil.getContentLocale());
		     }
		} else {
			return (String) nodeService.getProperty(nodeRef, QName.createQName(propQname, namespaceService));
		}
		return null;
	}
	
	
}
