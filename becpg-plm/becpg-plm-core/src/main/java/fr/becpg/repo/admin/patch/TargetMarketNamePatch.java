package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;

/**
 * <p>TargetMarketNamePatch class.</p>
 *
 * @author arthurazambre
 * @version $Id: $Id
 */
public class TargetMarketNamePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(TargetMarketNamePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.targetMarketNamePatch.result";

	@Override
	protected String applyInternal() throws Exception {
		NodeRef targetMarketsFolder = searchFolder("/app:company_home/cm:System/cm:Characts/bcpg:entityLists/cm:TargetMarkets");

		if (targetMarketsFolder != null) {
			List<ChildAssociationRef> children = nodeService.getChildAssocs(targetMarketsFolder);

			for (ChildAssociationRef childAssoc : children) {
				NodeRef child = childAssoc.getChildRef();
				if (nodeService.getProperty(child, BeCPGModel.PROP_CHARACT_NAME) == null) {
					String name = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
					nodeService.addProperties(child, properties);
					logger.debug("Updated charact name: " + name);
				}
			}
		} else {
			logger.warn("TargetMarkets folder not found, patch will not run.");
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}