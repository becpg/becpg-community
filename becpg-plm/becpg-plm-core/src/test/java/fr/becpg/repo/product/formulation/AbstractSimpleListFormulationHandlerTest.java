package fr.becpg.repo.product.formulation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

/**
 * Unit tests of the propagate up aspect of a composition line: a line carrying it only contributes to the
 * characteristics it lists, or to all of them when it lists none (#37114).
 */
public class AbstractSimpleListFormulationHandlerTest {

	private static final NodeRef COMPO_LINE = new NodeRef("workspace://SpacesStore/compoLine");

	private static final NodeRef VITAMIN_C = new NodeRef("workspace://SpacesStore/vitaminC");

	private static final NodeRef ZINC = new NodeRef("workspace://SpacesStore/zinc");

	private AssociationService associationService;

	private PhysicoChemCalculatingFormulationHandler handler;

	@Before
	public void setUp() {
		associationService = mock(AssociationService.class);
		handler = new PhysicoChemCalculatingFormulationHandler();
		handler.setAssociationService(associationService);
	}

	@Test
	public void lineWithoutAspectContributesToEveryCharact() {
		CompoListDataItem line = compoLine();

		assertFalse(handler.isOmittedByCompositionLine(line, VITAMIN_C));
		verify(associationService, never()).getTargetAssocs(any(NodeRef.class), any());
	}

	@Test
	public void lineWithAspectAndNoListContributesToEveryCharact() {
		CompoListDataItem line = compoLineWithPropagatedCharacts(List.of());

		assertFalse(handler.isOmittedByCompositionLine(line, VITAMIN_C));
	}

	@Test
	public void lineWithAspectContributesToListedCharact() {
		CompoListDataItem line = compoLineWithPropagatedCharacts(List.of(VITAMIN_C));

		assertFalse(handler.isOmittedByCompositionLine(line, VITAMIN_C));
	}

	@Test
	public void lineWithAspectLeavesUnlistedCharactOut() {
		CompoListDataItem line = compoLineWithPropagatedCharacts(List.of(ZINC));

		assertTrue(handler.isOmittedByCompositionLine(line, VITAMIN_C));
	}

	@Test
	public void unlistedCharactIsNotPropagatedEvenInPropagateMode() {
		CompoListDataItem line = compoLineWithPropagatedCharacts(List.of(ZINC));

		assertFalse(handler.shouldPropagate(line, VITAMIN_C, true));
	}

	private CompoListDataItem compoLine() {
		CompoListDataItem line = CompoListDataItem.build();
		line.setNodeRef(COMPO_LINE);
		return line;
	}

	private CompoListDataItem compoLineWithPropagatedCharacts(List<NodeRef> propagatedCharacts) {
		CompoListDataItem line = compoLine();
		line.getAspects().add(PLMModel.ASPECT_PROPAGATE_UP);
		when(associationService.getTargetAssocs(COMPO_LINE, PLMModel.ASSOC_PROPAGATED_CHARACTS)).thenReturn(propagatedCharacts);
		return line;
	}
}
