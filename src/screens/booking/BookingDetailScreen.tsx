import * as Location from 'expo-location';
import React, { useCallback, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { extractErrorMessage } from '../../api/client';
import { bookingApi, profileApi } from '../../api/endpoints';
import { Booking, BookingStatusHistory } from '../../api/types';
import AppButton from '../../components/AppButton';
import AppTextField from '../../components/AppTextField';
import SectionCard from '../../components/SectionCard';
import StatusBadge, { statusTone } from '../../components/StatusBadge';
import { ThemeColors, useThemeColors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Props = NativeStackScreenProps<RootStackParamList, 'BookingDetail'>;

const cancellableStatuses = new Set(['REQUESTED', 'ACCEPTED', 'DRIVER_ARRIVED']);

async function lastKnownLocation(): Promise<{ lat: number; lon: number } | null> {
  const { status } = await Location.requestForegroundPermissionsAsync();
  if (status !== 'granted') return null;
  const loc = await Location.getLastKnownPositionAsync();
  return loc ? { lat: loc.coords.latitude, lon: loc.coords.longitude } : null;
}

export default function BookingDetailScreen({ route }: Props) {
  const { bookingId } = route.params;
  const colors = useThemeColors();

  const [booking, setBooking] = useState<Booking | null>(null);
  const [timeline, setTimeline] = useState<BookingStatusHistory[]>([]);
  const [myUserId, setMyUserId] = useState<string | null>(null);
  const [isLoadingDetail, setIsLoadingDetail] = useState(true);
  const [detailError, setDetailError] = useState<string | null>(null);
  const [isActing, setIsActing] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [actionSuccessMessage, setActionSuccessMessage] = useState<string | null>(null);
  const [lastStartOtp, setLastStartOtp] = useState<string | null>(null);

  const [otpInput, setOtpInput] = useState('');
  const [distanceKmInput, setDistanceKmInput] = useState('');
  const [waitingMinutesInput, setWaitingMinutesInput] = useState('');
  const [tollInput, setTollInput] = useState('');
  const [parkingInput, setParkingInput] = useState('');
  const [cancelReason, setCancelReason] = useState('');
  const [cashAmountInput, setCashAmountInput] = useState('');
  const [rateStars, setRateStars] = useState(5);
  const [rateComment, setRateComment] = useState('');

  const load = useCallback(async () => {
    setIsLoadingDetail(true);
    setDetailError(null);
    try {
      const [bookingData, timelineData, profile] = await Promise.all([
        bookingApi.one(bookingId),
        bookingApi.timeline(bookingId),
        profileApi.getProfile(),
      ]);
      setBooking(bookingData);
      setTimeline(timelineData);
      setMyUserId(profile.id);
    } catch (err) {
      setDetailError(extractErrorMessage(err));
    } finally {
      setIsLoadingDetail(false);
    }
  }, [bookingId]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  const isDriver = !!booking && !!myUserId && booking.driverId === myUserId;
  const isCustomer = !!booking && !!myUserId && booking.customerId === myUserId;

  async function runAction<T>(action: () => Promise<T>, onDone?: (result: T) => void) {
    setIsActing(true);
    setActionError(null);
    setActionSuccessMessage(null);
    try {
      const result = await action();
      onDone?.(result);
      await load();
    } catch (err) {
      setActionError(extractErrorMessage(err));
    } finally {
      setIsActing(false);
    }
  }

  async function accept() {
    const loc = await lastKnownLocation();
    await runAction(
      () => bookingApi.accept(bookingId, loc?.lat ?? null, loc?.lon ?? null),
      (res) => setLastStartOtp(res.startOtp)
    );
  }

  async function arrived() {
    const loc = await lastKnownLocation();
    await runAction(() => bookingApi.arrived(bookingId, loc?.lat ?? null, loc?.lon ?? null));
  }

  async function start() {
    await runAction(() => bookingApi.start(bookingId, otpInput));
  }

  async function complete() {
    await runAction(() =>
      bookingApi.complete(
        bookingId,
        distanceKmInput.trim() ? parseFloat(distanceKmInput.trim()) : null,
        waitingMinutesInput.trim() ? parseInt(waitingMinutesInput.trim(), 10) : null,
        null,
        null,
        tollInput.trim() ? parseInt(tollInput.trim(), 10) : null,
        parkingInput.trim() ? parseInt(parkingInput.trim(), 10) : null
      )
    );
  }

  async function recordCashPayment() {
    await runAction(() => bookingApi.recordCashPayment(bookingId, parseInt(cashAmountInput.trim(), 10), null));
  }

  async function rate() {
    await runAction(
      () => bookingApi.rate(bookingId, rateStars, rateComment.trim() || null),
      () => setActionSuccessMessage('Thanks for rating your trip!')
    );
  }

  async function cancel() {
    await runAction(() => bookingApi.cancel(bookingId, cancelReason.trim()));
  }

  if (isLoadingDetail && !booking) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <ActivityIndicator />
      </View>
    );
  }

  if (!booking) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        {detailError && <Text style={{ color: colors.error }}>{detailError}</Text>}
      </View>
    );
  }

  const fare = booking.totalFarePaise ?? booking.estimatedFarePaise;
  const fareLabel = booking.totalFarePaise != null ? 'Fare' : 'Estimated fare';
  const cancellable = cancellableStatuses.has(booking.status);

  return (
    <ScrollView style={{ backgroundColor: colors.background }} contentContainerStyle={styles.content}>
      <SectionCard>
        <View style={styles.row}>
          <Text style={[styles.code, { color: colors.onSurface }]}>{booking.bookingCode}</Text>
          <StatusBadge text={booking.status} tone={statusTone(booking.status)} />
        </View>
        <Text style={[styles.meta, { color: colors.onSurfaceVariant }]}>
          {booking.serviceType} · {booking.tripType}
        </Text>

        {fare != null && (
          <Text style={[styles.fare, { color: colors.primary }]}>
            {fareLabel}: ₹{(fare / 100).toFixed(2)}
          </Text>
        )}

        {booking.surgeMultiplierBps > 10000 && (
          <View style={[styles.surgeBadge, { backgroundColor: colors.warning + '26' }]}>
            <Text style={[styles.surgeText, { color: colors.warning }]}>
              Surge {booking.surgeMultiplierX} — {booking.surgeLabel ?? 'high demand'}
            </Text>
          </View>
        )}

        {lastStartOtp && isDriver && (
          <>
            <Text style={[styles.startOtp, { color: colors.info }]}>Start OTP: {lastStartOtp}</Text>
            <Text style={[styles.hint, { color: colors.onSurfaceVariant }]}>
              Have the customer read this back to you to start the trip.
            </Text>
          </>
        )}
      </SectionCard>

      {booking.totalFarePaise != null && <FareBreakdown booking={booking} colors={colors} />}

      {actionError && <Text style={[styles.error, { color: colors.error }]}>{actionError}</Text>}
      {actionSuccessMessage && <Text style={[styles.success, { color: colors.success }]}>{actionSuccessMessage}</Text>}

      {booking.status === 'REQUESTED' && isDriver && (
        <View style={styles.spacingBottom}>
          <AppButton label="Accept booking" onPress={accept} loading={isActing} />
        </View>
      )}

      {booking.status === 'ACCEPTED' && isDriver && (
        <View style={styles.spacingBottom}>
          <AppButton label="Mark arrived" onPress={arrived} loading={isActing} />
        </View>
      )}

      {booking.status === 'DRIVER_ARRIVED' && isDriver && (
        <SectionCard>
          <AppTextField
            label="Start OTP"
            value={otpInput}
            onChangeText={(v) => /^\d{0,4}$/.test(v) && setOtpInput(v)}
            keyboardType="number-pad"
            secureTextEntry
            maxLength={4}
          />
          <View style={styles.spacingTop}>
            <AppButton label="Start trip" onPress={start} loading={isActing} disabled={otpInput.length !== 4} />
          </View>
        </SectionCard>
      )}

      {booking.status === 'IN_PROGRESS' && isDriver && (
        <SectionCard>
          <AppTextField
            label="Distance travelled, km (optional)"
            value={distanceKmInput}
            onChangeText={setDistanceKmInput}
            keyboardType="decimal-pad"
          />
          <AppTextField
            label="Waiting minutes (optional)"
            value={waitingMinutesInput}
            onChangeText={(v) => /^\d*$/.test(v) && setWaitingMinutesInput(v)}
            keyboardType="number-pad"
          />
          <AppTextField
            label="Toll paid, paise (optional)"
            value={tollInput}
            onChangeText={(v) => /^\d*$/.test(v) && setTollInput(v)}
            keyboardType="number-pad"
          />
          <AppTextField
            label="Parking paid, paise (optional)"
            value={parkingInput}
            onChangeText={(v) => /^\d*$/.test(v) && setParkingInput(v)}
            keyboardType="number-pad"
          />
          <View style={styles.spacingTop}>
            <AppButton label="Complete trip" onPress={complete} loading={isActing} />
          </View>
        </SectionCard>
      )}

      {booking.status === 'COMPLETED' && isDriver && (
        <SectionCard>
          <AppTextField
            label="Cash collected, paise"
            value={cashAmountInput}
            onChangeText={(v) => /^\d*$/.test(v) && setCashAmountInput(v)}
            keyboardType="number-pad"
            placeholder={booking.totalFarePaise != null ? String(booking.totalFarePaise) : undefined}
          />
          <View style={styles.spacingTop}>
            <AppButton
              label="Record cash payment"
              onPress={recordCashPayment}
              loading={isActing}
              disabled={!cashAmountInput.trim()}
            />
          </View>
        </SectionCard>
      )}

      {booking.status === 'COMPLETED' && isCustomer && (
        <SectionCard>
          <Text style={[styles.sectionTitle, { color: colors.onSurface }]}>Rate this trip</Text>
          <View style={styles.starsRow}>
            {[1, 2, 3, 4, 5].map((star) => (
              <Pressable key={star} onPress={() => setRateStars(star)} hitSlop={6}>
                <Text style={[styles.star, { color: colors.warning }]}>{star <= rateStars ? '★' : '☆'}</Text>
              </Pressable>
            ))}
          </View>
          <AppTextField label="Comment (optional)" value={rateComment} onChangeText={setRateComment} />
          <View style={styles.spacingTop}>
            <AppButton label="Submit rating" onPress={rate} loading={isActing} />
          </View>
        </SectionCard>
      )}

      {cancellable && (isCustomer || isDriver) && (
        <SectionCard>
          <Text style={[styles.sectionTitle, { color: colors.onSurface }]}>Cancel booking</Text>
          <AppTextField label="Cancellation reason" value={cancelReason} onChangeText={setCancelReason} />
          <View style={styles.spacingTop}>
            <AppButton
              label="Cancel booking"
              variant="outlined"
              color={colors.error}
              onPress={cancel}
              loading={isActing}
              disabled={!cancelReason.trim()}
            />
          </View>
        </SectionCard>
      )}

      {timeline.length > 0 && (
        <SectionCard>
          <Text style={[styles.sectionTitle, { color: colors.onSurface }]}>Timeline</Text>
          {timeline.map((event, index) => (
            <View key={event.id}>
              {index > 0 && <View style={[styles.divider, { backgroundColor: colors.outline }]} />}
              <View style={[styles.timelineRow, index === 0 && styles.spacingTop]}>
                <Text style={[styles.timelineText, { color: colors.onSurface }]}>
                  {event.fromStatus ?? '—'} → {event.toStatus}
                </Text>
                <Text style={[styles.timelineActor, { color: colors.onSurfaceVariant }]}>
                  {event.actorRole ?? 'system'}
                </Text>
              </View>
            </View>
          ))}
        </SectionCard>
      )}
    </ScrollView>
  );
}

