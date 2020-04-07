package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.impl.LazyLoadingDataList;

/**
 *
 * @author matthieu
 *
 */
@Service
public class DecernisRequirementsScanner implements RequirementScanner {

	@Autowired
	DecernisService decernisService;

	@Autowired
	AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private static Log logger = LogFactory.getLog(DecernisRequirementsScanner.class);

	private final static String DECERNIS_KEY = "decernis";

	private String updateChecksum(String value, String checksum) {
		try {
			JSONObject json = null;

			if (value == null) {
				json = new JSONObject();
			} else {
				json = new JSONObject(value);
			}

			json.put(DECERNIS_KEY, checksum);

			return json.toString();

		} catch (JSONException e) {
			logger.error(e, e);
		}
		return null;
	}

	private boolean isSameChecksum(String value, String checksum) {
		try {
			if ((value != null)) {
				JSONObject json = new JSONObject(value);
				if (json.has(DECERNIS_KEY)) {
					return (checksum != null) && checksum.equals(json.getString(DECERNIS_KEY));
				}
			}
		} catch (JSONException e) {
			logger.error(e, e);
		}
		return false;
	}

	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {

		if (!formulatedProduct.getRegulatoryCountries().isEmpty() && !formulatedProduct.getRegulatoryUsages().isEmpty()
				&& (formulatedProduct.getIngList() != null) && !formulatedProduct.getIngList().isEmpty()) {

			String checkSum = decernisService.createDecernisChecksum(formulatedProduct.getRegulatoryCountries(),
					formulatedProduct.getRegulatoryUsages());

			boolean shouldLaunchDecernis = !isSameChecksum(formulatedProduct.getRequirementChecksum(), checkSum);

			if (shouldLaunchDecernis) {

				boolean isLazyList = formulatedProduct.getIngList() instanceof LazyLoadingDataList;

				if (!isLazyList && !isDirty((LazyLoadingDataList<IngListDataItem>) formulatedProduct.getIngList())) {
					shouldLaunchDecernis = false;
				}

			}
			if (shouldLaunchDecernis) {

				try {

					updateChecksum(formulatedProduct.getRequirementChecksum(), checkSum);
					formulatedProduct.setRegulatoryFormulatedDate(new Date());

					return decernisService.extractDecernisRequirements(formulatedProduct, formulatedProduct.getRegulatoryCountries(),
							formulatedProduct.getRegulatoryUsages());

				} catch (FormulateException e) {
					return Arrays.asList(new ReqCtrlListDataItem(null, RequirementType.Forbidden,
							MLTextHelper.getI18NMessage("message.formulate.decernis.error", e.getLocalizedMessage()), null, new ArrayList<NodeRef>(),
							RequirementDataType.Specification));
				}
			} else {
				if (formulatedProduct.getReqCtrlList() != null) {
					Set<ReqCtrlListDataItem> toKeep = new HashSet<>();
					for (ReqCtrlListDataItem item : formulatedProduct.getReqCtrlList()) {
						if (RequirementDataType.Specification.equals(item.getReqDataType()) && (item.getRegulatoryCode() != null)
								&& !item.getRegulatoryCode().isEmpty()) {

							ReqCtrlListDataItem req = new ReqCtrlListDataItem(null, item.getReqType(), item.getReqMlMessage(), item.getCharact(),
									item.getSources(), item.getReqDataType());
							req.setRegulatoryCode(item.getRegulatoryCode());

							toKeep.add(req);
						}
					}

					formulatedProduct.getReqCtrlList().addAll(toKeep);
				}
			}

		}

		return Collections.emptyList();
	}

	private boolean isDirty(LazyLoadingDataList<IngListDataItem> dataList) {
		if (dataList.isLoaded() && !dataList.getDeletedNodes().isEmpty()) {
			return true;
		} else if (dataList.isLoaded()) {
			for (IngListDataItem item : dataList) {
				if (alfrescoRepository.isDirty(item)) {
					return true;
				}
			}

		}
		return false;
	}

}
