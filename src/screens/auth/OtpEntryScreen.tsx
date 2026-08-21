import React, { useEffect, useState } from 'react';
import { KeyboardAvoidingView, Platform, StyleSheet, Text, View } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { authApi } from '../../api/endpoints';
import { extractErrorMessage } from '../../api/client';
import AppButton from '../../components/AppButton';
import AppTextField from '../../components/AppTextField';
import SectionCard from '../../components/SectionCard';
import { useAuth } from '../../context/AuthContext';
import { OTP_LENGTH } from '../../constants';
import { useThemeColors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';
import { addSmsReceivedListener, startListening, stopListening } from '../../../modules/sms-retriever';

type Props = NativeStackScreenProps<RootStackParamList, 'OtpEntry'>;

export default function OtpEntryScreen({ route, navigation }: Props) {
  const { phone } = route.params;
  const colors = useThemeColors();
  const { login } = useAuth();
  const [otp, setOtp] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const isOtpValid = otp.length === OTP_LENGTH && /^\d+$/.test(otp);

  async function verify(otpValue: string) {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      await authApi.verifyOtp(phone, otpValue.trim());
      login(phone);
      // Navigation switches to the authenticated stack automatically once
      // AuthContext.isAuthenticated flips — see RootNavigator.
    } catch (err) {
      setErrorMessage(extractErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }

  // Silent SMS auto-read (Android SMS Retriever API — no permission, no
  // consent dialog, works as long as the SMS body ends with this build's
  // signature hash). No-ops on iOS since the native module isn't available.
  useEffect(() => {
    startListening();
    const otpRegex = new RegExp(`\\b\\d{${OTP_LENGTH}}\\b`);
    const subscription = addSmsReceivedListener(({ message }) => {
      const match = message.match(otpRegex);
      if (match) {
        setOtp(match[0]);
        verify(match[0]);
      }
    });
    return () => {
      subscription?.remove();
      stopListening();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <KeyboardAvoidingView
      style={[styles.flex, { backgroundColor: colors.background }]}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <View style={styles.centered}>
        <SectionCard>
          <Text style={[styles.title, { color: colors.onSurface }]}>
            Enter the {OTP_LENGTH}-digit code
          </Text>
          <Text style={[styles.subtitle, { color: colors.onSurfaceVariant }]}>Sent to {phone}</Text>

          <AppTextField
            label="OTP"
            value={otp}
            onChangeText={(v) => {
              if (v.length <= OTP_LENGTH && /^\d*$/.test(v)) setOtp(v);
            }}
            keyboardType="number-pad"
            secureTextEntry
            maxLength={OTP_LENGTH}
          />

          {errorMessage && (
            <Text style={[styles.error, { color: colors.error }]}>{errorMessage}</Text>
          )}

          <View style={styles.buttonSpacing}>
            <AppButton
              label="Verify"
              onPress={() => verify(otp)}
              loading={isLoading}
              disabled={!isOtpValid}
            />
          </View>
          <View style={styles.buttonSpacing}>
            <AppButton
              label="Change phone number"
              variant="text"
              onPress={() => navigation.goBack()}
              disabled={isLoading}
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
  title: { fontSize: 20, fontWeight: '600' },
  subtitle: { fontSize: 14, marginTop: 2 },
  error: { marginTop: 8 },
  buttonSpacing: { marginTop: 12 },
});
