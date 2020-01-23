//https://dev.becpg.fr/share/service/module/entity-datagrid/config/columns
//?itemType=bcpg%3AcompoList&list=compoList&siteId=snellman-translations-demo


// type	property
// name	bcpg:variantIds
// formsName	prop_bcpg_variantIds
// label	hidden
// mandatory	false
// readOnly	false
// protectedField	false
// repeating	true
// dataType	noderef

// type	association
// name	bcpg:compoListProduct
// formsName	assoc_bcpg_compoListProduct
// label	Produit
// readOnly	false
// protectedField	false
// dataType	bcpg:product


// type	property
// name	bcpg:compoListUnit
// formsName	prop_bcpg_compoListUnit
// label	Unité
// mandatory	true
// readOnly	false
// protectedField	false
// constraints	[…]
// 0	{…}
// type	LIST
// parameters	{…}
// repeating	false
// dataType	text

export class EntityListColumn {
   type: string; // association/property/dataLists/entity
   name: string;
   fieldName: string;
   label: string; 
   dataType: string;
   hidden: boolean;
   sortable: boolean;
   options? : any[];
   required: boolean;
}