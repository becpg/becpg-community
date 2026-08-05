/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity.remote;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteParams;

/**
 * Non-regression tests for {@link RemoteParams#requiresAssociations}.
 *
 * <p>
 * The optimisation this guards is a <b>skip</b>: on a listing whose {@code fields}
 * name only plain properties, {@code JsonEntityVisitor} no longer walks the child
 * and target associations of every row. The risk of a skip is always the same —
 * dropping something a caller asked for — so each case below pins one shape of the
 * {@code fields} parameter and states what must still be served.
 * </p>
 *
 * <p>
 * The trap worth knowing: a bare {@code fields=bcpg:suppliers} lands in the
 * <i>properties</i> set, exactly like {@code cm:name}. Only the dictionary can tell
 * the two apart, which is why the decision takes a lookup rather than reading the
 * parsed structure.
 * </p>
 *
 * @author matthieu
 */
public class RemoteParamsAssocFilterTest {

	/** Associations of the model, for the tests that need the distinction. */
	private static final Set<String> ASSOCIATIONS = Set.of(
			"{http://www.bcpg.fr/model/becpg/1.0}suppliers",
			"{http://www.bcpg.fr/model/becpg/1.0}clients",
			"{http://www.bcpg.fr/model/becpg/1.0}documentTypeRef");

	private NamespaceService namespaceService;

	/** Answers like the dictionary would, without booting Alfresco. */
	private final Predicate<QName> isAssociation = qname -> ASSOCIATIONS.contains(qname.toString());

	@Before
	public void setUp() {
		namespaceService = new TestNamespaceService();
	}

	private RemoteParams paramsFor(String fields) {
		RemoteParams params = new RemoteParams(RemoteEntityFormat.json);
		Set<String> set = new HashSet<>();
		if (fields != null && !fields.isBlank()) {
			for (String field : fields.split(",")) {
				set.add(field.trim());
			}
		}
		params.setFilteredFields(set, namespaceService);
		return params;
	}

	/**
	 * The case the optimisation exists for: only properties are asked for, so the
	 * associations are not walked.
	 */
	@Test
	public void plainPropertiesSkipTheAssociationWalk() {
		Assert.assertFalse(paramsFor("cm:name").requiresAssociations(isAssociation));
		Assert.assertFalse(paramsFor("cm:name,bcpg:code").requiresAssociations(isAssociation));
		Assert.assertFalse(
				paramsFor("cm:name,bcpg:code,cm:modified,bcpg:productState").requiresAssociations(isAssociation));
	}

	/** No filter at all means "everything": nothing may be skipped. */
	@Test
	public void noFilterKeepsEverything() {
		Assert.assertTrue(paramsFor(null).requiresAssociations(isAssociation));
		Assert.assertTrue(paramsFor("").requiresAssociations(isAssociation));
	}

	/**
	 * The trap: an association asked for without a sub-property is parsed into the
	 * <i>properties</i> set. Reading the parsed structure alone would have skipped
	 * the walk and silently dropped `bcpg:suppliers` from every row.
	 */
	@Test
	public void abareAssociationStillNeedsTheWalk() {
		Assert.assertTrue(paramsFor("bcpg:suppliers").requiresAssociations(isAssociation));
		Assert.assertTrue(paramsFor("cm:name,bcpg:suppliers").requiresAssociations(isAssociation));
		Assert.assertTrue(paramsFor("cm:name,bcpg:code,bcpg:clients").requiresAssociations(isAssociation));
	}

	/** `assoc|property` is an explicit association request. */
	@Test
	public void assocPropertySyntaxNeedsTheWalk() {
		Assert.assertTrue(paramsFor("bcpg:documentTypeRef|bcpg:docTypeCategory").requiresAssociations(isAssociation));
		Assert.assertTrue(
				paramsFor("cm:name,bcpg:documentTypeRef|bcpg:docTypeCategory").requiresAssociations(isAssociation));
	}

	/**
	 * A rejection filter says what to drop, not what to keep: everything else must
	 * still be served, associations included.
	 */
	@Test
	public void rejectionFilterKeepsTheWalk() {
		Assert.assertTrue(paramsFor("!bcpg:entityScore").requiresAssociations(isAssociation));
		Assert.assertTrue(paramsFor("cm:name,!bcpg:entityScore").requiresAssociations(isAssociation));
	}

