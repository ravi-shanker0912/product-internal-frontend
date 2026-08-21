import React from 'react';
import { useAuth } from '../context/AuthContext';
import AuthStackNavigator from './AuthStackNavigator';
import MainDrawer from './MainDrawer';

export default function RootNavigator() {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <MainDrawer /> : <AuthStackNavigator />;
}
