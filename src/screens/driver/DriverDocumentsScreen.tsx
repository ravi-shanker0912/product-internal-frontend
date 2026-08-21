import * as ImagePicker from 'expo-image-picker';
import React, { useCallback, useState } from 'react';
import { ActivityIndicator, Image, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { extractErrorMessage } from '../../api/client';
import { driverApi } from '../../api/endpoints';
import { DocType, DocumentView } from '../../api/types';
import AppButton from '../../components/AppButton';
import AppTextField from '../../components/AppTextField';
import Chip from '../../components/Chip';
import SectionCard from '../../components/SectionCard';
import { useThemeColors } from '../../theme/colors';

const docTypes: DocType[] = ['DL_FRONT', 'DL_BACK', 'SELFIE', 'RC', 'INSURANCE', 'AADHAAR'];

interface PickedFile {
  uri: string;
  mimeType: string;
}

export default function DriverDocumentsScreen() {
  const colors = useThemeColors();
  const [documents, setDocuments] = useState<DocumentView[]>([]);
  const [isLoadingDocuments, setIsLoadingDocuments] = useState(true);
  const [selectedDocType, setSelectedDocType] = useState<DocType>(docTypes[0]);
  const [picked, setPicked] = useState<PickedFile | null>(null);
  const [expiresAt, setExpiresAt] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadDocuments = useCallback(async () => {
    setIsLoadingDocuments(true);
    setError(null);
    try {
      const data = await driverApi.listDocuments();
      setDocuments(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setIsLoadingDocuments(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadDocuments();
    }, [loadDocuments])
  );

  async function pickFromLibrary() {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') {
      setError('Photo library permission is required to attach a document.');
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({ quality: 0.8 });
    if (!result.canceled && result.assets[0]) {
      setPicked({ uri: result.assets[0].uri, mimeType: result.assets[0].mimeType ?? 'image/jpeg' });
      setError(null);
    }
  }

  async function takePhoto() {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== 'granted') {
      setError('Camera permission is required to photograph a document.');
      return;
    }
    const result = await ImagePicker.launchCameraAsync({ quality: 0.8 });
    if (!result.canceled && result.assets[0]) {
      setPicked({ uri: result.assets[0].uri, mimeType: result.assets[0].mimeType ?? 'image/jpeg' });
      setError(null);
    }
  }

  async function upload() {
    if (!picked) return;
    setIsUploading(true);
    setError(null);
    try {
      await driverApi.uploadDocumentFile(selectedDocType, picked.uri, picked.mimeType, expiresAt.trim() || null);
      setPicked(null);
      setExpiresAt('');
      await loadDocuments();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setIsUploading(false);
    }
  }

  return (
    <ScrollView style={{ backgroundColor: colors.background }} contentContainerStyle={styles.content}>
      <SectionCard>
        <Text style={[styles.label, { color: colors.onSurface }]}>Document type</Text>
        <View style={styles.chipRow}>
          {docTypes.map((type) => (
            <Chip key={type} label={type} selected={selectedDocType === type} onPress={() => setSelectedDocType(type)} />
          ))}
        </View>

        {picked && (
          <Image source={{ uri: picked.uri }} style={styles.preview} resizeMode="cover" />
        )}

        <View style={styles.pickerRow}>
          <View style={styles.pickerButton}>
            <AppButton label="Choose photo" variant="outlined" onPress={pickFromLibrary} />
          </View>
          <View style={styles.pickerButton}>
            <AppButton label="Take photo" variant="outlined" onPress={takePhoto} />
          </View>
        </View>

        <AppTextField label="Expires (optional)" value={expiresAt} onChangeText={setExpiresAt} placeholder="YYYY-MM-DD" />

        {error && <Text style={[styles.error, { color: colors.error }]}>{error}</Text>}

        <View style={styles.spacingTop}>
          <AppButton label="Upload" onPress={upload} loading={isUploading} disabled={!picked} />
        </View>
      </SectionCard>

      <Text style={[styles.sectionTitle, { color: colors.onSurface }]}>Uploaded documents</Text>

      {isLoadingDocuments ? (
        <ActivityIndicator style={styles.spacingTop} />
      ) : documents.length === 0 ? (
        <Text style={[styles.empty, { color: colors.onSurfaceVariant }]}>No documents uploaded yet.</Text>
      ) : (
        documents.map((doc) => (
          <SectionCard key={doc.id}>
            <View style={styles.docRow}>
              <Image source={{ uri: doc.viewUrl }} style={styles.thumbnail} resizeMode="cover" />
              <View style={styles.docInfo}>
                <Text style={[styles.docType, { color: colors.onSurface }]}>{doc.docType}</Text>
                {doc.expiresAt && (
                  <Text style={[styles.docMeta, { color: colors.onSurfaceVariant }]}>Expires {doc.expiresAt}</Text>
                )}
              </View>
            </View>
          </SectionCard>
        ))
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20 },
  label: { fontSize: 13, fontWeight: '600' },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', marginTop: 8 },
  preview: { width: '100%', height: 180, borderRadius: 12, marginTop: 16 },
  pickerRow: { flexDirection: 'row', marginTop: 16, gap: 12 },
  pickerButton: { flex: 1 },
  error: { marginTop: 8 },
  spacingTop: { marginTop: 16 },
  sectionTitle: { fontSize: 17, fontWeight: '600', marginBottom: 12 },
  empty: { fontSize: 14 },
  docRow: { flexDirection: 'row', alignItems: 'center' },
  thumbnail: { width: 56, height: 56, borderRadius: 8 },
  docInfo: { marginLeft: 12, flex: 1 },
  docType: { fontSize: 16, fontWeight: '600' },
  docMeta: { fontSize: 13, marginTop: 4 },
});
