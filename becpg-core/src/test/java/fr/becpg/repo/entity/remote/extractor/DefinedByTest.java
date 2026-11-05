package fr.becpg.repo.entity.remote.extractor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;

/**
 * Unit tests for {@link DefinedBy}.
 */
public class DefinedByTest {

    private static final QName ASPECT_GEO_ORIGIN = QName.createQName(BeCPGModel.BECPG_URI, "productGeoOriginAspect");

    private static final QName TYPE_ING_LIST = QName.createQName(BeCPGModel.BECPG_URI, "ingList");

    private static final String GEO_ORIGIN_ASPECT_NAME = "bcpg:productGeoOriginAspect";

    private static final String ING_LIST_NAME = "bcpg:ingList";

    private static final String KIND = "kind";

    private static final String NAME = "name";

    private final NamespaceService namespaceService = mockNamespaceService();

    @Test
    public void describesAnAspectByItsKindAndPrefixedName() {
        JSONObject definedBy = DefinedBy.of(classDefinition(ASPECT_GEO_ORIGIN, true), namespaceService).toJson();

        assertEquals(DefinedBy.KIND_ASPECT, definedBy.optString(KIND, null));
        assertEquals(GEO_ORIGIN_ASPECT_NAME, definedBy.optString(NAME, null));
    }

    @Test
    public void describesATypeByItsKindAndPrefixedName() {
        JSONObject definedBy = DefinedBy.of(classDefinition(TYPE_ING_LIST, false), namespaceService).toJson();

        assertEquals(DefinedBy.KIND_TYPE, definedBy.optString(KIND, null));
        assertEquals(ING_LIST_NAME, definedBy.optString(NAME, null));
    }

    private static ClassDefinition classDefinition(QName name, boolean aspect) {
        ClassDefinition classDefinition = mock(ClassDefinition.class);
        when(classDefinition.getName()).thenReturn(name);
        when(classDefinition.isAspect()).thenReturn(aspect);
        return classDefinition;
    }

    private static NamespaceService mockNamespaceService() {
        NamespaceService namespaceService = mock(NamespaceService.class);
        when(namespaceService.getPrefixes(BeCPGModel.BECPG_URI)).thenReturn(List.of(BeCPGModel.BECPG_PREFIX));
        return namespaceService;
    }
}
