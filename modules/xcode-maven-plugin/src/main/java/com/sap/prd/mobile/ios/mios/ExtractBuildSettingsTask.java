package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;

public class ExtractBuildSettingsTask
{
  private XCodeContext ctx;
  private String configuration, sdk;
  private File buildDirectory;
  
  
  
  public ExtractBuildSettingsTask setCtx(XCodeContext ctx)
  {
    this.ctx = ctx;
    return this;
  }

  public ExtractBuildSettingsTask setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }

  public ExtractBuildSettingsTask setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }

  public ExtractBuildSettingsTask setBuildDirectory(File buildDirectory)
  {
    this.buildDirectory = buildDirectory;
    return this;
  }

  public void execute() throws XCodeException {
    
    CommandLineBuilder cmdLineBuilder = new CommandLineBuilder(configuration, sdk, ctx);
    PrintStream out = null;
    try {
      out = new PrintStream(EffectiveBuildSettings.getBuildSettingsFile(this.buildDirectory, configuration, sdk));
      final int returnValue = Forker.forkProcess(out, ctx.getProjectRootDirectory(), cmdLineBuilder.createShowBuildSettingsCall());
      if (returnValue != 0) {
        throw new XCodeException("Could not execute xcodebuild -showBuildSettings command for configuration " + configuration);
      }
    } catch(final IOException ex) {
      throw new XCodeException(ex.getMessage(), ex);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }
}
