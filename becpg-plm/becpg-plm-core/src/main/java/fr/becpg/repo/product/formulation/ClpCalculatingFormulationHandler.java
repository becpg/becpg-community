/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GHSModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaContext;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.ClpListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>ClpCalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ClpCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(ClpCalculatingFormulationHandler.class);

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private Repository repositoryHelper;

	@Autowired
	private SpelFormulaService spelFormulaService;

	private static final String DATABASES_FOLDER = "/app:company_home/cm:System/cm:CPLDatabases";

	public NodeRef getCPLDatabase() {

		NodeRef dbFolderNR = BeCPGQueryBuilder.createQuery().inDB().selectNodeByPath(repositoryHelper.getCompanyHome(), DATABASES_FOLDER);
		if (dbFolderNR != null) {
			return fileFolderService.listFiles(dbFolderNR).get(0).getNodeRef();
		} else {
			return null;
		}
	}

	private CSVReader getCSVReaderFromNodeRef(NodeRef file) {
		ContentReader fileReader = contentService.getReader(file, ContentModel.PROP_CONTENT);
		return new CSVReader(new InputStreamReader(fileReader.getContentInputStream()), ';');
	}

	private static class ClpSpelContext implements SpelFormulaContext<ProductData> {

		Map<String, Double> clpQuantities;

		ProductData entity;

		public ClpSpelContext(Map<String, Double> clpQuantities, ProductData entity) {
			this.clpQuantities = clpQuantities;
			this.entity = entity;
		}

		@Override
		public ProductData getEntity() {
			// TODO Auto-generated method stub
			return entity;
		}

		@Override
		public void setEntity(ProductData entity) {
			this.entity = entity;

		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @return a {@link java.lang.Class} object
	 */
	protected Class<ClpListDataItem> getInstanceClass() {
		return ClpListDataItem.class;
	}

	@Override
	public boolean process(ProductData formulatedProduct) {
		if (accept(formulatedProduct)) {
			logger.debug("CLP calculating visitor");

			if (formulatedProduct.getClpList() == null) {
				formulatedProduct.setClpList(new LinkedList<>());
			}

			Map<String, Double> clpQuantities = new HashMap<>();
			for (IngListDataItem ing : formulatedProduct.getIngList()) {
				double quantityPercentage = ing.getQtyPerc();
				String clpClassifications = (String) nodeService.getProperty(ing.getIng(), GHSModel.PROP_CLP_CLASSIFICATIONS);

				if ((clpClassifications != null) && !clpClassifications.isEmpty()) {
					String[] classifications = clpClassifications.split(",");
					for (String classification : classifications) {
						ClpListDataItem clp = ClpListDataItem.fromCode(classification.trim());
						clpQuantities.merge(clp.toCode(), quantityPercentage, Double::sum);
					}
				}
			}

			StandardEvaluationContext context = spelFormulaService.createCustomSpelContext(formulatedProduct,
					new ClpSpelContext(clpQuantities, formulatedProduct));

			try (CSVReader csvReader = getCSVReaderFromNodeRef(getCPLDatabase())) {
				String[] data;
				Set<String> matchedHPhrases = new HashSet<>();

				while ((data = csvReader.readNext()) != null) {
					if (data.length >= 6) {
						String category = data[0];
						String classification = data[2];
						String condition = data[5];

						if (!matchedHPhrases.contains(classification)) {
							Expression exp = new SpelExpressionParser().parseExpression(condition);
							if (Boolean.TRUE.equals(exp.getValue(context, Boolean.class))) {
								ClpListDataItem newClp = ClpListDataItem.fromCode(category + ":" + classification);
								formulatedProduct.getClpList().add(newClp);
								matchedHPhrases.add(classification);
							}
						}
					}
				}
			} catch (IOException e) {
				logger.error("Error reading CSV file", e);
				return false;
			}
		}
		return true;
	}

	//	         	Catégorie	Classification	Picto	Avertissement	Conditions	Complément
	//	        	Catégorie 3	H226	GHS02	Attention	60 >/= PE >/= 23	-> calcul du point éclair obligatoire pour déterminer ce danger
	//	        	Catégorie 2	H225	GHS02	Danger	PE < 23 + Point ébulition  > 35°C	-> calcul du point éclair obligatoire pour déterminer ce danger
	//	        	Catégorie 1	H224	GHS02	Danger	PE </= 35 + Point ébulition </= 35°C	-> calcul du point éclair obligatoire pour déterminer ce danger
	//	        	Catégorie 1	H290	GHS05	Attention	Si au moins 1 MP coché H290, alors formule classée H290.	
	//	        						
	//	        	Catégorie 1 (1A, 1B, 1C)	H314	GHS05	Danger	Somme des substances H314 >/= 5%	
	//	        	Catégorie 2	H315	GHS07	Attention	Somme des substances H314 >/= 1% mais < 5%	
	//	        	Catégorie 2	H315	GHS07	Attention	Somme des substances H315 >/= 10%	
	//	        	Catégorie 2	H315	GHS07	Attention	Sommes (10*substances H314) et susbtances H315 >/= 10%	
	//	        	Catégorie 1 (1A, 1B, 1C)	H318	GHS05	Danger	Somme des substances H318 et des susbtances H314 >/= 3%	
	//	        	Catégorie 2	H319	GHS07	Attention	Somme des substances H318 et des susbtances H314 >/= 1% mais < 3%	
	//	        	Catégorie 2	H319	GHS07	Attention	Somme des substances H319 >/= 10%	
	//	        	Catégorie 2	H319	GHS07	Attention	Sommes (10*substances H314 + 10* substances H318) et susbtances H319 >/= 10%	
	//	        	Catégorie 1 (1A, 1B, 1)	H317	GHS07	Attention	Substance H317 1 ou 1B >/= 1%	exception pour deux substances considérées comme "supersensibilisant" (isoeugenol et cinnamaldéhyde) :  - mélange classé H317 si >/= 0,01% - mélange classé EUH208 si >/= 0,001%
	//	        	Catégorie 1 (1A, 1B, 1)	H317	GHS07	Attention	Substance H317 1A >/= 0,1 %	exception pour deux substances considérées comme "supersensibilisant" (isoeugenol et cinnamaldéhyde) :  - mélange classé H317 si >/= 0,01% - mélange classé EUH208 si >/= 0,001%
	//	        	/	EUH208	/	/	Substance H317 1 ou 1B >/= 0,1%	exception pour deux substances considérées comme "supersensibilisant" (isoeugenol et cinnamaldéhyde) :  - mélange classé H317 si >/= 0,01% - mélange classé EUH208 si >/= 0,001%
	//	        	/	EUH208	/	/	Substance H317 1A >/= 0,01 %	exception pour deux substances considérées comme "supersensibilisant" (isoeugenol et cinnamaldéhyde) :  - mélange classé H317 si >/= 0,01% - mélange classé EUH208 si >/= 0,001%
	//	        	Catégorie 2	H341	GHS08	Attention	substance H341 2 >/= 1%	
	//	        	Catégorie 1B	H340	GHS08	Danger	substance H340 1A ou 1B >/= 0,1%	
	//	        	Catégorie 1A	H340	GHS08	Danger	substance H340 1A ou 1B >/= 0,1%	
	//	        	Catégorie 2	H351	GHS08	Attention	substance H351 2 >/= 1%	attention, si le mélange contient une substance cancérogène de la catégorie 2 >/= 0,1% et que le mélange n'est pas classé dangereux, alors la mention EUH210 devra apparaître
	//	        	Catégorie 1B	H350	GHS08	Danger	substance H350 1A ou 1B >/= 0,1%	attention, si le mélange contient une substance cancérogène de la catégorie 2 >/= 0,1% et que le mélange n'est pas classé dangereux, alors la mention EUH210 devra apparaître
	//	        	Catégorie 1A	H350	GHS08	Danger	substance H350 1A ou 1B >/= 0,1%	attention, si le mélange contient une substance cancérogène de la catégorie 2 >/= 0,1% et que le mélange n'est pas classé dangereux, alors la mention EUH210 devra apparaître
	//	        	Catégorie 2	H361	GHS08	Attention	substance H361 2 >/= 3%	attention, si le mélange contient une substance reprotoxique de la catégorie 2 >/= 0,1% et que le mélange n'est pas classé dangereux, alors la mention EUH210 devra apparaître
	//	        	Catégorie 1B	H360	GHS08	Danger	substance H360 1A ou 1B >/= 0,3%	attention, si le mélange contient une substance reprotoxique de la catégorie 2 >/= 0,1% et que le mélange n'est pas classé dangereux, alors la mention EUH210 devra apparaître
	//	        	Catégorie 1A	H360	GHS08	Danger	substance H360 1A ou 1B >/= 0,3%	attention, si le mélange contient une substance reprotoxique de la catégorie 2 >/= 0,1% et que le mélange n'est pas classé dangereux, alors la mention EUH210 devra apparaître
	//	        	/	H362	/	/	substance H362 >/= 0,3%	attention, si le mélange contient une substance reprotoxique de la catégorie 2 >/= 0,1% et que le mélange n'est pas classé dangereux, alors la mention EUH210 devra apparaître
	//	        	Catégorie 1 	H370	GHS08	Danger	substance H370 >/= 10%	attention, si le mélange contient une substance catégorie 2 >/= 1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 2	H371	GHS08	Danger	substance H370 < 10% mais >/= 1%	attention, si le mélange contient une substance catégorie 2 >/= 1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 2	H371	GHS08	Danger	substance H371 >/= 10%	attention, si le mélange contient une substance catégorie 2 >/= 1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 3	H335 (voies respiratoires) ou H336 (vertiges, somnolences)	GHS07	Attention	somme des substances H335 ou H336 >/= 20%	attention, si le mélange contient une substance catégorie 2 >/= 1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 1 	H372	GHS08	Danger	substance H372 >/= 10%	attention, si le mélange contient une substance catégorie 2 >/= 1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 2	H373	GHS08	Attention	substance H372 < 10% mais >/= 1%	attention, si le mélange contient une substance catégorie 2 >/= 1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 2	H373	GHS08	Attention	substance H373 >/= 10%	attention, si le mélange contient une substance catégorie 2 >/= 1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 1 	H304	GHS08	Danger	somme des substances H304 >/= 10% 	
	//	        	Catégorie 1 	H304	GHS08	Danger	calcul la somme des MP dont champs hydrocarbures = oui et H304 >/= 10% (sur la base des % d'hydrocarbure indiqué dans la MP)	
	//	        	Catégorie 1 	EUH380	/	Danger	substance EUH380 >/= 0,1%	attention, si le mélange contient une substance catégorie 2 >/= 0,1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 2	EUH381	/	Attention	substance EUH381 >/= 1%	attention, si le mélange contient une substance catégorie 2 >/= 0,1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 1 	H410	GHS09	Attention	(Somme des substances H410 x facteur M) >/= 25%	
	//	        	Catégorie 2	H411	GHS09	/	(Somme des substances H410 x facteur M x 10) + (sommes des substances H411) >/= 25%	
	//	        	Catégorie 3	H412	/	/	(Somme des substances H410 x facteur M x 100) + (sommes des substances H411 x facteur M x 10) + (sommes des substances H412) >/= 25%	
	//	        	Catégorie 4	H413	/	/	Sommes substances H410, H411, H412, H413 >/= 25%	
	//	        	Catégorie 1 	H400	GHS09	Attention	(Somme des substances H400 x facteur M) >/= 25%	
	//	        	Catégorie 1 	EUH430	/	Danger	substance EUH430 >/= 0,1%	attention, si le mélange contient une substance catégorie 2 >/= 0,1%, alors la mention EUH210 devra apparaître
	//	        	Catégorie 2	EUH431	/	Attention	substance EUH431 >/= 1%	attention, si le mélange contient une substance catégorie 2 >/= 0,1%, alors la mention EUH210 devra apparaître
	//	        	/	EUH440	/	Danger	substance EUH440 >/= 0,1%	
	//	        	/	EUH441	/	Danger	substance EUH441 >/= 0,1%	
	//	        	/	EUH450	/	Danger	substance EUH450 >/= 0,1%	
	//	        	/	EUH451	/	Danger	substance EUH451 >/= 0,1%	
	//	        	Catégorie 1 	H300	GHS06	Danger	ETA </= 5	Pour calculer l'ETA, il faut prendre en compte les champs sur la substances Pour Toxicité aigue orale : si >0 Pour Toxicité aigue dermale  si >0 Pour toxicité aigue inhalation : si >0, puis voir dans le champs Voie ATI si Gaz ou Brouillard particules ou Vapeur et faire la somme de toutes les substances sur chaque thématiques   Exemple sur un mélange Ingrédit 1 - Quantité 5% - Toxcité orale 1800 Ingrédient 2 - Quantité 0,5% - Toxcité orale 500 Calcul : =100/SOMME((C42/1800)+(1,5/500)) = 17307 alors pas de classification pour l'oral
	//	        	Catégorie 2	H300	GHS06	Danger	5 < ETA </= 50	
	//	        	Catégorie 3	H301	GHS06	Danger	50 < ETA </= 300	
	//	        	Catégorie 4	H302	GHS07	Attention	300 < ETA </= 2000	
	//	        	Catégorie 1 	H310	GHS06	Danger	ETA </= 50	
	//	        	Catégorie 2	H310	GHS06	Danger	50 < ETA </= 200	
	//	        	Catégorie 3	H311	GHS06	Danger	200 < ETA </= 1000	
	//	        	Catégorie 4	H312	GHS07	Attention	1000 < ETA </= 2000	
	//	        	Catégorie 1 	H330	GHS06	Danger	ETA </= 100	
	//	        	Catégorie 2	H330	GHS06	Danger	100 < ETA </= 500	
	//	        	Catégorie 3	H331	GHS06	Danger	500 < ETA </= 2500	
	//	        	Catégorie 4	H332	GHS07	Attention	2500 < ETA </= 20000	
	//	        	Catégorie 1 	H330	GHS06	Danger	ETA </= 0,5	
	//	        	Catégorie 2	H330	GHS06	Danger	0,5 < ETA </= 2	
	//	        	Catégorie 3	H331	GHS06	Danger	2 < ETA </= 10	
	//	        	Catégorie 4	H332	GHS07	Attention	10 < ETA </= 20	
	//	        	Catégorie 1 	H330	GHS06	Danger	ETA </= 0,05	
	//	        	Catégorie 2	H330	GHS06	Danger	0,05 < ETA </= 0,5	
	//	        	Catégorie 3	H331	GHS06	Danger	0,5 < ETA </= 1	
	//	        	Catégorie 4	H332	GHS07	Attention	1 < ETA </= 5	

	//	        	
	//	        	Pour chaque phrase H on peut avoir plusieurs conditions et pour lire ces conditions, il faut faire une boucle en démarrant par la première condition du CSV.
	//	        	Cela signifie que si je trouve la première condition, je m'arrête la, sinon je passe à la suivante.
	//////		        Par exemple, si on prend dans l'onglet SANTE, le tableau se référant à la corrosion.
	//////		        - Si dans la formule, j'ai une phrase H314 renseignée sur minimun une des substances, je somme toutes les substances ayant cette phrase H, si le seuil >5%, alors on a bien la phase H314 à étiquetter pour les dangers liés à la corrosion classifié dans la catégorie 1 (1a; 1b, 1c) et je m'arrete là sur ce danger.
	//////		        - si seuil <5% alors je vérifie la condition suivante : Si dans la formule, j'ai une phrase H314 renseignée sur minimun une des substances, je somme toutes les substances ayant cette phrase H, si le seuil >=1% et <5% alors H314 et le danger est de catégorie 2 sinon, je passe à la suivante
	//////		        - si dans la formule, j'ai une phrase H315...
	//
	//	        	Pour chque phrase H si la condition match j'ajoute  à     formulatedProduct.getClpList() 	Catégorie	Classification	Picto	Avertissement
	//	        	
	//	        	
	//	       

	private boolean accept(ProductData formulatedProduct) {
		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getClpList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, GHSModel.TYPE_CLPLIST)));
	}

}
