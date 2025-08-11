package fr.becpg.repo.survey.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyableEntity;
import jakarta.annotation.Nonnull;

/**
 * <p>SurveyableEntityHelper class.</p>
 *
 * @author matthieu
 */
public class SurveyableEntityHelper {

	private static final byte NB_OF_SURVEY_LISTS = 4;
	
	private static final String SURVEY_LIST_BASE_NAME = SurveyModel.TYPE_SURVEY_LIST.getLocalName();
	
	/** Constant <code>SURVEY_LIST_NAMES</code> */
	private static final List<String> SURVEY_LIST_NAMES;
	
	static {
	    SURVEY_LIST_NAMES = IntStream.range(0, SurveyableEntityHelper.NB_OF_SURVEY_LISTS)
	        .mapToObj(i -> SurveyableEntityHelper.SURVEY_LIST_BASE_NAME + (i == 0 ? "" : "@" + i))
	        .toList(); 
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
	 * <p>surveyListsNames.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public static List<String>  surveyListsNames() {
		return SURVEY_LIST_NAMES;
	}

	/**
	 * <p>getNamesSurveyLists.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param entity a {@link fr.becpg.repo.survey.data.SurveyableEntity} object
	 * @return a {@link java.util.Map} object
	 */
	@Nonnull
	public static Map<String, List<SurveyListDataItem>> getNamesSurveyLists(AlfrescoRepository<?> alfrescoRepository,
			SurveyableEntity entity) {
		final Map<String, List<SurveyListDataItem>> namesSurveyLists = new HashMap<>(
				SurveyableEntityHelper.NB_OF_SURVEY_LISTS, 1);

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
		}
		
		return namesSurveyLists;
		
	}
	
	private SurveyableEntityHelper() {
		throw new UnsupportedOperationException();
	}
}
