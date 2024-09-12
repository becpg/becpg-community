package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.impl.extractors.NodeRefAutoCompleteExtractor;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

/**
 * <p>ProductReportAutoCompletePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 *
 *  Autocomplete plugin that Get the report templates of the product type that user can choose from
 *
 * Example:
 * <pre>
 * {@code
 * <control template="/org/alfresco/components/form/controls/autocomplete-association.ftl">
 *	  <control-param name="ds">becpg/autocomplete/productreport/reports/bcpg:product
 * </control-param>
 * }
 * </pre>
 *
 *  Datasources available:
 *
 *  ds:/becpg/autocomplete/productreport/reports/{productType}
 *  param: {productType} Get the report templates of the product
 */
@Service("productReportAutoCompletePlugin")
public class ProductReportAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final String SOURCE_TYPE_PRODUCT_REPORT = "productreport";

	@Autowired
	private ReportTplService reportTplService;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_PRODUCT_REPORT };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String productType = (String) props.get(AutoCompleteService.PROP_PRODUCT_TYPE);

		QName productTypeQName = QName.createQName(productType, namespaceService);
		return suggestProductReportTemplates(productTypeQName, query, pageNum, pageSize);

	}

	/**
	 * Get the report templates of the product type that user can choose from
	 * UI.
	 *
	 * @param query
	 *            the query
	 * @return the map
	 */

	private AutoCompletePage suggestProductReportTemplates(QName nodeType, String query, Integer pageNum, Integer pageSize) {

		query = prepareQuery(query);
		List<NodeRef> tplsNodeRef = reportTplService.getUserReportTemplates(ReportType.Document, nodeType, query);

		return new AutoCompletePage(tplsNodeRef, pageNum, pageSize, new NodeRefAutoCompleteExtractor(ContentModel.PROP_NAME, nodeService));
	}

}
