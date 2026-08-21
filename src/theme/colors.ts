import { useColorScheme } from 'react-native';

// Ported 1:1 from the native app's ui/theme/Color.kt + Theme.kt.
const light = {
  primary: '#0B5FBB',
  secondary: '#00796B',
  tertiary: '#C77800',
  background: '#FFFBFE',
  surface: '#FFFBFE',
  surfaceVariant: '#E7E0EC',
  onSurface: '#1C1B1F',
  onSurfaceVariant: '#49454F',
  onPrimary: '#FFFFFF',
  error: '#BA1A1A',
  errorContainer: '#FFDAD6',
  outline: '#79747E',
  success: '#1E8E3E',
  warning: '#B8600A',
  info: '#0B5FBB',
  brandGradientStart: '#0B5FBB',
  brandGradientEnd: '#00796B',
};

const dark = {
  primary: '#9FCAFF',
  secondary: '#7FDBCF',
  tertiary: '#FFC978',
  background: '#1C1B1F',
  surface: '#1C1B1F',
  surfaceVariant: '#49454F',
  onSurface: '#E6E1E5',
  onSurfaceVariant: '#CAC4D0',
  onPrimary: '#00325A',
  error: '#FFB4AB',
  errorContainer: '#93000A',
  outline: '#938F99',
  success: '#8FDA8A',
  warning: '#FFC680',
  info: '#9FCAFF',
  // Kept identical in both themes — used on hero surfaces, not text/icons.
  brandGradientStart: '#0B5FBB',
  brandGradientEnd: '#00796B',
};

export type ThemeColors = typeof light;

export function useThemeColors(): ThemeColors {
  const scheme = useColorScheme();
  return scheme === 'dark' ? dark : light;
}
