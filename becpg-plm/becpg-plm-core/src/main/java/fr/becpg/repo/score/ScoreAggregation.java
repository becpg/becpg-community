/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

/**
 * Way the per nutrient verdicts of a threshold driven score are turned into the score of
 * the product.
 *
 * @author matthieu
 */
public enum ScoreAggregation {

	/** Every verdict is published, none decides the score of the product (traffic lights) */
	None,

	/** The worst verdict decides the class of the product (beverage grades) */
	Worst,

	/** The number of verdicts is the value of the product (warning labels) */
	Count,

	/** The points of the verdicts are summed (points based profiling models) */
	Sum;

	/**
	 * <p>Parses an aggregation name, falling back on {@link #None} when unknown.</p>
	 *
	 * @param value the stored aggregation name
	 * @return the matching aggregation, never null
	 */
	public static ScoreAggregation parse(String value) {
		for (ScoreAggregation aggregation : values()) {
			if (aggregation.name().equals(value)) {
				return aggregation;
			}
		}
		return None;
	}
}
