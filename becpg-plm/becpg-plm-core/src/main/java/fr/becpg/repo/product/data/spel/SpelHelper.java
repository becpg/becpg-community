/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
package fr.becpg.repo.product.data.spel;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpelHelper {

	static class SpelShortcut {
		public Pattern pattern;
		public String replacement;

		public SpelShortcut(String pattern, String replacement) {
			super();
			this.pattern = Pattern.compile(pattern);
			this.replacement = replacement;
		}

	}

	private static List<SpelShortcut> shortCuts = new LinkedList<>();

	static {
		shortCuts.add(new SpelShortcut("cost\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]"    , "costList.^[cost.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("nut\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]"     , "nutList.^[nut.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("allergen\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "allergenList.^[allergen.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("ing\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]"     , "ingList.^[ing.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("organo\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]"  , "organoList.^[organo.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("physico\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]"  ,"physicoChemList.^[physicoChem.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("microbio\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]" , "microbioList.^[microBio.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("compo\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]"    , "compoListView.compoList.^[product.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("process\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]" ,"processListView.processList.^[resource.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("resParam\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "resourceParamList.^[param.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("pack\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",	"packagingListView.packagingList.^[product.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("compoVar\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]","compoListView.dynamicCharactList.^[title == '$1']?.value"));
		shortCuts.add(new SpelShortcut("packVar\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]","packagingListView.dynamicCharactList.^[title == '$1']?.value"));
		shortCuts.add(new SpelShortcut("processVar\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]","processListView.dynamicCharactList.^[title == '$1']?.value"));
		shortCuts.add(new SpelShortcut("labelClaim\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "labelClaimList.^[labelClaim.toString() == '$1']"));
		shortCuts.add(new SpelShortcut("labeling\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",	"labelingListView.ingLabelingList.^[grp.toString() == '$1']"));

	}
	
	
	public static Pattern formulaVarPattern = Pattern.compile("^var\\s+(\\w+)\\s*=(.*)$");

	public static String formatFormula(String formula) {
		
		for(SpelShortcut shortCut : shortCuts) {
			 Matcher matcher = shortCut.pattern.matcher(formula);
			 formula = matcher.replaceAll(shortCut.replacement);
		}
		
		return formula.replace("&lt;", "<").replace("&gt;", ">").replace("\n", "").trim();
	}

	public static String[] formatMTFormulas(String formula) {
		return formatFormula(formula).split(";");
	}

}
