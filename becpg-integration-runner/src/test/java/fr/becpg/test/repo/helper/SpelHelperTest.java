package fr.becpg.test.repo.helper;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.formulation.spel.SpelHelper;

public class SpelHelperTest {

	@Test
	public void test() {

		Assert.assertEquals(SpelHelper.formatFormula("cost['workspace://SpacesStore/c558bfcf-e996-4df4-9eb6-5061b1f7a8d0'][0].valuePerProduct"),
				"costList.^[cost.toString() == 'workspace://SpacesStore/c558bfcf-e996-4df4-9eb6-5061b1f7a8d0'][0].valuePerProduct");

		Assert.assertEquals(SpelHelper.formatFormula("physico['workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value"),
				"physicoChemList.^[physicoChem.toString() == 'workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value");

		Assert.assertEquals(SpelHelper.formatFormula(
				"!dataListItemEntity.physico['workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'].isEmpty() and dataListItemEntity.physico['workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value != null ? dataListItemEntity.physico['workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value * dataListItem.qty / entity.recipeQtyUsed : \"\""),
				"!dataListItemEntity.physicoChemList.^[physicoChem.toString() == 'workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'].isEmpty() and dataListItemEntity.physicoChemList.^[physicoChem.toString() == 'workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value != null ? dataListItemEntity.physicoChemList.^[physicoChem.toString() == 'workspace://SpacesStore/b8b5de63-c3a8-4479-9a55-b5dfe88e6739'][0].value * dataListItem.qty / entity.recipeQtyUsed : \"\"");
	}

}