const fareLineLabels: Record<string, string> = {
  estimatedFare: 'Estimated fare',
  timeOrDistance: 'Time/distance',
  kmOverage: 'Extra distance',
  waiting: 'Waiting charge',
  nightExtra: 'Night charge',
  surge: 'Surge',
  allowance: 'Allowance',
  floorTopup: 'Minimum fare top-up',
  toll: 'Toll',
  parking: 'Parking',
  totalFare: 'Total fare',
  commission: 'Platform commission',
  driverEarning: 'Driver earning',
  cancellationFeeOwed: 'Cancellation fee owed',
  goodwillCreditOwed: 'Goodwill credit owed',
};
// Always shown even at zero -- everything else is hidden when it didn't apply.
const alwaysShown = new Set(['totalFare', 'commission', 'driverEarning']);
const fareLineOrder = Object.keys(fareLineLabels);

function FareBreakdown({ booking, colors }: { booking: Booking; colors: ThemeColors }) {
  const rupees = booking.rupees ?? {};
  const lines = fareLineOrder.filter((key) => rupees[key] != null && (alwaysShown.has(key) || rupees[key] !== '0.00'));

  if (lines.length === 0) return null;

  return (
    <SectionCard>
      <Text style={[styles.sectionTitle, { color: colors.onSurface }]}>Fare breakdown</Text>
      {lines.map((key) => (
        <View key={key} style={[styles.row, styles.fareLineRow]}>
          <Text style={[styles.fareLineLabel, { color: colors.onSurfaceVariant }]}>{fareLineLabels[key]}</Text>
          <Text
            style={[
              styles.fareLineValue,
              { color: key === 'totalFare' ? colors.primary : colors.onSurface },
              key === 'totalFare' && styles.fareLineValueBold,
            ]}
          >
            ₹{rupees[key]}
          </Text>
        </View>
      ))}
    </SectionCard>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  content: { padding: 20 },
  row: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  code: { fontSize: 19, fontWeight: '600' },
  meta: { fontSize: 13, marginTop: 6 },
  fare: { fontSize: 17, fontWeight: '600', marginTop: 12 },
  surgeBadge: { alignSelf: 'flex-start', borderRadius: 50, paddingHorizontal: 12, paddingVertical: 4, marginTop: 8 },
  surgeText: { fontSize: 12, fontWeight: '600' },
  fareLineRow: { marginTop: 8 },
  fareLineLabel: { fontSize: 14 },
  fareLineValue: { fontSize: 14 },
  fareLineValueBold: { fontWeight: '700', fontSize: 16 },
  startOtp: { fontSize: 17, fontWeight: '700', marginTop: 12 },
  hint: { fontSize: 12, marginTop: 2 },
  error: { marginBottom: 12 },
  success: { marginBottom: 12 },
  sectionTitle: { fontSize: 17, fontWeight: '600' },
  spacingTop: { marginTop: 12 },
  spacingBottom: { marginBottom: 16 },
  starsRow: { flexDirection: 'row', marginTop: 8 },
  star: { fontSize: 28, marginRight: 8 },
  divider: { height: StyleSheet.hairlineWidth, marginVertical: 8, opacity: 0.5 },
  timelineRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  timelineText: { fontSize: 14 },
  timelineActor: { fontSize: 12 },
});
