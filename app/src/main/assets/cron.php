<?php

	require("config.php");

	$is_new = false;
	$is_update = false;

	$url = "http://www.gunviolencearchive.org/reports/mass-shooting";

	$t = get_remote_data($url);     // GET request 
	$html = strip_tags($t, "<table><tr><td><a>");

	$dom = new DomDocument();
	$dom->loadHTML($html);

	$xpath = new DOMXPath($dom);
	$rows = $xpath->query('//tr');

	$count = 0;
	foreach ($rows as $row) {
		$shoot = array();
		$cols = $row->getElementsByTagName('td');
		$i = 0;
		foreach ($cols as $col) {
			$value = $col->nodeValue;
			if ($i == 0) {
				$shoot["incident_date"] = date('Y-m-d', strtotime($value));
			}
			else if ($i == 1) {
				$shoot["state"] = $value;
			}
			else if ($i == 2) {
				$shoot["city"] = $value;
			}
			else if ($i == 3) {
				$shoot["address"] = $value;
			}
			else if ($i == 4) {
				$shoot["killed"] = $value;
			}
			else if ($i == 5) {
				$shoot["injured"] = $value;
			}

			echo 'value = ' . $col->nodeValue;
			if($xpath->evaluate('count(./a)', $col) > 0) { // check if an anchor exists
	//            echo ' | link = ' . $xpath->evaluate('string(./a/@href)', $col); // if there is, then echo the href value
				echo ' | link = ' . trim($col->getElementsByTagName('a')->item(0)->getAttribute('href')); echo "<br/>";
								
				$shoot["url1"] = trim($col->getElementsByTagName('a')->item(0)->getAttribute('href'));
				if( strpos( $col->nodeValue, "Source" ) !== false ) {
					echo ' | link = ' . trim($col->getElementsByTagName('a')->item(1)->getAttribute('href')); echo "<br/>";
					$shoot["url2"] = trim($col->getElementsByTagName('a')->item(1)->getAttribute('href'));
				}
				else {
					$shoot["url2"] = "";
				}

				$address = $shoot["address"].",".$shoot["city"].",".$shoot["state"];
				$val = explode(",", get_lat_long($address));
				$shoot["latitude"] = $val[0];
				$shoot["longitude"] = $val[1];
			}
			echo '<br/>';
			$i ++;
		}
		if ($i > 0 ) {
			$query = "select * from shooting_tb where incident_date=\"" . $shoot['incident_date'] 
														. "\" and state=\"" . $shoot['state'] 
														. "\" and city =\"" . $shoot['city'] 
														. "\" and address =\"" . $shoot['address'] 
														. "\" and url1=\"" . $shoot['url1'] . "\"";
			$result = mysql_query($query);
			if (@mysql_num_rows($result) == 0) {
				$query = "insert into shooting_tb (incident_date, state, city, address, killed, injured, url1, url2, latitude, longitude) values (\"" . $shoot['incident_date'] . "\","
																																				. "\"" . $shoot['state'] . "\","
																																				. "\"" . $shoot['city'] . "\","
																																				. "\"" . $shoot['address'] . "\","
																																				. "\"" . $shoot['killed'] . "\","
																																				. "\"" . $shoot['injured'] . "\","
																																				. "\"" . $shoot['url1'] . "\","
																																				. "\"" . $shoot['url2'] . "\","
																																				. "\"" . $shoot['latitude'] . "\","
																																				. "\"" . $shoot['longitude'] . "\")";
				mysql_query($query);
				$is_new = true;
			}
			else {
				$row = @mysql_fetch_object($result);
				if ($row->killed != $shoot['killed'] || $row->injured != $shoot['injured'] ) {
					$query = "update shooting_tb set killed =\"" . $shoot['killed'] . "\", injured=\"" . $shoot['injured'] . "\", state=\"" . $shoot['state'] . "\", city=\"" . $shoot['city'] . "\", address =\"" . $shoot['address'] . "\" where incident_date=\"" . $shoot['incident_date'] . "\" and url1=\"" . $shoot['url1'] . "\"";
					mysql_query($query);
					$is_update = true;
				}
			}
			$count ++;
			if ($count > 5) break;
		}
		echo "<br/>";
	}
	if ($is_new == true) {
		$data = array('new'=>true, 'message'=>'New Shooting');

		$target="/topics/shooter";
		echo sendMessage($data, $target);
	}
	else if ($is_update == true) {
		$data = array('new'=>false, 'message'=>'Update Shooting');

		$target="/topics/shooter";
//		echo sendMessage($data, $target);
	}

	function get_lat_long($address){

		$address = str_replace(" ", "+", $address);

		$json = file_get_contents("http://maps.google.com/maps/api/geocode/json?address=$address&sensor=false");
		$json = json_decode($json);

		$lat = $json->{'results'}[0]->{'geometry'}->{'location'}->{'lat'};
		$long = $json->{'results'}[0]->{'geometry'}->{'location'}->{'lng'};
		return $lat.','.$long;
	}

	function get_remote_data($url, $post_paramtrs = false) {
		$c = curl_init();
		curl_setopt($c, CURLOPT_URL, $url);
		curl_setopt($c, CURLOPT_RETURNTRANSFER, 1);
		if ($post_paramtrs) {
			curl_setopt($c, CURLOPT_POST, TRUE);
			curl_setopt($c, CURLOPT_POSTFIELDS, "var1=bla&" . $post_paramtrs);
		} curl_setopt($c, CURLOPT_SSL_VERIFYHOST, false);
		curl_setopt($c, CURLOPT_SSL_VERIFYPEER, false);
		curl_setopt($c, CURLOPT_USERAGENT, "Mozilla/5.0 (Windows NT 6.1; rv:33.0) Gecko/20100101 Firefox/33.0");
		curl_setopt($c, CURLOPT_COOKIE, 'CookieName1=Value;');
		curl_setopt($c, CURLOPT_MAXREDIRS, 10);
		$follow_allowed = ( ini_get('open_basedir') || ini_get('safe_mode')) ? false : true;
		if ($follow_allowed) {
			curl_setopt($c, CURLOPT_FOLLOWLOCATION, 1);
		}curl_setopt($c, CURLOPT_CONNECTTIMEOUT, 9);
		curl_setopt($c, CURLOPT_REFERER, $url);
		curl_setopt($c, CURLOPT_TIMEOUT, 60);
		curl_setopt($c, CURLOPT_AUTOREFERER, true);
		curl_setopt($c, CURLOPT_ENCODING, 'gzip,deflate');
		$data = curl_exec($c);
		$status = curl_getinfo($c);
		curl_close($c);

		preg_match('/(http(|s)):\/\/(.*?)\/(.*\/|)/si', $status['url'], $link);
		$data = preg_replace('/(src|href|action)=(\'|\")((?!(http|https|javascript:|\/\/|\/)).*?)(\'|\")/si', '$1=$2' . $link[0] . '$3$4$5', $data);
		$data = preg_replace('/(src|href|action)=(\'|\")((?!(http|https|javascript:|\/\/)).*?)(\'|\")/si', '$1=$2' . $link[1] . '://' . $link[3] . '$3$4$5', $data);

		if ($status['http_code'] == 200) {
			return $data;
		} elseif ($status['http_code'] == 301 || $status['http_code'] == 302) {
			if (!$follow_allowed) {
				if (empty($redirURL)) {
					if (!empty($status['redirect_url'])) {
						$redirURL = $status['redirect_url'];
					}
				} if (empty($redirURL)) {
					preg_match('/(Location:|URI:)(.*?)(\r|\n)/si', $data, $m);
					if (!empty($m[2])) {
						$redirURL = $m[2];
					}
				} if (empty($redirURL)) {
					preg_match('/href\=\"(.*?)\"(.*?)here\<\/a\>/si', $data, $m);
					if (!empty($m[1])) {
						$redirURL = $m[1];
					}
				} if (!empty($redirURL)) {
					$t = debug_backtrace();
					return call_user_func($t[0]["function"], trim($redirURL), $post_paramtrs);
				}
			}
		} return "ERRORCODE22 with $url!!<br/>Last status codes<b/>:" . json_encode($status) . "<br/><br/>Last data got<br/>:$data";
	}

	function sendMessage($data,$target){

		//FCM api URL

		$url = 'https://fcm.googleapis.com/fcm/send';
		//api_key available in Firebase Console -> Project Settings -> CLOUD MESSAGING -> Server key
		$server_key = 'AIzaSyCg0RcRjssZZag1hvVjsfIME0Ce-FqpMQA';					

		$fields = array();
		$fields['data'] = $data;
		if(is_array($target)){
			$fields['registration_ids'] = $target;
		}else{
			$fields['to'] = $target;
		}

		$fields['notification']=array('title'=>'Shooter Alert', 'body'=>'New Shooting 30 miles away tap for more info.', 'click_action'=>'MAIN_ACTIVITY');

		//header with content_type api key
		$headers = array(
			'Content-Type:application/json',
		  'Authorization:key='.$server_key
		);					

		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $url);
		curl_setopt($ch, CURLOPT_POST, true);
		curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
		curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

		$result = curl_exec($ch);
		if ($result === FALSE) {
			die('FCM Send Error: ' . curl_error($ch));
		}
		curl_close($ch);

		return $result;
	}

	mysql_close($link);
?>