/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.score.ScoreAggregation;
import fr.becpg.repo.score.ScoreBasis;
import fr.becpg.repo.score.ScoreEngine;
import fr.becpg.repo.score.ScoreScale;

/**
 * Definition of a score: how it is computed, ranged and rendered.
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:scoreDefinition")
@AlfCacheable(isCharact = true)
public class ScoreDefinitionItem extends BeCPGDataObject {

	private static final long serialVersionUID = 1L;

	private String charactName;

	private String code;

	private String version;

	private String engine;

	private String pluginId;

	private String scale;

	private String range;

	private String unit;

	private String basis;

	private Double scaleFactor;

	private String formula;

	private String detailFormula;

	private String rangeFormula;

	private Boolean isOfficial = false;

	private Integer validityPeriod;

	private Date startEffectivity;

	private Date endEffectivity;

	private List<NodeRef> countries = new ArrayList<>();

	private List<NodeRef> usages = new ArrayList<>();

	private String aggregation;

	private String classOrder;

	private List<ScoreDefCoeffListDataItem> coeffList;

	private List<ScoreThresholdListDataItem> thresholdList;

	private List<ScoreBadgeListDataItem> badgeList;

	/**
	 * Way the per nutrient verdicts are turned into the score of the product.
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefAggregation")
	public String getAggregation() {
		return aggregation;
	}

	/**
	 * <p>Setter for the field <code>aggregation</code>.</p>
	 *
	 * @param aggregation a {@link java.lang.String} object
	 */
	public void setAggregation(String aggregation) {
		this.aggregation = aggregation;
	}

	/**
	 * Classes from the best to the worst, comma separated. Needed by the aggregations
	 * comparing verdicts, such as the worst one deciding the grade of a beverage.
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefClassOrder")
	public String getClassOrder() {
		return classOrder;
	}

	/**
	 * <p>Setter for the field <code>classOrder</code>.</p>
	 *
	 * @param classOrder a {@link java.lang.String} object
	 */
	public void setClassOrder(String classOrder) {
		this.classOrder = classOrder;
	}

	/**
	 * Thresholds of the score, one line per nutrient and per category.
	 *
	 * @return a {@link java.util.List} object
	 */
	@DataList
	@AlfQname(qname = "bcpg:scoreThresholdList")
	public List<ScoreThresholdListDataItem> getThresholdList() {
		return thresholdList;
	}

	/**
	 * <p>Setter for the field <code>thresholdList</code>.</p>
	 *
	 * @param thresholdList a {@link java.util.List} object
	 */
	public void setThresholdList(List<ScoreThresholdListDataItem> thresholdList) {
		this.thresholdList = thresholdList;
	}

	/**
	 * Badges of the score, one line per class.
	 *
	 * @return a {@link java.util.List} object
	 */
	@DataList
	@AlfQname(qname = "bcpg:scoreBadgeList")
	public List<ScoreBadgeListDataItem> getBadgeList() {
		return badgeList;
	}

	/**
	 * <p>Setter for the field <code>badgeList</code>.</p>
	 *
	 * @param badgeList a {@link java.util.List} object
	 */
	public void setBadgeList(List<ScoreBadgeListDataItem> badgeList) {
		this.badgeList = badgeList;
	}

	/**
	 * <p>Returns the basis as an enum, defaulting to {@link fr.becpg.repo.score.ScoreBasis#Per100g}.</p>
	 *
	 * @return a {@link fr.becpg.repo.score.ScoreBasis} object
	 */
	public ScoreBasis getScoreBasis() {
		return ScoreBasis.parse(basis);
	}

	/**
	 * <p>Returns the aggregation as an enum, defaulting to {@link fr.becpg.repo.score.ScoreAggregation#None}.</p>
	 *
	 * @return a {@link fr.becpg.repo.score.ScoreAggregation} object
	 */
	public ScoreAggregation getScoreAggregation() {
		return ScoreAggregation.parse(aggregation);
	}

	/**
	 * Markets the score applies to. An empty list means the score applies to every market,
	 * the same convention as the regulatory lists.
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:scoreDefCountries")
	public List<NodeRef> getCountries() {
		return countries;
	}

	/**
	 * <p>Setter for the field <code>countries</code>.</p>
	 *
	 * @param countries a {@link java.util.List} object
	 */
	public void setCountries(List<NodeRef> countries) {
		this.countries = countries;
	}

	/**
	 * Usages the score applies to. An empty list means every usage.
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:scoreDefUsages")
	public List<NodeRef> getUsages() {
		return usages;
	}

	/**
	 * <p>Setter for the field <code>usages</code>.</p>
	 *
	 * @param usages a {@link java.util.List} object
	 */
	public void setUsages(List<NodeRef> usages) {
		this.usages = usages;
	}

	/**
	 * Normalization and weighting factors of the score, one line per LCA indicator.
	 *
	 * @return a {@link java.util.List} object
	 */
	@DataList
	@AlfQname(qname = "bcpg:scoreDefCoeffList")
	public List<ScoreDefCoeffListDataItem> getCoeffList() {
		return coeffList;
	}

	/**
	 * <p>Setter for the field <code>coeffList</code>.</p>
	 *
	 * @param coeffList a {@link java.util.List} object
	 */
	public void setCoeffList(List<ScoreDefCoeffListDataItem> coeffList) {
		this.coeffList = coeffList;
	}

	/**
	 * <p>Getter for the field <code>charactName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:charactName")
	public String getCharactName() {
		return charactName;
	}

	/**
	 * <p>Setter for the field <code>charactName</code>.</p>
	 *
	 * @param charactName a {@link java.lang.String} object
	 */
	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}

	/**
	 * <p>Getter for the field <code>code</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefCode")
	public String getCode() {
		return code;
	}

	/**
	 * <p>Setter for the field <code>code</code>.</p>
	 *
	 * @param code a {@link java.lang.String} object
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * <p>Getter for the field <code>version</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefVersion")
	public String getVersion() {
		return version;
	}

	/**
	 * <p>Setter for the field <code>version</code>.</p>
	 *
	 * @param version a {@link java.lang.String} object
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * <p>Getter for the field <code>engine</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefEngine")
	public String getEngine() {
		return engine;
	}

	/**
	 * <p>Setter for the field <code>engine</code>.</p>
	 *
	 * @param engine a {@link java.lang.String} object
	 */
	public void setEngine(String engine) {
		this.engine = engine;
	}

	/**
	 * <p>Getter for the field <code>pluginId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefPluginId")
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * <p>Setter for the field <code>pluginId</code>.</p>
	 *
	 * @param pluginId a {@link java.lang.String} object
	 */
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	/**
	 * <p>Getter for the field <code>scale</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefScale")
	public String getScale() {
		return scale;
	}

	/**
	 * <p>Setter for the field <code>scale</code>.</p>
	 *
	 * @param scale a {@link java.lang.String} object
	 */
	public void setScale(String scale) {
		this.scale = scale;
	}

	/**
	 * <p>Getter for the field <code>range</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefRange")
	public String getRange() {
		return range;
	}

	/**
	 * <p>Setter for the field <code>range</code>.</p>
	 *
	 * @param range a {@link java.lang.String} object
	 */
	public void setRange(String range) {
		this.range = range;
	}

	/**
	 * <p>Getter for the field <code>unit</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefUnit")
	public String getUnit() {
		return unit;
	}

	/**
	 * <p>Setter for the field <code>unit</code>.</p>
	 *
	 * @param unit a {@link java.lang.String} object
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * <p>Getter for the field <code>basis</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefBasis")
	public String getBasis() {
		return basis;
	}

	/**
	 * <p>Setter for the field <code>basis</code>.</p>
	 *
	 * @param basis a {@link java.lang.String} object
	 */
	public void setBasis(String basis) {
		this.basis = basis;
	}

	/**
	 * <p>Getter for the field <code>scaleFactor</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefScaleFactor")
	public Double getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * <p>Setter for the field <code>scaleFactor</code>.</p>
	 *
	 * @param scaleFactor a {@link java.lang.Double} object
	 */
	public void setScaleFactor(Double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	/**
	 * <p>Getter for the field <code>formula</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefFormula")
	public String getFormula() {
		return formula;
	}

	/**
	 * <p>Setter for the field <code>formula</code>.</p>
	 *
	 * @param formula a {@link java.lang.String} object
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	/**
	 * <p>Getter for the field <code>detailFormula</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefDetailFormula")
	public String getDetailFormula() {
		return detailFormula;
	}

	/**
	 * <p>Setter for the field <code>detailFormula</code>.</p>
	 *
	 * @param detailFormula a {@link java.lang.String} object
	 */
	public void setDetailFormula(String detailFormula) {
		this.detailFormula = detailFormula;
	}

	/**
	 * <p>Getter for the field <code>rangeFormula</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefRangeFormula")
	public String getRangeFormula() {
		return rangeFormula;
	}

	/**
	 * <p>Setter for the field <code>rangeFormula</code>.</p>
	 *
	 * @param rangeFormula a {@link java.lang.String} object
	 */
	public void setRangeFormula(String rangeFormula) {
		this.rangeFormula = rangeFormula;
	}

	/**
	 * <p>Getter for the field <code>isOfficial</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefIsOfficial")
	public Boolean getIsOfficial() {
		return isOfficial;
	}

	/**
	 * <p>Setter for the field <code>isOfficial</code>.</p>
	 *
	 * @param isOfficial a {@link java.lang.Boolean} object
	 */
	public void setIsOfficial(Boolean isOfficial) {
		this.isOfficial = isOfficial;
	}

	/**
	 * <p>Getter for the field <code>validityPeriod</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefValidityPeriod")
	public Integer getValidityPeriod() {
		return validityPeriod;
	}

	/**
	 * <p>Setter for the field <code>validityPeriod</code>.</p>
	 *
	 * @param validityPeriod a {@link java.lang.Integer} object
	 */
	public void setValidityPeriod(Integer validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	/**
	 * <p>Getter for the field <code>startEffectivity</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefStartEffectivity")
	public Date getStartEffectivity() {
		return startEffectivity;
	}

	/**
	 * <p>Setter for the field <code>startEffectivity</code>.</p>
	 *
	 * @param startEffectivity a {@link java.util.Date} object
	 */
	public void setStartEffectivity(Date startEffectivity) {
		this.startEffectivity = startEffectivity;
	}

	/**
	 * <p>Getter for the field <code>endEffectivity</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:scoreDefEndEffectivity")
	public Date getEndEffectivity() {
		return endEffectivity;
	}

	/**
	 * <p>Setter for the field <code>endEffectivity</code>.</p>
	 *
	 * @param endEffectivity a {@link java.util.Date} object
	 */
	public void setEndEffectivity(Date endEffectivity) {
		this.endEffectivity = endEffectivity;
	}

	/**
	 * <p>Returns the engine as an enum, defaulting to {@link fr.becpg.repo.score.ScoreEngine#Criteria}.</p>
	 *
	 * @return a {@link fr.becpg.repo.score.ScoreEngine} object
	 */
	public ScoreEngine getScoreEngine() {
		return ScoreEngine.parse(engine);
	}

	/**
	 * <p>Returns the scale as an enum, defaulting to {@link fr.becpg.repo.score.ScoreScale#Numeric}.</p>
	 *
	 * @return a {@link fr.becpg.repo.score.ScoreScale} object
	 */
	public ScoreScale getScoreScale() {
		return ScoreScale.parse(scale);
	}

	/**
	 * <p>Checks whether this definition applies at the given date.</p>
	 *
	 * @param date the date to check, typically the formulation date
	 * @return true when the definition has no effectivity bound excluding the date
	 */
	public boolean isEffective(Date date) {
		if ((startEffectivity != null) && date.before(startEffectivity)) {
			return false;
		}
		return (endEffectivity == null) || !date.after(endEffectivity);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), code, version);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || (getClass() != obj.getClass())) {
			return false;
		}
		ScoreDefinitionItem other = (ScoreDefinitionItem) obj;
		return Objects.equals(code, other.code) && Objects.equals(version, other.version);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ScoreDefinitionItem [code=" + code + ", version=" + version + ", engine=" + engine + ", scale=" + scale + "]";
	}
}
