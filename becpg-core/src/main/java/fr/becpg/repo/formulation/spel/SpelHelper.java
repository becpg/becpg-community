/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.formulation.spel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>SpelHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SpelHelper {
	
	private SpelHelper() {
		super();
	}

	public static class SpelShortcut {
		Pattern pattern;
		String replacement;

		public SpelShortcut(String pattern, String replacement) {
			super();
			this.pattern = Pattern.compile(pattern);
			this.replacement = replacement;
		}

	}

	private static List<SpelShortcut> shortCuts = new ArrayList<>();


	/**
	 * <p>registerShortcut.</p>
	 *
	 * @param shortcut a {@link fr.becpg.repo.formulation.spel.SpelHelper.SpelShortcut} object.
	 */
	public static void registerShortcut(SpelShortcut shortcut) {
		shortCuts.add(shortcut);
	}
	

	
	/** Constant <code>formulaVarPattern</code> */
	public static final Pattern formulaVarPattern = Pattern.compile("^var\\s+(\\w+)\\s*=(.*)$");

	/**
	 * <p>formatFormula.</p>
	 *
	 * @param formula a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String formatFormula(String formula) {
		
		for(SpelShortcut shortCut : shortCuts) {
			
			 
			 Matcher matcher = shortCut.pattern.matcher(formula);
			 formula = matcher.replaceAll(shortCut.replacement);
		}
		
		return formula.replace("&lt;", "<").replace("&gt;", ">").replace("\n", "").trim();
	}

	/**
	 * <p>formatMTFormulas.</p>
	 *
	 * @param formula a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.String} objects.
	 */
	public static String[] formatMTFormulas(String formula) {
		return formatFormula(formula).split(";");
	}

}
