<?php

	require("config.php");

	$year = $_REQUEST["year"];

	$rows = array();
	$query = "select * from shooting_tb where incident_date like \"" . $year . "%\" order by incident_date desc";
	$result = mysql_query($query);

	while ($row = @mysql_fetch_assoc($result)) {
		$rows[] = $row;
	}
	echo json_encode(array("error"=>false, "shoot"=>$rows));
?>