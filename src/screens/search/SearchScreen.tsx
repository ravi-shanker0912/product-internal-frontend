import * as Location from 'expo-location';
import React, { useEffect, useState } from 'react';
import { ScrollView, StyleSheet, Switch, Text, View } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { extractErrorMessage } from '../../api/client';
import { searchApi } from '../../api/endpoints';
import { NearbyDriver, ServiceType } from '../../api/types';
import AppButton from '../../components/AppButton';
import Chip from '../../components/Chip';
import SectionCard from '../../components/SectionCard';
import { useThemeColors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

const serviceTypeOptions: ServiceType[] = ['WITH_CAR', 'WITHOUT_CAR'];

type Props = NativeStackScreenProps<RootStackParamList, 'Search'>;

export default function SearchScreen({ navigation }: Props) {
  const colors = useThemeColors();
  const [lat, setLat] = useState<number | null>(null);
  const [lon, setLon] = useState<number | null>(null);
  const [locationMessage, setLocationMessage] = useState<string | null>(null);
  const [serviceType, setServiceType] = useState<ServiceType>('WITH_CAR');
  const [automaticOnly, setAutomaticOnly] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [results, setResults] = useState<NearbyDriver[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') {
        setLocationMessage('Location permission is required to search.');
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

  async function search() {
    if (lat == null || lon == null) return;
    setIsSearching(true);
    setErrorMessage(null);
    try {
      const data = await searchApi.searchDrivers(lat, lon, serviceType, automaticOnly);
      setResults(data);
      setHasSearched(true);
    } catch (err) {
      setErrorMessage(extractErrorMessage(err));
    } finally {
      setIsSearching(false);
    }
  }

  return (
    <ScrollView style={{ backgroundColor: colors.background }} contentContainerStyle={styles.content}>
      <SectionCard>
        {lat != null && lon != null && (
          <Text style={[styles.hint, { color: colors.onSurfaceVariant }]}>
            Searching near {lat.toFixed(4)}, {lon.toFixed(4)}
          </Text>
        )}
        {locationMessage && <Text style={[styles.error, { color: colors.error }]}>{locationMessage}</Text>}

        <Text style={[styles.label, { color: colors.onSurface }]}>Service type</Text>
        <View style={styles.chipRow}>
          {serviceTypeOptions.map((option) => (
            <Chip key={option} label={option} selected={serviceType === option} onPress={() => setServiceType(option)} />
          ))}
        </View>

        <View style={styles.checkRow}>
          <Switch value={automaticOnly} onValueChange={setAutomaticOnly} />
          <Text style={[styles.checkLabel, { color: colors.onSurface }]}>Automatic transmission only</Text>
        </View>

        {errorMessage && <Text style={[styles.error, { color: colors.error }]}>{errorMessage}</Text>}

        <View style={styles.spacingTop}>
          <AppButton label="Search" onPress={search} loading={isSearching} disabled={lat == null || lon == null} />
        </View>
      </SectionCard>

      {hasSearched &&
        !isSearching &&
        (results.length === 0 ? (
          <Text style={[styles.empty, { color: colors.onSurfaceVariant }]}>No drivers nearby right now.</Text>
        ) : (
          results.map((driver) => (
            <SectionCard key={driver.driverId}>
              <Text style={[styles.driverName, { color: colors.onSurface }]}>{driver.fullName ?? 'Driver'}</Text>
              <Text style={[styles.driverMeta, { color: colors.onSurfaceVariant }]}>
                {driver.distanceKm != null ? `${driver.distanceKm.toFixed(1)} km away` : 'Distance unknown'} ·{' '}
                {driver.ratingAvg != null ? `★ ${driver.ratingAvg.toFixed(1)}` : 'No rating yet'} ·{' '}
                {driver.totalTrips ?? 0} trips
              </Text>
              <View style={styles.spacingTop}>
                <AppButton
                  label="Book"
                  onPress={() => navigation.navigate('CreateBooking', { driverId: driver.driverId, serviceType })}
                />
              </View>
            </SectionCard>
          ))
        ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20 },
  hint: { fontSize: 13 },
  label: { fontSize: 13, fontWeight: '600', marginTop: 12 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', marginTop: 8 },
  checkRow: { flexDirection: 'row', alignItems: 'center', marginTop: 12 },
  checkLabel: { marginLeft: 12, fontSize: 15 },
  error: { marginTop: 8 },
  spacingTop: { marginTop: 16 },
  empty: { fontSize: 14 },
  driverName: { fontSize: 16, fontWeight: '600' },
  driverMeta: { fontSize: 13, marginTop: 4 },
});
