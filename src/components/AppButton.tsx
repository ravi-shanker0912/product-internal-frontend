import React from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text } from 'react-native';
import { useThemeColors } from '../theme/colors';

export default function AppButton({
  label,
  onPress,
  loading = false,
  disabled = false,
  variant = 'filled',
  color,
}: {
  label: string;
  onPress: () => void;
  loading?: boolean;
  disabled?: boolean;
  variant?: 'filled' | 'outlined' | 'text';
  color?: string;
}) {
  const colors = useThemeColors();
  const tint = color ?? colors.primary;
  const isDisabled = disabled || loading;

  const containerStyle = [
    styles.base,
    variant === 'filled' && { backgroundColor: isDisabled ? colors.surfaceVariant : tint },
    variant === 'outlined' && { borderWidth: 1, borderColor: isDisabled ? colors.outline : tint },
    variant === 'text' && styles.textVariant,
  ];

  const textColor =
    variant === 'filled' ? colors.onPrimary : isDisabled ? colors.onSurfaceVariant : tint;

  return (
    <Pressable onPress={onPress} disabled={isDisabled} style={containerStyle}>
      {loading ? (
        <ActivityIndicator color={textColor} size="small" />
      ) : (
        <Text style={[styles.label, { color: textColor }]}>{label}</Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    borderRadius: 24,
    paddingVertical: 13,
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
  },
  textVariant: {
    paddingVertical: 8,
  },
  label: {
    fontSize: 15,
    fontWeight: '600',
  },
});
