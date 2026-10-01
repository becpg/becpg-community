package fr.becpg.repo.product.comparison;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;

/**
 * Unit tests of the key used to match multi-level datalist items in the comparison report.
 */
public class DefaultCompareEntityServicePluginTest {

	private static final QName PIVOT = QName.createQName(BeCPGModel.BECPG_URI, "pivotTest");

	private static final NodeRef CHILD = new NodeRef("workspace://SpacesStore/child");
	private static final NodeRef PARENT = new NodeRef("workspace://SpacesStore/parent");
	private static final NodeRef ROOT = new NodeRef("workspace://SpacesStore/root");

	private NodeService nodeService;
	private DefaultCompareEntityServicePlugin plugin;

	@Before
	public void setUp() {
		nodeService = mock(NodeService.class);
		DictionaryService dictionaryService = mock(DictionaryService.class);
		when(dictionaryService.getProperty(PIVOT)).thenReturn(mock(PropertyDefinition.class));
		plugin = new DefaultCompareEntityServicePlugin();
		inject("nodeService", nodeService);
		inject("dictionaryService", dictionaryService);
	}

	private void inject(String fieldName, Object value) {
		try {
			Field field = DefaultCompareEntityServicePlugin.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(plugin, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Cannot inject " + fieldName, e);
		}
	}

	@Test
	public void keyIncludesParentLevels() {
		givenItem(CHILD, "child", PARENT);
		givenItem(PARENT, "parent", ROOT);
		givenItem(ROOT, "root", null);

		assertEquals("child|parent|root", plugin.getKeyFromPivots(CHILD, List.of(PIVOT)));
	}

	@Test
	public void cyclicParentLevelsDoNotRecurseForever() {
		givenItem(CHILD, "child", PARENT);
		givenItem(PARENT, "parent", CHILD);

		assertEquals("child|parent", plugin.getKeyFromPivots(CHILD, List.of(PIVOT)));
	}

	@Test
	public void itemThatIsItsOwnParentIsKeyedOnce() {
		givenItem(CHILD, "child", CHILD);

		assertEquals("child", plugin.getKeyFromPivots(CHILD, List.of(PIVOT)));
	}

	private void givenItem(NodeRef item, String pivotValue, NodeRef parentLevel) {
		when(nodeService.getProperty(item, PIVOT)).thenReturn(pivotValue);
		when(nodeService.hasAspect(item, BeCPGModel.ASPECT_DEPTH_LEVEL)).thenReturn(true);
		when(nodeService.getProperty(item, BeCPGModel.PROP_PARENT_LEVEL)).thenReturn(parentLevel);
	}
}
