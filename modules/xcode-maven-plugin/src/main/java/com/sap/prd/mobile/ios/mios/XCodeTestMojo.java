package com.sap.prd.mobile.ios.mios;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import au.com.rayh.XcodeBuildOutputParser;

/**
 * Run the build with tests.
 * @author John Bito <jwbito@gmail.com>
 *
 * @goal xcodetest
 * @requiresDependencyResolution
 */
public class XCodeTestMojo extends XCodeBuildMojo 
{
  @Override
  protected void callXcodeBuild(XCodeContext ctx, String configuration,
		String sdk) throws XCodeException, IOException {
	PrintStream origOut = ctx.getOut(),
			  out = new PrintStream(new XcodeBuildOutputParser(new File("."), origOut).getOutputStream());
	ctx.setOut(out);
	try {
      xcodeMgr.callXcodeBuild(ctx, configuration, sdk, true);
	} finally {
		out.flush();
		ctx.setOut(origOut);
	}
  }
}
