import React from 'react';
import VehiclesForm from './VehiclesForm';

export default function MyVehiclesScreen() {
  return (
    <VehiclesForm
      ownerType="CUSTOMER"
      title="My car"
      subtitle="Register your car so a driver knows what they're driving."
      addButtonLabel="Add car"
      listTitle="Your cars"
      emptyListLabel="No cars added yet."
    />
  );
}
