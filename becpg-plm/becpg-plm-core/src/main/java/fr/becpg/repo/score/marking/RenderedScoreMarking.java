/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.marking;

/**
 * <p>The marking of a score line, rendered.</p>
 *
 * @param code the score code, never null
 * @param scoreClass the class reached by the score, may be null
 * @param svg the SVG document of the marking
 * @author matthieu
 */
public record RenderedScoreMarking(String code, String scoreClass, String svg) {

}
