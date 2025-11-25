function getTypes() {
    var myConfig = new XML(config.script);
    var types = [];
    for each (var xmlType in myConfig..type) {
        types.push({
            name: xmlType.@name.toString(),
            states: xmlType.@states.toString()
        });
    }
    return types;
}

function getStates() {
    var myConfig = new XML(config.script);
    var states = [];
    for each (var xmlState in myConfig..state) {
        states.push({
            value: xmlState.@value.toString(),
            label: xmlState.@label.toString()
        });
    }
    return states;
}

model.types = getTypes();
model.states = getStates();
