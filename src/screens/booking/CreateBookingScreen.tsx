import * as Location from 'expo-location';
import React, { useCallback, useEffect, useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { extractErrorMessage } from '../../api/client';
import { bookingApi, vehicleApi } from '../../api/endpoints';
import { TripType, Vehicle } from '../../api/types';
import AppButton from '../../components/AppButton';
import AppTextField from '../../components/AppTextField';
import Chip from '../../components/Chip';
import SectionCard from '../../components/SectionCard';
import { useThemeColors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

const tripTypeOptions: TripType[] = ['HOURLY', 'FULL_DAY', 'OUTSTATION', 'CAB_TRIP'];

type Props = NativeStackScreenProps<RootStackParamList, 'CreateBooking'>;

export default function CreateBookingScreen({ route, navigation }: Props) {
  const { driverId, serviceType } = route.params;
  const colors = useThemeColors();
  const needsVehicle = serviceType === 'WITHOUT_CAR';

  const [lat, setLat] = useState<number | null>(null);
  const [lon, setLon] = useState<number | null>(null);
  const [locationMessage, setLocationMessage] = useState<string | null>(null);
  const [tripType, setTripType] = useState<TripType>(tripTypeOptions[0]);
  const [pickupAddress, setPickupAddress] = useState('');
  const [dropAddress, setDropAddress] = useState('');
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState<string | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') {
        setLocationMessage('Location permission is required to book.');
        return;
      }
      const loc = await Location.getLastKnownPositionAsync();
      if (!loc) {
        setLocationMessage('No location available. Enable GPS and try again.');
        return;
      }
      setLat(loc.coords.latitude);
      setLon(loc.coords.longitude);
    })();
  }, []);

  const loadVehicles = useCallback(async () => {
    if (!needsVehicle) return;
    try {
      const data = await vehicleApi.listVehicles('CUSTOMER');
      setVehicles(data);
    } catch {
      // Non-fatal: the picker just stays empty and the user can retry via "Manage my cars".
    }
  }, [needsVehicle]);

  useFocusEffect(
    useCallback(() => {
      loadVehicles();
    }, [loadVehicles])
  );

  async function createBooking() {
    if (lat == null || lon == null) return;
    setIsCreating(true);
    setCreateError(null);
    try {
      const booking = await bookingApi.create(
        driverId,
        serviceType,
        tripType,
        lat,
        lon,
        pickupAddress.trim() || null,
        null,
        null,
        dropAddress.trim() || null,
        selectedVehicleId
      );
      navigation.reset({
        index: 1,
        routes: [{ name: 'Home' }, { name: 'BookingDetail', params: { bookingId: booking.id } }],
      });
    } catch (err) {
      setCreateError(extractErrorMessage(err));
    } finally {
      setIsCreating(false);
    }
  }

  return (
    <ScrollView style={{ backgroundColor: colors.background }} contentContainerStyle={styles.content}>
      <SectionCard>
        {lat != null && lon != null && (
          <Text style={[styles.hint, { color: colors.onSurfaceVariant }]}>
            Pickup: {lat.toFixed(4)}, {lon.toFixed(4)} (your current location)
          </Text>
        )}
        {locationMessage && <Text style={[styles.error, { color: colors.error }]}>{locationMessage}</Text>}

        <Text style={[styles.label, { color: colors.onSurface }]}>Trip type</Text>
        <View style={styles.chipRow}>
          {tripTypeOptions.map((option) => (
            <Chip key={option} label={option} selected={tripType === option} onPress={() => setTripType(option)} />
          ))}
        </View>

        <AppTextField label="Pickup address (optional)" value={pickupAddress} onChangeText={setPickupAddress} />
        <AppTextField label="Drop address (optional)" value={dropAddress} onChangeText={setDropAddress} />

        {needsVehicle && (
          <>
            <Text style={[styles.label, { color: colors.onSurface }]}>Your car</Text>
            {vehicles.length === 0 ? (
              <Text style={[styles.hint, { color: colors.onSurfaceVariant, marginTop: 4 }]}>No car registered yet.</Text>
            ) : (
              <View style={styles.chipRow}>
                {vehicles.map((vehicle) => (
                  <Chip
                    key={vehicle.id}
                    label={`${vehicle.make} ${vehicle.model}`}
                    selected={selectedVehicleId === vehicle.id}
                    onPress={() => setSelectedVehicleId(vehicle.id)}
                  />
                ))}
              </View>
            )}
            <View style={styles.spacingSmall}>
              <AppButton label="Manage my cars" variant="text" onPress={() => navigation.navigate('MyVehicles')} />
            </View>
          </>
        )}

        {createError && <Text style={[styles.error, { color: colors.error }]}>{createError}</Text>}

        <View style={styles.spacingTop}>
          <AppButton
            label="Book this driver"
            onPress={createBooking}
            loading={isCreating}
            disabled={lat == null || lon == null}
          />
        </View>
      </SectionCard>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20 },
  hint: { fontSize: 13 },
  label: { fontSize: 13, fontWeight: '600', marginTop: 16 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', marginTop: 8 },
  error: { marginTop: 8 },
  spacingTop: { marginTop: 16 },
  spacingSmall: { marginTop: 4 },
});
