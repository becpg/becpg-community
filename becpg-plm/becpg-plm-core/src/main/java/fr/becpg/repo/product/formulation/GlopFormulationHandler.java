package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestClientException;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.glop.GlopService;
import fr.becpg.repo.glop.model.GlopConstraint;
import fr.becpg.repo.glop.model.GlopContext;
import fr.becpg.repo.glop.model.GlopData;
import fr.becpg.repo.glop.model.GlopTarget;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;


public class GlopFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(GlopFormulationHandler.class);

	private GlopService glopService;
	
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private NamespaceService namespaceService;
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}
	
	public void setGlopService(GlopService glopService) {
		this.glopService = glopService;
	}
	
	@Override
	public boolean process(ProductData productData) throws FormulateException {
		if (!L2CacheSupport.isCacheOnlyEnable() && isGlopApplicable(productData)) {
			
			SimpleCharactDataItem glopListItem = extractGlopListItem(productData);
			if (glopListItem != null) {
				GlopTarget glopTarget = new GlopTarget(glopListItem, productData.getGlopTargetTask().toLowerCase());
				
				List<GlopConstraint> glopConstraints = new ArrayList<>();
				
				double totalQuantity = getTotalQuantity(productData);
				
				for (String listName : productData.getGlopConstraintLists()) {
					QName listQName = QName.createQName(listName, namespaceService);
					List<SimpleCharactDataItem> list = alfrescoRepository.getList(productData, listQName, listQName);
					for (SimpleCharactDataItem item : list) {
						nodeService.removeProperty(item.getNodeRef(), PLMModel.PROP_GLOP_VALUE);
						String glopItemTarget = (String) nodeService.getProperty(item.getNodeRef(), PLMModel.PROP_GLOP_TARGET);
						if (glopItemTarget != null && !glopItemTarget.isBlank()) {
							
							Double tolerance = null;
							
							glopItemTarget = glopItemTarget.replace(" ", "");
							
							if (glopItemTarget.contains("(") && glopItemTarget.contains(")")) {
								tolerance = Double.parseDouble(glopItemTarget.split("\\(")[1].replace(")", ""));
								
								glopItemTarget = glopItemTarget.replace(glopItemTarget.split("\\(")[1], "").replace("(", "");
							}
							
							String[] split = glopItemTarget.split(";");
							
							Double minValue = Double.parseDouble(split[0]) * (item instanceof SimpleListDataItem ? totalQuantity : 1d);
							Double maxValue = split.length < 2 ? minValue : Double.parseDouble(split[1]) * (item instanceof SimpleListDataItem ? totalQuantity : 1d);
							
							GlopConstraint glopConstraint = new GlopConstraint(item, minValue, maxValue);
							
							if (tolerance == null && productData.getGlopTolerance() != null) {
								tolerance = productData.getGlopTolerance();
							}
							
							glopConstraint.setTolerance(tolerance);
							glopConstraints.add(glopConstraint);
						}
					}
				}
				
				glopConstraints.add(new GlopConstraint("recipeQtyUsed", productData.getRecipeQtyUsed(), productData.getRecipeQtyUsed()));

				GlopContext glopContext = new GlopContext(glopTarget, glopConstraints);
				glopContext.setTotalQuantity(totalQuantity);

				try {
					GlopData glopData = glopService.optimize(productData, glopContext);
					productData.setGlopData(glopData);
					if (Boolean.TRUE.equals(productData.getGlopApplyOptimization())) {
						glopData.applyValues(productData, true);
					}
				} catch (RestClientException e) {
					logger.error("Error while calling GlopService: " + e.getMessage(), e);
				}
			}
		}
		return true;
	}
	
	private double getTotalQuantity(ProductData entity) {
		if (entity.getNetWeight() != null && entity.getNetWeight() != 0d) {
			return entity.getNetWeight();
		}
		if (entity.getRecipeQtyUsed() != null && entity.getRecipeQtyUsed() != 0d) {
			return entity.getRecipeQtyUsed();
		}
		return 1d;
	}

	private SimpleCharactDataItem extractGlopListItem(ProductData productData) {
		for (String listName : productData.getGlopConstraintLists()) {
			QName listQName = QName.createQName(listName, namespaceService);
			List<SimpleCharactDataItem> list = alfrescoRepository.getList(productData, listQName, listQName);
			for (SimpleCharactDataItem listItem : list) {
				if (listItem.getCharactNodeRef().equals(productData.getGlopTargetCharact())) {
					return listItem;
				}
			}
		}
		return null;
	}

	private boolean isGlopApplicable(ProductData productData) {
		if (!productData.getAspects().contains(PLMModel.ASPECT_GLOP_PRODUCT)) {
			if (logger.isDebugEnabled()) {
				logger.debug("product has no glopProductAspect, skip");
			}
			return false;
		}
		if (productData.getGlopTargetCharact() == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("product has no glopTargetCharact, skip");
			}
			return false;
		}
		if (productData.getGlopConstraintLists() == null || productData.getGlopConstraintLists().isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("product has no glopConstraintLists, skip");
			}
			return false;
		}
		return !productData.isEntityTemplate();
	}

}
