// Mirrors the backend's JSON contracts exactly (see D:\Product Internal\driverapp).
// Money fields are in paise (1/100 rupee) unless noted otherwise.

export interface OtpRequestBody {
  phone: string;
}

export interface OtpRequestResponse {
  status: string;
}

export interface OtpVerifyBody {
  phone: string;
  otp: string;
  signupRole: string;
  deviceId: string;
  platform: string;
}

export interface RefreshBody {
  refreshToken: string;
}

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  newUser: boolean;
}

/** Mirrors backend's ApiError: { code, message, fields, timestamp }. */
export interface ApiError {
  code?: string;
  message?: string;
  fields?: Record<string, string>;
  timestamp?: string;
}

export interface UserProfile {
  id: string;
  createdAt: string;
  updatedAt: string;
  phoneE164: string;
  fullName: string | null;
  email: string | null;
  role: 'CUSTOMER' | 'DRIVER' | 'ADMIN';
  status: 'ACTIVE' | 'BLOCKED' | 'DELETED';
  cityId: number | null;
  photoUrl: string | null;
  ratingAvg: number;
  ratingCount: number;
  fcmToken: string | null;
  blockedReason: string | null;
  deletedAt: string | null;
}

export interface UpdateProfileBody {
  fullName?: string | null;
  email?: string | null;
  cityId?: number | null;
  photoUrl?: string | null;
  fcmToken?: string | null;
}

export interface CreateDriverProfileBody {
  licenseNumber: string;
  licenseExpiry: string | null;
  ownsVehicle: boolean;
  canDriveAutomatic: boolean;
}

export type AvailabilityStatus = 'OFFLINE' | 'ONLINE' | 'ON_TRIP';

export interface AvailabilityBody {
  status: AvailabilityStatus;
}

export interface LocationBody {
  lat: number;
  lon: number;
  bearing: number | null;
}

export type DocType = 'DL_FRONT' | 'DL_BACK' | 'SELFIE' | 'RC' | 'INSURANCE' | 'AADHAAR';

export interface UploadDocumentBody {
  docType: DocType;
  fileUrl: string;
  expiresAt: string | null;
}

export interface DriverProfile {
  userId: string;
  licenseNumber: string;
  licenseExpiry: string | null;
  ownsVehicle: boolean;
  canDriveAutomatic: boolean;
  verifyStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  verifiedBy: string | null;
  verifiedAt: string | null;
  rejectReason: string | null;
  availability: AvailabilityStatus;
  totalTrips: number;
}

export interface DriverDocument {
  id: string;
  driverId: string;
  docType: DocType;
  fileUrl: string;
  expiresAt: string | null;
  uploadedAt: string;
}

export type OwnerType = 'DRIVER' | 'CUSTOMER';
export type Gearbox = 'MANUAL' | 'AUTOMATIC';

export interface AddVehicleBody {
  ownerType: OwnerType;
  registrationNo: string | null;
  make: string;
  model: string;
  gearbox: Gearbox;
  seats: number | null;
  insuranceExpiry: string | null;
}

export interface Vehicle {
  id: string;
  createdAt: string;
  updatedAt: string;
  ownerUserId: string;
  ownerType: OwnerType;
  registrationNo: string | null;
  make: string;
  model: string;
  gearbox: Gearbox;
  seats: number;
  insuranceExpiry: string | null;
  active: boolean;
}

export type ServiceType = 'WITH_CAR' | 'WITHOUT_CAR';

export interface NearbyDriver {
  driverId: string;
  fullName: string | null;
  photoUrl: string | null;
  ratingAvg: number | null;
  totalTrips: number | null;
  ownsVehicle: boolean | null;
  lat: number | null;
  lon: number | null;
  distanceKm: number | null;
}

export type TripType = 'HOURLY' | 'FULL_DAY' | 'OUTSTATION' | 'CAB_TRIP';

export interface CreateBookingBody {
  driverId: string;
  serviceType: ServiceType;
  tripType: TripType;
  pickupLat: number;
  pickupLon: number;
  pickupAddress: string | null;
  dropLat: number | null;
  dropLon: number | null;
  dropAddress: string | null;
  vehicleId: string | null;
}

export interface LocationStamp {
  lat: number | null;
  lon: number | null;
}

export interface StartBody {
  otp: string;
}

export interface CompleteBody {
  distanceKm: number | null;
  waitingMinutes: number | null;
  daysAway: number | null;
  nightHalts: number | null;
}

export interface CancelBody {
  reason: string;
}

export interface AcceptResponse {
  booking: Booking;
  startOtp: string;
}

export type BookingStatus =
  | 'REQUESTED'
  | 'ACCEPTED'
  | 'DRIVER_ARRIVED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED_BY_CUSTOMER'
  | 'CANCELLED_BY_DRIVER'
  | 'EXPIRED';

export interface Booking {
  id: string;
  createdAt: string;
  updatedAt: string;
  bookingCode: string;
  customerId: string;
  driverId: string | null;
  vehicleId: string | null;
  cityId: number;
  serviceType: ServiceType;
  tripType: TripType;
  status: BookingStatus;
  pickupLat: number;
  pickupLon: number;
  pickupAddress: string | null;
  dropLat: number | null;
  dropLon: number | null;
  dropAddress: string | null;
  requestedAt: string;
  acceptedAt: string | null;
  arrivedAt: string | null;
  startedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
  cancelReason: string | null;
  estimatedFarePaise: number | null;
  billedMinutes: number | null;
  billedKm: number | null;
  waitingMinutes: number;
  totalFarePaise: number | null;
  commissionPaise: number | null;
  driverEarningPaise: number | null;
  paymentMethod: string | null;
  settledAt: string | null;
  settlementRef: string | null;
}

export interface BookingStatusHistory {
  id: number;
  bookingId: string;
  fromStatus: string | null;
  toStatus: string;
  actorId: string | null;
  actorRole: string | null;
  lat: number | null;
  lon: number | null;
  note: string | null;
  createdAt: string;
}

export interface RateBody {
  stars: number;
  comment: string | null;
}

export interface Rating {
  id: string;
  bookingId: string;
  raterId: string;
  rateeId: string;
  stars: number;
  comment: string | null;
  createdAt: string;
}

export interface CashPaymentBody {
  amountPaise: number;
  note: string | null;
}

export interface Payment {
  id: string;
  bookingId: string;
  payerId: string;
  collectedBy: string;
  amountPaise: number;
  method: string;
  status: 'COLLECTED' | 'DISPUTED';
  note: string | null;
  collectedAt: string;
  createdAt: string;
}
