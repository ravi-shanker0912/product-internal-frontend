import { Platform } from 'react-native';
import { apiClient } from './client';
import { getDeviceId } from '../storage/deviceId';
import { tokenStore } from '../storage/tokenStore';
import {
  AcceptResponse,
  AddVehicleBody,
  Booking,
  BookingStatusHistory,
  CancelBody,
  CashPaymentBody,
  CompleteBody,
  CreateBookingBody,
  CreateDriverProfileBody,
  DocType,
  DriverDocument,
  DriverProfile,
  Gearbox,
  LocationBody,
  LocationStamp,
  NearbyDriver,
  OtpRequestBody,
  OtpRequestResponse,
  OtpVerifyBody,
  OwnerType,
  Payment,
  RateBody,
  Rating,
  RefreshBody,
  ServiceType,
  StartBody,
  TokenPair,
  TripType,
  UpdateProfileBody,
  UserProfile,
  Vehicle,
} from './types';

// ---- Auth ----

export const authApi = {
  async requestOtp(phone: string): Promise<void> {
    const body: OtpRequestBody = { phone };
    await apiClient.post<OtpRequestResponse>('api/auth/otp/request', body);
  },

  async verifyOtp(phone: string, otp: string): Promise<TokenPair> {
    const deviceId = await getDeviceId();
    const body: OtpVerifyBody = {
      phone,
      otp,
      signupRole: 'CUSTOMER',
      deviceId,
      platform: Platform.OS === 'ios' ? 'IOS' : 'ANDROID',
    };
    const { data } = await apiClient.post<TokenPair>('api/auth/otp/verify', body);
    await tokenStore.saveTokens(data.accessToken, data.refreshToken);
    return data;
  },

  /** Best-effort server-side logout; local tokens are always cleared regardless. */
  async logout(): Promise<void> {
    const refreshToken = await tokenStore.getRefreshToken();
    if (refreshToken) {
      const body: RefreshBody = { refreshToken };
      await apiClient.post('api/auth/logout', body).catch(() => undefined);
    }
    await tokenStore.clear();
  },
};

// ---- Profile ----

export const profileApi = {
  async getProfile(): Promise<UserProfile> {
    const { data } = await apiClient.get<UserProfile>('api/me');
    return data;
  },

  async updateProfile(fullName: string | null, email: string | null, cityId: number | null): Promise<UserProfile> {
    const body: UpdateProfileBody = { fullName, email, cityId };
    const { data } = await apiClient.patch<UserProfile>('api/me', body);
    return data;
  },
};

// ---- Driver ----

export const driverApi = {
  async getProfile(): Promise<DriverProfile> {
    const { data } = await apiClient.get<DriverProfile>('api/me/driver');
    return data;
  },

  async createProfile(
    licenseNumber: string,
    licenseExpiry: string | null,
    ownsVehicle: boolean,
    canDriveAutomatic: boolean
  ): Promise<DriverProfile> {
    const body: CreateDriverProfileBody = { licenseNumber, licenseExpiry, ownsVehicle, canDriveAutomatic };
    const { data } = await apiClient.post<DriverProfile>('api/me/driver', body);
    return data;
  },

  async setAvailability(online: boolean): Promise<DriverProfile> {
    const { data } = await apiClient.post<DriverProfile>('api/me/driver/availability', {
      status: online ? 'ONLINE' : 'OFFLINE',
    });
    return data;
  },

  async pingLocation(lat: number, lon: number, bearing: number | null = null): Promise<void> {
    const body: LocationBody = { lat, lon, bearing };
    await apiClient.post('api/me/driver/location', body);
  },

  async uploadDocument(docType: DocType, fileUrl: string, expiresAt: string | null): Promise<DriverDocument> {
    const { data } = await apiClient.post<DriverDocument>('api/me/driver/documents', {
      docType,
      fileUrl,
      expiresAt,
    });
    return data;
  },

  async listDocuments(): Promise<DriverDocument[]> {
    const { data } = await apiClient.get<DriverDocument[]>('api/me/driver/documents');
    return data;
  },
};

// ---- Vehicles ----
// Backend's /api/me/vehicles is shared by both roles: a driver adds their own
// car (ownerType DRIVER), a customer adds the car they want a driver for
// (ownerType CUSTOMER). Callers pass which they mean and filter accordingly.

