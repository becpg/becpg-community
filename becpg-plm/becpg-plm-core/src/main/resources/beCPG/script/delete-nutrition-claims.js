var labelClaims = search.luceneSearch("+TYPE:\"bcpg:labelClaim\" +@bcpg\\:labelClaimType:\"Nutritionnelle\"");
logger.log(labelClaims.length);


for (var i = 0; i < labelClaims.length; i++){
	var labelClaimLists = labelClaims[i].sourceAssocs["bcpg:lclLabelClaim"];
	logger.log("labelClaims " + labelClaims[i].properties["bcpg:charactName"]);
	if(labelClaimLists != null){
		for (var j = 0; j < labelClaimLists.length; j++){
			logger.log(labelClaimLists[j].type);
			labelClaimLists[j].remove();
		}
	}
	labelClaims[i].remove();
}