package fr.becpg.repo.product.formulation.score;

import java.util.Objects;

import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

public class EcoScoreContext {
	
	private int ecoScore;
	private String scoreClass;
	private int acvScore;
	private int claimBonus;
	private int transportScore;
	private int politicalScore;
	private int packagingMalus;

	public int getEcoScore() {
		return ecoScore;
	}

	public void setEcoScore(int ecoScore) {
		this.ecoScore = ecoScore;
	}

	public String getScoreClass() {
		return scoreClass;
	}

	public void setScoreClass(String scoreClass) {
		this.scoreClass = scoreClass;
	}

	public int getAcvScore() {
		return acvScore;
	}

	public void setAcvScore(int acvScore) {
		this.acvScore = acvScore;
	}

	public int getClaimBonus() {
		return claimBonus;
	}

	public void setClaimBonus(int claimBonus) {
		this.claimBonus = claimBonus;
	}

	public int getTransportScore() {
		return transportScore;
	}

	public void setTransportScore(int transportScore) {
		this.transportScore = transportScore;
	}


	public int getPoliticalScore() {
		return politicalScore;
	}

	public void setPoliticalScore(int politicalScore) {
		this.politicalScore = politicalScore;
	}

	public int getPackagingMalus() {
		return packagingMalus;
	}

	public void setPackagingMalus(int packagingMalus) {
		this.packagingMalus = packagingMalus;
	}

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
	
	@Override
	public int hashCode() {
		return Objects.hash(ecoScore, scoreClass, acvScore, claimBonus, transportScore, politicalScore, packagingMalus);
	}

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

	@Override
	public String toString() {
		return "EcoScoreContext [ecoScore=" + ecoScore + ", scoreClass=" + scoreClass + ", acvScore=" + acvScore + ", claimBonus=" + claimBonus + ", transportScore="
				+ transportScore + ", politicalScore=" + politicalScore + ", packagingMalus=" + packagingMalus + "]";
	}
}
