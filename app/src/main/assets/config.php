<?php
	
	$host='localhost';
	$dbuser='app_shoot';
	$password='QWERasd123$';
	$dbname='shoot_db';

	$link = mysql_connect($host,$dbuser,$password);
	
	// Check connection
	if (!$link) {
		echo "Failed to connect to MySQL: " . mysql_error();
	}
	//setup db connection
	mysql_select_db($dbname);
?>