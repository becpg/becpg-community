package fr.becpg.repo.toxicology.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ToxType;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.toxicology.ToxicologyService;

@Service("toxicologyService")
public class ToxicologyServiceImpl implements ToxicologyService {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private Repository repository;
	
	@Autowired
	private RepoService repoService;
	
	@Override
	public NodeRef createOrUpdateToxIngNodeRef(NodeRef ingNodeRef, NodeRef toxNodeRef) {
		NodeRef companyHomeNodeRef = repository.getCompanyHome();
		NodeRef systemNodeRef = repoService.getFolderByPath(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);
		NodeRef charactsNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);
		NodeRef listContainer = nodeService.getChildByName(charactsNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
		NodeRef toxIngFolder = nodeService.getChildByName(listContainer, ContentModel.ASSOC_CONTAINS, PlmRepoConsts.PATH_TOX_ING);
		NodeRef toxIngNodeRef = BeCPGQueryBuilder.createQuery().andPropEquals(PLMModel.PROP_TOX_ING_ING, ingNodeRef.toString()).andPropEquals(PLMModel.PROP_TOX_ING_TOX, toxNodeRef.toString()).singleValue();
		if (toxIngNodeRef == null) {
			toxIngNodeRef = nodeService.createNode(toxIngFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, PLMModel.TYPE_TOX_ING).getChildRef();
			nodeService.setProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_ING, ingNodeRef);
			nodeService.setProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_TOX, toxNodeRef);
		}
		
		Double maxValue = computeMaxValue(ingNodeRef, toxNodeRef);
		nodeService.setProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_MAX_VALUE, maxValue);
		
		Boolean calculateSystemic = (Boolean) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_CALCULATE_SYSTEMIC);
		if (Boolean.TRUE.equals(calculateSystemic)) {
			Double podMax = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_POD_SYSTEMIC);
			Double dermalAbsorption = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_DERMAL_ABSORPTIION);
			Double mosMoe = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MOS_MOE);
			Double finalQuantity = (Double) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_VALUE);
			if (podMax != null && dermalAbsorption != null && dermalAbsorption != 0 && mosMoe != null && mosMoe != 0
					&& finalQuantity != null) {
				Double systemicValue = (podMax * 60 / (finalQuantity * dermalAbsorption / 100 * mosMoe)) * 100;
				nodeService.setProperty(toxIngNodeRef, PLMModel.PROP_TOX_ING_SYSTEMIC_VALUE, systemicValue);
			}
		}
		return toxIngNodeRef;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Double computeMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef) {
		List<String> toxTypes = (List<String>) nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_TYPES);
		List<Double> maxList = new ArrayList<>();
		
		if (toxTypes.contains(ToxType.OcularIrritation.toString())) {
			Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_OCULAR_IRRITATION);
			if (value != null) {
				maxList.add(value);
			}
		}
		if (toxTypes.contains(ToxType.PhototoxicalPotential.toString())) {
			Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_PHOTOTOXIC);
			if (value != null) {
				maxList.add(value);
			}
		}
		if (toxTypes.contains(ToxType.Sensitization.toString())) {
			Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_SENSITIZATION);
			if (value != null) {
				maxList.add(value);
			}
		}
		if (toxTypes.contains(ToxType.SkinIrritation.toString())) {
			Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION);
			if (value != null) {
				maxList.add(value);
			}
		}
		if (toxTypes.contains(ToxType.SystemicIngredient.toString())) {
			Double value = (Double) nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_POD_SYSTEMIC);
			if (value != null) {
				maxList.add(value);
			}
		}
		
		if (!maxList.isEmpty()) {
			return maxList.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
		}
		return null;
	}
}
