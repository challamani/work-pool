// Shared TypeScript types mirroring backend DTOs

export interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  profileImageUrl?: string;
  roles: UserRole[];
  skills: string[];
  location?: GeoLocation;
  serviceRadiusKm: number;
  bio?: string;
  aadhaarVerification: VerificationStatus;
  averageRating: number;
  totalRatings: number;
  emailVerified: boolean;
  createdAt: string;
}

export interface GeoLocation {
  latitude: number;
  longitude: number;
  city: string;
  district: string;
  state: string;
  pincode?: string;
}

export type UserRole = 'PUBLISHER' | 'FINISHER' | 'ADMIN' | 'SUPPORT';

export type VerificationStatus = 'UNVERIFIED' | 'PENDING' | 'VERIFIED' | 'REJECTED';

export type TaskStatus =
  | 'DRAFT' | 'OPEN' | 'BIDDING' | 'ASSIGNED'
  | 'IN_PROGRESS' | 'PENDING_REVIEW' | 'COMPLETED'
  | 'CANCELLED' | 'DISPUTED';

export type TaskCategory =
  | 'HOME_REPAIR' | 'CLEANING' | 'PLUMBING' | 'ELECTRICAL' | 'PAINTING'
  | 'CARPENTRY' | 'GARDENING' | 'TEACHING_TUTORING' | 'COOKING' | 'CHILDCARE'
  | 'ELDER_CARE' | 'PET_CARE' | 'MOVING_SHIFTING' | 'DELIVERY' | 'LAUNDRY'
  | 'MARKETING_PROMOTION' | 'BUSINESS_SUPPORT' | 'IT_TECH_SUPPORT'
  | 'PHOTOGRAPHY_VIDEOGRAPHY' | 'OTHER';

export interface Task {
  id: string;
  publisherId: string;
  title: string;
  description: string;
  category: TaskCategory;
  requiredSkills: string[];
  location: GeoLocation;
  budgetMin: number;
  budgetMax: number;
  status: TaskStatus;
  scheduledStart?: string;
  scheduledEnd?: string;
  assignedFinisherId?: string;
  agreedAmount?: number;
  tags: string[];
  bidCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Bid {
  id: string;
  taskId: string;
  finisherId: string;
  finisherName: string;
  proposedAmount: number;
  coverNote?: string;
  estimatedDurationHours: number;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN' | 'EXPIRED';
  createdAt: string;
}

export interface Notification {
  id: string;
  recipientUserId: string;
  type: string;
  title: string;
  message: string;
  metadata?: Record<string, string>;
  read: boolean;
  createdAt: string;
  readAt?: string;
}

export interface Transaction {
  id: string;
  taskId: string;
  publisherId: string;
  finisherId: string;
  agreedAmount: number;
  publisherCommission: number;
  finisherCommission: number;
  finisherNetPayout: number;
  status: 'PENDING' | 'ESCROW_HELD' | 'RELEASED' | 'REFUNDED' | 'FAILED' | 'DISPUTED';
  gatewayOrderId?: string;
  escrowHeldAt?: string;
  releasedAt?: string;
  createdAt: string;
}

export interface Wallet {
  userId: string;
  balance: number;
  escrowBalance: number;
  updatedAt?: string;
}

export interface Rating {
  id: string;
  taskId: string;
  raterId: string;
  ratedUserId: string;
  stars: number;
  review?: string;
  createdAt: string;
}

export interface UserRatingSummary {
  userId: string;
  averageRating: number;
  totalRatings: number;
  fiveStars: number;
  fourStars: number;
  threeStars: number;
  twoStars: number;
  oneStar: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  errorCode?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
