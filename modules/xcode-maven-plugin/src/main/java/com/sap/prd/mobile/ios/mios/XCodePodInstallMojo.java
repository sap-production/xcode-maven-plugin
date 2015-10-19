package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Removes existing Pods and performs <code>pod install</code> execution in project compilation directory.
 *
 * @goal pod-install
 */
public class XCodePodInstallMojo extends AbstractMojo {

  private static final Logger LOGGER = LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName());

  private static final String[] POD_INSTALL = new String[]{ "pod", "install" };
  private static final String[] RM_POD_FILE_LOCK = new String[]{ "rm", "Podfile.lock" };
  private static final String[] RM_PODS = new String[]{"rm", "-r", "-f", "./Pods"};

  /**
   * The xcode directory of the copied sources below the checkout directory.
   *
   * @parameter expression="${xcode.compileDirectory}"
   */
  private File xcodeCompileDirectory;

  /**
   * Indicates whenever plugin should perform <code>pod install</code> before executing <code>xcodebuild</code>.
   *
   * @parameter expression="${xcode.installPods}"
   */
  private boolean installPods;

  @Override
  public void execute() throws MojoExecutionException {
    if (installPods == false) {
      return;
    }

    try {
      LOGGER.info("Removing `Podfile.lock` ...");
      Forker.forkProcess(System.out, xcodeCompileDirectory, RM_POD_FILE_LOCK);

      LOGGER.info("Removing `Pods` directory ...");
      Forker.forkProcess(System.out, xcodeCompileDirectory, RM_PODS);

      LOGGER.info("Executing `pod install` command ... ");
      final int returnValue = Forker.forkProcess(System.out, xcodeCompileDirectory, POD_INSTALL);

      if (returnValue != 0) {
        throw new MojoExecutionException("Could not execute `pod install`.");
      }

    } catch (final IOException e) {
      throw new MojoExecutionException("Could not execute `pod install`.");
    }
  }
}
