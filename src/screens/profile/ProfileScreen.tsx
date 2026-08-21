import React, { useCallback, useState } from 'react';
import { ActivityIndicator, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { extractErrorMessage } from '../../api/client';
import { profileApi } from '../../api/endpoints';
import { UserProfile } from '../../api/types';
import AppButton from '../../components/AppButton';
import AppTextField from '../../components/AppTextField';
import SectionCard from '../../components/SectionCard';
import StatusBadge, { statusTone } from '../../components/StatusBadge';
import { useThemeColors } from '../../theme/colors';

export default function ProfileScreen() {
  const colors = useThemeColors();
  const [isLoading, setIsLoading] = useState(true);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [cityId, setCityId] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState(false);

  const loadProfile = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await profileApi.getProfile();
      setProfile(data);
      setFullName(data.fullName ?? '');
      setEmail(data.email ?? '');
      setCityId(data.cityId != null ? String(data.cityId) : '');
    } catch (err) {
      setErrorMessage(extractErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadProfile();
    }, [loadProfile])
  );

  async function save() {
    setIsSaving(true);
    setErrorMessage(null);
    setSaveSuccess(false);
    try {
      const parsedCityId = cityId.trim() ? parseInt(cityId.trim(), 10) : null;
      const updated = await profileApi.updateProfile(
        fullName.trim() || null,
        email.trim() || null,
        Number.isNaN(parsedCityId) ? null : parsedCityId
      );
      setProfile(updated);
      setSaveSuccess(true);
    } catch (err) {
      setErrorMessage(extractErrorMessage(err));
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <ScrollView style={{ backgroundColor: colors.background }} contentContainerStyle={styles.content}>
      {isLoading ? (
        <ActivityIndicator style={styles.spacingTop} />
      ) : (
        <>
          {profile && (
            <SectionCard>
              <Text style={[styles.phoneText, { color: colors.onSurface }]}>{profile.phoneE164}</Text>
              <View style={styles.badgeRow}>
                <StatusBadge text={profile.role} tone="INFO" />
                <View style={{ width: 8 }} />
                <StatusBadge text={profile.status} tone={statusTone(profile.status)} />
              </View>
              {profile.ratingCount > 0 && (
                <Text style={[styles.rating, { color: colors.onSurface }]}>
                  ★ {profile.ratingAvg.toFixed(1)} · {profile.ratingCount} ratings
                </Text>
              )}
            </SectionCard>
          )}

          <SectionCard>
            <Text style={[styles.sectionTitle, { color: colors.onSurface }]}>Edit details</Text>

            <AppTextField label="Full name" value={fullName} onChangeText={setFullName} />
            <AppTextField label="Email" value={email} onChangeText={setEmail} keyboardType="email-address" />
            <AppTextField
              label="City ID"
              value={cityId}
              onChangeText={setCityId}
              placeholder="1"
              keyboardType="number-pad"
            />
            <Text style={[styles.hint, { color: colors.onSurfaceVariant }]}>
              Required to book a driver. This deployment has one city: 1 = Patna.
            </Text>

            {errorMessage && <Text style={[styles.error, { color: colors.error }]}>{errorMessage}</Text>}
            {saveSuccess && <Text style={[styles.success, { color: colors.primary }]}>Saved</Text>}

            <View style={styles.spacingTop}>
              <AppButton label="Save" onPress={save} loading={isSaving} />
            </View>
          </SectionCard>
        </>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20 },
  spacingTop: { marginTop: 16 },
  phoneText: { fontSize: 18, fontWeight: '600' },
  badgeRow: { flexDirection: 'row', marginTop: 8 },
  rating: { fontSize: 14, marginTop: 12 },
  sectionTitle: { fontSize: 17, fontWeight: '600' },
  hint: { fontSize: 12, marginTop: 4 },
  error: { marginTop: 8 },
  success: { marginTop: 8 },
});
