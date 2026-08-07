/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.spel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.spel.CustomSpelFunctions;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.score.ScoreContext;
import fr.becpg.repo.score.ScoredEntity;
import fr.becpg.repo.score.data.RegulatoryScoreListDataItem;

/**
 * <p>Custom SPEL helper accessible as {@code @score}.</p>
 *
 * <p>Reads the scores published on the entity, so a formula, a labelling rule or a report can
 * use a score without knowing how it was computed. A score is addressed by its code, the one
 * carried by {@code bcpg:scoreDefCode}.</p>
 *
 * <p>Usage examples:</p>
 * <pre>
 *   {@literal @}score.value("PLANETSCORE_CLIMAT")     // the value, null when not published
 *   {@literal @}score.grade("NUTRISCORE")             // the class, for instance "E"
 *   {@literal @}score.has("PLANETSCORE")              // whether the score was published
 *   {@literal @}score.codes()                         // every code published on the entity
 * </pre>
 *
 * @author matthieu
 */
@Service
public class ScoreSpelFunctions implements CustomSpelFunctions {

	/** {@inheritDoc} */
	@Override
	public boolean match(String beanName) {
		return "score".equals(beanName);
	}

	/** {@inheritDoc} */
	@Override
	public Object create(RepositoryEntity repositoryEntity) {
		return new ScoreSpelFunctionsWrapper(repositoryEntity);
	}

	/**
	 * Wrapper actually exposed to SPEL as {@code @score}.
	 */
	public class ScoreSpelFunctionsWrapper {

		private final RepositoryEntity entity;

		/**
		 * <p>Constructor for ScoreSpelFunctionsWrapper.</p>
		 *
		 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object
		 */
		public ScoreSpelFunctionsWrapper(RepositoryEntity entity) {
			this.entity = entity;
		}

		/**
		 * <p>Value of a score published on the entity.</p>
		 *
		 * @param code the score code
		 * @return a {@link java.lang.Double} object, null when the score is not published
		 */
		public Double value(String code) {
			RegulatoryScoreListDataItem score = find(code);
			return score != null ? score.getValue() : null;
		}

		/**
		 * <p>Class of a score published on the entity.</p>
		 *
		 * @param code the score code
		 * @return a {@link java.lang.String} object, null when the score is not published
		 */
		public String grade(String code) {
			RegulatoryScoreListDataItem score = find(code);
			return score != null ? score.getScoreClass() : null;
		}

		/**
		 * <p>Breakdown of a score, in the normalized detail format.</p>
		 *
		 * @param code the score code
		 * @return a {@link java.lang.String} object, null when the score is not published
		 */
		public String details(String code) {
			RegulatoryScoreListDataItem score = find(code);
			return score != null ? score.getDetails() : null;
		}

		/**
		 * <p>Whether a score is published on the entity.</p>
		 *
		 * @param code the score code
		 * @return a boolean
		 */
		public boolean has(String code) {
			return find(code) != null;
		}

		/**
		 * <p>Codes of every score published on the entity.</p>
		 *
		 * @return a {@link java.util.List} object, never null
		 */
		public List<String> codes() {
			List<String> codes = new ArrayList<>();
			for (RegulatoryScoreListDataItem score : scores()) {
				String code = codeOf(score);
				if (code != null) {
					codes.add(code);
				}
			}
			return codes;
		}

		/**
		 * <p>find.</p>
		 *
		 * @param code the score code
		 * @return a {@link fr.becpg.repo.score.data.RegulatoryScoreListDataItem} object
		 */
		private RegulatoryScoreListDataItem find(String code) {
			for (RegulatoryScoreListDataItem score : scores()) {
				if (Objects.equals(codeOf(score), code)) {
					return score;
				}
			}
			return null;
		}

		/**
		 * The code is not stored on the line, the breakdown carries it: reading it there avoids
		 * loading the definition of every score to answer a formula.
		 *
		 * @param score a {@link fr.becpg.repo.score.data.RegulatoryScoreListDataItem} object
		 * @return a {@link java.lang.String} object
		 */
		private String codeOf(RegulatoryScoreListDataItem score) {
			String details = score.getDetails();
			if ((details == null) || details.isBlank()) {
				return null;
			}
			return ScoreContext.parse(details).getCode();
		}

		/**
		 * <p>scores.</p>
		 *
		 * @return a {@link java.util.List} object, never null
		 */
		private List<RegulatoryScoreListDataItem> scores() {
			if (!(entity instanceof ScoredEntity scored) || (scored.getRegulatoryScoreList() == null)) {
				return Collections.emptyList();
			}
			return Collections.unmodifiableList(scored.getRegulatoryScoreList());
		}

	}

}
