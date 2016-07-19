package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 *
 * @author steven
 *
 */
public class ReqCtrlWebScript extends AbstractProductWebscript {

	private static final Log logger = LogFactory.getLog(ReqCtrlWebScript.class);

	AlfrescoRepository<ProductData> alfrescoRepository;

	NodeService nodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		NodeRef productNodeRef = getProductNodeRef(req);

		ProductData product = alfrescoRepository.findOne(productNodeRef);
		StopWatch watch = null;

		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		List<ReqCtrlListDataItem> ctrlList = product.getReqCtrlList();

		NodeRef reqCtrlListNodeRef = null;

		// fetch rclDataItemList node ref
		for (ReqCtrlListDataItem item : ctrlList) {
			if (reqCtrlListNodeRef == null) {
				reqCtrlListNodeRef = item.getParentNodeRef();
				break;
			}
		}

		// put rclDataItemList NR and product score in response
		try {
			JSONObject ret = new JSONObject();

			logger.debug("\n\nreqCtrlListNodeRef: " + reqCtrlListNodeRef + "\n");
			ret.put("reqCtrlListNodeRef", reqCtrlListNodeRef);

			// might be null if product has never been formulated, if not put it
			// in res
			if (product.getEntityScore() != null) {
				JSONObject scores = new JSONObject(product.getEntityScore());
				ret.put("scores", scores);
				if (logger.isDebugEnabled()) {
					logger.debug("\n\nret : " + ret);
				}
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

		if (logger.isDebugEnabled()) {
			assert watch != null;
			watch.stop();
			logger.debug("ReqCtrlWebScript : " + this.getClass().getName() + " takes " + watch.getTotalTimeSeconds() + " seconds");
		}
	}

}
