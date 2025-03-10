package fr.becpg.repo.product.formulation.ecoscore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.spel.CustomSpelFunctions;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.helper.AllocationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Register custom EcoScore SPEL helper accessible with @ecoScore.
 **/
@Service
public class EcoScoreSpelFunctions implements CustomSpelFunctions {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EcoScoreService ecoScoreService;

	@Autowired
	private AlfrescoRepository<BeCPGDataObject> alfrescoRepository;

	/** {@inheritDoc} */
	@Override
	public boolean match(String beanName) {
		return beanName.equals("ecoScore");
	}

	/** {@inheritDoc} */
	@Override
	public Object create(RepositoryEntity repositoryEntity) {
		return new EcoScoreSpelFunctionsWrapper(repositoryEntity);
	}

	public class EcoScoreSpelFunctionsWrapper {

		RepositoryEntity entity;

		public EcoScoreSpelFunctionsWrapper(RepositoryEntity entity) {
			this.entity = entity;
		}

		public Double countryEPI(List<NodeRef> geoOrigins) {
			Double ret = 100d;
			for (NodeRef geoOrigin : geoOrigins) {
				String geoCode = (String) nodeService.getProperty(geoOrigin, PLMModel.PROP_GEO_ORIGIN_ISOCODE);
				ret = Math.min(ret, ecoScoreService.countryEPI(geoCode));
			}
			return ret;
		}

		public Double countrySPI(List<NodeRef> geoOrigins) {
			Double ret = 100d;
			for (NodeRef geoOrigin : geoOrigins) {
				String geoCode = (String) nodeService.getProperty(geoOrigin, PLMModel.PROP_GEO_ORIGIN_ISOCODE);
				ret = Math.min(ret, ecoScoreService.countrySPI(geoCode));
			}
			return ret;
		}

		public Double distance(NodeRef fromGeoOrigin, List<NodeRef> geoOrigins) {
			Double ret = 0d;
			String fromCode = Locale.getDefault().getCountry();
			if (fromGeoOrigin != null) {
				fromCode = (String) nodeService.getProperty(fromGeoOrigin, PLMModel.PROP_GEO_ORIGIN_ISOCODE);
			}

			for (NodeRef geoOrigin : geoOrigins) {
				String geoCode = (String) nodeService.getProperty(geoOrigin, PLMModel.PROP_GEO_ORIGIN_ISOCODE);
				ret = Math.max(ret, ecoScoreService.distance(fromCode, geoCode));
			}
			return ret;
		}

		public Map<NodeRef, Double> extractAllocation(ProductData productData) {
			Double productNetWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
			return AllocationHelper.extractAllocations(productData, new HashMap<>(), productNetWeight, alfrescoRepository);

		}

		public Double countRawMaterials(ProductData entity) {
			return extractAllocation(entity).size() * 1d;
		}
	}

}
