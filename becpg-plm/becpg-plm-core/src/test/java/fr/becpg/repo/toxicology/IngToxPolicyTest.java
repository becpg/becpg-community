package fr.becpg.repo.toxicology;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.transaction.TransactionListener;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.model.ToxType;

/**
 * Unit tests for {@link IngToxPolicy}: only the properties read by the toxicology computation
 * trigger the recalculation of the ingredients when a tox is created or updated.
 */
public class IngToxPolicyTest {

    private static final String LATE_LISTENERS_RESOURCE_KEY = "AlfrescoTransactionSupport.preListeners";

    private static final String TOX_NAME = "Tox A";

    private static final String RENAMED_TOX_NAME = "Tox B";

    private static final String ORAL_ABSORPTION = "Oral";

    private static final String ERP_CODE = "ERP-A";

    private static final String OTHER_ERP_CODE = "ERP-B";

    private static final String MODIFIER = "admin";

    private static final String OTHER_MODIFIER = "jdoe";

    private static final String DESCRIPTION = "Leave-on body lotion";

    private static final String OTHER_DESCRIPTION = "Leave-on body lotion for adults";

    private static final String READ_GROUP = "GROUP_A";

    private static final String OTHER_READ_GROUP = "GROUP_B";

    private static final Double TOX_VALUE = 110d;

    private static final Double OTHER_TOX_VALUE = 220d;

    private static final Double BODY_WEIGHT = 60d;

    private static final Double OTHER_BODY_WEIGHT = 30d;

    private NodeRef toxNodeRef;

    private NodeRef ingNodeRef;

    private NodeService nodeService;

    private ToxicologyService toxicologyService;

    private IngToxPolicy policy;

    @BeforeClass
    public static void initAuthenticationUtil() throws Exception {
        new AuthenticationUtil().afterPropertiesSet();
    }

    @Before
    public void setUp() {
        TransactionSynchronizationManager.initSynchronization();
        // Alfresco keeps the transaction resources in its own thread local, so each test uses its own nodes
        toxNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        ingNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        nodeService = mock(NodeService.class);
        toxicologyService = mock(ToxicologyService.class);
        when(nodeService.getType(toxNodeRef)).thenReturn(PLMModel.TYPE_TOX);
        when(nodeService.getType(ingNodeRef)).thenReturn(PLMModel.TYPE_ING);
        when(nodeService.exists(any(NodeRef.class))).thenReturn(true);
        policy = new IngToxPolicy();
        policy.setNodeService(nodeService);
        policy.setToxicologyService(toxicologyService);
    }

