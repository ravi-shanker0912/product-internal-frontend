import React, { useCallback, useState } from 'react';
import { ActivityIndicator, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { extractErrorMessage } from '../../api/client';
import { driverApi } from '../../api/endpoints';
import { DocType, DriverDocument } from '../../api/types';
import AppButton from '../../components/AppButton';
import AppTextField from '../../components/AppTextField';
import Chip from '../../components/Chip';
import SectionCard from '../../components/SectionCard';
import { useThemeColors } from '../../theme/colors';

const docTypes: DocType[] = ['DL_FRONT', 'DL_BACK', 'SELFIE', 'RC', 'INSURANCE', 'AADHAAR'];

export default function DriverDocumentsScreen() {
  const colors = useThemeColors();
  const [documents, setDocuments] = useState<DriverDocument[]>([]);
  const [isLoadingDocuments, setIsLoadingDocuments] = useState(true);
  const [selectedDocType, setSelectedDocType] = useState<DocType>(docTypes[0]);
  const [fileUrl, setFileUrl] = useState('');
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

  async function upload() {
    setIsUploading(true);
    setError(null);
    try {
      await driverApi.uploadDocument(selectedDocType, fileUrl.trim(), expiresAt.trim() || null);
      setFileUrl('');
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

        <AppTextField label="File URL" value={fileUrl} onChangeText={setFileUrl} placeholder="https://..." />
        <AppTextField label="Expires (optional)" value={expiresAt} onChangeText={setExpiresAt} placeholder="YYYY-MM-DD" />

        {error && <Text style={[styles.error, { color: colors.error }]}>{error}</Text>}

        <View style={styles.spacingTop}>
          <AppButton label="Upload" onPress={upload} loading={isUploading} disabled={!fileUrl.trim()} />
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
            <Text style={[styles.docType, { color: colors.onSurface }]}>{doc.docType}</Text>
            <Text style={[styles.docUrl, { color: colors.onSurfaceVariant }]}>{doc.fileUrl}</Text>
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
  error: { marginTop: 8 },
  spacingTop: { marginTop: 16 },
  sectionTitle: { fontSize: 17, fontWeight: '600', marginBottom: 12 },
  empty: { fontSize: 14 },
  docType: { fontSize: 16, fontWeight: '600' },
  docUrl: { fontSize: 13, marginTop: 4 },
});
