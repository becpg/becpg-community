/*
 * 
 */
package fr.becpg.repo.project;

import fr.becpg.repo.project.data.ProjectData;

/**
 * The Interface ProjectVisitor.
 *
 * @author querephi
 */
public interface ProjectVisitor {

public ProjectData visit(ProjectData projectData) throws ProjectException;

}
