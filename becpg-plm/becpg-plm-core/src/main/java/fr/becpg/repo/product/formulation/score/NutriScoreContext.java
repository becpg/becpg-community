package fr.becpg.repo.product.formulation.score;

import java.util.EnumMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

public class NutriScoreContext {

	public enum NutriScorePart {
		CLASS, ENERGY, SAT_FAT, TOTAL_FAT, TOTAL_SUGAR, SODIUM, FRUITS_AND_VEG, NSP_FIBRE, AOAC_FIBRE, PROTEIN
	}

	private Map<NutriScorePart, Object[]> nutriScoreParts = new EnumMap<>(NutriScorePart.class);

	private int nutriScore;

	@Override
	public String toString() {

		int negativePart = 0;
		int positivePart = 0;

		StringBuilder sb = new StringBuilder();

		sb.append(I18NUtil.getMessage("nutriscore.display.negative"));
		sb.append("\n");

		negativePart += appendPart(sb, NutriScorePart.ENERGY, "nutriscore.display.energy");
		negativePart += appendPart(sb, NutriScorePart.SAT_FAT, "nutriscore.display.satfat");
		negativePart += appendPart(sb, NutriScorePart.TOTAL_FAT, "nutriscore.display.totalfat");
		negativePart += appendPart(sb, NutriScorePart.TOTAL_SUGAR, "nutriscore.display.totalsugar");
		negativePart += appendPart(sb, NutriScorePart.SODIUM, "nutriscore.display.sodium");

		sb.append("\n");
		sb.append(I18NUtil.getMessage("nutriscore.display.positive"));
		sb.append("\n");

		positivePart += appendPart(sb, NutriScorePart.PROTEIN, "nutriscore.display.protein");
		positivePart += appendPart(sb, NutriScorePart.FRUITS_AND_VEG, "nutriscore.display.percfruitsandveg");
		positivePart += appendPart(sb, NutriScorePart.NSP_FIBRE, "nutriscore.display.nspfibre");
		positivePart += appendPart(sb, NutriScorePart.AOAC_FIBRE, "nutriscore.display.aoacfibre");

		sb.append("\n");
		sb.append(I18NUtil.getMessage("nutriscore.display.finalScore", negativePart, positivePart, nutriScore));
		sb.append("\n");
		sb.append("\n");
		appendPart(sb, NutriScorePart.CLASS, "nutriscore.display.class");
		
		return sb.toString();
	}
	
	public JSONObject getJsonValue() {
		
		JSONObject parts = new JSONObject();
		
		parts.put("score", nutriScore);
		parts.put("class", getJsonValue(NutriScorePart.CLASS));
		parts.put("energy", getJsonValue(NutriScorePart.ENERGY));
		parts.put("satFat", getJsonValue(NutriScorePart.SAT_FAT));
		parts.put("totalFat", getJsonValue(NutriScorePart.TOTAL_FAT));
		parts.put("totalSugar", getJsonValue(NutriScorePart.TOTAL_SUGAR));
		parts.put("sodium", getJsonValue(NutriScorePart.SODIUM));
		parts.put("percFruitsAndVetgs", getJsonValue(NutriScorePart.FRUITS_AND_VEG));
		parts.put("nspFibre", getJsonValue(NutriScorePart.NSP_FIBRE));
		parts.put("aoacFibre", getJsonValue(NutriScorePart.AOAC_FIBRE));
		parts.put("protein", getJsonValue(NutriScorePart.PROTEIN));

		return parts;
	}

	private Object getJsonValue(NutriScorePart part) {
		JSONObject jsonPart = new JSONObject();
		
		if (getNutriScoreParts().get(part) != null) {
			jsonPart.put("lower", getNutriScoreParts().get(part)[0]);
			jsonPart.put("value", getNutriScoreParts().get(part)[1]);
			jsonPart.put("upper", getNutriScoreParts().get(part)[2]);
			jsonPart.put("result", getNutriScoreParts().get(part)[3]);
		}

		return jsonPart;
	}
	
	private int appendPart(StringBuilder sb, NutriScorePart part, String messageKey) {

		if (nutriScoreParts.get(part) != null) {
			sb.append(I18NUtil.getMessage(messageKey, nutriScoreParts.get(part)));
			sb.append("\n");

			if (nutriScoreParts.get(part)[3] instanceof Integer) {
				return (int) nutriScoreParts.get(part)[3];
			}
		}

		return 0;
	}

	public void setNutriScore(int nutriScore) {
		this.nutriScore = nutriScore;
	}

	public Map<NutriScorePart, Object[]> getNutriScoreParts() {
		return nutriScoreParts;
	}
}
