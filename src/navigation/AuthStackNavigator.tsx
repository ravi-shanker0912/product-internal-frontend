import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useThemeColors } from '../theme/colors';
import { RootStackParamList } from './types';
import PhoneEntryScreen from '../screens/auth/PhoneEntryScreen';
import OtpEntryScreen from '../screens/auth/OtpEntryScreen';

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function AuthStackNavigator() {
  const colors = useThemeColors();

  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: colors.primary },
        headerTintColor: colors.onPrimary,
        headerTitleStyle: { fontWeight: '700' },
      }}
    >
      <Stack.Screen name="PhoneEntry" component={PhoneEntryScreen} options={{ headerShown: false }} />
      <Stack.Screen name="OtpEntry" component={OtpEntryScreen} options={{ title: 'Verify phone' }} />
    </Stack.Navigator>
  );
}
