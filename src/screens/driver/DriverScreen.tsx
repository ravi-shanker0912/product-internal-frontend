import { MaterialIcons } from '@expo/vector-icons';
import * as Location from 'expo-location';
import React, { useCallback, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Switch, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { extractErrorMessage } from '../../api/client';
import { driverApi } from '../../api/endpoints';
import { DriverProfile } from '../../api/types';
import AppButton from '../../components/AppButton';
import AppTextField from '../../components/AppTextField';
import SectionCard from '../../components/SectionCard';
import StatusBadge, { statusTone } from '../../components/StatusBadge';
import { useThemeColors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Props = NativeStackScreenProps<RootStackParamList, 'Driver'>;

export default function DriverScreen({ navigation }: Props) {
  const colors = useThemeColors();
  const [isLoading, setIsLoading] = useState(true);
  const [profile, setProfile] = useState<DriverProfile | null>(null);
  const [hasChecked, setHasChecked] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Signup form state
  const [licenseNumber, setLicenseNumber] = useState('');
  const [licenseExpiry, setLicenseExpiry] = useState('');
  const [ownsVehicle, setOwnsVehicle] = useState(false);
  const [canDriveAutomatic, setCanDriveAutomatic] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [isTogglingAvailability, setIsTogglingAvailability] = useState(false);
  const [isPingingLocation, setIsPingingLocation] = useState(false);
  const [locationStatusMessage, setLocationStatusMessage] = useState<string | null>(null);

  const loadProfile = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await driverApi.getProfile();
      setProfile(data);
    } catch (err: any) {
      if (err?.response?.status === 404) {
        setProfile(null);
      } else {
        setErrorMessage(extractErrorMessage(err));
      }
    } finally {
      setIsLoading(false);
      setHasChecked(true);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadProfile();
    }, [loadProfile])
  );

  async function submitProfile() {
    setIsSubmitting(true);
    setErrorMessage(null);
    try {
      const data = await driverApi.createProfile(
        licenseNumber.trim(),
        licenseExpiry.trim() || null,
        ownsVehicle,
        canDriveAutomatic
      );
      setProfile(data);
    } catch (err) {
      setErrorMessage(extractErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function toggleAvailability() {
    if (!profile) return;
    const goOnline = profile.availability !== 'ONLINE';
    setIsTogglingAvailability(true);
    setErrorMessage(null);
    try {
      const data = await driverApi.setAvailability(goOnline);
      setProfile(data);
    } catch (err) {
      setErrorMessage(extractErrorMessage(err));
    } finally {
      setIsTogglingAvailability(false);
    }
  }

  async function updateLocation() {
    setIsPingingLocation(true);
    setLocationStatusMessage(null);
    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') {
        setLocationStatusMessage('No location available. Enable GPS/location on the device and try again.');
        return;
      }
      const loc = await Location.getLastKnownPositionAsync();
      if (!loc) {
        setLocationStatusMessage('No location available. Enable GPS/location on the device and try again.');
        return;
      }
      await driverApi.pingLocation(loc.coords.latitude, loc.coords.longitude);
      setLocationStatusMessage('Location updated');
    } catch (err) {
      setLocationStatusMessage(extractErrorMessage(err));
    } finally {
      setIsPingingLocation(false);
    }
  }

  if (isLoading && !hasChecked) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <ActivityIndicator />
      </View>
    );
  }

  return (
    <ScrollView style={{ backgroundColor: colors.background }} contentContainerStyle={styles.content}>
      {!profile ? (
        <SectionCard>
          <Text style={[styles.title, { color: colors.onSurface }]}>Become a driver</Text>
          <Text style={[styles.subtitle, { color: colors.onSurfaceVariant }]}>
            Tell us about your license so we can get you approved.
          </Text>

          <AppTextField label="Driving licence number" value={licenseNumber} onChangeText={setLicenseNumber} />
          <AppTextField
            label="Licence expiry"
            value={licenseExpiry}
            onChangeText={setLicenseExpiry}
            placeholder="YYYY-MM-DD"
          />

          <CheckRow
            label="I own my vehicle"
            checked={ownsVehicle}
            onToggle={() => setOwnsVehicle((v) => !v)}
          />
          <CheckRow
            label="I can drive automatic"
            checked={canDriveAutomatic}
            onToggle={() => setCanDriveAutomatic((v) => !v)}
          />

          {errorMessage && <Text style={[styles.error, { color: colors.error }]}>{errorMessage}</Text>}

          <View style={styles.spacingTop}>
            <AppButton
              label="Submit"
              onPress={submitProfile}
              loading={isSubmitting}
              disabled={!licenseNumber.trim()}
            />
          </View>
        </SectionCard>
      ) : (
        <>
          <SectionCard>
            <Text style={[styles.title, { color: colors.onSurface }]}>Driver status</Text>
            <View style={styles.spacingSmall}>
              <StatusBadge text={profile.verifyStatus} tone={statusTone(profile.verifyStatus)} />
            </View>
            {profile.verifyStatus === 'PENDING' && (
              <Text style={[styles.subtitle, { color: colors.onSurfaceVariant, marginTop: 8 }]}>
                Your documents are awaiting review before you can go online.
              </Text>
            )}
            {profile.verifyStatus === 'REJECTED' && (
              <Text style={[styles.error, { marginTop: 8 }]}>
                {profile.rejectReason ?? 'Your application was rejected.'}
              </Text>
            )}

            <View style={styles.availabilityRow}>
              <Text style={[styles.bodyLarge, { color: colors.onSurface }]}>Available for trips</Text>
              {isTogglingAvailability ? (
                <ActivityIndicator />
              ) : (
                <Switch value={profile.availability === 'ONLINE'} onValueChange={toggleAvailability} />
              )}
            </View>

            {errorMessage && <Text style={[styles.error, { color: colors.error }]}>{errorMessage}</Text>}

            <View style={styles.spacingTop}>
              <AppButton
                label="Update my location"
                variant="outlined"
                onPress={updateLocation}
                loading={isPingingLocation}
              />
            </View>
            {locationStatusMessage && (
              <Text style={[styles.hint, { color: colors.onSurfaceVariant }]}>{locationStatusMessage}</Text>
            )}
          </SectionCard>

          <SectionCard>
            <Text style={[styles.sectionTitle, { color: colors.onSurface }]}>Manage</Text>
            <View style={styles.spacingSmall}>
              <IconButton
                label="Documents"
                icon="description"
                onPress={() => navigation.navigate('DriverDocuments')}
              />
            </View>
            <View style={styles.spacingSmall}>
              <IconButton
                label="Vehicles"
                icon="directions-car"
                onPress={() => navigation.navigate('DriverVehicles')}
              />
            </View>
          </SectionCard>
        </>
      )}
    </ScrollView>
  );
}

