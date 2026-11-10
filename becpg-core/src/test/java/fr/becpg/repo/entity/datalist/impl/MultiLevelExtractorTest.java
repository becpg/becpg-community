package fr.becpg.repo.entity.datalist.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.search.PaginatedSearchCache;

/**
 * Unit tests for the depth preference handling of {@link MultiLevelExtractor}.
 */
public class MultiLevelExtractorTest {

    private static final String USER_NAME = "export.user";
    private static final String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";
    private static final QName ING_LIST_TYPE = QName.createQName(BECPG_URI, "ingList");
    private static final NodeRef ENTITY_NODE_REF = new NodeRef("workspace://SpacesStore/entity");

    private PreferenceService preferenceService;
    private MultiLevelExtractor extractor;

    @Before
    public void setUp() {
        preferenceService = mock(PreferenceService.class);
        when(preferenceService.getPreferences(anyString())).thenReturn(new HashMap<String, Serializable>());

        PaginatedSearchCache paginatedSearchCache = mock(PaginatedSearchCache.class);
        when(paginatedSearchCache.getSearchMultiLevelResults(any())).thenReturn(new MultiLevelListData(ENTITY_NODE_REF, 0));

        extractor = new MultiLevelExtractor();
        extractor.setPreferenceService(preferenceService);
        extractor.setPaginatedSearchCache(paginatedSearchCache);
    }

    @Test
    public void readOnlyExportPageDoesNotSaveDepthPreference() {
        extractInTransaction(TxnReadState.TXN_READ_ONLY);

        verify(preferenceService, never()).setPreferences(anyString(), anyMap());
    }

    @Test
    public void readWriteExtractionSavesDepthPreference() {
        extractInTransaction(TxnReadState.TXN_READ_WRITE);

        verify(preferenceService).setPreferences(eq(USER_NAME), anyMap());
    }

    private void extractInTransaction(TxnReadState readState) {
        try (MockedStatic<AlfrescoTransactionSupport> transactionSupport = mockStatic(AlfrescoTransactionSupport.class);
                MockedStatic<AuthenticationUtil> authenticationUtil = mockStatic(AuthenticationUtil.class)) {
            transactionSupport.when(AlfrescoTransactionSupport::getTransactionReadState).thenReturn(readState);
            authenticationUtil.when(AuthenticationUtil::getFullyAuthenticatedUser).thenReturn(USER_NAME);
            extractor.extract(createFilterWithDepth(), Collections.emptyList());
        }
    }

    private DataListFilter createFilterWithDepth() {
        DataListFilter dataListFilter = new DataListFilter();
        dataListFilter.setDataType(ING_LIST_TYPE);
        dataListFilter.setEntityNodeRefs(Collections.singletonList(ENTITY_NODE_REF));
        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put(DataListFilter.PROP_DEPTH_LEVEL, "0");
        dataListFilter.setCriteriaMap(criteriaMap);
        return dataListFilter;
    }
}
