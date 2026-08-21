import React from 'react';
import VehiclesForm from './VehiclesForm';

export default function DriverVehiclesScreen() {
  return (
    <VehiclesForm
      ownerType="DRIVER"
      title="Vehicles"
      subtitle="Register the vehicle you'll drive so customers know what to expect."
      addButtonLabel="Add vehicle"
      listTitle="Your vehicles"
      emptyListLabel="No vehicles added yet."
    />
  );
}
