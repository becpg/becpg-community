package fr.becpg.repo.entity.remote.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.helper.MLTextHelper;

/**
 * Unit tests for the <code>definedBy</code> keyword of {@link JsonSchemaEntityVisitor}: every field
 * of the schema names the type or the aspect that declares it.
 */
public class JsonSchemaEntityVisitorTest {

    private static final QName TYPE_RAW_MATERIAL = bcpg("rawMaterial");

    private static final QName TYPE_PRODUCT = bcpg("product");

    private static final QName TYPE_ING_LIST = bcpg("ingList");

    private static final QName TYPE_CLIENT = bcpg("client");

    private static final QName ASPECT_GEO_ORIGIN = bcpg("productGeoOriginAspect");

    private static final QName PROP_GEO_ORIGIN = bcpg("productGeoOrigin");

    private static final QName PROP_ING_LIST_GEO_ORIGIN = bcpg("ingListGeoOrigin");

    private static final QName PROP_LEGAL_NAME = bcpg("legalName");

    private static final QName PROP_LABELS = bcpg("productLabels");

    private static final QName PROP_REFERENCE = bcpg("productReference");

    private static final QName ASSOC_CLIENTS = bcpg("clients");

    private static final String GEO_ORIGIN_ASPECT_NAME = "bcpg:productGeoOriginAspect";

    private static final String ING_LIST_NAME = "bcpg:ingList";

    private static final String PRODUCT_NAME = "bcpg:product";

    private static final String PROPERTIES = "properties";

    private static final String DEFINED_BY = "definedBy";

    private static final String KIND = "kind";

    private static final String NAME = "name";

    private final NodeRef product = new NodeRef("workspace://SpacesStore/product");

    private final NodeRef target = new NodeRef("workspace://SpacesStore/target");

    private final NodeService nodeService = mock(NodeService.class);

    private final EntityDictionaryService entityDictionaryService = mock(EntityDictionaryService.class);

    private final NamespaceService namespaceService = mock(NamespaceService.class);

    private final RemoteParams params = new RemoteParams(RemoteEntityFormat.json_schema);

    private JsonSchemaEntityVisitor visitor;

    private Locale defaultLocale;

    @Before
    public void setUp() {
        // The schema locale is the nearest supported one to the JVM default, which a French build machine would not find
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        MLTextHelper.flushCache();
        MLTextHelper.setSupportedLocales("en");
        when(namespaceService.getPrefixes(BeCPGModel.BECPG_URI)).thenReturn(List.of(BeCPGModel.BECPG_PREFIX));
        when(namespaceService.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI)).thenReturn(List.of(NamespaceService.CONTENT_MODEL_PREFIX));
        when(namespaceService.getPrefixes(NamespaceService.DICTIONARY_MODEL_1_0_URI)).thenReturn(List.of(NamespaceService.DICTIONARY_MODEL_PREFIX));
        when(entityDictionaryService.toPrefixString(any(QName.class))).thenAnswer(invocation -> prefixed(invocation.getArgument(0)));
        when(nodeService.getType(product)).thenReturn(TYPE_RAW_MATERIAL);
        when(nodeService.getType(target)).thenReturn(TYPE_CLIENT);

        RemoteServiceRegisty registry = mock(RemoteServiceRegisty.class);
        when(registry.nodeService()).thenReturn(nodeService);
        when(registry.unsecuredNodeService()).thenReturn(nodeService);
        when(registry.mlNodeService()).thenReturn(nodeService);
        when(registry.namespaceService()).thenReturn(namespaceService);
        when(registry.entityDictionaryService()).thenReturn(entityDictionaryService);

