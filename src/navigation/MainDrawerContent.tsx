import { LinearGradient } from 'expo-linear-gradient';
import { MaterialIcons } from '@expo/vector-icons';
import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { DrawerContentComponentProps } from '@react-navigation/drawer';
import { useAuth } from '../context/AuthContext';
import { Brand } from '../theme/brand';
import { useThemeColors } from '../theme/colors';

interface DrawerItem {
  label: string;
  icon: keyof typeof MaterialIcons.glyphMap;
  screen: string;
}

const items: DrawerItem[] = [
  { label: 'Profile', icon: 'person', screen: 'Profile' },
  { label: 'Become a driver', icon: 'directions-car', screen: 'Driver' },
  { label: 'Find a driver', icon: 'search', screen: 'Search' },
  { label: 'My bookings', icon: 'list', screen: 'Bookings' },
  { label: 'My car', icon: 'directions-car', screen: 'MyVehicles' },
];

export default function MainDrawerContent(props: DrawerContentComponentProps) {
  const colors = useThemeColors();
  const { phone, logout } = useAuth();

  function go(screen: string) {
    props.navigation.closeDrawer();
    props.navigation.navigate('AppStack', { screen });
  }

  async function handleLogout() {
    props.navigation.closeDrawer();
    await logout();
  }

  return (
    <View style={[styles.flex, { backgroundColor: colors.surface }]}>
      <LinearGradient colors={[colors.brandGradientStart, colors.brandGradientEnd]} style={styles.header}>
        <Text style={styles.headerName}>{Brand.NAME}</Text>
        <Text style={styles.headerPhone}>{phone}</Text>
      </LinearGradient>

      <View style={styles.items}>
        {items.map((item) => (
          <Pressable key={item.label} style={styles.row} onPress={() => go(item.screen)}>
            <MaterialIcons name={item.icon} size={22} color={colors.onSurface} />
            <Text style={[styles.rowLabel, { color: colors.onSurface }]}>{item.label}</Text>
          </Pressable>
        ))}

        <View style={[styles.divider, { backgroundColor: colors.outline }]} />

        <Pressable style={styles.row} onPress={handleLogout}>
          <MaterialIcons name="logout" size={22} color={colors.error} />
          <Text style={[styles.rowLabel, { color: colors.error }]}>Log out</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  header: { paddingTop: 56, paddingBottom: 24, paddingHorizontal: 24 },
  headerName: { fontSize: 22, fontWeight: '800', color: '#FFFFFF' },
  headerPhone: { fontSize: 14, color: 'rgba(255,255,255,0.85)', marginTop: 4 },
  items: { paddingTop: 8 },
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 14, paddingHorizontal: 20 },
  rowLabel: { fontSize: 16, marginLeft: 20 },
  divider: { height: StyleSheet.hairlineWidth, marginVertical: 8, marginHorizontal: 20, opacity: 0.4 },
});
