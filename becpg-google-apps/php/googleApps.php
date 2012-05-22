<?php

$instances = array(
    "becpg.fr" => "http://nas.leslaborie.org:8080/share"
);
$domain = $_GET["domain"];

if($instances[$domain]){
	$location = $instances[$domain];
	if($_GET["admin"]){
		$location = "$location/page/console/admin-console";
	}
	header("Location: $location"); 
	exit;
} else {
	header("Location: https://becpg.fr/fr/google/setup"); 
	exit;
}
?>
