import React, { useCallback, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { extractErrorMessage } from '../../api/client';
import { bookingApi } from '../../api/endpoints';
import { Booking } from '../../api/types';
import Chip from '../../components/Chip';
import SectionCard from '../../components/SectionCard';
import StatusBadge, { statusTone } from '../../components/StatusBadge';
import { useThemeColors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Props = NativeStackScreenProps<RootStackParamList, 'Bookings'>;

export default function BookingsScreen({ navigation }: Props) {
  const colors = useThemeColors();
  const [asDriver, setAsDriver] = useState(false);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (driverView: boolean) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await bookingApi.mine(driverView);
      setBookings(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      load(asDriver);
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [asDriver])
  );

  return (
    <ScrollView style={{ backgroundColor: colors.background }} contentContainerStyle={styles.content}>
      <View style={styles.chipRow}>
        <Chip label="As customer" selected={!asDriver} onPress={() => setAsDriver(false)} />
        <Chip label="As driver" selected={asDriver} onPress={() => setAsDriver(true)} />
      </View>

      {isLoading ? (
        <ActivityIndicator style={styles.spacingTop} />
      ) : error ? (
        <Text style={[styles.error, { color: colors.error }]}>{error}</Text>
      ) : bookings.length === 0 ? (
        <Text style={[styles.empty, { color: colors.onSurfaceVariant }]}>No bookings yet.</Text>
      ) : (
        bookings.map((booking) => (
          <Pressable key={booking.id} onPress={() => navigation.navigate('BookingDetail', { bookingId: booking.id })}>
            <SectionCard>
              <View style={styles.row}>
                <Text style={[styles.code, { color: colors.onSurface }]}>{booking.bookingCode}</Text>
                <StatusBadge text={booking.status} tone={statusTone(booking.status)} />
              </View>
              <Text style={[styles.meta, { color: colors.onSurfaceVariant }]}>
                {booking.serviceType} · {booking.tripType}
              </Text>
            </SectionCard>
          </Pressable>
        ))
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20 },
  chipRow: { flexDirection: 'row', marginBottom: 16 },
  spacingTop: { marginTop: 16 },
  error: { fontSize: 14 },
  empty: { fontSize: 14 },
  row: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  code: { fontSize: 16, fontWeight: '600' },
  meta: { fontSize: 13, marginTop: 8 },
});
