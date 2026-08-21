import { LinearGradient } from 'expo-linear-gradient';
import React, { useState } from 'react';
import { KeyboardAvoidingView, Platform, StyleSheet, Text, View } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { authApi } from '../../api/endpoints';
import { extractErrorMessage } from '../../api/client';
import AppButton from '../../components/AppButton';
import AppTextField from '../../components/AppTextField';
import SectionCard from '../../components/SectionCard';
import { Brand } from '../../theme/brand';
import { useThemeColors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

const phoneRegex = /^\+[1-9]\d{7,14}$/;

type Props = NativeStackScreenProps<RootStackParamList, 'PhoneEntry'>;

export default function PhoneEntryScreen({ navigation }: Props) {
  const colors = useThemeColors();
  const [phone, setPhone] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const trimmedPhone = phone.trim();
  const isPhoneValid = phoneRegex.test(trimmedPhone);

  async function sendOtp() {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await authApi.requestOtp(trimmedPhone);
      navigation.navigate('OtpEntry', { phone: trimmedPhone, devOtp: response.devOtp });
    } catch (err) {
      setErrorMessage(extractErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <KeyboardAvoidingView
      style={[styles.flex, { backgroundColor: colors.background }]}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <View style={styles.centered}>
        <LinearGradient
          colors={[colors.brandGradientStart, colors.brandGradientEnd]}
          style={styles.logoCircle}
        >
          <Text style={styles.logoLetter}>{Brand.NAME.charAt(0)}</Text>
        </LinearGradient>

        <Text style={[styles.brandName, { color: colors.primary }]}>{Brand.NAME}</Text>
        <Text style={[styles.tagline, { color: colors.onSurfaceVariant }]}>{Brand.TAGLINE}</Text>

        <SectionCard style={styles.card}>
          <Text style={[styles.title, { color: colors.onSurface }]}>Enter your phone number</Text>

          <AppTextField
            label="Phone number"
            value={phone}
            onChangeText={setPhone}
            placeholder="+91XXXXXXXXXX"
            keyboardType="phone-pad"
            error={phone.length > 0 && !isPhoneValid}
          />

          {errorMessage && (
            <Text style={[styles.error, { color: colors.error }]}>{errorMessage}</Text>
          )}

          <View style={styles.buttonSpacing}>
            <AppButton
              label="Send OTP"
              onPress={sendOtp}
              loading={isLoading}
              disabled={!isPhoneValid}
            />
          </View>
        </SectionCard>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', padding: 24 },
  logoCircle: {
    width: 72,
    height: 72,
    borderRadius: 36,
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoLetter: { fontSize: 32, fontWeight: '800', color: '#FFFFFF' },
  brandName: { fontSize: 28, fontWeight: '800', marginTop: 16 },
  tagline: { fontSize: 14, marginTop: 2, marginBottom: 32 },
  card: { marginTop: 0 },
  title: { fontSize: 20, fontWeight: '600' },
  error: { marginTop: 8 },
  buttonSpacing: { marginTop: 16 },
});
