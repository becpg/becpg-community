package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.test.repo.StandardChocolateEclairTestProduct;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class EvaporatingLabelingFormulationIT extends AbstractFinishedProductTest {

	@Test
	public void testEvaporatedRate() throws Exception {
		final NodeRef finishedProductNodeRef = inWriteTx(() -> {

			StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
					.withAlfrescoRepository(alfrescoRepository).withNodeService(nodeService).withDestFolder(getTestFolderNodeRef()).withCompo(true)
					.withLabeling(true).build();

			return testProduct.createTestProduct().getNodeRef();
		});

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		checkILL(finishedProductNodeRef, labelingRuleList,
				"pâte à choux 45,5% (eau 32,5%, lait 6,5%, sucre 6,5%), crème pâtissière 36,4% (chocolat 9,1%, lait 7,3%, oeuf 7,3%, sucre 7,3%, farine 5,5%), nappage 18,2% (oeuf 7,3%, chocolat 5,5%, sucre 5,5%)",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef, labelingRuleList,
				"pâte à choux 45,5% (sucre 41,3%, lait 4,1%), crème pâtissière 36,4% (chocolat 9,1%, lait 7,3%, oeuf 7,3%, sucre 7,3%, farine 5,5%), nappage 18,2% (oeuf 7,3%, chocolat 5,5%, sucre 5,5%)",
				Locale.FRENCH);

	}
}
