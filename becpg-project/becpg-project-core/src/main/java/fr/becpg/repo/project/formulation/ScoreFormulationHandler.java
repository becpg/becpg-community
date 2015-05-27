/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.project.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
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
		for (ScoreListDataItem sl : projectData.getScoreList()) {

			if (sl.getWeight() != null && sl.getScore() != null) {
				totalScore += sl.getWeight() * sl.getScore();
				totalWeight += sl.getWeight();
			}
			logger.debug("totalScore: " + totalScore + " totalWeight: " + totalWeight);
		}

		if (totalWeight == 0) {
			logger.debug("Total weight of project " + projectData.getNodeRef() + " is equal to 0.");
		} else if (totalWeight != TOTAL_WEIGHT) {
			logger.debug("Total weight of project " + projectData.getNodeRef() + " is different of 100. totalWeight: " + totalWeight);
		} else {
			projectData.setScore(totalScore / totalWeight);
		}

		return true;
	}
}
