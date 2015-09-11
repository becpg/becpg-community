package fr.becpg.test.repo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.becpg.test.repo.product.formulation.CharactDetailsFormulationTest;
import fr.becpg.test.repo.product.formulation.CompareFormulationTest;
import fr.becpg.test.repo.product.formulation.FormulationCalcILWithYieldOnCompTest;
import fr.becpg.test.repo.product.formulation.FormulationCheckPropsOfCompTest;
import fr.becpg.test.repo.product.formulation.FormulationFullTest;
import fr.becpg.test.repo.product.formulation.FormulationGenericRawMaterialTest;
import fr.becpg.test.repo.product.formulation.FormulationMultiLevelILTest;
import fr.becpg.test.repo.product.formulation.FormulationProductWithoutQtyTest;
import fr.becpg.test.repo.product.formulation.FormulationTareTest;
import fr.becpg.test.repo.product.formulation.FormulationTest;
import fr.becpg.test.repo.product.formulation.FormulationWithIngRequirementsTest;
import fr.becpg.test.repo.product.formulation.LabelingFormulationTest;


@RunWith(Suite.class)
@SuiteClasses(value={
	CharactDetailsFormulationTest.class,
	CompareFormulationTest.class,
	FormulationCalcILWithYieldOnCompTest.class,
	FormulationCheckPropsOfCompTest.class,
	FormulationFullTest.class,
	FormulationGenericRawMaterialTest.class,
	FormulationMultiLevelILTest.class,
	FormulationProductWithoutQtyTest.class,
	FormulationTareTest.class,
	FormulationTest.class,
	FormulationWithIngRequirementsTest.class,
	LabelingFormulationTest.class
})
public class FormulationSuiteTest {

}
