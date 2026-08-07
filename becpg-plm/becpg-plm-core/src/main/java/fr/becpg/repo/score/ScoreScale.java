/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

/**
 * Visual scale of a score, drives the rendering of the badge.
 *
 * @author matthieu
 */
public enum ScoreScale {

	/** Letter classes, one badge per class (Nutri-Score, Eco-Score, Planet-Score) */
	Letter,

	/** Raw value with its unit (environmental cost in points) */
	Numeric,

	/** Half to five stars (Health Star Rating) */
	Stars,

	/** Warning pictograms, the class holds the number of warnings (ALTO EN) */
	Warnings,

	/** Traffic lights, one light per nutrient (UK multiple traffic lights) */
	Traffic,

	/** Named grade without ordering (NOVA group, EcoVadis medal) */
	Grade,
	Gauge,
	Mark;

	/**
	 * <p>Parses a scale name, falling back on {@link #Numeric} when unknown.</p>
	 *
	 * @param value the stored scale name
	 * @return the matching scale, never null
	 */
	public static ScoreScale parse(String value) {
		for (ScoreScale scale : values()) {
			if (scale.name().equals(value)) {
				return scale;
			}
		}
		return Numeric;
	}
}
