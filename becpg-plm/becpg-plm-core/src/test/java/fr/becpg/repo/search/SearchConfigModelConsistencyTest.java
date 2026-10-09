package fr.becpg.repo.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Checks that every data list criterion of the advanced search configuration refers to a type,
 * property or association that the content models really declare.
 *
 * A renamed model element leaves a stale name in <code>search.json</code> without any error: the
 * criterion is silently ignored or matches nothing, and the advanced search returns no product.
 *
 * @author matthieu
 */
public class SearchConfigModelConsistencyTest {

    private static final String SEARCH_CONFIG = "/beCPG/search/search.json";

    private static final String CLASSPATH_MODELS = "classpath*:alfresco/module/**/model/*.xml";

    private static final String BECPG_MODEL = "src/main/assembly/config/alfresco/module/becpg-core/model/becpgModel.xml";

    private static final String DATA_LIST_SEARCH_FILTERS = "dataListSearchFilters";

    private static final String ATTRIBUTE = "attribute";

    private static final String LIST_TYPE = "listType";

    private static final String NAME = "name";

    private static final String[] CLASS_ELEMENTS = { "type", "aspect" };

    private static final String[] ATTRIBUTE_ELEMENTS = { "property", "association", "child-association" };

    private static final Set<String> declaredClasses = new HashSet<>();

    private static final Set<String> declaredAttributes = new HashSet<>();

    private static final List<JSONObject> criteria = new ArrayList<>();

    @BeforeClass
    public static void loadModelsAndSearchConfig() throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try (InputStream becpgModel = Files.newInputStream(Path.of(BECPG_MODEL))) {
            collectDeclaredNames(builder.parse(becpgModel));
        }
        for (Resource model : new PathMatchingResourcePatternResolver().getResources(CLASSPATH_MODELS)) {
            try (InputStream in = model.getInputStream()) {
                collectDeclaredNames(builder.parse(in));
            }
        }
        loadCriteria();
    }

    @Test
    public void testEveryCriterionAttributeIsDeclared() {
        for (JSONObject criterion : criteria) {
            String attribute = criterion.getString(ATTRIBUTE);
            assertTrue("search.json refers to an unknown property or association: " + attribute, declaredAttributes.contains(attribute));
        }
    }

    @Test
    public void testEveryCriterionListTypeIsDeclared() {
        for (JSONObject criterion : criteria) {
            if (criterion.has(LIST_TYPE)) {
                String listType = criterion.getString(LIST_TYPE);
                assertTrue("search.json refers to an unknown list type: " + listType, declaredClasses.contains(listType));
            }
        }
    }

    private static void collectDeclaredNames(Document model) {
        collectNames(model, CLASS_ELEMENTS, declaredClasses);
        collectNames(model, ATTRIBUTE_ELEMENTS, declaredAttributes);
    }

    private static void collectNames(Document model, String[] tagNames, Set<String> names) {
        for (String tagName : tagNames) {
            NodeList elements = model.getElementsByTagName(tagName);
            for (int i = 0; i < elements.getLength(); i++) {
                String name = ((Element) elements.item(i)).getAttribute(NAME);
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }
    }

    private static void loadCriteria() throws IOException {
        try (InputStream in = SearchConfigModelConsistencyTest.class.getResourceAsStream(SEARCH_CONFIG)) {
            JSONObject filters = new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getJSONObject(DATA_LIST_SEARCH_FILTERS);
            for (String filterName : filters.keySet()) {
                JSONArray filterCriteria = filters.getJSONArray(filterName);
                for (int i = 0; i < filterCriteria.length(); i++) {
                    criteria.add(filterCriteria.getJSONObject(i));
                }
            }
        }
    }
}
