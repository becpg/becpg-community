package fr.becpg.repo.product.formulation.labeling;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;

/**
 * Unit tests for {@link LabelingFormulaContext#renderPALAllergens()}: the precautionary labelling
 * is the involuntary allergen statement, rendered only when a PAL framework is selected.
 */
public class LabelingFormulaContextTest {

    private static final String SESAME = "Sesame";

    private static final String VITAL_3 = "VITAL_3";

    private final NodeRef sesameNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());

    private final FinishedProductData product = new FinishedProductData();

    private LabelingFormulaContext labelingFormulaContext;

    private Locale previousLocale;

    @Before
    public void setUp() {
        previousLocale = I18NUtil.getLocale();
        I18NUtil.setLocale(Locale.ENGLISH);

        NodeService mlNodeService = mock(NodeService.class);
        when(mlNodeService.getProperty(sesameNodeRef, BeCPGModel.PROP_LEGAL_NAME)).thenReturn(new MLText(Locale.ENGLISH, SESAME));

        labelingFormulaContext = new LabelingFormulaContext(mlNodeService, mock(AssociationService.class), null, null, null, null);
        labelingFormulaContext.setEntity(product);
        labelingFormulaContext.addInVolAllergens(List.of(sesameNodeRef));
    }

    @After
    public void tearDown() {
        I18NUtil.setLocale(previousLocale);
    }

    @Test
    public void rendersInvoluntaryAllergensWhenFrameworkSelected() {
        product.setAllergenRegulatoryFramework(VITAL_3);

        assertEquals(SESAME, labelingFormulaContext.renderPALAllergens());
    }

    @Test
    public void rendersNothingWithoutFramework() {
        assertEquals("", labelingFormulaContext.renderPALAllergens());
    }

    @Test
    public void rendersNothingWithBlankFramework() {
        product.setAllergenRegulatoryFramework(" ");

        assertEquals("", labelingFormulaContext.renderPALAllergens());
    }

}
