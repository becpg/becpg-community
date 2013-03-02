package fr.becpg.repo.project.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;

/**
 * Project visitor to calculate project score
 * 
 * @author quere
 * 
 */
public class ScoreFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private static Log logger = LogFactory.getLog(ScoreFormulationHandler.class);
	
	private static final int TOTAL_WEIGHT = 100;

	@Override
	public boolean process(ProjectData projectData) throws FormulateException {

		int totalScore = 0;
		int totalWeight = 0;
		projectData.setScore(null);
		
		for(ScoreListDataItem sl : projectData.getScoreList()){
		
			if(sl.getWeight() != null && sl.getScore() != null){
				totalScore += sl.getWeight() * sl.getScore();
				totalWeight += sl.getWeight();
			}
			logger.debug("totalScore: " + totalScore + " totalWeight: " + totalWeight);
		}
		
		if(totalWeight == 0){
			logger.debug("Total weight of project " + projectData.getNodeRef() + " is equal to 0.");
		}
		else if(totalWeight != TOTAL_WEIGHT){
			logger.debug("Total weight of project " + projectData.getNodeRef() + " is different of 100. totalWeight: " + totalWeight);
		}
		else{
			projectData.setScore(totalScore/totalWeight);
		}
		
		return true;
	}
}