	/** Without a dictionary the safe answer is to walk. */
	@Test
	public void noDictionaryFallsBackToWalking() {
		Assert.assertTrue(paramsFor("cm:name").requiresAssociations(null));
	}

	/** An unparseable field is ignored and must not, by itself, allow a skip. */
	@Test
	public void unparseableFieldsAreNotASkipSignal() {
		// Nothing valid was parsed, so the filter is empty and everything is served.
		Assert.assertTrue(paramsFor("notaqname").requiresAssociations(isAssociation));
		// One valid property alongside: the filter is a property filter.
		Assert.assertFalse(paramsFor("notaqname,cm:name").requiresAssociations(isAssociation));
	}

	/**
	 * Minimal namespace resolution, enough for the prefixes the tests use. Booting
	 * Alfresco for a string split would make this suite an integration test.
	 */
	private static final class TestNamespaceService implements NamespaceService {

		private static String uriFor(String prefix) {
			switch (prefix) {
				case "cm":
					return "http://www.alfresco.org/model/content/1.0";
				case "bcpg":
					return "http://www.bcpg.fr/model/becpg/1.0";
				case "pjt":
					return "http://www.bcpg.fr/model/project/1.0";
				default:
					return null;
			}
		}

		@Override
		public String getNamespaceURI(String prefix) {
			String uri = uriFor(prefix);
			if (uri == null) {
				throw new org.alfresco.service.namespace.NamespaceException("Unknown prefix: " + prefix);
			}
			return uri;
		}

		@Override
		public java.util.Collection<String> getPrefixes(String namespaceURI) {
			return java.util.Collections.emptyList();
		}

		@Override
		public java.util.Collection<String> getPrefixes() {
			return java.util.List.of("cm", "bcpg", "pjt");
		}

		@Override
		public java.util.Collection<String> getURIs() {
			return java.util.List.of(uriFor("cm"), uriFor("bcpg"), uriFor("pjt"));
		}

		@Override
		public void registerNamespace(String prefix, String uri) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void unregisterNamespace(String prefix) {
			throw new UnsupportedOperationException();
		}
	}

	private static QName q(String prefixed) {
		String[] parts = prefixed.split(":");
		String uri = "cm".equals(parts[0]) ? "http://www.alfresco.org/model/content/1.0"
				: "http://www.bcpg.fr/model/becpg/1.0";
		return QName.createQName(uri, parts[1]);
	}

	/** A positive property filter drops everything it did not name, on sight. */
	@Test
	public void positiveFilterSkipsUnrequestedProperties() {
		RemoteParams params = paramsFor("cm:name,bcpg:code");
		Assert.assertFalse("cm:name was asked for", params.canSkipProperty(null, q("cm:name")));
		Assert.assertFalse("bcpg:code was asked for", params.canSkipProperty(null, q("bcpg:code")));
		Assert.assertTrue("cm:title was not", params.canSkipProperty(null, q("cm:title")));
		Assert.assertTrue("bcpg:legalName was not", params.canSkipProperty(null, q("bcpg:legalName")));
	}

	/** No filter: nothing may be skipped. */
	@Test
	public void noFilterSkipsNothing() {
		Assert.assertFalse(paramsFor(null).canSkipProperty(null, q("cm:title")));
		Assert.assertFalse(paramsFor("").canSkipProperty(null, q("cm:title")));
	}

	/** A rejection filter says what to drop, so the full path must keep deciding. */
	@Test
	public void rejectionFilterSkipsNothingEarly() {
		Assert.assertFalse(paramsFor("!bcpg:entityScore").canSkipProperty(null, q("cm:title")));
		Assert.assertFalse(paramsFor("cm:name,!bcpg:entityScore").canSkipProperty(null, q("cm:title")));
	}

	/**
	 * Inside an association covered by an `assoc|property` request, the decision
	 * belongs to the association branch — the shortcut must stand aside.
	 */
	@Test
	public void assocPropertyBranchIsLeftAlone() {
		RemoteParams params = paramsFor("cm:name,bcpg:documentTypeRef|bcpg:docTypeCategory");
		Assert.assertFalse(params.canSkipProperty(q("bcpg:documentTypeRef"), q("bcpg:docTypeCategory")));
		Assert.assertFalse(params.canSkipProperty(q("bcpg:documentTypeRef"), q("cm:title")));
		// On the node itself the property filter applies again.
		Assert.assertTrue(params.canSkipProperty(null, q("cm:title")));
	}
}