        visitor = new JsonSchemaEntityVisitor(registry);
        visitor.setParams(params);
    }

    @After
    public void tearDown() {
        MLTextHelper.flushCache();
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void typeSchemaNamesTheAspectDeclaringARequestedField() throws Exception {
        mockProperty(PROP_GEO_ORIGIN, classDefinition(ASPECT_GEO_ORIGIN, true), false);

        JSONObject field = typeSchemaAttribute(PROP_GEO_ORIGIN);

        assertDefinedBy(field, DefinedBy.KIND_ASPECT, GEO_ORIGIN_ASPECT_NAME);
    }

    @Test
    public void typeSchemaNamesTheUnrelatedTypeDeclaringARequestedField() throws Exception {
        mockProperty(PROP_ING_LIST_GEO_ORIGIN, classDefinition(TYPE_ING_LIST, false), false);

        JSONObject field = typeSchemaAttribute(PROP_ING_LIST_GEO_ORIGIN);

        assertDefinedBy(field, DefinedBy.KIND_TYPE, ING_LIST_NAME);
    }

    @Test
    public void nodeSchemaNamesTheClassDeclaringASingleValuedProperty() {
        mockProperty(PROP_LEGAL_NAME, classDefinition(TYPE_PRODUCT, false), false);

        JSONObject attributes = nodeSchemaAttributes(PROP_LEGAL_NAME, "Legal name");

        assertDefinedBy(field(attributes, PROP_LEGAL_NAME), DefinedBy.KIND_TYPE, PRODUCT_NAME);
    }

    @Test
    public void nodeSchemaNamesTheClassDeclaringAMultiValuedProperty() {
        mockProperty(PROP_LABELS, classDefinition(ASPECT_GEO_ORIGIN, true), true);

        JSONObject attributes = nodeSchemaAttributes(PROP_LABELS, new ArrayList<>(List.of("Organic", "Fair trade")));

        assertDefinedBy(field(attributes, PROP_LABELS), DefinedBy.KIND_ASPECT, GEO_ORIGIN_ASPECT_NAME);
    }

    @Test
    public void nodeSchemaNamesTheClassDeclaringAJsonValuedProperty() {
        mockProperty(BeCPGModel.PROP_ENTITY_SCORE, classDefinition(TYPE_PRODUCT, false), false);

        JSONObject attributes = nodeSchemaAttributes(BeCPGModel.PROP_ENTITY_SCORE, "{\"score\":1}");

        assertDefinedBy(field(attributes, BeCPGModel.PROP_ENTITY_SCORE), DefinedBy.KIND_TYPE, PRODUCT_NAME);
    }

    @Test
    public void nodeSchemaNamesTheClassDeclaringANodeRefProperty() {
        mockProperty(PROP_REFERENCE, classDefinition(TYPE_PRODUCT, false), false);

        JSONObject attributes = nodeSchemaAttributes(PROP_REFERENCE, target);

        assertDefinedBy(field(attributes, PROP_REFERENCE), DefinedBy.KIND_TYPE, PRODUCT_NAME);
    }

    @Test
    public void nodeSchemaNamesTheSourceClassOfAnAssociation() throws Exception {
        mockAssociation(ASSOC_CLIENTS, classDefinition(TYPE_PRODUCT, false));

        JSONObject attributes = new JSONObject();
        visitor.visitAssocs(product, attributes, null, new RemoteJSONContext(product));

        assertDefinedBy(field(attributes, ASSOC_CLIENTS), DefinedBy.KIND_TYPE, PRODUCT_NAME);
    }

    private JSONObject typeSchemaAttribute(QName propName) throws Exception {
        TypeDefinition typeDefinition = mock(TypeDefinition.class);
        when(entityDictionaryService.getType(TYPE_RAW_MATERIAL)).thenReturn(typeDefinition);
        when(entityDictionaryService.getClass(TYPE_RAW_MATERIAL)).thenReturn(typeDefinition);
        params.setFilteredProperties(Set.of(propName));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        visitor.visit(TYPE_RAW_MATERIAL, out);

        JSONObject entity = new JSONObject(out.toString(StandardCharsets.UTF_8)).getJSONObject(PROPERTIES)
                .getJSONObject(RemoteEntityService.ELEM_ENTITY);
        JSONObject attributes = entity.getJSONObject(PROPERTIES).getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES);
        return field(attributes, propName);
    }

    private JSONObject nodeSchemaAttributes(QName propName, Serializable value) {
        JSONObject attributes = new JSONObject();
        visitor.visitProps(product, attributes, null, Map.of(propName, value), new RemoteJSONContext(product));
        return attributes;
    }

    private static JSONObject field(JSONObject parent, QName fieldName) {
        JSONObject field = parent.getJSONObject(PROPERTIES).optJSONObject(prefixed(fieldName));
        assertNotNull("No schema for " + prefixed(fieldName), field);
        return field;
    }

    private static void assertDefinedBy(JSONObject field, String kind, String name) {
        JSONObject definedBy = field.optJSONObject(DEFINED_BY);
        assertNotNull("No definedBy on " + field, definedBy);
        assertEquals(kind, definedBy.optString(KIND, null));
        assertEquals(name, definedBy.optString(NAME, null));
    }

    private void mockProperty(QName propName, ClassDefinition declaringClass, boolean multiValued) {
        PropertyDefinition definition = mock(PropertyDefinition.class);
        DataTypeDefinition dataType = mock(DataTypeDefinition.class);
        when(dataType.getName()).thenReturn(DataTypeDefinition.TEXT);
        when(definition.getName()).thenReturn(propName);
        when(definition.getDataType()).thenReturn(dataType);
        when(definition.isMultiValued()).thenReturn(multiValued);
        when(definition.getContainerClass()).thenReturn(declaringClass);
        when(entityDictionaryService.getProperty(propName)).thenReturn(definition);
        when(entityDictionaryService.getPropDef(propName)).thenReturn(definition);
    }

    private void mockAssociation(QName assocName, ClassDefinition sourceClass) {
        AssociationDefinition definition = mock(AssociationDefinition.class);
        when(definition.getName()).thenReturn(assocName);
        when(definition.getSourceClass()).thenReturn(sourceClass);
        TypeDefinition typeDefinition = mock(TypeDefinition.class);
        when(typeDefinition.getAssociations()).thenReturn(Map.of(assocName, definition));
        when(entityDictionaryService.getType(TYPE_RAW_MATERIAL)).thenReturn(typeDefinition);
        when(nodeService.getTargetAssocs(product, assocName)).thenReturn(List.of(new AssociationRef(product, assocName, target)));
    }

    private static ClassDefinition classDefinition(QName name, boolean aspect) {
        ClassDefinition classDefinition = mock(ClassDefinition.class);
        when(classDefinition.getName()).thenReturn(name);
        when(classDefinition.isAspect()).thenReturn(aspect);
        return classDefinition;
    }

    private static QName bcpg(String localName) {
        return QName.createQName(BeCPGModel.BECPG_URI, localName);
    }

    private static String prefixed(QName qName) {
        return BeCPGModel.BECPG_PREFIX + ":" + qName.getLocalName();
    }
}
