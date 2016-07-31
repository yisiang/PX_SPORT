
<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
    <head>
        <title>TODO supply a title</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
         <style>
             body, html
             {
                 width: 100%;
                 height: 100%;
             }
             #showMap
             {
                 width: 100%;
                 height: 100%;
             }
         </style>
    </head>
    <body>
        <div id='showMap'></div>
        <script>
            var map, marker, bikeLayer;
            var poly = [];
            var total = 0;
            var style = [{"featureType":"all","elementType":"labels.text.fill","stylers":[{"color":"#ffffff"}]},{"featureType":"all","elementType":"labels.text.stroke","stylers":[{"color":"#000000"},{"lightness":13}]},{"featureType":"administrative","elementType":"geometry.fill","stylers":[{"color":"#000000"}]},{"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#144b53"},{"lightness":14},{"weight":1.4}]},{"featureType":"landscape","elementType":"all","stylers":[{"color":"#08304b"}]},{"featureType":"poi","elementType":"geometry","stylers":[{"color":"#0c4152"},{"lightness":5}]},{"featureType":"road.highway","elementType":"geometry.fill","stylers":[{"color":"#000000"}]},{"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#0b434f"},{"lightness":25}]},{"featureType":"road.arterial","elementType":"geometry.fill","stylers":[{"color":"#000000"}]},{"featureType":"road.arterial","elementType":"geometry.stroke","stylers":[{"color":"#0b3d51"},{"lightness":16}]},{"featureType":"road.local","elementType":"geometry","stylers":[{"color":"#000000"}]},{"featureType":"transit","elementType":"all","stylers":[{"color":"#146474"}]},{"featureType":"water","elementType":"all","stylers":[{"color":"#021019"}]}];

            function initMap()
            {
                var myLatLng = {lat: 25.047755664726715, lng: 121.51703953742981};
                map = new google.maps.Map(
                    document.getElementById("showMap"),
                    {
                        center: myLatLng,
                        zoom: 15,
                        // ROADMAP : normal, default 2D map
                        mapTypeId: google.maps.MapTypeId.ROADMAP,
                        styles: style,
                        disableDefaultUI: true
                    }
                );
                image = './bike.png'
                marker = new google.maps.Marker({
                    position: myLatLng,
                    map: map,
                    icon: image
                });
                bikeLayer = new google.maps.BicyclingLayer();
                bikeLayer.setMap(map);
            }

            //取得使用者位置
            function userLocation(newLat, newLng)
            {
                var center = {lat: newLat, lng: newLng};
                map.panTo(center);
                marker.setPosition(center);
                poly.push(center);

                //繪製路徑
                var flightPath = new google.maps.Polyline({
                    path: poly,
                    geodesic: false,
                    strokeColor: '#FF0000',
                    strokeOpacity: 1.0,
                    strokeWeight: 7
                });
                flightPath.setMap(map);

                //計算總公里數
                var directionsService = new google.maps.DirectionsService();
                var polyLength = poly.length;
                if (polyLength > 1)
                {
                    var request = {
                        origin : poly[ polyLength-2 ],
                        destination : poly[ polyLength-1 ],
                        travelMode : google.maps.TravelMode.WALKING
                    };

                    directionsService.route(request, function(response, status) {
                        if (status == google.maps.DirectionsStatus.OK) {
                            total += response.routes[0].legs[0].distance.value;
                            sport.setTotalDistance( total );
                        }
                    });
                }

            }

        </script>
        <script src="https://maps.googleapis.com/maps/api/js?v=3.exp&key=AIzaSyDlrc-l8e3vipTuWZ5bnmk7u1Ax6qqd8SQ&callback=initMap"></script>
    </body>
</html>
