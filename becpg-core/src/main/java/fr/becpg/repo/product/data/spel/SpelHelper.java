package fr.becpg.repo.product.data.spel;

public class SpelHelper {


	public static String formatFormula(String formula) {
		return formula.replace("&lt;", "<").replace("&gt;", ">");
	}
}
