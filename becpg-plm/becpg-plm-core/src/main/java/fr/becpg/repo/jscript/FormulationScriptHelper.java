package fr.becpg.repo.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>FormulationScriptHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FormulationScriptHelper extends BaseScopableProcessorExtension {

	private NodeService nodeService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>getNetWeight.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double getNetWeight(final ScriptNode entity) {
		return FormulationHelper.getNetWeight(entity.getNodeRef(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT);
	}

	/**
	 * <p>computeDuplicateChildQty.</p>
	 *
	 * @param parentCompoList a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param compoList a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double computeDuplicateChildQty(final ScriptNode parentCompoList, final ScriptNode compoList) {

		CompoListDataItem parentCompoListItem = (CompoListDataItem) alfrescoRepository.findOne(parentCompoList.getNodeRef());
		CompoListDataItem compoListDataItem = (CompoListDataItem) alfrescoRepository.findOne(compoList.getNodeRef());
		ProductData productData = (ProductData) alfrescoRepository.findOne(parentCompoListItem.getProduct());
		Double parentQty = FormulationHelper.getQtyInKg(parentCompoListItem);

		ProductUnit compoListUnit = compoListDataItem.getCompoListUnit();

		Double qty = compoListUnit.isPerc() ? FormulationHelper.getQtyInKg(compoListDataItem) : compoListDataItem.getQtySubFormula();
		Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

		if ((qty != null) && (netWeight != 0d)) {
			qty = (parentQty * qty * FormulationHelper.getYield(compoListDataItem)) / (100 * netWeight);

		}

		return qty;

	}

}
