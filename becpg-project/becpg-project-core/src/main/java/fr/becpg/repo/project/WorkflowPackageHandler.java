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
package fr.becpg.repo.project;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.project.data.ProjectData;

/**
 * Extension point allowing modules to add extra content to a workflow package
 * when a project task workflow is started.
 *
 * @author matthieu
 */
public interface WorkflowPackageHandler {

    /**
     * Add extra nodes to the workflow package for the given project.
     *
     * @param wfPackage the workflow package node to enrich
     * @param projectData the project whose workflow is being started
     */
    void addToWorkflowPackage(NodeRef wfPackage, ProjectData projectData);

}
