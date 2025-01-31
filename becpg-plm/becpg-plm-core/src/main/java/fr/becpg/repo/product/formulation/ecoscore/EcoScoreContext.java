package fr.becpg.repo.product.formulation.ecoscore;

import java.util.Objects;

import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * <p>EcoScoreContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EcoScoreContext {
	
	private int ecoScore;
	private String scoreClass;
	private int acvScore;
	private int claimBonus;
	private int transportScore;
	private int politicalScore;
	private int packagingMalus;

	/**
	 * <p>Getter for the field <code>ecoScore</code>.</p>
	 *
	 * @return a int
	 */
	public int getEcoScore() {
		return ecoScore;
	}

	/**
	 * <p>Setter for the field <code>ecoScore</code>.</p>
	 *
	 * @param ecoScore a int
	 */
	public void setEcoScore(int ecoScore) {
		this.ecoScore = ecoScore;
	}

	/**
	 * <p>Getter for the field <code>scoreClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getScoreClass() {
		return scoreClass;
	}

	/**
	 * <p>Setter for the field <code>scoreClass</code>.</p>
	 *
	 * @param scoreClass a {@link java.lang.String} object
	 */
	public void setScoreClass(String scoreClass) {
		this.scoreClass = scoreClass;
	}

	/**
	 * <p>Getter for the field <code>acvScore</code>.</p>
	 *
	 * @return a int
	 */
	public int getAcvScore() {
		return acvScore;
	}

	/**
	 * <p>Setter for the field <code>acvScore</code>.</p>
	 *
	 * @param acvScore a int
	 */
	public void setAcvScore(int acvScore) {
		this.acvScore = acvScore;
	}

	/**
	 * <p>Getter for the field <code>claimBonus</code>.</p>
	 *
	 * @return a int
	 */
	public int getClaimBonus() {
		return claimBonus;
	}

	/**
	 * <p>Setter for the field <code>claimBonus</code>.</p>
	 *
	 * @param claimBonus a int
	 */
	public void setClaimBonus(int claimBonus) {
		this.claimBonus = claimBonus;
	}

	/**
	 * <p>Getter for the field <code>transportScore</code>.</p>
	 *
	 * @return a int
	 */
	public int getTransportScore() {
		return transportScore;
	}

	/**
	 * <p>Setter for the field <code>transportScore</code>.</p>
	 *
	 * @param transportScore a int
	 */
	public void setTransportScore(int transportScore) {
		this.transportScore = transportScore;
	}


	/**
	 * <p>Getter for the field <code>politicalScore</code>.</p>
	 *
	 * @return a int
	 */
	public int getPoliticalScore() {
		return politicalScore;
	}

	/**
	 * <p>Setter for the field <code>politicalScore</code>.</p>
	 *
	 * @param politicalScore a int
	 */
	public void setPoliticalScore(int politicalScore) {
		this.politicalScore = politicalScore;
	}

	/**
	 * <p>Getter for the field <code>packagingMalus</code>.</p>
	 *
	 * @return a int
	 */
	public int getPackagingMalus() {
		return packagingMalus;
	}

	/**
	 * <p>Setter for the field <code>packagingMalus</code>.</p>
	 *
	 * @param packagingMalus a int
	 */
	public void setPackagingMalus(int packagingMalus) {
		this.packagingMalus = packagingMalus;
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("ecoScore", ecoScore);
		json.put("scoreClass", scoreClass);
		json.put("acvScore", acvScore);
		json.put("claimBonus", claimBonus);
		json.put("transportScore", transportScore);
		json.put("politicalScore", politicalScore);
		json.put("packagingMalus", packagingMalus);

		return json;
	}
	
	/**
	 * <p>toHtmlDisplayValue.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String toHtmlDisplayValue() {

		StringBuilder sb = new StringBuilder();
		
		sb.append("@html");
		
		sb.append("<ul>");
		sb.append("<li>" + I18NUtil.getMessage("ecoscore.acvScore", acvScore));
		sb.append("<li>" + I18NUtil.getMessage("ecoscore.claimBonus", claimBonus));
		sb.append("<li>" + I18NUtil.getMessage("ecoscore.transportScore", transportScore));
		sb.append("<li>" + I18NUtil.getMessage("ecoscore.politicalScore", politicalScore));
		sb.append("<li>" + I18NUtil.getMessage("ecoscore.packagingMalus", packagingMalus));
		sb.append("<li><b>" + I18NUtil.getMessage("ecoscore.score", ecoScore) + "</b>");
		sb.append("<li><b>" + I18NUtil.getMessage("ecoscore.class", scoreClass) + "</b>");
		sb.append("<ul>");

		String classString = "ecoscore-class-error";
		
		if ("A".equals(scoreClass)) {
			classString = "ecoscore-class-a";
		} else if ("B".equals(scoreClass)) {
			classString = "ecoscore-class-b";
		} else if ("C".equals(scoreClass)) {
			classString = "ecoscore-class-c";
		} else if ("D".equals(scoreClass)) {
			classString = "ecoscore-class-d";
		} else if ("E".equals(scoreClass)) {
			classString = "ecoscore-class-e";
		}
		
		sb.append("<p>");
        sb.append("<span class=\"" + classString + "\"></span>");
		sb.append("</p>");

		return sb.toString();
		
	}
	
	/**
	 * <p>parse.</p>
	 *
	 * @param nutriScoreDetails a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.formulation.score.EcoScoreContext} object
	 */
	public static EcoScoreContext parse(String nutriScoreDetails) {

		JSONObject jsonValue = new JSONObject(nutriScoreDetails);

		EcoScoreContext ecoScoreContext = new EcoScoreContext();

		ecoScoreContext.setEcoScore(jsonValue.getInt("ecoScore"));
		ecoScoreContext.setScoreClass(jsonValue.getString("scoreClass"));
		ecoScoreContext.setAcvScore(jsonValue.getInt("acvScore"));
		ecoScoreContext.setClaimBonus(jsonValue.getInt("claimBonus"));
		ecoScoreContext.setTransportScore(jsonValue.getInt("transportScore"));
		ecoScoreContext.setPoliticalScore(jsonValue.getInt("politicalScore"));
		ecoScoreContext.setPackagingMalus(jsonValue.getInt("packagingMalus"));
		
		return ecoScoreContext;

	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(ecoScore, scoreClass, acvScore, claimBonus, transportScore, politicalScore, packagingMalus);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		EcoScoreContext other = (EcoScoreContext) obj;
		return Objects.equals(ecoScore, other.ecoScore) && Objects.equals(scoreClass, other.scoreClass) && Objects.equals(acvScore, other.acvScore)
				&& Objects.equals(claimBonus, other.claimBonus) && Objects.equals(transportScore, other.transportScore)
				&& Objects.equals(politicalScore, other.politicalScore) && Objects.equals(packagingMalus, other.packagingMalus);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "EcoScoreContext [ecoScore=" + ecoScore + ", scoreClass=" + scoreClass + ", acvScore=" + acvScore + ", claimBonus=" + claimBonus + ", transportScore="
				+ transportScore + ", politicalScore=" + politicalScore + ", packagingMalus=" + packagingMalus + "]";
	}
}
