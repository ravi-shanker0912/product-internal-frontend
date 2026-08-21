import React, { useCallback, useState } from 'react';
import { ActivityIndicator, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { extractErrorMessage } from '../../api/client';
import { vehicleApi } from '../../api/endpoints';
import { Gearbox, OwnerType, Vehicle } from '../../api/types';
import AppButton from '../../components/AppButton';
import AppTextField from '../../components/AppTextField';
import Chip from '../../components/Chip';
import SectionCard from '../../components/SectionCard';
import { useThemeColors } from '../../theme/colors';

const gearboxOptions: Gearbox[] = ['MANUAL', 'AUTOMATIC'];

interface Props {
  ownerType: OwnerType;
  title: string;
  subtitle: string;
  addButtonLabel: string;
  listTitle: string;
  emptyListLabel: string;
}

/**
 * Shared by both the driver's own-vehicle screen and the customer's own-car
 * screen — the backend's /api/me/vehicles is one endpoint distinguished only
 * by ownerType, so this one form (with role-specific copy passed in) covers
 * both, matching the native app's unified MyVehiclesScreen.
 */
export default function VehiclesForm({ ownerType, title, subtitle, addButtonLabel, listTitle, emptyListLabel }: Props) {
  const colors = useThemeColors();
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isAdding, setIsAdding] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [registrationNo, setRegistrationNo] = useState('');
  const [make, setMake] = useState('');
  const [model, setModel] = useState('');
  const [gearbox, setGearbox] = useState<Gearbox>(gearboxOptions[0]);
  const [seats, setSeats] = useState('');
  const [insuranceExpiry, setInsuranceExpiry] = useState('');

  const loadVehicles = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await vehicleApi.listVehicles(ownerType);
      setVehicles(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }, [ownerType]);

  useFocusEffect(
    useCallback(() => {
      loadVehicles();
    }, [loadVehicles])
  );

  async function addVehicle() {
    setIsAdding(true);
    setError(null);
    try {
      await vehicleApi.addVehicle(
        ownerType,
        registrationNo.trim() || null,
        make.trim(),
        model.trim(),
        gearbox,
        seats.trim() ? parseInt(seats.trim(), 10) : null,
        insuranceExpiry.trim() || null
      );
      setRegistrationNo('');
      setMake('');
      setModel('');
      setSeats('');
      setInsuranceExpiry('');
      await loadVehicles();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setIsAdding(false);
    }
  }

  return (
    <ScrollView style={{ backgroundColor: colors.background }} contentContainerStyle={styles.content}>
      <SectionCard>
        <Text style={[styles.subtitle, { color: colors.onSurfaceVariant }]}>{subtitle}</Text>

        <AppTextField label="Registration number" value={registrationNo} onChangeText={setRegistrationNo} />
        <AppTextField label="Make" value={make} onChangeText={setMake} placeholder="e.g. Maruti" />
        <AppTextField label="Model" value={model} onChangeText={setModel} placeholder="e.g. Swift" />

        <Text style={[styles.label, { color: colors.onSurface }]}>Gearbox</Text>
        <View style={styles.chipRow}>
          {gearboxOptions.map((option) => (
            <Chip key={option} label={option} selected={gearbox === option} onPress={() => setGearbox(option)} />
          ))}
        </View>

        <AppTextField
          label="Seats (optional)"
          value={seats}
          onChangeText={(v) => /^\d*$/.test(v) && setSeats(v)}
          keyboardType="number-pad"
        />
        <AppTextField
          label="Insurance expiry (optional)"
          value={insuranceExpiry}
          onChangeText={setInsuranceExpiry}
          placeholder="YYYY-MM-DD"
        />

        {error && <Text style={[styles.error, { color: colors.error }]}>{error}</Text>}

        <View style={styles.spacingTop}>
          <AppButton
            label={addButtonLabel}
            onPress={addVehicle}
            loading={isAdding}
            disabled={!make.trim() || !model.trim()}
          />
        </View>
      </SectionCard>

      <Text style={[styles.sectionTitle, { color: colors.onSurface }]}>{listTitle}</Text>

      {isLoading ? (
        <ActivityIndicator style={styles.spacingTop} />
      ) : vehicles.length === 0 ? (
        <Text style={[styles.empty, { color: colors.onSurfaceVariant }]}>{emptyListLabel}</Text>
      ) : (
        vehicles.map((vehicle) => (
          <SectionCard key={vehicle.id}>
            <Text style={[styles.vehicleTitle, { color: colors.onSurface }]}>
              {vehicle.make} {vehicle.model}
            </Text>
            <Text style={[styles.vehicleSubtitle, { color: colors.onSurfaceVariant }]}>
              {vehicle.registrationNo ?? 'No reg. number'} · {vehicle.gearbox} · {vehicle.seats} seats
            </Text>
          </SectionCard>
        ))
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20 },
  subtitle: { fontSize: 13 },
  label: { fontSize: 13, fontWeight: '600', marginTop: 16 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', marginTop: 8 },
  error: { marginTop: 8 },
  spacingTop: { marginTop: 16 },
  sectionTitle: { fontSize: 17, fontWeight: '600', marginBottom: 12 },
  empty: { fontSize: 14 },
  vehicleTitle: { fontSize: 16, fontWeight: '600' },
  vehicleSubtitle: { fontSize: 13, marginTop: 4 },
});
