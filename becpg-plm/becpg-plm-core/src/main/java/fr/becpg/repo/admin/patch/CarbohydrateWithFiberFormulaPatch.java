package fr.becpg.repo.admin.patch;

import java.util.HashMap;
import java.util.Map;

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
 * so that the formulation no longer fails with EL1030E when only one of them is set.
 *
 * @author arthurazambre
 * @version $Id: $Id
 */
public class CarbohydrateWithFiberFormulaPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(CarbohydrateWithFiberFormulaPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.carbohydrateWithFiberFormulaPatch.result";

	private static final String OLD_FORMULA = "(nut['%1$s']?.value(\"EU\")== null and nut['%2$s']?.value(\"EU\")== null) ? null : nut['%1$s']?.value(\"EU\") + nut['%2$s']?.value(\"EU\")";
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
		String oldFormula = String.format(OLD_FORMULA, choAvlNodeRef, fibtgNodeRef);

		// Leave a formula customized on the instance untouched
		if ((formula != null) && removeWhitespaces(formula).equals(removeWhitespaces(oldFormula))) {
			nodeService.setProperty(choNodeRef, PLMModel.PROP_NUT_FORMULA, String.format(NEW_FORMULA, choAvlNodeRef, fibtgNodeRef));
			logger.info("Updated the formula of the CHO- nutrient: " + choNodeRef);
		} else {
			logger.info("The formula of the CHO- nutrient is not the original one, patch will not update it: " + choNodeRef);
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private static String removeWhitespaces(String value) {
		return value.replaceAll("\\s", "");
	}

}
