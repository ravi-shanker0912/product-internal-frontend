import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useThemeColors } from '../theme/colors';

export type StatusTone = 'SUCCESS' | 'WARNING' | 'INFO' | 'ERROR' | 'NEUTRAL';

/** Maps the backend's uppercase status/enum strings to a visual tone for badges. */
export function statusTone(status: string): StatusTone {
  if (['APPROVED', 'ONLINE', 'COMPLETED', 'ACTIVE'].includes(status)) return 'SUCCESS';
  if (['PENDING', 'REQUESTED', 'ACCEPTED', 'DRIVER_ARRIVED'].includes(status)) return 'WARNING';
  if (status === 'IN_PROGRESS') return 'INFO';
  if (status === 'REJECTED' || status.startsWith('CANCELLED')) return 'ERROR';
  return 'NEUTRAL';
}

function hexToRgba(hex: string, alpha: number): string {
  const clean = hex.replace('#', '');
  const r = parseInt(clean.substring(0, 2), 16);
  const g = parseInt(clean.substring(2, 4), 16);
  const b = parseInt(clean.substring(4, 6), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

export default function StatusBadge({ text, tone }: { text: string; tone: StatusTone }) {
  const colors = useThemeColors();
  const map: Record<StatusTone, { bg: string; fg: string }> = {
    SUCCESS: { bg: hexToRgba(colors.success, 0.16), fg: colors.success },
    WARNING: { bg: hexToRgba(colors.warning, 0.18), fg: colors.warning },
    INFO: { bg: hexToRgba(colors.info, 0.16), fg: colors.info },
    ERROR: { bg: colors.errorContainer, fg: colors.error },
    NEUTRAL: { bg: colors.surfaceVariant, fg: colors.onSurfaceVariant },
  };
  const { bg, fg } = map[tone];

  return (
    <View style={[styles.badge, { backgroundColor: bg }]}>
      <Text style={[styles.text, { color: fg }]}>{text.replace(/_/g, ' ')}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    borderRadius: 50,
    paddingHorizontal: 12,
    paddingVertical: 4,
    alignSelf: 'flex-start',
  },
  text: {
    fontSize: 12,
    fontWeight: '600',
  },
});
