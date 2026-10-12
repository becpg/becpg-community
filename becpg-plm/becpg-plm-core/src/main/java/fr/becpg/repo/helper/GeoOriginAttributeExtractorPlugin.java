package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

/**
 * <p>LabelingAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class GeoOriginAttributeExtractorPlugin implements AttributeExtractorPlugin {

	private static final String ACTIVITY_TYPE_MESSAGE_PREFIX = "listconstraint.gs1_productActivityTypeCodes.";

	@Autowired
	private  NamespaceService namespaceService;	
	
	@Autowired
	private NodeService nodeService;
	
	
	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		StringBuilder ret = new StringBuilder();
		
		// Get the character name
		Object charactName = nodeService.getProperty(nodeRef, BeCPGModel.PROP_CHARACT_NAME);
		if (charactName != null) {
			ret.append(charactName);
		}
		
		// Get activity type code if available
		@SuppressWarnings("unchecked")
		List<String> placeOfActivityCode = (List<String>) nodeService.getProperty(nodeRef, GS1Model.PROP_PRODUCT_ACTIVITY_TYPE_CODE);
		if (placeOfActivityCode != null && !placeOfActivityCode.isEmpty() && placeOfActivityCode.get(0) != null) {
			ret.append(" (").append(activityTypeLabel(placeOfActivityCode.get(0))).append(")");
		}
		
		return ret.toString();
	}

	/**
	 * Returns the label of a GS1 place of activity type, the code itself when no bundle translates it,
	 * so that an untranslated code never renders as empty parentheses.
	 *
	 * @param activityTypeCode the GS1 code of the place of activity type, eg. SLAUGHTER
	 * @return the translated label, or the code when it has no translation
	 */
	static String activityTypeLabel(String activityTypeCode) {
		String label = I18NUtil.getMessage(ACTIVITY_TYPE_MESSAGE_PREFIX + activityTypeCode);
		if ((label == null) || label.isBlank()) {
			return activityTypeCode;
		}
		return label;
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		StringBuilder ret = new StringBuilder();
		
		// Add the base type name
		String[] typeParts = PLMModel.TYPE_GEO_ORIGIN.toPrefixString(namespaceService).split(":");
		if (typeParts.length > 1) {
			ret.append(typeParts[1]);
		}
		
		// Add activity type code if available
		@SuppressWarnings("unchecked")
		List<String> placeOfActivityCode = (List<String>) nodeService.getProperty(nodeRef, GS1Model.PROP_PRODUCT_ACTIVITY_TYPE_CODE);
		if (placeOfActivityCode != null && !placeOfActivityCode.isEmpty() && placeOfActivityCode.get(0) != null) {
			ret.append("-").append(placeOfActivityCode.get(0));
		}
		
		return ret.toString();
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_GEO_ORIGIN);
	}

	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return HIGH_PRIORITY;
	}

}
