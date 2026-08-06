/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

/**
 * Way a score definition produces its value.
 *
 * @author matthieu
 */
public enum ScoreEngine {

	/** Computed by a dedicated {@link fr.becpg.repo.product.formulation.score.ScoreCalculatingPlugin} */
	Plugin,

	/** Computed by the criteria engine, from SpEL formulas held by the score criteria */
	Criteria,

	/** Not computed: the value is entered by the user */
	Manual;

	/**
	 * <p>Parses an engine name, falling back on {@link #Criteria} when unknown.</p>
	 *
	 * @param value the stored engine name
	 * @return the matching engine, never null
	 */
	public static ScoreEngine parse(String value) {
		for (ScoreEngine engine : values()) {
			if (engine.name().equals(value)) {
				return engine;
			}
		}
		return Criteria;
	}
}
