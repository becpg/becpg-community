package fr.becpg.repo.admin.patch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;

/**
 * Defaults the empty carbohydrate or fiber value to 0 in the formula of the carbohydrate with fiber nutrient (CHO-),
 * so that the formulation no longer fails with EL1030E when only one of them is set. The formula shipped without
 * parentheses around each default is fixed as well: it left the fiber out as soon as the carbohydrate was set.
 *
 * @author arthurazambre
 * @version $Id: $Id
 */
public class CarbohydrateWithFiberFormulaPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(CarbohydrateWithFiberFormulaPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.carbohydrateWithFiberFormulaPatch.result";

	private static final String FORMULA_WITHOUT_DEFAULT = "(nut['%1$s']?.value(\"EU\")== null and nut['%2$s']?.value(\"EU\")== null) ? null : nut['%1$s']?.value(\"EU\") + nut['%2$s']?.value(\"EU\")";
	private static final String FORMULA_WITHOUT_PARENTHESES = "(nut['%1$s']?.value(\"EU\")== null and nut['%2$s']?.value(\"EU\")== null) ? null : nut['%1$s']?.value(\"EU\") ?: 0.0 + nut['%2$s']?.value(\"EU\") ?: 0.0";
	private static final List<String> OUTDATED_FORMULAS = List.of(FORMULA_WITHOUT_DEFAULT, FORMULA_WITHOUT_PARENTHESES);
	private static final String NEW_FORMULA = "(nut['%1$s']?.value(\"EU\")== null and nut['%2$s']?.value(\"EU\")== null) ? null : (nut['%1$s']?.value(\"EU\") ?: 0.0) + (nut['%2$s']?.value(\"EU\") ?: 0.0)";

	@Override
	protected String applyInternal() throws Exception {
		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		// Browse the folder: the in-transaction metadata queries are not available yet while the patches run
		NodeRef nutsFolder = searchFolder("/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:Nuts");

		if (nutsFolder == null) {
			logger.warn("Nuts folder not found, patch will not run.");
			return I18NUtil.getMessage(MSG_SUCCESS);
		}

		Map<String, NodeRef> nutsByCode = new HashMap<>();
		for (ChildAssociationRef childAssoc : nodeService.getChildAssocs(nutsFolder)) {
			NodeRef nutNodeRef = childAssoc.getChildRef();
			String code = (String) nodeService.getProperty(nutNodeRef, GS1Model.PROP_NUTRIENT_TYPE_CODE);
			if ((code != null) && PLMModel.TYPE_NUT.equals(nodeService.getType(nutNodeRef))) {
				nutsByCode.putIfAbsent(code, nutNodeRef);
			}
		}

		NodeRef choNodeRef = nutsByCode.get("CHO-");
		NodeRef choAvlNodeRef = nutsByCode.get("CHOAVL");
		NodeRef fibtgNodeRef = nutsByCode.get("FIBTG");

		if ((choNodeRef == null) || (choAvlNodeRef == null) || (fibtgNodeRef == null)) {
			logger.warn("CHO-, CHOAVL or FIBTG nutrient not found, patch will not run.");
			return I18NUtil.getMessage(MSG_SUCCESS);
		}

		String formula = (String) nodeService.getProperty(choNodeRef, PLMModel.PROP_NUT_FORMULA);
		Optional<String> correctedFormula = correctFormula(formula, choAvlNodeRef, fibtgNodeRef);

		if (correctedFormula.isPresent()) {
			nodeService.setProperty(choNodeRef, PLMModel.PROP_NUT_FORMULA, correctedFormula.get());
			logger.info("Updated the formula of the CHO- nutrient: " + choNodeRef);
		} else {
			logger.info("The formula of the CHO- nutrient is not the original one, patch will not update it: " + choNodeRef);
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	/**
	 * Gives the formula to store on the carbohydrate with fiber nutrient when the current one is a shipped formula
	 * to fix. A formula customized on the instance is left untouched.
	 *
	 * @param formula the current formula of the nutrient, possibly null
	 * @param choAvlNodeRef the carbohydrate nutrient
	 * @param fibtgNodeRef the fiber nutrient
	 * @return the corrected formula, or empty when the current formula is not one of the shipped formulas to fix
	 */
	static Optional<String> correctFormula(String formula, NodeRef choAvlNodeRef, NodeRef fibtgNodeRef) {
		if (formula == null) {
			return Optional.empty();
		}
		String currentFormula = removeWhitespaces(formula);
		for (String outdatedFormula : OUTDATED_FORMULAS) {
			if (currentFormula.equals(removeWhitespaces(String.format(outdatedFormula, choAvlNodeRef, fibtgNodeRef)))) {
				return Optional.of(String.format(NEW_FORMULA, choAvlNodeRef, fibtgNodeRef));
			}
		}
		return Optional.empty();
	}

	private static String removeWhitespaces(String value) {
		return value.replaceAll("\\s", "");
	}

}
