<import resource="classpath:/beCPG/rules/helpers.js">
var productNodeRefs = JSON.parse(formData).assoc_bcpg_entityTplRef_added;

function main() {
	if (isEmpty(productNodeRefs)) {
		throw i18n("plm.script.add-to-list.js.noproduct");
	}

	var refs = productNodeRefs.split(",");
	for (var i = 0; i < refs.length; i++) {
		addItemsToList(refs[i], "bcpg:compoList", "bcpg:compoListProduct", items);
	}

	return productNodeRefs;
}

main();
