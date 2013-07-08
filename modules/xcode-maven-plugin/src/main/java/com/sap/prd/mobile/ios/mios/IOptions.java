package com.sap.prd.mobile.ios.mios;

import java.util.Map;

interface IOptions
{

  Map<String, String> getAllOptions();
  Map<String, String> getManagedOptions();
  Map<String, String> getUserOptions();

}