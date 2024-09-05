
/**
 * JS Controller for Share's footer component
 */

function getBeCPGAuthTocken(user) {
	for (var i in user.capabilities) {
		if (i.indexOf("beCPGAuthTocken_") == 0) {
			return i.substring(16);
		}
	}
	return null;
}

function main() {

	var basket = {
		id: "basket",
		name: "beCPG.component.Basket"
	};
	model.widgets = [basket];

	model.isAIEnable = (user != null && user.capabilities["isAIUser"] != null && user.capabilities["isAIUser"] == true) || false;

	if (model.isAIEnable) {

		var watson = {
			id: "Watson",
			name: "beCPG.component.Watson",
			options: {
				ticket: getBeCPGAuthTocken(user),
				locale: locale
			}
		};
		model.widgets.push(watson);

	}
}

main();

