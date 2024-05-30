package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>
 * IngRequirementScanner class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IngRequirementScanner extends AbstractRequirementScanner<ForbiddenIngListDataItem> {

	private static Log logger = LogFactory.getLog(IngRequirementScanner.class);

	private static final String MESSAGE_NOTAUTHORIZED_ING = "message.formulate.notauhorized.ing";

	private static final String MESSAGE_FORBIDDEN_ING = "message.formulate.ingredient.forbidden";

	AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>alfrescoRepository</code>.
	 * </p>
	 *
	 * @param alfrescoRepository
	 *            a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public List<ReqCtrlListDataItem> checkRequirements(ProductData productData, List<ProductSpecificationData> specifications) {

		List<ReqCtrlListDataItem> reqCtrlMap = new ArrayList<>();

		if ((productData.getIngList() != null) && !productData.getIngList().isEmpty()) {

			for (Map.Entry<ProductSpecificationData, List<ForbiddenIngListDataItem>> entry : extractRequirements(specifications).entrySet()) {
				boolean checkAutorized = false;

				List<ForbiddenIngListDataItem> requirements = entry.getValue();
				ProductSpecificationData specification = entry.getKey();

				Set<NodeRef> visited = new HashSet<>();

				if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					for (CompoListDataItem compoListDataItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

						if ((compoListDataItem.getQtySubFormula() != null) && (compoListDataItem.getQtySubFormula() > 0)) {
							ProductData componentProductData = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());

							checkILOfPart(compoListDataItem.getProduct(), compoListDataItem.getDeclType(), componentProductData, requirements,
									specification, reqCtrlMap, visited);

						}

					}
				}

				for (ForbiddenIngListDataItem fil : requirements) {
					
					
					if (!RequirementType.Authorized.equals(fil.getReqType())) {
						
						Double qtyPerc = null;
						
						
						for (IngListDataItem ingListDataItem : productData.getIngList()) {
							
							// Ings
							if (!fil.getIngs().isEmpty() && (fil.getReqMessage() != null) && !fil.getReqMessage().isEmpty()) {
								
								if (fil.getIngs().contains(ingListDataItem.getIng())) {
									
									Double ingQtyPerc = ingListDataItem.getQtyPerc();
									
									if (qtyPerc == null) {
										qtyPerc = ingQtyPerc;
									} else if (ingQtyPerc != null) {
										qtyPerc += ingQtyPerc;
									}
								}
							}
						}

						for (IngListDataItem ingListDataItem : productData.getIngList()) {
							// Ings
							if (!fil.getIngs().isEmpty() && (fil.getReqMessage() != null) && !fil.getReqMessage().isEmpty()) {
								
								if (fil.getIngs().contains(ingListDataItem.getIng())) {
									
									Double filMaxQtyPerc = getFilMaxQtyPerc(productData, fil);
									if ((qtyPerc == null) || ((filMaxQtyPerc != null) && (filMaxQtyPerc <= qtyPerc))
											|| Boolean.TRUE.equals(addInfoReqCtrl)) {

										boolean isInfo = qtyPerc != null && filMaxQtyPerc != null && (filMaxQtyPerc > qtyPerc);
										
										// req not respecte
										ReqCtrlListDataItem reqCtrl = ReqCtrlListDataItem.build()
												.ofType(isInfo ? RequirementType.Info : fil.getReqType()).withMessage(fil.getReqMessage())
												.withCharact(ingListDataItem.getIng())
												.withSources(List.of(ingListDataItem.getIng()))
												.ofDataType(RequirementDataType.Specification);										
										
										String regulatoryId = extractRegulatoryId(fil, specification);
										
										if (regulatoryId != null && !regulatoryId.isBlank()) {
											reqCtrl.setRegulatoryCode(regulatoryId);
										} else if ((specification.getRegulatoryCode() != null) && !specification.getRegulatoryCode().isBlank()) {
											reqCtrl.setRegulatoryCode(specification.getRegulatoryCode());
										} else {
											reqCtrl.setRegulatoryCode(specification.getName());
										}

										if (!isInfo && (qtyPerc != null) && (filMaxQtyPerc != null) && (qtyPerc != 0)) {
											reqCtrl.setReqMaxQty((filMaxQtyPerc / qtyPerc) * 100d);
										}
										
										reqCtrlMap.add(reqCtrl);

									}
								}
							} else if ((productData.getCompoListView().getCompoList() == null)
									|| productData.getCompoListView().getCompoList().isEmpty()) {
								if (checkRuleMatchIng(ingListDataItem, fil)) {
									MLText curMessage = fil.getReqMessage();
									if ((curMessage == null) || curMessage.values().stream().noneMatch(mes -> (mes != null) && !mes.isEmpty())) {
										curMessage = MLTextHelper.getI18NMessage(MESSAGE_FORBIDDEN_ING,
												mlNodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
									}

									ReqCtrlListDataItem reqCtrl = ReqCtrlListDataItem.build().ofType( fil.getReqType())
											.withMessage(curMessage).withCharact(ingListDataItem.getIng()).ofDataType(RequirementDataType.Specification).withRegulatoryCode(
													(specification.getRegulatoryCode() != null) && !specification.getRegulatoryCode().isBlank()
													? specification.getRegulatoryCode()
													: specification.getName());
									
									reqCtrlMap.add(reqCtrl);


								}

							}

						}
					} else {
						checkAutorized = true;
					}
				}
 
				// Check autorized

				if (checkAutorized) {

					for (IngListDataItem ingListDataItem : productData.getIngList()) {
						boolean autorized = false;

						for (ForbiddenIngListDataItem fil : requirements) {
							if (RequirementType.Authorized.equals(fil.getReqType())) {
								if (checkRuleMatchIng(ingListDataItem, fil)) {
									autorized = true;
									if ((fil.getReqMessage() != null) && (fil.getReqMessage().getDefaultValue() != null)
											&& (!fil.getReqMessage().getDefaultValue().isEmpty())) {
										addReqCtrl(reqCtrlMap, RequirementType.Authorized, fil.getReqMessage(),
												ingListDataItem.getIng(), specification, RequirementDataType.Specification);
									}
									break;
								}
							}
						}

						if (!autorized) {
							String message = I18NUtil.getMessage(MESSAGE_NOTAUTHORIZED_ING);
							addReqCtrl(reqCtrlMap, RequirementType.Forbidden, new MLText(message), ingListDataItem.getIng(), specification, RequirementDataType.Specification);
						}

					}

				}
			}
		}

		return reqCtrlMap;

	}
	
	private Double getFilMaxQtyPerc(ProductData product, ForbiddenIngListDataItem fil) {
		String unit = fil.getQtyPercMaxiUnit();
		if ("%".equals(unit)) {
			return fil.getQtyPercMaxi();
		}
		if ("mg/kg".equals(unit)) {
			return fil.getQtyPercMaxi() / 10000;
		}
		if ("mg/L".equals(unit)) {
			Double density = product.getDensity();
			if (density == null || density == 0d) {
				density = 1d;
			}
			return fil.getQtyPercMaxi() / density / 10000;
		}
		return fil.getQtyPercMaxi();
	}
	
	private String extractRegulatoryId(ForbiddenIngListDataItem fil, ProductSpecificationData specification) {
		if (fil.getRegulatoryCountriesRef() != null && !fil.getRegulatoryCountriesRef().isEmpty()) {
			String countryCode = (String) nodeService.getProperty(fil.getRegulatoryCountriesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			if (fil.getRegulatoryUsagesRef() != null && !fil.getRegulatoryUsagesRef().isEmpty()) {
				return countryCode + " - " + (String) nodeService.getProperty(fil.getRegulatoryUsagesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			}
		}
		if (specification.getRegulatoryCountriesRef() != null && !specification.getRegulatoryCountriesRef().isEmpty()) {
			String countryCode = (String) nodeService.getProperty(specification.getRegulatoryCountriesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			if (specification.getRegulatoryUsagesRef() != null && !specification.getRegulatoryUsagesRef().isEmpty()) {
				return countryCode + " - " + (String) nodeService.getProperty(specification.getRegulatoryUsagesRef().get(0), PLMModel.PROP_REGULATORY_CODE);
			}
		}
		return null;
	}

	/**
	 * check the ingredients of the part according to the specification
	 *
	 * @param specification
	 *
	 * @param compoListDataItem
	 *            the compo list data item
	 * @param ingMap
	 *            the ing map
	 * @param totalQtyIngMap
	 *            the total qty ing map
	 */
	private void checkILOfPart(NodeRef productNodeRef, DeclarationType declType, ProductData componentProductData,
			List<ForbiddenIngListDataItem> forbiddenIngredientsList, ProductSpecificationData specification,
			List<ReqCtrlListDataItem> reqCtrlMap, Set<NodeRef> visited) {

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(mlNodeService.getType(productNodeRef)) && !visited.contains(productNodeRef)) {

			visited.add(productNodeRef);

			if ((componentProductData.getIngList() != null) && !componentProductData.getIngList().isEmpty()) {

				forbiddenIngredientsList.forEach(fil ->

				componentProductData.getIngList().forEach(ingListDataItem -> {

					if (!RequirementType.Authorized.equals(fil.getReqType()) && (fil.getReqMessage() != null) && !fil.getReqMessage().isEmpty()) {

						if (checkRuleMatchIng(ingListDataItem, fil)) {
							// Look for raw material
							if ((componentProductData.getCompoListView().getCompoList() != null)
									&& !componentProductData.getCompoListView().getCompoList().isEmpty()) {
								for (CompoListDataItem c : componentProductData.getCompoListView().getCompoList()) {
									checkILOfPart(c.getProduct(), declType, (ProductData) alfrescoRepository.findOne(c.getProduct()),
											forbiddenIngredientsList, specification, reqCtrlMap, visited);
								}
							} else {
								MLText curMessage = fil.getReqMessage();
								if ((curMessage == null) || curMessage.values().stream().noneMatch(mes -> (mes != null) && !mes.isEmpty())) {
									curMessage = MLTextHelper.getI18NMessage(MESSAGE_FORBIDDEN_ING,
											mlNodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
								}

								if (logger.isDebugEnabled()) {
									logger.debug("Adding not respected for: " + curMessage);
								}

								ReqCtrlListDataItem reqCtrl = ReqCtrlListDataItem.build().ofType(fil.getReqType())
										.withMessage(curMessage).withCharact(ingListDataItem.getIng()).ofDataType(RequirementDataType.Specification).withRegulatoryCode(
												(specification.getRegulatoryCode() != null) && !specification.getRegulatoryCode().isBlank()
												? specification.getRegulatoryCode()
												: specification.getName())
										.withSources(Arrays.asList(productNodeRef));
										
										
								
								reqCtrlMap.add(reqCtrl);

								
							}
						}
					}

				}));

			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected List<ForbiddenIngListDataItem> getDataListVisited(ProductData partProduct) {
		return ((ProductSpecificationData) partProduct).getForbiddenIngList() != null ? ((ProductSpecificationData) partProduct).getForbiddenIngList()
				: new ArrayList<>();
	}

	private boolean checkRuleMatchIng(IngListDataItem ingListDataItem, ForbiddenIngListDataItem fil) {

		if ((fil.getIsGMO() != null) && !fil.getIsGMO().isEmpty() && (!fil.getIsGMO().equals(ingListDataItem.getIsGMO().toString())
				|| (Boolean.FALSE.equals(Boolean.valueOf(fil.getIsGMO())) && Boolean.FALSE.equals(ingListDataItem.getIsGMO())))) {

			return false; // check next rule
		}

		// Ionized
		if ((fil.getIsIonized() != null) && !fil.getIsIonized().isEmpty() && (!fil.getIsIonized().equals(ingListDataItem.getIsIonized().toString())
				|| (Boolean.FALSE.equals(Boolean.valueOf(fil.getIsIonized())) && Boolean.FALSE.equals(ingListDataItem.getIsIonized())))) {
			return false; // check next rule
		}

		// Ings
		if (!fil.getIngs().isEmpty()) {
			if (!fil.getIngs().contains(ingListDataItem.getIng()) || (fil.getQtyPercMaxi() != null)) {
				return false; // check next rule
			}
		}

		// GeoOrigins
		if (!fil.getGeoOrigins().isEmpty()) {
			boolean hasGeoOrigin = false;
			for (NodeRef n : ingListDataItem.getGeoOrigin()) {
				if (fil.getGeoOrigins().contains(n)) {
					hasGeoOrigin = true;
				}
			}

			if (!hasGeoOrigin) {
				return false; // check next rule
			}
		}

		// Required GeoOrigins
		if (!fil.getRequiredGeoOrigins().isEmpty()) {
			boolean hasGeoOrigin = true;
			for (NodeRef n : ingListDataItem.getGeoOrigin()) {
				if (!fil.getRequiredGeoOrigins().contains(n)) {
					hasGeoOrigin = false;
				}
			}

			if (hasGeoOrigin) {
				return false; // check next rule
			}
		}

		// GeoTransfo
		if (!fil.getGeoTransfo().isEmpty()) {
			boolean hasGeoTransfo = false;
			for (NodeRef n : ingListDataItem.getGeoTransfo()) {
				if (fil.getGeoTransfo().contains(n)) {
					hasGeoTransfo = true;
				}
			}

			if (!hasGeoTransfo) {
				return false; // check next rule
			}
		}

		// BioOrigins
		if (!fil.getBioOrigins().isEmpty()) {
			boolean hasBioOrigin = false;
			for (NodeRef n : ingListDataItem.getBioOrigin()) {
				if (fil.getBioOrigins().contains(n)) {
					hasBioOrigin = true;
				}
			}
			if (!hasBioOrigin) {
				return false; // check next rule
			}

		}

		return true;
	}

	private void addReqCtrl(List<ReqCtrlListDataItem> reqCtrlMap, RequirementType requirementType, MLText message,
			NodeRef sourceNodeRef, ProductSpecificationData specification, RequirementDataType requirementDataType) {

		reqCtrlMap.add( ReqCtrlListDataItem.build().ofType(requirementType)
				.withMessage(message).ofDataType(requirementDataType).withSources(Arrays.asList(sourceNodeRef)).withRegulatoryCode(
						(specification.getRegulatoryCode() != null) && !specification.getRegulatoryCode().isBlank()
						? specification.getRegulatoryCode()
						: specification.getName()));

	}

	/** {@inheritDoc} */
	@Override
	protected void mergeRequirements(List<ForbiddenIngListDataItem> ret, List<ForbiddenIngListDataItem> toAdd) {
		ret.addAll(toAdd);
	}

}