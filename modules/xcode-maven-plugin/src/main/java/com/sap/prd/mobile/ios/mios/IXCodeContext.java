package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.PrintStream;

public interface IXCodeContext
{

  String getSDK();
  String getConfiguration();
  File getProjectRootDirectory();
  IOptions getOptions();
  ISettings getSettings();
  PrintStream getOut();

}