/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.formulation.ecobalyse;

/**
 * One entry of the Ecobalyse ingredient reference.
 *
 * <p>The ecosystemic services are the non-LCA complements of the environmental cost. They
 * are published per kilogram and already carry the threshold and the weighting of the
 * method, so beCPG only has to scale them by the quantity used. A positive value is a
 * bonus: it is subtracted from the environmental cost.</p>
 *
 * @author matthieu
 */
public class EcobalyseIngredient {

	private final String id;

	private final String alias;

	private final String name;

	private final String cropGroup;

	private final String scenario;

	private final String defaultOrigin;

	private final Double landOccupation;

	private final Double cropDiversity;

	private final Double hedges;

	private final Double permanentPasture;

	private final Double plotSize;

	/**
	 * <p>Constructor for EcobalyseIngredient.</p>
	 *
	 * @param builder the builder holding the parsed values
	 */
	private EcobalyseIngredient(Builder builder) {
		this.id = builder.id;
		this.alias = builder.alias;
		this.name = builder.name;
		this.cropGroup = builder.cropGroup;
		this.scenario = builder.scenario;
		this.defaultOrigin = builder.defaultOrigin;
		this.landOccupation = builder.landOccupation;
		this.cropDiversity = builder.cropDiversity;
		this.hedges = builder.hedges;
		this.permanentPasture = builder.permanentPasture;
		this.plotSize = builder.plotSize;
	}

	/**
	 * <p>builder.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredient.Builder} object
	 */
	public static Builder builder() {
		return new Builder();
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
	 * <p>Getter for the field <code>alias</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Getter for the field <code>cropGroup</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getCropGroup() {
		return cropGroup;
	}

	/**
	 * <p>Getter for the field <code>scenario</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getScenario() {
		return scenario;
	}

	/**
	 * <p>Getter for the field <code>defaultOrigin</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getDefaultOrigin() {
		return defaultOrigin;
	}

	/**
	 * Land occupation in square metres per year and per kilogram.
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getLandOccupation() {
		return landOccupation;
	}

	/**
	 * <p>Getter for the field <code>cropDiversity</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getCropDiversity() {
		return cropDiversity;
	}

	/**
	 * <p>Getter for the field <code>hedges</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getHedges() {
		return hedges;
	}

	/**
	 * <p>Getter for the field <code>permanentPasture</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getPermanentPasture() {
		return permanentPasture;
	}

	/**
	 * <p>Getter for the field <code>plotSize</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getPlotSize() {
		return plotSize;
	}

	/**
	 * Sum of the ecosystemic services of the ingredient, per kilogram. Zero when the
	 * ingredient earns none.
	 *
	 * @return a double
	 */
	public double totalEcosystemicServices() {
		return sum(cropDiversity) + sum(hedges) + sum(permanentPasture) + sum(plotSize);
	}

	/**
	 * <p>sum.</p>
	 *
	 * @param value a {@link java.lang.Double} object
	 * @return a double
	 */
	private double sum(Double value) {
		return value != null ? value : 0d;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return alias + " - " + name;
	}

	/**
	 * Builder of {@link fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredient}.
	 */
	public static class Builder {

		private String id;
		private String alias;
		private String name;
		private String cropGroup;
		private String scenario;
		private String defaultOrigin;
		private Double landOccupation;
		private Double cropDiversity;
		private Double hedges;
		private Double permanentPasture;
		private Double plotSize;

		/**
		 * <p>withIdentity.</p>
		 *
		 * @param ingredientId the Ecobalyse identifier
		 * @param ingredientAlias the Ecobalyse alias
		 * @param ingredientName the display name
		 * @return this builder
		 */
		public Builder withIdentity(String ingredientId, String ingredientAlias, String ingredientName) {
			this.id = ingredientId;
			this.alias = ingredientAlias;
			this.name = ingredientName;
			return this;
		}

		/**
		 * <p>withOrigin.</p>
		 *
		 * @param group the crop group
		 * @param productionScenario the production scenario
		 * @param origin the default origin
		 * @return this builder
		 */
		public Builder withOrigin(String group, String productionScenario, String origin) {
			this.cropGroup = group;
			this.scenario = productionScenario;
			this.defaultOrigin = origin;
			return this;
		}

		/**
		 * <p>withLandOccupation.</p>
		 *
		 * @param occupation the land occupation in square metres per year and per kilogram
		 * @return this builder
		 */
		public Builder withLandOccupation(Double occupation) {
			this.landOccupation = occupation;
			return this;
		}

		/**
		 * <p>withEcosystemicServices.</p>
		 *
		 * @param diversity the crop diversity complement
		 * @param hedge the hedges complement
		 * @param pasture the permanent pasture complement
		 * @param plot the plot size complement
		 * @return this builder
		 */
		public Builder withEcosystemicServices(Double diversity, Double hedge, Double pasture, Double plot) {
			this.cropDiversity = diversity;
			this.hedges = hedge;
			this.permanentPasture = pasture;
			this.plotSize = plot;
			return this;
		}

		/**
		 * <p>build.</p>
		 *
		 * @return a {@link fr.becpg.repo.product.formulation.ecobalyse.EcobalyseIngredient} object
		 */
		public EcobalyseIngredient build() {
			return new EcobalyseIngredient(this);
		}
	}
}
