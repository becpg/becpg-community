/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation.nutrient.facts;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.template.BeCPGTemplateRenderService;

/**
 * <p>Renders the nutrition facts panel a technical sheet prints, for the regulation of the locale
 * the report is generated in, without any labeling rule.</p>
 *
 * <p>The sheet used to draw these panels itself; it now embeds the very SVG a Render rule would
 * produce. Only the regulations holding a panel are served: a report in {@code en_US} gets the
 * standard vertical panel, or the dual column one when the product asks for it, a report in
 * {@code en_CA} or {@code fr_CA} the bilingual Canadian standard panel, as the sheet always printed it.</p>
 *
 * @author matthieu
 */
@Service("nutritionFactsPanelRenderer")
public class NutritionFactsPanelRenderer {

	private static final Log logger = LogFactory.getLog(NutritionFactsPanelRenderer.class);

	/** Report parameter asking the sheet for the dual column panel, 21 CFR 101.9(e)(6). */
	public static final String SHOW_DUAL_COLUMN_PARAMETER = "Show dual NFP";

	private static final String US_REGULATION = "US";

	private static final String CA_REGULATION = "CA";

	private static final String VERTICAL_FORMAT = "vertical";

	private static final String DUAL_COLUMN_FORMAT = "dualColumn";

	/** Canada requires both official languages on the panel, B.01.012. */
	private static final String CANADIAN_FORMAT = "canadaBilingual";

	private final NodeService mlNodeService;

	private final AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private final BeCPGTemplateRenderService templateRenderService;

	/**
	 * <p>Constructor for NutritionFactsPanelRenderer.</p>
	 *
	 * @param mlNodeService the multilingual aware node service
	 * @param alfrescoRepository the repository
	 * @param templateRenderService the template render service
	 */
	@Autowired
	public NutritionFactsPanelRenderer(@Qualifier("mlAwareNodeService") NodeService mlNodeService, AlfrescoRepository<RepositoryEntity> alfrescoRepository,
			@Qualifier("beCPGTemplateRenderService") BeCPGTemplateRenderService templateRenderService) {
		this.mlNodeService = mlNodeService;
		this.alfrescoRepository = alfrescoRepository;
		this.templateRenderService = templateRenderService;
	}

	/**
	 * <p>Renders the panel of a product for the locale of a report.</p>
	 *
	 * @param product the product
	 * @param locale the locale of the report
	 * @return the SVG, empty when the regulation of the locale holds no panel
	 */
	public Optional<String> render(ProductData product, Locale locale) {
		String regulation = RegulationFormulationHelper.getLocalKey(locale);
		Serializable reportParameters = reportParameters(product);
		Optional<String> format = panelFormat(regulation, reportParameters);
		if (format.isEmpty()) {
			return Optional.empty();
		}

		boolean showOptional = NutritionFactsPanelFormats.hasReportParameter(reportParameters, NutritionFactsPanelFormats.SHOW_OPTIONAL_NUTRIENTS_PARAMETER);
		return renderPanel(product, locale, format.get(), NutritionFactsPanelFormats.options(regulation, format.get(), showOptional));
	}

	/**
	 * <p>Panel format the technical sheet prints for a regulation.</p>
	 *
	 * @param regulation the regulation key
	 * @param reportParameters the report parameters of the product, may be null
	 * @return the format, empty for a regulation holding no panel
	 */
	static Optional<String> panelFormat(String regulation, Serializable reportParameters) {
		if (US_REGULATION.equals(regulation)) {
			return Optional.of(NutritionFactsPanelFormats.hasReportParameter(reportParameters, SHOW_DUAL_COLUMN_PARAMETER) ? DUAL_COLUMN_FORMAT : VERTICAL_FORMAT);
		}
		return CA_REGULATION.equals(regulation) ? Optional.of(CANADIAN_FORMAT) : Optional.empty();
	}

	private Serializable reportParameters(ProductData product) {
		return (product.getNodeRef() != null) && mlNodeService.exists(product.getNodeRef())
				? mlNodeService.getProperty(product.getNodeRef(), ReportModel.PROP_REPORT_PARAMETERS)
				: null;
	}

	/**
	 * A panel is one part of a technical sheet: whatever makes it fail, a product without
	 * serving size or a template broken by a customer override, must cost the panel and never the
	 * sheet, hence the broad catch.
	 */
	private Optional<String> renderPanel(ProductData product, Locale locale, String format, NutritionFactsOptions options) {
		try {
			NutritionFactsData data = new NutritionFactsDataBuilder(mlNodeService, alfrescoRepository).build(product, locale, format, options);
			return Optional.of(templateRenderService
					.render(NutritionFactsPanelFormats.templateName(format), locale, Map.of(NutritionFactsPanelFormats.MODEL_NUTRITION_FACTS, data)).strip());
		} catch (RuntimeException e) {
			logger.error("Cannot render the " + format + " nutrition facts panel of " + product.getNodeRef() + " for the technical sheet", e);
			return Optional.empty();
		}
	}

}
