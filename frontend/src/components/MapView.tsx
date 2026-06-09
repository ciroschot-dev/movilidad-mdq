import {
  GoogleMap,
  Marker,
  DirectionsRenderer,
} from "@react-google-maps/api";

import { useEffect, useState } from "react";

const containerStyle = {
  width: "100%",
  height: "300px",
  borderRadius: "24px",
};

const darkMapStyle = [
  { elementType: "geometry", stylers: [{ color: "#242f3e" }] },
  { elementType: "labels.text.stroke", stylers: [{ color: "#242f3e" }] },
  { elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
  {
    featureType: "administrative.locality",
    elementType: "labels.text.fill",
    stylers: [{ color: "#d59563" }],
  },
  {
    featureType: "poi",
    elementType: "labels.text.fill",
    stylers: [{ color: "#d59563" }],
  },
  {
    featureType: "poi.park",
    elementType: "geometry",
    stylers: [{ color: "#263c3f" }],
  },
  {
    featureType: "poi.park",
    elementType: "labels.text.fill",
    stylers: [{ color: "#6b9a76" }],
  },
  {
    featureType: "road",
    elementType: "geometry",
    stylers: [{ color: "#38414e" }],
  },
  {
    featureType: "road",
    elementType: "geometry.stroke",
    stylers: [{ color: "#212a37" }],
  },
  {
    featureType: "road",
    elementType: "labels.text.fill",
    stylers: [{ color: "#9ca5b3" }],
  },
  {
    featureType: "road.highway",
    elementType: "geometry",
    stylers: [{ color: "#746855" }],
  },
  {
    featureType: "road.highway",
    elementType: "geometry.stroke",
    stylers: [{ color: "#1f2835" }],
  },
  {
    featureType: "road.highway",
    elementType: "labels.text.fill",
    stylers: [{ color: "#f3d19c" }],
  },
  {
    featureType: "transit",
    elementType: "geometry",
    stylers: [{ color: "#2f3948" }],
  },
  {
    featureType: "transit.station",
    elementType: "labels.text.fill",
    stylers: [{ color: "#d59563" }],
  },
  {
    featureType: "water",
    elementType: "geometry",
    stylers: [{ color: "#17263c" }],
  },
  {
    featureType: "water",
    elementType: "labels.text.fill",
    stylers: [{ color: "#515c6d" }],
  },
  {
    featureType: "water",
    elementType: "labels.text.stroke",
    stylers: [{ color: "#17263c" }],
  },
];

type LatLng = {
  lat: number;
  lng: number;
};

interface Props {
  origen?: LatLng;
  destino?: LatLng;
}
export default function MapView({ origen, destino }: Props) {
  const [directions, setDirections] = useState<google.maps.DirectionsResult | null>(null);
  const [isDarkMode, setIsDarkMode] = useState(false);

  useEffect(() => {
    const observer = new MutationObserver(() => {
      setIsDarkMode(document.documentElement.classList.contains('dark'));
    });
    observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
    setIsDarkMode(document.documentElement.classList.contains('dark'));
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!origen || !destino || !window.google) {
      setDirections(null);
      return;
    }

    const directionsService =
      new window.google.maps.DirectionsService();

    directionsService.route(
      {
        origin: origen,
        destination: destino,
        travelMode: window.google.maps.TravelMode.DRIVING,
      },
      (result, status) => {
        if (status === "OK" && result) {
          setDirections(result);
        } else {
          console.error("Error en DirectionsService:", status, result);
        }
      }
    );
  }, [origen, destino, window.google]);

  return (
    <GoogleMap
      mapContainerStyle={containerStyle}
      center={origen || { lat: -38.0055, lng: -57.5426 }}
      zoom={13}
      options={{
        styles: isDarkMode ? darkMapStyle : [],
        disableDefaultUI: true,
        zoomControl: true,
      }}
    >
      {origen && <Marker position={origen} />}

      {destino && <Marker position={destino} />}

      {directions && (
        <DirectionsRenderer 
          directions={directions} 
          options={{
            suppressMarkers: true,
            polylineOptions: {
              strokeColor: isDarkMode ? "#ffffff" : "#000000",
              strokeWeight: 5,
            }
          }}
        />
      )}
    </GoogleMap>
  );
}