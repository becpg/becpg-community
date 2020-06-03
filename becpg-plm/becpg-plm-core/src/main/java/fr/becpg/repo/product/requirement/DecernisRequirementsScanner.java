package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.CheckSumHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.impl.BeCPGHashCodeBuilder;
import fr.becpg.repo.repository.impl.LazyLoadingDataList;

/**
 *
 * @author matthieu
 *
 */
public class DecernisRequirementsScanner implements RequirementScanner {

	private static Log logger = LogFactory.getLog(DecernisRequirementsScanner.class);

	private final static String DECERNIS_KEY = "decernis";

	DecernisService decernisService;

	AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setDecernisService(DecernisService decernisService) {
		this.decernisService = decernisService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {

		if ((formulatedProduct.getRegulatoryCountries() != null) && !formulatedProduct.getRegulatoryCountries().isEmpty()
				&& (formulatedProduct.getRegulatoryUsages() != null) && !formulatedProduct.getRegulatoryUsages().isEmpty()
				&& (formulatedProduct.getIngList() != null) && !formulatedProduct.getIngList().isEmpty()) {

			boolean shouldLaunchDecernis = false;

			String checkSum = decernisService.createDecernisChecksum(formulatedProduct.getRegulatoryCountries(),
					formulatedProduct.getRegulatoryUsages());

			shouldLaunchDecernis = !CheckSumHelper.isSameChecksum(DECERNIS_KEY, formulatedProduct.getRequirementChecksum(), checkSum);

			if (!shouldLaunchDecernis) {
				logger.debug("Decernis checksum match test ingList");
				shouldLaunchDecernis = true;

				if ((formulatedProduct.getIngList() instanceof LazyLoadingDataList)
						&& !isDirty((LazyLoadingDataList<IngListDataItem>) formulatedProduct.getIngList())) {

					shouldLaunchDecernis = false;
				} else if (logger.isDebugEnabled() && (formulatedProduct.getIngList() instanceof LazyLoadingDataList)) {
					logger.debug("- ingList is dirty");
				}

			} else if (logger.isDebugEnabled()) {
				logger.debug("Decernis checksum doesn't match: " + formulatedProduct.getRequirementChecksum());
			}

			if (!FormulationService.FAST_FORMULATION_CHAINID.equals(formulatedProduct.getFormulationChainId())) {

				if (shouldLaunchDecernis) {
					StopWatch watch = null;
					try {

						if (logger.isDebugEnabled()) {
							watch = new StopWatch();
							watch.start();
						}

						formulatedProduct.setFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);

						List<ReqCtrlListDataItem> ret = decernisService.extractDecernisRequirements(formulatedProduct,
								formulatedProduct.getRegulatoryCountries(), formulatedProduct.getRegulatoryUsages());

						formulatedProduct.setRequirementChecksum(
								CheckSumHelper.updateChecksum(DECERNIS_KEY, formulatedProduct.getRequirementChecksum(), checkSum));
						formulatedProduct.setRegulatoryFormulatedDate(new Date());
						

						return ret;

					} catch (FormulateException e) {

						if (logger.isWarnEnabled()) {
							logger.warn(e, e);
						}

						ReqCtrlListDataItem req = new ReqCtrlListDataItem(null, RequirementType.Forbidden,
								MLTextHelper.getI18NMessage("message.decernis.error", e.getMessage()), null, new ArrayList<NodeRef>(),
								RequirementDataType.Specification);
								req.setFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
						return Arrays.asList(req);

					} finally {
						if (logger.isDebugEnabled() && (watch != null)) {
							watch.stop();
							logger.debug("Running decernis requirement scanner in: " + watch.getTotalTimeSeconds() + "s");
						}
					}

				}
			} else {

				logger.debug("Fast formulation skipping decernis");
				if (shouldLaunchDecernis) {
					logger.debug(" - mark dirty");
					formulatedProduct
							.setRequirementChecksum(CheckSumHelper.updateChecksum(DECERNIS_KEY, formulatedProduct.getRequirementChecksum(), null));
				}
				shouldLaunchDecernis = false;

			}

			if (!shouldLaunchDecernis) {
				logger.debug("Decernis requirement is up to date");
				
			}

		}

		return Collections.emptyList();
	}

	private boolean isDirty(LazyLoadingDataList<IngListDataItem> dataList) {
		if (dataList.isLoaded()) {
			if (!dataList.getDeletedNodes().isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("IngList has deleted nodes");
				}
				return true;
			} else {
				for (IngListDataItem item : dataList) {
					if (alfrescoRepository.isDirty(item)) {
						if (logger.isTraceEnabled()) {
							logger.trace("IngList item is dirty: " + item.toString() + " previous checksum " + item.getDbHashCode() + " new checksum "
									+ BeCPGHashCodeBuilder.reflectionHashCode(item) + " old "
									+ BeCPGHashCodeBuilder.reflectionHashCode(alfrescoRepository.findOne(item.getNodeRef())));
							logger.trace(" HashDiff :" + BeCPGHashCodeBuilder.printDiff(item, alfrescoRepository.findOne(item.getNodeRef())));

						}
						return true;
					}
				}

			}
		}
		return false;
	}

}
