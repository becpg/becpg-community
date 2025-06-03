package fr.becpg.test.repo.repository;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.impl.BeCPGHashCodeBuilder;

public class BeCPGHashCodeBuilderTest {

	@Test
	public void testEmptyMLTextDiff() {

		NutListDataItem nutList1 = NutListDataItem.build().withValue(10d);
		nutList1.setRegulatoryMessage(new MLText());

		NutListDataItem nutList2 = NutListDataItem.build().withValue(10d);
		nutList2.setRegulatoryMessage(null);

		Assert.assertEquals(BeCPGHashCodeBuilder.reflectionHashCode(nutList1), BeCPGHashCodeBuilder.reflectionHashCode(nutList2));

	}

	@Test
	public void testyMLTextDiff() {

		MLText mlText1 = new MLText();
		MLText mlText2 = new MLText();

		NutListDataItem nutList1 = NutListDataItem.build().withValue(10d);

		NutListDataItem nutList2 = NutListDataItem.build().withValue(10d);

		nutList1.setRegulatoryMessage(mlText1);
		nutList2.setRegulatoryMessage(mlText2);

		Assert.assertEquals(BeCPGHashCodeBuilder.reflectionHashCode(nutList1), BeCPGHashCodeBuilder.reflectionHashCode(nutList2));
		mlText1.put(Locale.CANADA, "TEST");

		nutList1.setRegulatoryMessage(mlText1);
		nutList2.setRegulatoryMessage(mlText2);
		System.out.println(BeCPGHashCodeBuilder.printDiff(nutList1, nutList2));
		Assert.assertNotEquals(BeCPGHashCodeBuilder.reflectionHashCode(nutList1), BeCPGHashCodeBuilder.reflectionHashCode(nutList2));

		nutList1.setRegulatoryMessage(mlText1);
		nutList2.setRegulatoryMessage(mlText1);
		Assert.assertEquals(BeCPGHashCodeBuilder.reflectionHashCode(nutList1), BeCPGHashCodeBuilder.reflectionHashCode(nutList2));

		mlText2.put(Locale.CANADA, "TEST");

		nutList1.setRegulatoryMessage(mlText1);
		nutList2.setRegulatoryMessage(mlText2);
		Assert.assertEquals(BeCPGHashCodeBuilder.reflectionHashCode(nutList1), BeCPGHashCodeBuilder.reflectionHashCode(nutList2));

	}

}
