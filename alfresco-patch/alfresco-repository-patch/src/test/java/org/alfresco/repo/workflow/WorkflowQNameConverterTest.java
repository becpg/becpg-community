package org.alfresco.repo.workflow;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

/**
 * Unit tests of the beCPG caching of the workflow name to QName conversion.
 */
public class WorkflowQNameConverterTest {

	private static final String BCPGWF_PREFIX = "bcpgwf";

	private static final String BCPGWF_URI = "http://www.bcpg.fr/model/workflow/1.0";

	private static final String START_TASK = "bcpgwf:initiateProductValidationTask";

	private static final QName START_TASK_QNAME = QName.createQName(BCPGWF_URI, "initiateProductValidationTask");

	@Test
	public void unresolvedPrefixFallsBackToDefaultNamespace() {
		WorkflowQNameConverter converter = new WorkflowQNameConverter(new MapPrefixResolver());

		assertEquals(QName.createQName(NamespaceService.DEFAULT_URI, START_TASK), converter.mapNameToQName(START_TASK));
	}

	@Test
	public void unresolvedPrefixIsNotCached() {
		MapPrefixResolver prefixResolver = new MapPrefixResolver();
		WorkflowQNameConverter converter = new WorkflowQNameConverter(prefixResolver);

		converter.mapNameToQName(START_TASK);
		prefixResolver.uris.put(BCPGWF_PREFIX, BCPGWF_URI);

		assertEquals(START_TASK_QNAME, converter.mapNameToQName(START_TASK));
	}

	@Test
	public void resolvedNameIsCached() {
		MapPrefixResolver prefixResolver = new MapPrefixResolver();
		prefixResolver.uris.put(BCPGWF_PREFIX, BCPGWF_URI);
		WorkflowQNameConverter converter = new WorkflowQNameConverter(prefixResolver);

		converter.mapNameToQName(START_TASK);
		prefixResolver.uris.clear();

		assertEquals(START_TASK_QNAME, converter.mapNameToQName(START_TASK));
	}

	@Test
	public void underscoreNameIsResolved() {
		MapPrefixResolver prefixResolver = new MapPrefixResolver();
		prefixResolver.uris.put(BCPGWF_PREFIX, BCPGWF_URI);

		assertEquals(QName.createQName(BCPGWF_URI, "pvCallerActor"), WorkflowQNameConverter.convertNameToQName("bcpgwf_pvCallerActor", prefixResolver));
	}

	@Test
	public void staticConversionKeepsDefaultNamespaceFallback() {
		assertEquals(QName.createQName(NamespaceService.DEFAULT_URI, "bcpgwf_pvCallerActor"),
				WorkflowQNameConverter.convertNameToQName("bcpgwf_pvCallerActor", new MapPrefixResolver()));
	}

	private static class MapPrefixResolver implements NamespacePrefixResolver {

		private final Map<String, String> uris = new HashMap<>();

		@Override
		public String getNamespaceURI(String prefix) {
			String uri = uris.get(prefix);
			if (uri == null) {
				throw new NamespaceException("Namespace prefix " + prefix + " is not mapped to a namespace URI");
			}
			return uri;
		}

		@Override
		public Collection<String> getPrefixes(String namespaceURI) {
			return uris.entrySet().stream().filter(e -> e.getValue().equals(namespaceURI)).map(Map.Entry::getKey).toList();
		}

		@Override
		public Collection<String> getPrefixes() {
			return uris.keySet();
		}

		@Override
		public Collection<String> getURIs() {
			return uris.values();
		}
	}
}
