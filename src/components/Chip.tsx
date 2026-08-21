import React from 'react';
import { Pressable, StyleSheet, Text } from 'react-native';
import { useThemeColors } from '../theme/colors';

export default function Chip({
  label,
  selected,
  onPress,
}: {
  label: string;
  selected: boolean;
  onPress: () => void;
}) {
  const colors = useThemeColors();
  return (
    <Pressable
      onPress={onPress}
      style={[
        styles.chip,
        {
          backgroundColor: selected ? colors.primary + '26' : 'transparent',
          borderColor: selected ? colors.primary : colors.outline,
        },
      ]}
    >
      <Text style={[styles.label, { color: selected ? colors.primary : colors.onSurface }]}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  chip: {
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 14,
    paddingVertical: 8,
    marginRight: 8,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
  },
});
