package com.sap.prd.mobile.ios.mios;

import java.io.File;

abstract class ProjectModifier
{
  protected File testExecutionDirectory = null;
  
  final void setTestExecutionDirectory(File testExecutionDirectory) {
    this.testExecutionDirectory = testExecutionDirectory;
  }
  abstract void execute() throws Exception;
}