export const vehicleApi = {
  async addVehicle(
    ownerType: OwnerType,
    registrationNo: string | null,
    make: string,
    model: string,
    gearbox: Gearbox,
    seats: number | null,
    insuranceExpiry: string | null
  ): Promise<Vehicle> {
    const body: AddVehicleBody = { ownerType, registrationNo, make, model, gearbox, seats, insuranceExpiry };
    const { data } = await apiClient.post<Vehicle>('api/me/vehicles', body);
    return data;
  },

  async listVehicles(ownerType: OwnerType): Promise<Vehicle[]> {
    const { data } = await apiClient.get<Vehicle[]>('api/me/vehicles');
    return data.filter((v) => v.ownerType === ownerType);
  },
};

// ---- Search ----

export const searchApi = {
  async searchDrivers(
    lat: number,
    lon: number,
    serviceType: ServiceType,
    automaticOnly: boolean
  ): Promise<NearbyDriver[]> {
    const { data } = await apiClient.get<NearbyDriver[]>('api/search/drivers', {
      params: { lat, lon, serviceType, automaticOnly },
    });
    return data;
  },
};

// ---- Bookings ----

export const bookingApi = {
  async create(
    driverId: string,
    serviceType: ServiceType,
    tripType: TripType,
    pickupLat: number,
    pickupLon: number,
    pickupAddress: string | null,
    dropLat: number | null,
    dropLon: number | null,
    dropAddress: string | null,
    vehicleId: string | null
  ): Promise<Booking> {
    const body: CreateBookingBody = {
      driverId,
      serviceType,
      tripType,
      pickupLat,
      pickupLon,
      pickupAddress,
      dropLat,
      dropLon,
      dropAddress,
      vehicleId,
    };
    const { data } = await apiClient.post<Booking>('api/bookings', body);
    return data;
  },

  async mine(asDriver: boolean): Promise<Booking[]> {
    const { data } = await apiClient.get<Booking[]>('api/bookings', {
      params: { as: asDriver ? 'driver' : 'customer' },
    });
    return data;
  },

  async one(id: string): Promise<Booking> {
    const { data } = await apiClient.get<Booking>(`api/bookings/${id}`);
    return data;
  },

  async timeline(id: string): Promise<BookingStatusHistory[]> {
    const { data } = await apiClient.get<BookingStatusHistory[]>(`api/bookings/${id}/timeline`);
    return data;
  },

  async accept(id: string, lat: number | null, lon: number | null): Promise<AcceptResponse> {
    const body: LocationStamp = { lat, lon };
    const { data } = await apiClient.post<AcceptResponse>(`api/bookings/${id}/accept`, body);
    return data;
  },

  async arrived(id: string, lat: number | null, lon: number | null): Promise<Booking> {
    const body: LocationStamp = { lat, lon };
    const { data } = await apiClient.post<Booking>(`api/bookings/${id}/arrived`, body);
    return data;
  },

  async start(id: string, otp: string): Promise<Booking> {
    const body: StartBody = { otp };
    const { data } = await apiClient.post<Booking>(`api/bookings/${id}/start`, body);
    return data;
  },

  async complete(
    id: string,
    distanceKm: number | null,
    waitingMinutes: number | null,
    daysAway: number | null = null,
    nightHalts: number | null = null
  ): Promise<Booking> {
    const body: CompleteBody = { distanceKm, waitingMinutes, daysAway, nightHalts };
    const { data } = await apiClient.post<Booking>(`api/bookings/${id}/complete`, body);
    return data;
  },

  async cancel(id: string, reason: string): Promise<Booking> {
    const body: CancelBody = { reason };
    const { data } = await apiClient.post<Booking>(`api/bookings/${id}/cancel`, body);
    return data;
  },

  async rate(id: string, stars: number, comment: string | null): Promise<Rating> {
    const body: RateBody = { stars, comment };
    const { data } = await apiClient.post<Rating>(`api/bookings/${id}/rate`, body);
    return data;
  },

  async recordCashPayment(id: string, amountPaise: number, note: string | null): Promise<Payment> {
    const body: CashPaymentBody = { amountPaise, note };
    const { data } = await apiClient.post<Payment>(`api/bookings/${id}/payment/cash`, body);
    return data;
  },
};
