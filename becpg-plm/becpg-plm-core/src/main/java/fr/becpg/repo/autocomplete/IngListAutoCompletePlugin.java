package fr.becpg.repo.autocomplete;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.impl.extractors.TargetAssocAutoCompleteExtractor;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.impl.CharactAttributeExtractorPlugin;

/**
 * <p>IngListAutoCompletePlugin class.</p>
 *
 * @author Alexandre Masanes
 * @version $Id: $Id
 *
 * Autocomplete plugin that provide a custom extractor for nut and physico appending unit to charact name
 *
 * Example:
 * <pre>
 * {@code
 * 		<control template="/org/alfresco/components/form/controls/autocomplete-association.ftl" >
 *			<control-param name="ds">becpg/autocomplete/physicoChem</control-param>
 *		</control>
 * }
 * </pre>
 *
 *  Datasources available:
 *
 *  becpg/autocomplete/ing
 */
@Service
public class IngListAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final String SOURCE_TYPE_ING = "ing";
	
	//private static final String PROP_CHARAC_NAME_FORMAT = "characNameFormat";
	
	//private final ThreadLocal<String> charactNameFormat = new ThreadLocal<>();
	
	@Component
	public static class IngListAutoCompleteExtractor extends TargetAssocAutoCompleteExtractor {
		
		@Autowired
		private PermissionService permissionService;
		
		@Autowired
		private NodeService nodeService;
		
		@Autowired
		private CharactAttributeExtractorPlugin charactAttributeExtractorPlugin;
		
		@Override
		protected String extractPropName(QName type, NodeRef nodeRef) {
			return AttributeExtractorService.extractPropName(permissionService, nodeService, type, nodeRef,
					charactAttributeExtractorPlugin.extractExpr(nodeRef, "{ml_bcpg:charactName} - {bcpg:casNumber}"));
		}
	}
	
	@Autowired
	private IngListAutoCompleteExtractor ingListAutoCompleteExtractor;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_ING };
	}

	
	/** {@inheritDoc} */
	/*
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		if (props != null) {
			@SuppressWarnings("unchecked")
			final Map<String, String> extras = (Map<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);
			if (extras != null) {
				charactNameFormat.set(extras.get(PROP_CHARAC_NAME_FORMAT));
			}
		}
		return suggestTargetAssoc(null, PLMModel.TYPE_ING, query, pageNum, pageSize, null, props);
	}*/

	/** {@inheritDoc} */
	@Override
	protected AutoCompleteExtractor<NodeRef> getTargetAssocValueExtractor() {
		return ingListAutoCompleteExtractor;
	}

}
