import React from 'react';
import { createDrawerNavigator } from '@react-navigation/drawer';
import AppStackNavigator from './AppStackNavigator';
import MainDrawerContent from './MainDrawerContent';

const Drawer = createDrawerNavigator();

export default function MainDrawer() {
  return (
    <Drawer.Navigator
      drawerContent={(props) => <MainDrawerContent {...props} />}
      screenOptions={{ headerShown: false, drawerType: 'front' }}
    >
      <Drawer.Screen name="AppStack" component={AppStackNavigator} />
    </Drawer.Navigator>
  );
}
