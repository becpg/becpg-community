<?php

$instances = array(
    "becpg.fr" => "http://nas.leslaborie.org:8080/share"
);

$domain = $_GET["domain"];
$from = $_GET["from"];

if($from == "google" && $instances[$domain]){
	header("Location: $instances[$domain]"); 
	exit;
} else {
 ?>
<html>
<head>
 <title>www.becpg.fr | le PLM de l'agroalimentaire et de la cosmétique</title>
</head>
<body>
	The domain as not been created is disable or doesn't exist. Please contact support@becpg.fr or retry in a moment. 
</body>
</html>
</ 
<?php
}
?>
