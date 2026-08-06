package fr.becpg.repo.product.formulation.lca;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * One entry of an LCA database: the impacts of a reference product, indexed by indicator
 * code.
 *
 * <p>The codes are the ones held by {@code bcpg:lcaCode}, so a database plugin publishing
 * a new indicator has nothing to declare here.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
public class LCAData {

	private final String id;

	private final String value;

	private final Double score;

	private final Map<String, Double> impacts = new LinkedHashMap<>();

	/**
	 * <p>Constructor for LCAData.</p>
	 *
	 * @param id the identifier of the entry in its database
	 * @param value the display name of the entry
	 * @param score the single score published by the database, may be null
	 */
	public LCAData(String id, String value, Double score) {
		this.id = id;
		this.value = value;
		this.score = score;
	}

	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getId() {
		return id;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getValue() {
		return value;
	}

	/**
	 * <p>Getter for the field <code>score</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getScore() {
		return score;
	}

	/**
	 * <p>Records the impact of one indicator, ignoring the ones the database leaves empty.</p>
	 *
	 * @param lcaCode the indicator code, as held by {@code bcpg:lcaCode}
	 * @param impact the impact value
	 * @return this entry
	 */
	public LCAData withImpact(String lcaCode, Double impact) {
		if (impact != null) {
			impacts.put(lcaCode, impact);
		}
		return this;
	}

	/**
	 * <p>Getter for the field <code>impacts</code>.</p>
	 *
	 * @return the impacts indexed by indicator code, never null
	 */
	public Map<String, Double> getImpacts() {
		return Collections.unmodifiableMap(impacts);
	}

	/**
	 * <p>Indicator codes held by this entry, in the order the database published them.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<String> getLcaCodes() {
		return getImpacts().keySet();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return id + " - " + value;
	}
}
