import * as Application from 'expo-application';
import * as Crypto from 'expo-crypto';
import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';

const KEY_DEVICE_ID = 'device_id';

/**
 * Android has a stable per-app ANDROID_ID; iOS has no equivalent without extra
 * permissions (identifierForVendor changes if all the vendor's apps are
 * uninstalled), so for parity across both we generate one random UUID on
 * first launch and persist it — this only needs to be stable, not tied to
 * hardware.
 */
export async function getDeviceId(): Promise<string> {
  if (Platform.OS === 'android') {
    const androidId = Application.getAndroidId();
    if (androidId) return androidId;
  }

  const existing = await SecureStore.getItemAsync(KEY_DEVICE_ID);
  if (existing) return existing;

  const generated = Crypto.randomUUID();
  await SecureStore.setItemAsync(KEY_DEVICE_ID, generated);
  return generated;
}
