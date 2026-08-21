import { LinearGradient } from 'expo-linear-gradient';
import { MaterialIcons } from '@expo/vector-icons';
import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { DrawerActions } from '@react-navigation/native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useAuth } from '../../context/AuthContext';
import { Brand } from '../../theme/brand';
import { useThemeColors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

export default function HomeScreen({ navigation }: Props) {
  const colors = useThemeColors();
  const { phone } = useAuth();

  return (
    <View style={[styles.flex, { backgroundColor: colors.background }]}>
      <View style={[styles.topBar, { backgroundColor: colors.primary }]}>
        <Pressable
          onPress={() => navigation.dispatch(DrawerActions.openDrawer())}
          hitSlop={12}
          style={styles.menuButton}
        >
          <MaterialIcons name="menu" size={26} color={colors.onPrimary} />
        </Pressable>
        <Text style={[styles.topBarTitle, { color: colors.onPrimary }]}>{Brand.NAME}</Text>
      </View>

      <LinearGradient
        colors={[colors.brandGradientStart + '1A', colors.background]}
        style={styles.hero}
      >
        <LinearGradient colors={[colors.brandGradientStart, colors.brandGradientEnd]} style={styles.logoCircle}>
          <Text style={styles.logoLetter}>{Brand.NAME.charAt(0)}</Text>
        </LinearGradient>

        <Text style={[styles.brandName, { color: colors.primary }]}>{Brand.NAME}</Text>
        <Text style={[styles.tagline, { color: colors.onSurfaceVariant }]}>{Brand.TAGLINE}</Text>

        <Text style={[styles.welcome, { color: colors.onSurface }]}>Welcome back</Text>
        <Text style={[styles.phone, { color: colors.onSurfaceVariant }]}>{phone}</Text>
        <Text style={[styles.hint, { color: colors.onSurfaceVariant }]}>Tap the menu to get moving.</Text>
      </LinearGradient>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 50,
    paddingBottom: 16,
    paddingHorizontal: 12,
  },
  menuButton: { padding: 8 },
  topBarTitle: { fontSize: 20, fontWeight: '700', marginLeft: 8 },
  hero: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 24 },
  logoCircle: { width: 96, height: 96, borderRadius: 48, alignItems: 'center', justifyContent: 'center' },
  logoLetter: { fontSize: 40, fontWeight: '800', color: '#FFFFFF' },
  brandName: { fontSize: 30, fontWeight: '800', marginTop: 20 },
  tagline: { fontSize: 15, marginTop: 4 },
  welcome: { fontSize: 17, fontWeight: '600', marginTop: 40 },
  phone: { fontSize: 14, marginTop: 2 },
  hint: { fontSize: 13, marginTop: 16 },
});
