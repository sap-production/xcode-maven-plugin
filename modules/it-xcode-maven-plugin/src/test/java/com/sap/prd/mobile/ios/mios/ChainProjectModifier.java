package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ChainProjectModifier extends ProjectModifier
{

  @Override
  void setTestExecutionDirectory(File testExecutionDirectory)
  {
    super.setTestExecutionDirectory(testExecutionDirectory);
    for (ProjectModifier projectModifier : projectModifiers) {
      projectModifier.setTestExecutionDirectory(testExecutionDirectory);
    }
  }

  private final List<ProjectModifier> projectModifiers;
  
  public ChainProjectModifier(ProjectModifier... projectModifiers)
  {
    this.projectModifiers = Arrays.asList(projectModifiers);
  }
  
  @Override
  void execute() throws Exception
  {
    for(ProjectModifier projectModifier : projectModifiers) {
      projectModifier.execute();
    }
  }
}
