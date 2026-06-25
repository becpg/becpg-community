package fr.becpg.repo.product.extractor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.DataListItemDecorator;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Adds a human-readable {@code displayLabel} to requirement list rows by resolving
 * the legal names of the country and usage referenced by the stored
 * {@code bcpg:regulatoryCode} ({@code <country> - <usage>}). Resolved nodes are
 * cached by code, as the code-to-node mapping is stable.
 *
 * @author matthieu
 */
public class ReqCtrlListDisplayLabelDecorator implements DataListItemDecorator {

	private static final String DISPLAY_LABEL = "displayLabel";

	private static final String CODE_SEPARATOR = " - ";

	private static final String DECERNIS_PREFIX = "DECERNIS_";

	private static final NodeRef NOT_FOUND = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "not-found");

	private NodeService nodeService;

	private final Map<String, NodeRef> nodeRefByCode = new ConcurrentHashMap<>();

	/**
	 * <p>Constructor for ReqCtrlListDisplayLabelDecorator.</p>
	 */
	public ReqCtrlListDisplayLabelDecorator() {
		AbstractDataListExtractor.registerItemDecorator(PLMModel.TYPE_REQCTRLLIST, this);
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> decorate(NodeRef nodeRef) {
		String regulatoryCode = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_REGULATORY_CODE);
		if (regulatoryCode == null || regulatoryCode.isBlank()) {
			return Collections.emptyMap();
		}

		int separator = regulatoryCode.indexOf(CODE_SEPARATOR);
		String countryCode = (separator >= 0) ? regulatoryCode.substring(0, separator).trim() : regulatoryCode.trim();
		String usageCode = (separator >= 0) ? regulatoryCode.substring(separator + CODE_SEPARATOR.length()).trim() : "";

		String countryLabel = resolveLegalName(countryCode);
		String usageLabel = resolveLegalName(usageCode);

		String displayLabel = usageLabel.isEmpty() ? countryLabel : countryLabel + CODE_SEPARATOR + usageLabel;
		return Map.of(DISPLAY_LABEL, displayLabel);
	}

	/**
	 * <p>Resolves the legal name of the regulatory country or usage carrying the given
	 * code, falling back to the raw code when no node matches.</p>
	 *
	 * @param code the regulatory code
	 * @return the legal name, or the raw code as fallback
	 */
	private String resolveLegalName(String code) {
		if (code == null || code.isEmpty()) {
			return "";
		}
		NodeRef charact = nodeRefByCode.computeIfAbsent(code, this::findCharactByCode);
		if (NOT_FOUND.equals(charact)) {
			return code;
		}
		String legalName = (String) nodeService.getProperty(charact, BeCPGModel.PROP_CHARACT_NAME);
		if (legalName == null || legalName.isBlank()) {
			legalName = (String) nodeService.getProperty(charact, ContentModel.PROP_NAME);
		}
		return (legalName == null || legalName.isBlank()) ? code : legalName;
	}

	/**
	 * <p>Queries the charact (country or usage) carrying the given regulatory code.</p>
	 *
	 * <p>A usage code may be stored as a compound {@code <beCPGcode>,DECERNIS_<phrase>}
	 * value, so an exact match on the whole property fails for the requirement token,
	 * which holds only a single component (the beCPG code or the Decernis phrase). When
	 * the exact match misses, the regulatory usages are scanned and matched against each
	 * component of their stored code.</p>
	 *
	 * @param code the regulatory code
	 * @return the matching node, or {@link #NOT_FOUND} when none exists
	 */
	private NodeRef findCharactByCode(String code) {
		List<NodeRef> results = BeCPGQueryBuilder.createQuery().inDB()
				.andPropEquals(PLMModel.PROP_REGULATORY_CODE, code).list();
		if (!results.isEmpty()) {
			return results.get(0);
		}
		for (NodeRef usageRef : BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_REGULATORY_USAGE).list()) {
			String charactCode = (String) nodeService.getProperty(usageRef, PLMModel.PROP_REGULATORY_CODE);
			if (matchesComponent(charactCode, code)) {
				return usageRef;
			}
		}
		return NOT_FOUND;
	}

	/**
	 * <p>Tests whether the given token equals any component of a compound
	 * {@code <beCPGcode>,DECERNIS_<phrase>} regulatory code, stripping the
	 * {@code DECERNIS_} prefix from the phrase component.</p>
	 *
	 * @param charactCode the stored compound regulatory code, possibly null
	 * @param token       the requirement code component to match
	 * @return true when the token matches a component
	 */
	private static boolean matchesComponent(String charactCode, String token) {
		if (charactCode == null) {
			return false;
		}
		for (String part : charactCode.split(",(?=" + DECERNIS_PREFIX + ")")) {
			String candidate = part.startsWith(DECERNIS_PREFIX) ? part.substring(DECERNIS_PREFIX.length()) : part;
			if (token.equals(candidate.trim())) {
				return true;
			}
		}
		return false;
	}

}
