function formattedDate() {
    var d = new Date(),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [day, month, year].join('/');
}

var descr = document.properties["cm:description"] == null ? "" : document.properties["cm:description"];

if(descr != ""){
	descr += "; ";
}

descr += (document.parent.properties["cm:title"] == "" ? "Validé" : document.parent.properties["cm:title"]) + " par: " + person.properties["cm:userName"] + ", le: " + formattedDate();

document.properties["cm:description"] = descr;
document.save();