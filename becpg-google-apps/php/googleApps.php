<?php

$instances = array(
    "becpg.fr" => "https://inst3.becpg.fr/share"
);
$domain = $_GET["domain"];

if($instances[$domain]){
	$location = $instances[$domain];
	if($_GET["admin"]){
		$location = "$location/page/console/admin-console/application";
	}
	header("Location: $location"); 
	exit;
} else {
	header("Location: https://becpg.fr/fr/google/setup"); 
	exit;
}
?>