    @After
    public void tearDown() {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    public void shouldRecalculateWhenToxValueChanges() {
        Map<QName, Serializable> after = impactingProperties();
        after.put(PLMModel.PROP_TOX_VALUE, OTHER_TOX_VALUE);

        updateToxAndCommit(impactingProperties(), after);

        verify(toxicologyService, times(1)).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenCalculateSystemicChanges() {
        Map<QName, Serializable> after = impactingProperties();
        after.put(PLMModel.PROP_TOX_CALCULATE_SYSTEMIC, Boolean.FALSE);

        updateToxAndCommit(impactingProperties(), after);

        verify(toxicologyService).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenCalculateMaxChanges() {
        Map<QName, Serializable> after = impactingProperties();
        after.put(PLMModel.PROP_TOX_CALCULATE_MAX, Boolean.FALSE);

        updateToxAndCommit(impactingProperties(), after);

        verify(toxicologyService).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenToxTypesChange() {
        Map<QName, Serializable> after = impactingProperties();
        after.put(PLMModel.PROP_TOX_TYPES, toxTypes(ToxType.SkinIrritationRinseOff, ToxType.Sensitization));

        updateToxAndCommit(impactingProperties(), after);

        verify(toxicologyService).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenAbsorptionTypeChanges() {
        Map<QName, Serializable> after = impactingProperties();
        after.put(PLMModel.PROP_TOX_ABSORPTION_TYPE, ORAL_ABSORPTION);

        updateToxAndCommit(impactingProperties(), after);

        verify(toxicologyService).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenBodyWeightChanges() {
        Map<QName, Serializable> after = impactingProperties();
        after.put(PLMModel.PROP_TOX_BODY_WEIGHT, OTHER_BODY_WEIGHT);

        updateToxAndCommit(impactingProperties(), after);

        verify(toxicologyService).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenToxIsMarkedDeleted() {
        Map<QName, Serializable> after = impactingProperties();
        after.put(BeCPGModel.PROP_IS_DELETED, Boolean.TRUE);

        updateToxAndCommit(impactingProperties(), after);

        verify(toxicologyService).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenToxIsRenamed() {
        Map<QName, Serializable> after = impactingProperties();
        after.put(BeCPGModel.PROP_CHARACT_NAME, new MLText(Locale.getDefault(), RENAMED_TOX_NAME));

        updateToxAndCommit(impactingProperties(), after);

        verify(toxicologyService).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenToxIsCreated() {
        Map<QName, Serializable> after = new HashMap<>();
        after.put(BeCPGModel.PROP_CHARACT_NAME, new MLText(Locale.getDefault(), TOX_NAME));
        after.put(BeCPGModel.PROP_IS_DELETED, Boolean.FALSE);
        after.put(PLMModel.PROP_TOX_BODY_WEIGHT, BODY_WEIGHT);
        after.put(PLMModel.PROP_TOX_VALUE, TOX_VALUE);

        updateToxAndCommit(new HashMap<>(), after);

        verify(toxicologyService).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenImpactingValueIsRemoved() {
        Map<QName, Serializable> after = impactingProperties();
        after.remove(PLMModel.PROP_TOX_VALUE);

        updateToxAndCommit(impactingProperties(), after);

        verify(toxicologyService).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldRecalculateWhenInertAndImpactingPropertiesChangeTogether() {
        Map<QName, Serializable> before = impactingProperties();
        before.put(BeCPGModel.PROP_ERP_CODE, ERP_CODE);
        before.put(ContentModel.PROP_MODIFIED, new Date(0L));
        Map<QName, Serializable> after = impactingProperties();
        after.put(BeCPGModel.PROP_ERP_CODE, OTHER_ERP_CODE);
        after.put(ContentModel.PROP_MODIFIED, new Date(1L));
        after.put(PLMModel.PROP_TOX_VALUE, OTHER_TOX_VALUE);

        updateToxAndCommit(before, after);

        verify(toxicologyService, times(1)).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldNotRecalculateWhenOnlyErpCodeChanges() {
        Map<QName, Serializable> before = impactingProperties();
        before.put(BeCPGModel.PROP_ERP_CODE, ERP_CODE);
        Map<QName, Serializable> after = impactingProperties();
        after.put(BeCPGModel.PROP_ERP_CODE, OTHER_ERP_CODE);

        updateToxAndCommit(before, after);

        verify(toxicologyService, never()).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldNotRecalculateWhenOnlyAuditPropertiesChange() {
        Map<QName, Serializable> before = impactingProperties();
        before.put(ContentModel.PROP_MODIFIED, new Date(0L));
        before.put(ContentModel.PROP_MODIFIER, MODIFIER);
        before.put(ContentModel.PROP_DESCRIPTION, DESCRIPTION);
        before.put(SecurityModel.ASSOC_READ_GROUPS, READ_GROUP);
        Map<QName, Serializable> after = impactingProperties();
        after.put(ContentModel.PROP_MODIFIED, new Date(1L));
        after.put(ContentModel.PROP_MODIFIER, OTHER_MODIFIER);
        after.put(ContentModel.PROP_DESCRIPTION, OTHER_DESCRIPTION);
        after.put(SecurityModel.ASSOC_READ_GROUPS, OTHER_READ_GROUP);

        updateToxAndCommit(before, after);

        verify(toxicologyService, never()).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldNotRecalculateWhenImpactingValuesAreSavedUnchanged() {
        updateToxAndCommit(impactingProperties(), impactingProperties());

        verify(toxicologyService, never()).updateIngredientsFromTox(toxNodeRef);
    }

    @Test
    public void shouldStillRecalculateIngredientWhenToxDataFlagIsSet() {
        Map<QName, Serializable> after = new HashMap<>();
        after.put(PLMModel.PROP_ING_TOX_DATA, Boolean.TRUE);

        policy.onUpdateProperties(ingNodeRef, new HashMap<>(), after);
        commitTransaction();

        verify(toxicologyService).updateIngredient(ingNodeRef);
    }

    private void updateToxAndCommit(Map<QName, Serializable> before, Map<QName, Serializable> after) {
        policy.onUpdateProperties(toxNodeRef, before, after);
        commitTransaction();
    }

    /**
     * Runs the before commit callback of the late listeners bound through BeCPGTransactionUtil.
     * The patched TransactionSupportUtil that runs them on a real commit is not on the classpath of
     * this module, so the test runs them the way the patch does. No listener is bound when the
     * policy queued nothing.
     */
    private void commitTransaction() {
        Set<TransactionListener> lateListeners = TransactionSupportUtil.getResource(LATE_LISTENERS_RESOURCE_KEY);
        if (lateListeners == null) {
            return;
        }
        for (TransactionListener listener : new ArrayList<>(lateListeners)) {
            listener.beforeCommit(false);
        }
    }

    /**
     * Builds the properties of a fully filled tox, all of them read by the toxicology computation.
     * Each call returns a new map holding equal values, as a repository save would.
     */
    private Map<QName, Serializable> impactingProperties() {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(BeCPGModel.PROP_CHARACT_NAME, new MLText(Locale.getDefault(), TOX_NAME));
        properties.put(BeCPGModel.PROP_IS_DELETED, Boolean.FALSE);
        properties.put(PLMModel.PROP_TOX_VALUE, TOX_VALUE);
        properties.put(PLMModel.PROP_TOX_CALCULATE_SYSTEMIC, Boolean.TRUE);
        properties.put(PLMModel.PROP_TOX_CALCULATE_MAX, Boolean.TRUE);
        properties.put(PLMModel.PROP_TOX_TYPES, toxTypes(ToxType.SkinIrritationRinseOff));
        properties.put(PLMModel.PROP_TOX_BODY_WEIGHT, BODY_WEIGHT);
        return properties;
    }

    private ArrayList<String> toxTypes(ToxType... types) {
        ArrayList<String> values = new ArrayList<>();
        for (ToxType type : List.of(types)) {
            values.add(type.toString());
        }
        return values;
    }
}
