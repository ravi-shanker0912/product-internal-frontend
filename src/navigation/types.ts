export type RootStackParamList = {
  PhoneEntry: undefined;
  OtpEntry: { phone: string };
  Home: undefined;
  Profile: undefined;
  Driver: undefined;
  DriverDocuments: undefined;
  DriverVehicles: undefined;
  Search: undefined;
  CreateBooking: { driverId: string; serviceType: 'WITH_CAR' | 'WITHOUT_CAR' };
  MyVehicles: undefined;
  Bookings: undefined;
  BookingDetail: { bookingId: string };
};
