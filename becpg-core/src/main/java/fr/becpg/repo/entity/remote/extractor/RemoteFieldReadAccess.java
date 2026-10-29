package fr.becpg.repo.entity.remote.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.security.SecurityService;

/**
 * Field level read access of the remote export: whether a security rule hides a field from the
 * current user.
 * <p>
 * {@link SecurityService#computeAccessMode(NodeRef, QName, QName)} resolves the rules of a node on
 * every call, and an export reads dozens of fields on hundreds of nodes that share a handful of
 * types. The answer depends on the rules in force, the node type and the field only, so it is
 * computed once per such triple and kept for the request. A field no rule mentions is readable
 * whoever asks: the ruled fields of a type are read once per request, and the full evaluation,
 * which starts by resolving the authorities of the user, is only paid for those few fields.
 * <p>
 * The rules in force are the global ones, except on a node carrying a local rule
 * (<code>sec:securityRef</code>) or on an entity template, which the service treats apart: those
 * nodes get a scope of their own. The node level restrictions of the security plugins are left
 * out on purpose: whether the node itself may be read is a question for its ACL, already answered
 * by the search or by the visitor.
 * <p>
 * Not thread safe: one instance per visitor, that is per request.
 *
 * @author matthieu
 */
public class RemoteFieldReadAccess {

	private static final Log logger = LogFactory.getLog(RemoteFieldReadAccess.class);

	private static final String GLOBAL_SCOPE = "global";
	private static final String KEY_SEPARATOR = "|";

	private final SecurityService securityService;
	private final NodeService unsecuredNodeService;
	private final NamespaceService namespaceService;

	private final Map<NodeRef, RuleTarget> ruleTargetByNode = new HashMap<>();
	private final Map<QName, RuleTarget> globalRuleTargetByType = new HashMap<>();
	private final Map<String, Set<QName>> fieldsWithRulesByType = new HashMap<>();
	private final Map<String, Boolean> readAccessByField = new HashMap<>();

	/**
	 * The rules a field of a node is checked against: its type, the node itself when it has rules
	 * of its own, and the fields those rules are set on. Resolved once per node, so that reading a
	 * field costs a set lookup.
	 *
	 * @param nodeType the node type
	 * @param ruleScope the node, when it carries its own rules
	 * @param typeKey the identity of the rules in force for the type
	 * @param ruledFields the fields a rule is set on
	 */
	private record RuleTarget(QName nodeType, Optional<NodeRef> ruleScope, String typeKey, Set<QName> ruledFields) {

		private String cacheKey(QName fieldName) {
			return String.join(KEY_SEPARATOR, typeKey, fieldName.toString());
		}
	}

	/**
	 * <p>Constructor for RemoteFieldReadAccess.</p>
	 *
	 * @param securityService the service evaluating the security rules
	 * @param unsecuredNodeService the node service used to read the type and aspects of a node
	 * @param namespaceService the namespace service used to name a field as the rules do
	 */
	public RemoteFieldReadAccess(SecurityService securityService, NodeService unsecuredNodeService, NamespaceService namespaceService) {
		this.securityService = securityService;
		this.unsecuredNodeService = unsecuredNodeService;
		this.namespaceService = namespaceService;
	}

	/**
	 * Whether the current user may read a field of a node.
	 *
	 * @param nodeRef the node holding the field
	 * @param fieldName the property or association
	 * @return false when a security rule hides the field from the current user
	 */
	public boolean canRead(NodeRef nodeRef, QName fieldName) {
		RuleTarget ruleTarget = ruleTargetByNode.computeIfAbsent(nodeRef, this::resolveRuleTarget);
		return canRead(ruleTarget, fieldName);
	}

	/**
	 * Whether the current user may read a field of a type under the global rules, for a node that
	 * is no longer in the repository, such as an entity serialized in a version archive.
	 *
	 * @param nodeType the type the field belongs to
	 * @param fieldName the property or association
	 * @return false when a security rule hides the field from the current user
	 */
	public boolean canReadOnType(QName nodeType, QName fieldName) {
		RuleTarget ruleTarget = globalRuleTargetByType.computeIfAbsent(nodeType, type -> createRuleTarget(type, Optional.empty()));
		return canRead(ruleTarget, fieldName);
	}

	private boolean canRead(RuleTarget ruleTarget, QName fieldName) {
		if (!ruleTarget.ruledFields().contains(fieldName)) {
			return true;
		}
		return readAccessByField.computeIfAbsent(ruleTarget.cacheKey(fieldName), key -> computeReadAccess(ruleTarget, fieldName));
	}

	/**
	 * Without a rule entry on the field, the access mode is read or write, never none: a default
	 * read only group gives read access and the security plugins never go below it.
	 */
	private RuleTarget createRuleTarget(QName nodeType, Optional<NodeRef> ruleScope) {
		String typeKey = String.join(KEY_SEPARATOR, ruleScope.map(NodeRef::getId).orElse(GLOBAL_SCOPE), nodeType.toString());
		Set<QName> ruledFields = fieldsWithRulesByType.computeIfAbsent(typeKey,
				key -> toFieldQNames(securityService.getFieldsWithRules(ruleScope.orElse(null), nodeType)));
		return new RuleTarget(nodeType, ruleScope, typeKey, ruledFields);
	}

	/**
	 * Rules also name entries that are not fields, such as <code>View-documents</code>, or fields of
	 * a model no longer deployed: those never match a field and are left out.
	 */
	private Set<QName> toFieldQNames(Set<String> prefixedFieldNames) {
		Set<QName> fieldNames = new HashSet<>();
		for (String prefixedFieldName : prefixedFieldNames) {
			if (prefixedFieldName.indexOf(QName.NAMESPACE_PREFIX) > 0) {
				try {
					fieldNames.add(QName.createQName(prefixedFieldName, namespaceService));
				} catch (NamespaceException e) {
					logger.debug("Security rule on an unknown field ignored by the export: " + prefixedFieldName);
				}
			}
		}
		return fieldNames;
	}

	private boolean computeReadAccess(RuleTarget ruleTarget, QName fieldName) {
		boolean readable = securityService.computeAccessMode(ruleTarget.ruleScope().orElse(null), ruleTarget.nodeType(),
				fieldName) != SecurityService.NONE_ACCESS;
		if (!readable && logger.isDebugEnabled()) {
			logger.debug("Field " + fieldName + " of " + ruleTarget.nodeType() + " hidden by a security rule, left out of the export");
		}
		return readable;
	}

	private RuleTarget resolveRuleTarget(NodeRef nodeRef) {
		Optional<NodeRef> ruleScope = hasOwnRules(nodeRef) ? Optional.of(nodeRef) : Optional.empty();
		return createRuleTarget(unsecuredNodeService.getType(nodeRef), ruleScope);
	}

	private boolean hasOwnRules(NodeRef nodeRef) {
		return unsecuredNodeService.hasAspect(nodeRef, SecurityModel.ASPECT_SECURITY)
				|| unsecuredNodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL);
	}
}
