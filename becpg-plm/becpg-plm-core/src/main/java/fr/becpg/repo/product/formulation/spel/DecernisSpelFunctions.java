package fr.becpg.repo.product.formulation.spel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.decernis.DecernisService;
import fr.becpg.repo.product.data.ProductData;

@Service
public class DecernisSpelFunctions implements CustomSpelFunctions {

	@Autowired
	private DecernisService decernisService;

	@Override
	public boolean match(String beanName) {
		return beanName.equals("decernis");
	}

	@Override
	public Object create(ProductData productData) {
		return new BeCPGSpelFunctionsWrapper(productData);
	}

	public class BeCPGSpelFunctionsWrapper {

		ProductData productData;

		public BeCPGSpelFunctionsWrapper(ProductData productData) {
			super();
			this.productData = productData;
		}

		
		/**
		 * @decernis.launch($countries,$usages);
		 * 
		 * 
		 * @param countries
		 * @param usages
		 * @return
		 * @throws Exception
		 */
		public String launch(List<String> countries, List<String> usages) throws Exception {
			return decernisService.launchDecernisAnalysis(productData, countries, usages);
		}

		
		/**
		 * @decernis.launch($productData, $countries,$usages);
		 * 
		 * 
		 * @param countries
		 * @param usages
		 * @return
		 * @throws Exception
		 */
		public String launch(ProductData productData, List<String> countries, List<String> usages) throws Exception {
			return decernisService.launchDecernisAnalysis(productData, countries, usages);
		}
		
	}

}
