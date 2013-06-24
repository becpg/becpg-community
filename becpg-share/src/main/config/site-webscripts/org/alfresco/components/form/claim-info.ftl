<div class="claim-info">Mémo pour ajouter une pièce jointe : soit par le bouton Ajouter, soit en envoyant un mail à YYYY-RC-XX@daunat.com</div>
<div class="claim-info">
	Si des photos du (des) produit(s) sont disponibles les transmettre par mail à : <a href="mailto:sabrina.fourel@daunat.com">sabrina.fourel@daunat.com</a> et <a href="mailto:lucie.bizec@daunat.com">lucie.bizec@daunat.com</a>
	<br/><br/>
	Si problématique corps étrangers transmettre impérativement le corps étranger par courrier à l’adresse suivante :<br/>
	<address>Daunat services (Qualité Groupe)<br/>
	ZI de Bellevue<br/>
	BP 30131<br/>
	22201 Guingamp</address>
	<br/>
</div>
<#list set.children as item>
         <#if item.kind == "set">
            <@renderSet set=item />
         <#else>
            <@renderField field=form.fields[item.id] />
         </#if>
</#list>
