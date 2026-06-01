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

type LatLng = {
  lat: number;
  lng: number;
};

interface Props {
  origen?: LatLng;
  destino?: LatLng;
}
export default function MapView({ origen, destino }: Props) {
  const [directions, setDirections] = useState<any>(null);

  useEffect(() => {
    if (!origen || !destino || !window.google) return;

    const directionsService =
      new window.google.maps.DirectionsService();

    directionsService.route(
      {
        origin: origen,
        destination: destino,
        travelMode: window.google.maps.TravelMode.DRIVING,
      },
      (result, status) => {
        console.log("Directions status:", status);

        if (status === "OK" && result) {
          setDirections(result);
        }
      }
    );
  }, [origen, destino]);

  return (
    <GoogleMap
      mapContainerStyle={containerStyle}
      center={origen || { lat: -38.0055, lng: -57.5426 }}
      zoom={13}
    >
      {origen && <Marker position={origen} />}

      {destino && <Marker position={destino} />}

      {directions && (
        <DirectionsRenderer directions={directions} />
      )}
    </GoogleMap>
  );
}