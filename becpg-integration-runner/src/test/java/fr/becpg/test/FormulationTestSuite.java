package fr.becpg.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.becpg.test.repo.product.formulation.CharactDetailsFormulationIT;
import fr.becpg.test.repo.product.formulation.CompareFormulationIT;
import fr.becpg.test.repo.product.formulation.FormulationCalcILWithYieldOnCompIT;
import fr.becpg.test.repo.product.formulation.FormulationCheckPropsOfCompIT;
import fr.becpg.test.repo.product.formulation.FormulationCostsIT;
import fr.becpg.test.repo.product.formulation.FormulationFullIT;
import fr.becpg.test.repo.product.formulation.FormulationGenericRawMaterialIT;
import fr.becpg.test.repo.product.formulation.FormulationIT;
import fr.becpg.test.repo.product.formulation.FormulationMultiLevelILIT;
import fr.becpg.test.repo.product.formulation.FormulationProductWithoutQtyIT;
import fr.becpg.test.repo.product.formulation.FormulationTareIT;
import fr.becpg.test.repo.product.formulation.FormulationWithIngRequirementsIT;
import fr.becpg.test.repo.product.formulation.LabelingFormulationIT;

@RunWith(Suite.class)
@SuiteClasses(value = { CharactDetailsFormulationIT.class, CompareFormulationIT.class, FormulationCalcILWithYieldOnCompIT.class,
		FormulationCheckPropsOfCompIT.class, FormulationFullIT.class, FormulationGenericRawMaterialIT.class, FormulationMultiLevelILIT.class,
		FormulationProductWithoutQtyIT.class, FormulationTareIT.class, FormulationIT.class, FormulationWithIngRequirementsIT.class,
		LabelingFormulationIT.class, FormulationCostsIT.class, LabelingFormulationIT.class })
public class FormulationTestSuite {

}
