package com.sap.prd.mobile.ios.mios;

import org.apache.maven.plugin.logging.Log;

public interface IEffectiveBuildSettings
{
  static final String PRODUCT_NAME = "PRODUCT_NAME";
  static final String SRC_ROOT = "SRCROOT";
  static final String GCC_GENERATE_DEBUGGING_SYMBOLS = "GCC_GENERATE_DEBUGGING_SYMBOLS";
  static final String DEBUG_INFORMATION_FORMAT = "DEBUG_INFORMATION_FORMAT";
  static final String CODE_SIGN_IDENTITY = "CODE_SIGN_IDENTITY";
  static final String CODESIGNING_FOLDER_PATH = "CODESIGNING_FOLDER_PATH";
  static final String INFOPLIST_FILE = "INFOPLIST_FILE";
  static final String PUBLIC_HEADERS_FOLDER_PATH = "PUBLIC_HEADERS_FOLDER_PATH";
  static final String BUILT_PRODUCTS_DIR = "BUILT_PRODUCTS_DIR";
  static final String CONFIGURATION_BUILD_DIR = "CONFIGURATION_BUILD_DIR";

  
  String getBuildSettingByKey(IXCodeContext context, Log log, String key) throws XCodeException;
}