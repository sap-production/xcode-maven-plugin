package com.sap.prd.mobile.ios.mios;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.sap.prd.mobile.ios.mios.CodeSignManager.ExecResult;
import com.sap.prd.mobile.ios.mios.CodeSignManager.ExecutionResultVerificationException;

public class CodeSignManagerTest
{

  private ExecResult validEntitlements;
  private ExecResult invalidEntitlements;
  private File appWithValidEntitlements;
  private File appWithInvalidEntitlements;
  private ExecResult validSecurityCms;
  private ExecResult invalidSecurityCms;

  @Before
  public void before() throws IOException
  {

    appWithValidEntitlements = new File("src/test/resources/MyApp_withEntitlements.app");
    appWithInvalidEntitlements = new File("src/test/resources/MyApp_missingEntitlements.app");

    assertTrue(appWithValidEntitlements.isDirectory());
    assertTrue(appWithInvalidEntitlements.isDirectory());

    validEntitlements = CodeSignManager.getCodesignEntitlementsInformation(appWithValidEntitlements);
    invalidEntitlements = CodeSignManager.getCodesignEntitlementsInformation(appWithInvalidEntitlements);

    validSecurityCms = CodeSignManager.getSecurityCMSInformation(appWithValidEntitlements);
    invalidSecurityCms = CodeSignManager.getSecurityCMSInformation(appWithInvalidEntitlements);
  }

  @Test
  public void testVerifyCodesignEntitlementsValid() throws IOException, ExecutionResultVerificationException
  {
    ExecResult resultFromAppWithValidEntitlements = CodeSignManager
      .getCodesignEntitlementsInformation(appWithValidEntitlements);
    CodeSignManager.verify(resultFromAppWithValidEntitlements, validEntitlements);
  }

  @Test(expected = ExecutionResultVerificationException.class)
  public void testVerifyCodesignEntitlementsInvalid() throws ExecutionResultVerificationException
  {
    CodeSignManager.verify(validEntitlements, invalidEntitlements);

  }

  @Test
  public void testCodesignEntitlementsContainedInValidResult()
  {
    assertTrue(validEntitlements.result.contains("<string>973WEN5QX7.com.sap.MyApp</string>"));
  }

  @Test
  public void testCodesignEntitlementsNotContainedInInvalidResult()
  {
    assertFalse(invalidEntitlements.result.contains("<string>973WEN5QX7.com.sap.MyApp</string>"));
  }

  @Test
  public void testVerifySecurityCmsValid() throws IOException, ExecutionResultVerificationException
  {
    ExecResult cmsSecurityInfoFromAppWithValid = CodeSignManager
      .getSecurityCMSInformation(appWithValidEntitlements);
    CodeSignManager.verify(cmsSecurityInfoFromAppWithValid, validSecurityCms);
  }

  @Test(expected = ExecutionResultVerificationException.class)
  public void testVerifySecurityCmsInvalid() throws ExecutionResultVerificationException
  {
    CodeSignManager.verify(validSecurityCms, invalidSecurityCms);
  }

  @Test
  public void testSecurityCmsContainedInValidResult()
  {
    assertTrue(validSecurityCms.result.contains("<string>973WEN5QX7.com.sap.*</string>"));
  }

}
