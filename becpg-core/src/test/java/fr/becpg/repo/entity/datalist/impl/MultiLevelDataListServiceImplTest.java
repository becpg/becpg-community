package fr.becpg.repo.entity.datalist.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.security.SecurityService;

/**
 * Checks which rows of a list holding its own hierarchy (bcpg:parentLevel) are kept in the tree, depending on the rows
 * the user collapsed: every descendant of a collapsed row is hidden, whatever its level, while a row whose parent is
 * filtered out of the list is still displayed at the root (#37722, #33948). Whatever the order the list is sorted in,
 * rows stay under their parent: roots are sorted among themselves, then the children of each row (#37984).
 *
 * @author matthieu
 */
public class MultiLevelDataListServiceImplTest {

    private static final QName LIST_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "ingList");

    private static final QName ENTITY_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "product");

    private static final NodeRef ENTITY_NODE_REF = new NodeRef("workspace://SpacesStore/entity");

    private static final NodeRef CONTAINER_NODE_REF = new NodeRef("workspace://SpacesStore/container");

    private static final NodeRef LIST_NODE_REF = new NodeRef("workspace://SpacesStore/list");

    private static final NodeRef LEVEL1_NODE_REF = new NodeRef("workspace://SpacesStore/level1");

    private static final NodeRef LEVEL2_NODE_REF = new NodeRef("workspace://SpacesStore/level2");

    private static final NodeRef LEVEL3_NODE_REF = new NodeRef("workspace://SpacesStore/level3");

    private static final NodeRef SIBLING_NODE_REF = new NodeRef("workspace://SpacesStore/sibling");

    private static final NodeRef CHILD_A_NODE_REF = new NodeRef("workspace://SpacesStore/childA");

    private static final NodeRef CHILD_Z_NODE_REF = new NodeRef("workspace://SpacesStore/childZ");

    private static final NodeRef CYCLE_A_NODE_REF = new NodeRef("workspace://SpacesStore/cycleA");

    private static final NodeRef CYCLE_B_NODE_REF = new NodeRef("workspace://SpacesStore/cycleB");

    @Mock
    private EntityListDAO entityListDAO;

    @Mock
    private NodeService nodeService;

    @Mock
    private EntityDictionaryService entityDictionaryService;

    @Mock
    private SecurityService securityService;

    @Mock
    private NamespaceService namespaceService;

    @Mock
    private BeCPGCacheService beCPGCacheService;

    @InjectMocks
    private MultiLevelDataListServiceImpl multiLevelDataListService;

    private final Map<NodeRef, Boolean> expandedNodes = new HashMap<>();

    private AutoCloseable mocks;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(nodeService.getType(ENTITY_NODE_REF)).thenReturn(ENTITY_TYPE);
        when(entityListDAO.getListContainer(ENTITY_NODE_REF)).thenReturn(CONTAINER_NODE_REF);
        when(entityListDAO.getList(CONTAINER_NODE_REF, LIST_TYPE)).thenReturn(LIST_NODE_REF);
        when(entityDictionaryService.isSubClass(LIST_TYPE, BeCPGModel.TYPE_ENTITYLIST_ITEM)).thenReturn(true);
        when(securityService.computeAccessMode(eq(ENTITY_NODE_REF), eq(ENTITY_TYPE), anyString())).thenReturn(SecurityService.READ_ACCESS);
        when(namespaceService.getPrefixes(BeCPGModel.BECPG_URI)).thenReturn(Collections.singletonList(BeCPGModel.BECPG_PREFIX));
        when(beCPGCacheService.getFromCache(eq(MultiLevelDataListService.class.getName()), any(), any())).thenReturn(expandedNodes);
        mockRow(LEVEL1_NODE_REF, 1, null);
        mockRow(LEVEL2_NODE_REF, 2, LEVEL1_NODE_REF);
        mockRow(LEVEL3_NODE_REF, 3, LEVEL2_NODE_REF);
        mockRow(SIBLING_NODE_REF, 1, null);
        mockRow(CHILD_A_NODE_REF, 2, LEVEL1_NODE_REF);
        mockRow(CHILD_Z_NODE_REF, 2, LEVEL1_NODE_REF);
        mockRow(CYCLE_A_NODE_REF, 2, CYCLE_B_NODE_REF);
        mockRow(CYCLE_B_NODE_REF, 2, CYCLE_A_NODE_REF);
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void testExpandedRowsKeepTheirWholeHierarchy() {
        MultiLevelListData tree = buildTree(LEVEL1_NODE_REF, LEVEL2_NODE_REF, LEVEL3_NODE_REF, SIBLING_NODE_REF);

        assertEquals(List.of(LEVEL1_NODE_REF, SIBLING_NODE_REF), List.copyOf(tree.getTree().keySet()));
        MultiLevelListData level2 = tree.getTree().get(LEVEL1_NODE_REF).getTree().get(LEVEL2_NODE_REF);
        assertTrue(level2.getTree().containsKey(LEVEL3_NODE_REF));
    }

    @Test
    public void testCollapsedRowHidesAllItsDescendants() {
        expandedNodes.put(LEVEL1_NODE_REF, false);

        MultiLevelListData tree = buildTree(LEVEL1_NODE_REF, LEVEL2_NODE_REF, LEVEL3_NODE_REF, SIBLING_NODE_REF);

        assertEquals(List.of(LEVEL1_NODE_REF, SIBLING_NODE_REF), List.copyOf(tree.getTree().keySet()));
        assertTrue(tree.getTree().get(LEVEL1_NODE_REF).getTree().isEmpty());
    }

    @Test
    public void testCollapsedSubRowHidesOnlyItsDescendants() {
        expandedNodes.put(LEVEL2_NODE_REF, false);

        MultiLevelListData tree = buildTree(LEVEL1_NODE_REF, LEVEL2_NODE_REF, LEVEL3_NODE_REF, SIBLING_NODE_REF);

        MultiLevelListData level2 = tree.getTree().get(LEVEL1_NODE_REF).getTree().get(LEVEL2_NODE_REF);
        assertTrue(level2.getTree().isEmpty());
        assertEquals(List.of(LEVEL1_NODE_REF, SIBLING_NODE_REF), List.copyOf(tree.getTree().keySet()));
    }

    @Test
    public void testRowWhoseParentIsFilteredOutIsDisplayedAtRoot() {
        MultiLevelListData tree = buildTree(LEVEL2_NODE_REF, LEVEL3_NODE_REF);

        assertEquals(List.of(LEVEL2_NODE_REF), List.copyOf(tree.getTree().keySet()));
        assertTrue(tree.getTree().get(LEVEL2_NODE_REF).getTree().containsKey(LEVEL3_NODE_REF));
    }

    @Test
    public void testChildSortedBeforeItsParentStaysUnderIt() {
        MultiLevelListData tree = buildTree(CHILD_A_NODE_REF, LEVEL1_NODE_REF, CHILD_Z_NODE_REF);

        assertEquals(List.of(LEVEL1_NODE_REF), List.copyOf(tree.getTree().keySet()));
        assertEquals(List.of(CHILD_A_NODE_REF, CHILD_Z_NODE_REF), childrenOf(tree, LEVEL1_NODE_REF));
    }

    @Test
    public void testChildrenKeepTheSortOrderUnderTheirParent() {
        MultiLevelListData tree = buildTree(CHILD_Z_NODE_REF, LEVEL1_NODE_REF, CHILD_A_NODE_REF);

        assertEquals(List.of(CHILD_Z_NODE_REF, CHILD_A_NODE_REF), childrenOf(tree, LEVEL1_NODE_REF));
    }

    @Test
    public void testRootsKeepTheSortOrderAmongThemselves() {
        MultiLevelListData tree = buildTree(SIBLING_NODE_REF, CHILD_A_NODE_REF, LEVEL1_NODE_REF);

        assertEquals(List.of(SIBLING_NODE_REF, LEVEL1_NODE_REF), List.copyOf(tree.getTree().keySet()));
        assertEquals(List.of(CHILD_A_NODE_REF), childrenOf(tree, LEVEL1_NODE_REF));
    }

    @Test
    public void testGrandChildSortedBeforeItsAncestorsStaysAtItsLevel() {
        MultiLevelListData tree = buildTree(LEVEL3_NODE_REF, LEVEL2_NODE_REF, LEVEL1_NODE_REF);

        assertEquals(List.of(LEVEL1_NODE_REF), List.copyOf(tree.getTree().keySet()));
        MultiLevelListData level2 = tree.getTree().get(LEVEL1_NODE_REF).getTree().get(LEVEL2_NODE_REF);
        assertEquals(List.of(LEVEL3_NODE_REF), List.copyOf(level2.getTree().keySet()));
    }

    @Test
    public void testCollapsedRowHidesDescendantsSortedBeforeIt() {
        expandedNodes.put(LEVEL1_NODE_REF, false);

        MultiLevelListData tree = buildTree(LEVEL3_NODE_REF, LEVEL2_NODE_REF, LEVEL1_NODE_REF);

        assertEquals(List.of(LEVEL1_NODE_REF), List.copyOf(tree.getTree().keySet()));
        assertTrue(tree.getTree().get(LEVEL1_NODE_REF).getTree().isEmpty());
    }

    @Test
    public void testRowsWhoseParentsFormACycleAreDisplayedAtRoot() {
        MultiLevelListData tree = buildTree(CYCLE_A_NODE_REF, CYCLE_B_NODE_REF);

        assertEquals(List.of(CYCLE_A_NODE_REF, CYCLE_B_NODE_REF), List.copyOf(tree.getTree().keySet()));
    }

    private List<NodeRef> childrenOf(MultiLevelListData tree, NodeRef rootNodeRef) {
        return List.copyOf(tree.getTree().get(rootNodeRef).getTree().keySet());
    }

    private MultiLevelListData buildTree(NodeRef... rows) {
        DataListFilter dataListFilter = new DataListFilter();
        dataListFilter.setDataType(LIST_TYPE);
        dataListFilter.setEntityNodeRefs(Collections.singletonList(ENTITY_NODE_REF));
        dataListFilter.setParentNodeRef(LIST_NODE_REF);
        dataListFilter.updateMaxDepth(-1);
        when(entityListDAO.getListItems(LIST_NODE_REF, LIST_TYPE, dataListFilter.getSortMap())).thenReturn(List.of(rows));
        return multiLevelDataListService.getMultiLevelListData(dataListFilter, true, false);
    }

    private void mockRow(NodeRef rowNodeRef, int depthLevel, NodeRef parentNodeRef) {
        when(nodeService.getType(rowNodeRef)).thenReturn(LIST_TYPE);
        when(nodeService.getProperty(rowNodeRef, BeCPGModel.PROP_DEPTH_LEVEL)).thenReturn(depthLevel);
        when(nodeService.getProperty(rowNodeRef, BeCPGModel.PROP_PARENT_LEVEL)).thenReturn(parentNodeRef);
    }
}
