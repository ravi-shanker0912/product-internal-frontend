import { requireNativeModule, EventSubscription } from 'expo-modules-core';
import { Platform } from 'react-native';

interface SmsReceivedEvent {
  message: string;
}

interface SmsRetrieverModuleType {
  getAppSignatureHash(): string | null;
  startListening(): Promise<void>;
  stopListening(): void;
  addListener(eventName: 'onSmsReceived', listener: (event: SmsReceivedEvent) => void): EventSubscription;
  addListener(eventName: 'onSmsTimeout', listener: () => void): EventSubscription;
}

const nativeModule: SmsRetrieverModuleType | null =
  Platform.OS === 'android' ? requireNativeModule<SmsRetrieverModuleType>('SmsRetriever') : null;

/**
 * The 11-char signature hash this build's incoming SMS must end with for
 * the (silent, no-permission, no-consent-dialog) SMS Retriever API to pick
 * them up. Only meaningful on Android; returns null on iOS/if unavailable.
 */
export function getAppSignatureHash(): string | null {
  return nativeModule?.getAppSignatureHash() ?? null;
}

export function startListening(): Promise<void> {
  return nativeModule?.startListening() ?? Promise.resolve();
}

export function stopListening(): void {
  nativeModule?.stopListening();
}

export function addSmsReceivedListener(listener: (event: SmsReceivedEvent) => void): EventSubscription | null {
  return nativeModule?.addListener('onSmsReceived', listener) ?? null;
}

export function addSmsTimeoutListener(listener: () => void): EventSubscription | null {
  return nativeModule?.addListener('onSmsTimeout', listener) ?? null;
}
