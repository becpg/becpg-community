package fr.becpg.repo.formulation.spel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.spel.BeCPGSpelFunctions.BeCPGSpelFunctionsWrapper;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;

/**
 * Checks that {@code @beCPG.propValue($entity, $qname)} never keeps the value it reads in the
 * entity extra properties, which are saved with the entity: a value read under the locale of a
 * labeling render rule used to overwrite the stored value of a multilingual field (#37294).
 *
 * @author matthieu
 */
public class BeCPGSpelFunctionsTest {

    private static final String PREFIX = "cm";

    private static final String NAMESPACE_URI = "http://www.alfresco.org/model/content/1.0";

    private static final String PROP_NAME = "cm:description";

    private static final QName PROP_QNAME = QName.createQName(NAMESPACE_URI, "description");

    private static final NodeRef ENTITY_NODE_REF = new NodeRef("workspace://SpacesStore/entity");

    private static final String FRENCH_VALUE = "Valeur";

    private static final String ENGLISH_VALUE = "Value";

    @Mock
    private NodeService nodeService;

    @Mock
    private NamespaceService namespaceService;

    @Mock
    private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;

    @InjectMocks
    private BeCPGSpelFunctions beCPGSpelFunctions;

    private RepositoryEntity entity;

    private Map<QName, Serializable> extraProperties;

    private BeCPGSpelFunctionsWrapper wrapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(namespaceService.getNamespaceURI(PREFIX)).thenReturn(NAMESPACE_URI);
        when(namespaceService.getPrefixes(NAMESPACE_URI)).thenReturn(List.of(PREFIX));
        when(nodeService.exists(ENTITY_NODE_REF)).thenReturn(true);

        extraProperties = new HashMap<>();
        entity = mock(RepositoryEntity.class);
        when(entity.getNodeRef()).thenReturn(ENTITY_NODE_REF);
        when(entity.getExtraProperties()).thenReturn(extraProperties);

        wrapper = (BeCPGSpelFunctionsWrapper) beCPGSpelFunctions.create(entity);
    }

    @Test
    public void propValueReadsTheNodeWithoutKeepingTheValue() {
        when(nodeService.getProperty(ENTITY_NODE_REF, PROP_QNAME)).thenReturn(ENGLISH_VALUE);

        assertEquals(ENGLISH_VALUE, wrapper.propValue(entity, PROP_NAME));
        assertTrue(extraProperties.isEmpty());
    }

    @Test
    public void propValueReadsTheValueOfTheCurrentLocaleOnEachCall() {
        when(nodeService.getProperty(ENTITY_NODE_REF, PROP_QNAME)).thenReturn(ENGLISH_VALUE, FRENCH_VALUE);

        assertEquals(ENGLISH_VALUE, wrapper.propValue(entity, PROP_NAME));
        assertEquals(FRENCH_VALUE, wrapper.propValue(entity, PROP_NAME));
    }

    @Test
    public void propValueReturnsTheValueSetByTheFormula() {
        wrapper.setValue(entity, PROP_NAME, FRENCH_VALUE);

        assertEquals(FRENCH_VALUE, wrapper.propValue(entity, PROP_NAME));
        verify(nodeService, never()).getProperty(ENTITY_NODE_REF, PROP_QNAME);
    }

    @Test
    public void propValueOnTheCurrentEntityDoesNotKeepTheValue() {
        when(nodeService.getProperty(ENTITY_NODE_REF, PROP_QNAME)).thenReturn(ENGLISH_VALUE);

        assertEquals(ENGLISH_VALUE, wrapper.propValue(PROP_NAME));
        assertTrue(extraProperties.isEmpty());
    }

    @Test
    public void propValueRejectsAMappedProperty() {
        when(repositoryEntityDefReader.isRegisteredQName(any(), any(), anyBoolean())).thenReturn(true);

        assertThrows(FormulateException.class, () -> wrapper.propValue(entity, PROP_NAME));
    }

    @Test
    public void propValueOnANullEntityReturnsNull() {
        assertNull(wrapper.propValue((RepositoryEntity) null, PROP_NAME));
    }
}
