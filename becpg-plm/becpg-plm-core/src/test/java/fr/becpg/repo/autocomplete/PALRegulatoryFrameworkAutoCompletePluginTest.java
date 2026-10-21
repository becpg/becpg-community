package fr.becpg.repo.autocomplete;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.repo.product.formulation.allergen.PALDatabaseService;

/**
 * Unit tests for {@link PALRegulatoryFrameworkAutoCompletePlugin}: the frameworks offered on the
 * products are the published PAL grids, the dropdown toggle listing all of them.
 */
public class PALRegulatoryFrameworkAutoCompletePluginTest {

    private static final String SOURCE_TYPE = "palRegulatoryFrameworks";

    private static final String WILDCARD_QUERY = "*";

    private static final String NL_ED05 = "NL_ED05";

    private static final String VITAL_3 = "VITAL_3";

    private static final String VITAL_4 = "VITAL_4";

    private static final Integer FIRST_PAGE = 1;

    private static final Integer PAGE_SIZE = 10;

    @Mock
    private PALDatabaseService palDatabaseService;

    @InjectMocks
    private PALRegulatoryFrameworkAutoCompletePlugin plugin;

    private AutoCloseable mocks;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        List<FileInfo> grids = List.of(grid(NL_ED05 + ".csv"), grid(VITAL_3 + ".csv"), grid(VITAL_4 + ".csv"));
        when(palDatabaseService.getPALDatabases()).thenReturn(grids);
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void wildcardQueryListsEveryFramework() {
        assertEquals(List.of(NL_ED05, VITAL_3, VITAL_4), suggestedCodes(WILDCARD_QUERY));
    }

    @Test
    public void typedQueryFiltersFrameworksIgnoringCase() {
        assertEquals(List.of(VITAL_3, VITAL_4), suggestedCodes("vital"));
    }

    @Test
    public void emptyQueryListsEveryFramework() {
        assertEquals(List.of(NL_ED05, VITAL_3, VITAL_4), suggestedCodes(""));
    }

    private List<String> suggestedCodes(String query) {
        List<String> codes = new ArrayList<>();
        for (AutoCompleteEntry entry : plugin.suggest(SOURCE_TYPE, query, FIRST_PAGE, PAGE_SIZE, null).getResults()) {
            codes.add(entry.getValue());
        }
        return codes;
    }

    private static FileInfo grid(String fileName) {
        FileInfo fileInfo = mock(FileInfo.class);
        when(fileInfo.getName()).thenReturn(fileName);
        return fileInfo;
    }

}
