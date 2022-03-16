package fr.becpg.repo.product.formulation.nutrient;

/**
 * <p>NutrientCode interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface NutrientCode {

	/** Constant <code>Energykcal="ENER-E14"</code> */
	String Energykcal = "ENER-E14";
	/** Constant <code>EnergykJ="ENER-KJO"</code> */
	String EnergykJ = "ENER-KJO";
	/** Constant <code>EnergykcalUS="US_ENER-E14"</code> */
	String EnergykcalUS = "US_ENER-E14";
	/** Constant <code>Fat="FAT"</code> */
	String Fat = "FAT";
	/** Constant <code>FatSaturated="FASAT"</code> */
	String FatSaturated = "FASAT";
	/** Constant <code>FatMonounsaturated="FAMSCIS"</code> */
	String FatMonounsaturated = "FAMSCIS";
	/** Constant <code>FatPolyunsaturated="FAPUCIS"</code> */
	String FatPolyunsaturated = "FAPUCIS";
	/** Constant <code>FatTrans="FATRN"</code> */
	String FatTrans = "FATRN";
	/** Constant <code>FatOmega3="FAPUN3F"</code> */
	String FatOmega3 = "FAPUN3F";
	/** Constant <code>FatOmega6="FAPUN6F"</code> */
	String FatOmega6 = "FAPUN6F";
	/** Constant <code>Protein="PRO-"</code> */
	String Protein = "PRO-";
	/** Constant <code>CarbohydrateByDiff="CHOAVL"</code> */
	String CarbohydrateByDiff = "CHOAVL";
	/** Constant <code>CarbohydrateWithFiber="CHO-"</code> */
	String CarbohydrateWithFiber = "CHO-";
	/** Constant <code>Sugar="SUGAR"</code> */
	String Sugar = "SUGAR";
	/** Constant <code>FiberDietary="FIBTG"</code> */
	String FiberDietary = "FIBTG";	
	/** Constant <code>Cholesterol="CHOL-"</code> */
	String Cholesterol = "CHOL-";
	/** Constant <code>FiberSoluble="FIBSOL"</code> */
	String FiberSoluble = "FIBSOL";
	/** Constant <code>FiberInsoluble="FIBINS"</code> */
	String FiberInsoluble = "FIBINS";
	/** Constant <code>Polyols="POLYL"</code> */
	String Polyols = "POLYL";
	/** Constant <code>SugarAdded="SUGAD"</code> */
	String SugarAdded = "SUGAD";
	
	//Vitamin
	/** Constant <code>FolicAcid="FOLAC"</code> */
	String FolicAcid = "FOLAC";
	/** Constant <code>VitA="VITA-"</code> */
	String VitA = "VITA-";
	/** Constant <code>VitC="VITC-"</code> */
	String VitC = "VITC-";
	/** Constant <code>VitD="VITD-"</code> */
	String VitD = "VITD-";
	/** Constant <code>VitE="VITE-"</code> */
	String VitE = "VITE-";
	/** Constant <code>VitE="VITE-"</code> */
	String VitTocpha = "TOCPHA";
	/** Constant <code>VitK1="VITK1"</code> */
	String VitK1 = "VITK1";
	/** Constant <code>VitK2="VITK2"</code> */
	String VitK2 = "VITK2";
	/** Constant <code>VitB1="VITB1"</code> */
	String VitB1 = "VITB1";
	/** Constant <code>VitB2="VITB2"</code> */
	String VitB2 = "VITB2";
	/** Constant <code>VitB3="VITB3"</code> */
	String VitB3 = "VITB3"; // Nicotinic acid
	/** Constant <code>PantoAcid="PANTAC"</code> */
	String PantoAcid = "PANTAC";
	/** Constant <code>VitB6="VITB6-"</code> */
	String VitB6 = "VITB6-";
	/** Constant <code>VitB12="VITB12"</code> */
	String VitB12 = "VITB12";
	/** Constant <code>Thiamin="THIA"</code> */
	String Thiamin = "THIA";
	/** Constant <code>Riboflavin="RIBF"</code> */
	String Riboflavin = "RIBF";
	/** Constant <code>Niacin="NIA"</code> */
	String Niacin = "NIA";
	/** Constant <code>Folate="FOL"</code> */
	String Folate  = "FOL";
	/** Constant <code>FolateDFE="FOLDFE"</code> */
	String FolateDFE  = "FOLDFE";
	/** Constant <code>Biotin="BIOT"</code> */
	String Biotin = "BIOT";
	/** Constant <code>Choline="CHOLN"</code> */
	String Choline = "CHOLN";
	/** Constant <code>Retinol="RETOL"</code> */
	String Retinol = "RETOL";
	/** Constant <code>BetaCarotene="CARTB"</code> */
	String BetaCarotene = "CARTB";
	/** Constant <code>BetaCrypt="CRYPX"</code> */
	String BetaCrypt = "CRYPX";
	/** Constant <code>Lycopene="LYCPN"</code> */
	String Lycopene = "LYCPN";
	/** Constant <code>AlphaCarot="CARTA"</code> */
	String AlphaCarot = "CARTA";
	/** Constant <code>ProvitaminA="CARTBEQ"</code> */
	String ProvitaminA = "CARTBEQ";
	
	//Minerals
	/** Constant <code>Calcium="CA"</code> */
	String Calcium = "CA";
	/** Constant <code>Sodium="NA"</code> */
	String Sodium = "NA";
	/** Constant <code>Potassium="K"</code> */
	String Potassium = "K";
	/** Constant <code>Iron="FE"</code> */
	String Iron = "FE";	
	/** Constant <code>Salt="NACL"</code> */
	String Salt = "NACL";
	/** Constant <code>Copper="CU"</code> */
	String Copper = "CU";
	/** Constant <code>Phosphorus="P"</code> */
	String Phosphorus = "P";
	/** Constant <code>Magnesium="MG"</code> */
	String Magnesium = "MG";
	/** Constant <code>Zinc="ZN"</code> */
	String Zinc = "ZN";
	/** Constant <code>Iodine="ID"</code> */
	String Iodine = "ID";
	/** Constant <code>Selenium="SE"</code> */
	String Selenium = "SE";
	/** Constant <code>Fluoride="FD"</code> */
	String Fluoride = "FD"; // Fluoride, Fluorine
	/** Constant <code>Manganese="MN"</code> */
	String Manganese = "MN";
	/** Constant <code>Chromium="CR"</code> */
	String Chromium  = "CR";
	/** Constant <code>Starch="STARCH"</code> */
	String Starch = "STARCH";
	/** Constant <code>Molybdenum="MO"</code> */
	String Molybdenum = "MO";
	/** Constant <code>Chloride="CLD"</code> */
	String Chloride = "CLD";
}
