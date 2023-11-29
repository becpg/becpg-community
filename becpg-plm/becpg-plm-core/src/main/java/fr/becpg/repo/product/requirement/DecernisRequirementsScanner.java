package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
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
import fr.becpg.repo.product.data.productList.RegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.impl.BeCPGHashCodeBuilder;
import fr.becpg.repo.repository.impl.LazyLoadingDataList;

/**
 * <p>DecernisRequirementsScanner class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DecernisRequirementsScanner implements RequirementScanner {

	 public static final String DECERNIS_KEY = "decernis";

	private static final Log logger = LogFactory.getLog(DecernisRequirementsScanner.class);

	private DecernisService decernisService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * <p>Setter for the field <code>decernisService</code>.</p>
	 *
	 * @param decernisService a {@link fr.becpg.repo.decernis.DecernisService} object.
	 */
	public void setDecernisService(DecernisService decernisService) {
		this.decernisService = decernisService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications) {

		if (FormulationService.FAST_FORMULATION_CHAINID.equals(formulatedProduct.getFormulationChainId())) {
			logger.debug("Fast formulation skipping decernis");
			return Collections.emptyList();
		}
		
		if (formulatedProduct.getReformulateCount() != null && !formulatedProduct.getReformulateCount().equals(formulatedProduct.getCurrentReformulateCount())) {
			logger.debug("Skip decernis in reformulateCount " + formulatedProduct.getCurrentReformulateCount());
			return Collections.emptyList();
		}
		
		if (!decernisService.isEnabled()) {
			logger.debug("Decernis service is not enabled");
			return Collections.emptyList();
		}
		
		if (formulatedProduct.getRegulatoryList() == null) {
			formulatedProduct.setRegulatoryList(new LinkedList<>());
		}
		
		updateProductFromRegulatoryList(formulatedProduct);
		
		boolean isDirty = isDirty(formulatedProduct);
		if (isDirty) {
			StopWatch watch = null;
			try {
				if (logger.isDebugEnabled()) {
					watch = new StopWatch();
					watch.start();
				}
				
				formulatedProduct.setFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
				
				List<ReqCtrlListDataItem> requirements = decernisService.extractRequirements(formulatedProduct);
				formulatedProduct.setRegulatoryFormulatedDate(new Date());
				return requirements;

			} catch (FormulateException e) {
				if (logger.isWarnEnabled()) {
					logger.warn(e, e);
				}
				ReqCtrlListDataItem req = new ReqCtrlListDataItem(null, RequirementType.Forbidden,
						MLTextHelper.getI18NMessage("message.decernis.error", e.getMessage()), null, new ArrayList<>(),
						RequirementDataType.Specification);
				req.setFormulationChainId(DecernisService.DECERNIS_CHAIN_ID);
				return Arrays.asList(req);

			} finally {
				updateChecksums(formulatedProduct);
				if (logger.isDebugEnabled() && (watch != null)) {
					watch.stop();
					logger.debug("Running decernis requirement scanner in: " + watch.getTotalTimeSeconds() + "s");
				}
			}

		} else {
			logger.debug("product is not dirty");
		}

		return Collections.emptyList();
	}
	
	private boolean isSameRequirementChecksum(ProductData product) {
		
		if (!CheckSumHelper.isSameChecksum(DECERNIS_KEY, product.getRequirementChecksum(), createProductCheckum(product))) {
			return false;
		}
		
		for (RegulatoryListDataItem regulatoryListDataItem : product.getRegulatoryList()) {
			Set<String> countries = regulatoryListDataItem.getRegulatoryCountries().stream().map(this::extractCode).collect(Collectors.toSet());
			Set<String> usages = regulatoryListDataItem.getRegulatoryUsages().stream().map(this::extractCode).collect(Collectors.toSet());
			if (!CheckSumHelper.isSameChecksum(DECERNIS_KEY, regulatoryListDataItem.getRequirementChecksum(), createRequirementChecksum(countries, usages))) {
				return false;
			}
		}
		return true;
	}

	private void updateChecksums(ProductData formulatedProduct) {
		
		String checkSum = createProductCheckum(formulatedProduct);
		formulatedProduct.setRequirementChecksum(CheckSumHelper.updateChecksum(DECERNIS_KEY, formulatedProduct.getRequirementChecksum(), checkSum));
		
		for (RegulatoryListDataItem regulatoryListDataItem : formulatedProduct.getRegulatoryList()) {
			Set<String> itemCountries = regulatoryListDataItem.getRegulatoryCountries().stream().map(this::extractCode).collect(Collectors.toSet());
			Set<String> itemUsages = regulatoryListDataItem.getRegulatoryUsages().stream().map(this::extractCode).collect(Collectors.toSet());
			String itemCheckSum = createRequirementChecksum(itemCountries, itemUsages);
			regulatoryListDataItem.setRequirementChecksum(CheckSumHelper.updateChecksum(DECERNIS_KEY, regulatoryListDataItem.getRequirementChecksum(), itemCheckSum));
		}
	}
	
	private String createProductCheckum(ProductData formulatedProduct) {
		Set<String> countries = formulatedProduct.getRegulatoryCountries().stream().map(this::extractCode).collect(Collectors.toSet());
		Set<String> usages = formulatedProduct.getRegulatoryUsages().stream().map(this::extractCode).collect(Collectors.toSet());
		StringBuilder checksumBuilder = new StringBuilder();
		checksumBuilder.append(createRequirementChecksum(countries, usages));
		for (RegulatoryListDataItem regulatoryListDataItem : formulatedProduct.getRegulatoryList()) {
			Set<String> itemCountries = regulatoryListDataItem.getRegulatoryCountries().stream().map(this::extractCode).collect(Collectors.toSet());
			Set<String> itemUsages = regulatoryListDataItem.getRegulatoryUsages().stream().map(this::extractCode).collect(Collectors.toSet());
			checksumBuilder.append(createRequirementChecksum(itemCountries, itemUsages));
		}
		
		if (formulatedProduct.getIngList() != null) {
			formulatedProduct.getIngList().stream()
			.filter(ing -> ing != null && ing.getNodeRef() != null)
			.map(ing -> ing.getNodeRef().toString() + ing.getIng() + ing.getValue())
			.sorted()
			.forEach(checksumBuilder::append);
		}
		
		return checksumBuilder.toString();
	}
	
	private String createRequirementChecksum(Set<String> countries, Set<String> usages) {
		StringBuilder key = new StringBuilder();
		if (countries != null) {
			countries.stream().filter(c -> (c != null) && !c.isEmpty()).sorted().forEach(key::append);
		}
		if (usages != null) {
			usages.stream().filter(c -> (c != null) && !c.isEmpty()).sorted().forEach(key::append);
		}
		return key.toString();
	}
	
	private String extractCode(NodeRef node) {
		return (String) nodeService.getProperty(node, PLMModel.PROP_REGULATORY_CODE);
	}
	
	private void updateProductFromRegulatoryList(ProductData product) {
		
		Set<NodeRef> countries = new HashSet<>();
		Set<NodeRef> usages = new HashSet<>();
		
		for (RegulatoryListDataItem item : product.getRegulatoryList()) {
			if (SystemState.Valid.equals(item.getRegulatoryState())) {
				countries.addAll(item.getRegulatoryCountries());
				usages.addAll(item.getRegulatoryUsages());
			}
		}
		
		if (!countries.isEmpty() || !usages.isEmpty()) {
			product.getRegulatoryCountries().clear();
			product.getRegulatoryCountries().addAll(countries);
			product.getRegulatoryUsages().clear();
			product.getRegulatoryUsages().addAll(usages);
		}
	}

	private boolean isDirty(ProductData formulatedProduct) {
		
		if (!isProductCompatibleWithDecernis(formulatedProduct)) {
			logger.debug("Product is not compatible with Decernis");
			return false;
		}
			
		if (!isSameRequirementChecksum(formulatedProduct)) {
			logger.debug("Decernis checksum doesn't match: " + formulatedProduct.getRequirementChecksum());
			return true;
		}
		
		logger.debug("Decernis checksum match test ingList");
		
		if (formulatedProduct.getIngList() instanceof LazyLoadingDataList 
				&& !isIngListDirty((LazyLoadingDataList<IngListDataItem>) formulatedProduct.getIngList())) {
			return false;
		} else if (logger.isDebugEnabled() && (formulatedProduct.getIngList() instanceof LazyLoadingDataList)) {
			logger.debug("- ingList is dirty");
		}
		
		return true;
	}

	private boolean isProductCompatibleWithDecernis(ProductData product) {
		if (product.getRequirementChecksum() != null) {
			return true;
		}
		if (!product.getRegulatoryCountries().isEmpty() && !product.getRegulatoryUsages().isEmpty()) {
			return true;
		}
		for (RegulatoryListDataItem item : product.getRegulatoryList()) {
			if (!item.getRegulatoryCountries().isEmpty() && !item.getRegulatoryUsages().isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isIngListDirty(LazyLoadingDataList<IngListDataItem> dataList) {
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
