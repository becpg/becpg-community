/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
package fr.becpg.repo.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;

import fr.becpg.repo.annotation.AnnotationService;

/**
 * Annotation script methods
 *
 * @author Philippe
 *
 */
public final class AnnotationScriptHelper extends BaseScopableProcessorExtension {

	private AnnotationService annotationService;
		
	public void setAnnotationService(AnnotationService annotationService) {
		this.annotationService = annotationService;
	}

	public String uploadDocument(ScriptNode scriptNode){
		return annotationService.uploadDocument(scriptNode.getNodeRef());
	}

	public String createSession(ScriptNode scriptNode, String userId, int sessionDurationInDays){
		return annotationService.createSession(scriptNode.getNodeRef(), userId, sessionDurationInDays);
	}
	
	public void exportDocument(ScriptNode scriptNode){
		annotationService.exportDocument(scriptNode.getNodeRef());
	}

	public void deleteDocument(ScriptNode scriptNode){
		annotationService.deleteDocument(scriptNode.getNodeRef());
	}
}