function CheckRow({ label, checked, onToggle }: { label: string; checked: boolean; onToggle: () => void }) {
  const colors = useThemeColors();
  return (
    <View style={styles.checkRow}>
      <Switch value={checked} onValueChange={onToggle} />
      <Text style={[styles.checkLabel, { color: colors.onSurface }]}>{label}</Text>
    </View>
  );
}

function IconButton({
  label,
  icon,
  onPress,
}: {
  label: string;
  icon: keyof typeof MaterialIcons.glyphMap;
  onPress: () => void;
}) {
  const colors = useThemeColors();
  return (
    <Pressable
      onPress={onPress}
      style={[styles.iconButton, { borderColor: colors.primary }]}
    >
      <MaterialIcons name={icon} size={18} color={colors.primary} />
      <Text style={[styles.iconButtonLabel, { color: colors.primary }]}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  content: { padding: 20 },
  title: { fontSize: 19, fontWeight: '600' },
  subtitle: { fontSize: 13, marginTop: 4 },
  sectionTitle: { fontSize: 17, fontWeight: '600' },
  checkRow: { flexDirection: 'row', alignItems: 'center', marginTop: 12 },
  checkLabel: { marginLeft: 12, fontSize: 15 },
  error: { color: '#BA1A1A', marginTop: 8 },
  spacingTop: { marginTop: 16 },
  spacingSmall: { marginTop: 8 },
  availabilityRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 20,
  },
  bodyLarge: { fontSize: 16 },
  hint: { fontSize: 12, marginTop: 4 },
  iconButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderRadius: 24,
    paddingVertical: 13,
  },
  iconButtonLabel: { fontSize: 15, fontWeight: '600', marginLeft: 8 },
});
