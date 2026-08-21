import * as SecureStore from 'expo-secure-store';

// expo-secure-store backs onto Android Keystore-encrypted SharedPreferences and
// iOS Keychain — the RN equivalent of the native app's EncryptedSharedPreferences.
const KEY_ACCESS_TOKEN = 'access_token';
const KEY_REFRESH_TOKEN = 'refresh_token';

export const tokenStore = {
  async saveTokens(accessToken: string, refreshToken: string): Promise<void> {
    await SecureStore.setItemAsync(KEY_ACCESS_TOKEN, accessToken);
    await SecureStore.setItemAsync(KEY_REFRESH_TOKEN, refreshToken);
  },

  async getAccessToken(): Promise<string | null> {
    return SecureStore.getItemAsync(KEY_ACCESS_TOKEN);
  },

  async getRefreshToken(): Promise<string | null> {
    return SecureStore.getItemAsync(KEY_REFRESH_TOKEN);
  },

  async clear(): Promise<void> {
    await SecureStore.deleteItemAsync(KEY_ACCESS_TOKEN);
    await SecureStore.deleteItemAsync(KEY_REFRESH_TOKEN);
  },
};
