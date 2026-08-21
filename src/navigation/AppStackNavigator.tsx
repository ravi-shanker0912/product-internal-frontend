import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useThemeColors } from '../theme/colors';
import { RootStackParamList } from './types';
import HomeScreen from '../screens/home/HomeScreen';
import ProfileScreen from '../screens/profile/ProfileScreen';
import DriverScreen from '../screens/driver/DriverScreen';
import DriverDocumentsScreen from '../screens/driver/DriverDocumentsScreen';
import DriverVehiclesScreen from '../screens/vehicle/DriverVehiclesScreen';
import SearchScreen from '../screens/search/SearchScreen';
import CreateBookingScreen from '../screens/booking/CreateBookingScreen';
import MyVehiclesScreen from '../screens/vehicle/MyVehiclesScreen';
import BookingsScreen from '../screens/booking/BookingsScreen';
import BookingDetailScreen from '../screens/booking/BookingDetailScreen';

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AppStackNavigator() {
  const colors = useThemeColors();

  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: colors.primary },
        headerTintColor: colors.onPrimary,
        headerTitleStyle: { fontWeight: '700' },
      }}
    >
      <Stack.Screen name="Home" component={HomeScreen} options={{ headerShown: false }} />
      <Stack.Screen name="Profile" component={ProfileScreen} options={{ title: 'Profile' }} />
      <Stack.Screen name="Driver" component={DriverScreen} options={{ title: 'Driver' }} />
      <Stack.Screen
        name="DriverDocuments"
        component={DriverDocumentsScreen}
        options={{ title: 'Documents' }}
      />
      <Stack.Screen name="DriverVehicles" component={DriverVehiclesScreen} options={{ title: 'Vehicles' }} />
      <Stack.Screen name="Search" component={SearchScreen} options={{ title: 'Find a driver' }} />
      <Stack.Screen
        name="CreateBooking"
        component={CreateBookingScreen}
        options={{ title: 'Confirm booking' }}
      />
      <Stack.Screen name="MyVehicles" component={MyVehiclesScreen} options={{ title: 'My car' }} />
      <Stack.Screen name="Bookings" component={BookingsScreen} options={{ title: 'My bookings' }} />
      <Stack.Screen
        name="BookingDetail"
        component={BookingDetailScreen}
        options={{ title: 'Booking' }}
      />
    </Stack.Navigator>
  );
}
