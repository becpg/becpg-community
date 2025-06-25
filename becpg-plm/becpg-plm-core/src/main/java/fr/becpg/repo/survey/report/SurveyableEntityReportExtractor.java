package fr.becpg.repo.survey.report;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.SurveyService;
import fr.becpg.repo.survey.data.SurveyListDataItem;

@Component
public class SurveyableEntityReportExtractor extends DefaultEntityReportExtractor {
	
	private static final List<QName> TARGET_TYPES = List.of(PLMModel.TYPE_PRODUCT, PLMModel.TYPE_SUPPLIER);
	
	@Autowired
	private SurveyService surveyService;
	
	/** {@inheritDoc} */
	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return TARGET_TYPES.stream().anyMatch(targetType -> entityDictionaryService.isSubClass(type, targetType))
				? EntityReportExtractorPriority.HIGHT
				: EntityReportExtractorPriority.NONE;
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends BeCPGDataObject> filterDataListItems(QName dataListQName,
			List<BeCPGDataObject> dataListItems) {
		if (!SurveyModel.TYPE_SURVEY_LIST.equals(dataListQName)) { 
			return super.filterDataListItems(dataListQName, dataListItems);
		}
		final List<SurveyListDataItem> surveyListDataItems = dataListItems.stream()
				.filter(SurveyListDataItem.class::isInstance)
				.map(SurveyListDataItem.class::cast)
				.collect(Collectors.toList());
		return surveyService.getVisibles(surveyListDataItems);
	}

}
