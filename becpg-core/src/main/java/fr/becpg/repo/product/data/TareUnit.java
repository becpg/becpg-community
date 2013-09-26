package fr.becpg.repo.product.data;

public enum TareUnit {
	g,
	kg,
	gPerm2;
	
	public static TareUnit parse(String value){
		
		if(value != null){
			if(value.equals("g")){
				return TareUnit.g;
			}
			else if(value.equals("kg")){
				return TareUnit.kg;
			}
			else if(value.equals("g/m2")){
				return TareUnit.gPerm2;
			}			
		}
		return TareUnit.kg;
	}
}
