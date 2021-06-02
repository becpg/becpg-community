package fr.becpg.test.repo.product.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.formulation.score.FrenchEcoScore;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FrenchEcoScoreIT extends AbstractFinishedProductTest {

	@Autowired
	FrenchEcoScore frenchEcoScore;
	

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParts();
	}

	@Test
	public void testFrenchEcoScore() {
		RawMaterialData rawMaterialData = new RawMaterialData();
		rawMaterialData.setName("Prince goût chocolat");

		ListValuePage ret = frenchEcoScore.suggest(FrenchEcoScore.ECO_SCORE_SOURCE_TYPE, "Biscuit sec chocolaté, préemballé", 1, 500, null);

		Assert.assertTrue(ret.getFullListSize() > 0);

		boolean found = false;

		for (ListValueEntry entry : ret.getResults()) {
			if (entry.getValue().equals("24036")) {
				found = true;
			}

		}
		Assert.assertTrue(found);

		rawMaterialData.setEcoScoreCategory("24036");

		List<NodeRef> geoOrigins = new ArrayList<>();
		geoOrigins.add(geoOrigin1);
		List<IngListDataItem> ingList = new ArrayList<>();
		
		ingList.add(new IngListDataItem(null, 48d, geoOrigins, new ArrayList<>(), true, true, false, ing1, false));
		
		rawMaterialData.setIngList(ingList);

		frenchEcoScore.formulateScore(rawMaterialData);
		
		/*-- Characteristics --*/
		Map<QName, Serializable> properties = new HashMap<>();

//	TOCO Cf FormulationPackMaterialIT	/*-- Pack materials --*/
//		properties.put(BeCPGModel.PROP_LV_VALUE, "Alluminium");
//		packMaterial1NodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
//				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
//				PackModel.TYPE_PACKAGING_MATERIAL, properties).getChildRef();
//		properties.clear();



		Assert.assertEquals(rawMaterialData.getEcoScore(), (Double) 53d);
		Assert.assertEquals(rawMaterialData.getEcoScoreClass(), "C");

	}

}
