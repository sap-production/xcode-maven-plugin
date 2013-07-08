package com.sap.prd.mobile.ios.mios;

import java.util.Map;

interface ISettings
{

  Map<String, String> getAllSettings();
  Map<String, String> getUserSettings();
  Map<String, String> getManagedSettings();

}