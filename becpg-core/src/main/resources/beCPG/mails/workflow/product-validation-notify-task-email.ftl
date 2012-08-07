Bonjour,

${person.properties["cm:firstName"]!""} ${person.properties["cm:lastName"]!""} vous a envoyé une demande de Validation Produit.

- code produit: ${document.children[0].properties["bcpg:code"]!""}
- nom du produit: ${document.children[0].properties["cm:name"]!""}

Cordialement,
beCPG