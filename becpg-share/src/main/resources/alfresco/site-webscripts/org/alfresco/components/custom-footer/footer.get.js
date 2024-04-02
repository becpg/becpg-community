
/**
 * JS Controller for Share's footer component
 */

function main() {

	var basket = {
		id: "basket",
		name: "beCPG.component.Basket"
	};
	model.widgets = [basket];

	model.isAIEnable = (user!=null && user.capabilities["isAIUser"] != null && user.capabilities["isAIUser"] == true) || false;

	if (model.isAIEnable) {

		//https://gweb-cloud-chat-prod.appspot.com/chat/bot

		var watson = {
			id: "Watson",
			name: "beCPG.component.Watson"
		};
		model.widgets.push(watson);

	}
}

main();

