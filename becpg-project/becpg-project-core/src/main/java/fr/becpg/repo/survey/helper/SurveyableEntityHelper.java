package fr.becpg.repo.survey.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyableEntity;

/**
 * <p>SurveyableEntityHelper class.</p>
 *
 * @author matthieu
 */
public class SurveyableEntityHelper {

	private static final byte NB_OF_SURVEY_LISTS = 4;
	
	private static final String SURVEY_LIST_BASE_NAME = SurveyModel.TYPE_SURVEY_LIST.getLocalName();
	
	/** Constant <code>SURVEY_LIST_NAMES</code> */
	public static final List<String> SURVEY_LIST_NAMES;
	
	static {
		final List<String> surveyListNames = new ArrayList<>(SurveyableEntityHelper.NB_OF_SURVEY_LISTS);
		for (byte i = 0; i < SurveyableEntityHelper.NB_OF_SURVEY_LISTS; ++i) {
			surveyListNames.add(SurveyableEntityHelper.SURVEY_LIST_BASE_NAME + (i == 0 ? "" : "@" + i));
		}
		SURVEY_LIST_NAMES = Collections.unmodifiableList(surveyListNames);
	}
	
	/**
	 * <p>isTransient.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.survey.data.SurveyableEntity} object
	 * @return a boolean
	 */
	public static boolean isTransient(SurveyableEntity entity) {
		return entity.getNodeRef() == null;
	}
	
	/**
	 * <p>isDefault.</p>
	 *
	 * @param surveyListName a {@link java.lang.String} object
	 * @return a boolean
	 */
	public static boolean isDefault(String surveyListName) {
		return SURVEY_LIST_BASE_NAME.equals(surveyListName);
	}
	
	/**
	 * <p>getNamesSurveyLists.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param entity a {@link fr.becpg.repo.survey.data.SurveyableEntity} object
	 * @return a {@link java.util.Map} object
	 */
	public static Map<String, List<SurveyListDataItem>> getNamesSurveyLists(AlfrescoRepository<?> alfrescoRepository,
			SurveyableEntity entity) {
		final Map<String, List<SurveyListDataItem>> namesSurveyLists = new HashMap<>(
				SurveyableEntityHelper.NB_OF_SURVEY_LISTS, 1);
		// run from a unit test ?
		final boolean transientEntity = isTransient(entity);
		for (final String surveyListName : SURVEY_LIST_NAMES) {
			final boolean defaultSurveyListName = isDefault(surveyListName);
			if (defaultSurveyListName || alfrescoRepository.hasDataList(entity.getNodeRef(), surveyListName)) {
				namesSurveyLists.put(surveyListName,
						defaultSurveyListName ? entity.getSurveyList()
								: alfrescoRepository
										.loadDataList(entity.getNodeRef(), surveyListName, SurveyModel.TYPE_SURVEY_LIST)
										.stream().map(SurveyListDataItem.class::cast)
										.collect(Collectors.toCollection(ArrayList::new)));
			}
			// if test context, only the default survey list is added to the map
			if (transientEntity) break;
		}
		return namesSurveyLists;
	}
	
	private SurveyableEntityHelper() {
		throw new UnsupportedOperationException();
	}
}
