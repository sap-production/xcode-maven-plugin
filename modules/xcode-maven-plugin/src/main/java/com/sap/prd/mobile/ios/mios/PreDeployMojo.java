package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferListener;

/**
 * Registers a TransferListener on Aether that reacts on successful deployments. For each ipa file
 * deployed successfully a pointer file is written. This pointer file redirects to XXX
 * 
 * 
 * @goal pre-deploy
 * 
 */
public class PreDeployMojo extends AbstractXCodeMojo
{

  /**
   * The folder used for hudson archiving
   * 
   * @parameter expression="${archive.dir}" default-value="${project.build.directory}"
   * @readonly
   */
  private File archiveFolder;

  /**
   * The current repository/network configuration of Maven.
   * 
   * @parameter default-value="${repositorySystemSession}"
   * @readonly
   */
  protected RepositorySystemSession repoSession;

  private final static String HTML_TEMPLATE = "<html><head><script type=\"text/javascript\">function redirect() {window.location.href=\"$LOCATION\"}</script></head><body onload=\"redirect()\">You will be redirected within the next few seconds.<br />In case this does not work click <a href=\"$LOCATION\">here</a></body></html>";

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    class PrepareIpaPointerFileTransferListener implements TransferListener
    {
      private TransferListener forward;

      PrepareIpaPointerFileTransferListener(TransferListener forward)
      {
        this.forward = forward;
      }

      @Override
      public void transferSucceeded(TransferEvent event)
      {
        if (forward != null)
          forward.transferSucceeded(event);

        if (!event
          .getResource()
          .getResourceName()
          .endsWith(
                XCodeOtaHtmlGeneratorMojo.OTA_CLASSIFIER_APPENDIX + "."
                      + XCodeOtaHtmlGeneratorMojo.OTA_HTML_FILE_APPENDIX))
        {
          return;
        }

        try {

          final String url = event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
          final String html = HTML_TEMPLATE.replaceAll("\\$LOCATION", url);

          writeIpaHtmlFile(archiveFolder, url, html);

        }
        catch (RuntimeException ex) {

          getLog().error("Could not create IPA pointer file for '" + event.getResource().getResourceName(), ex);
        }
      }

      @Override
      public void transferStarted(TransferEvent event) throws TransferCancelledException
      {
        if (forward != null)
          forward.transferStarted(event);
      }

      @Override
      public void transferProgressed(TransferEvent event) throws TransferCancelledException
      {
        if (forward != null)
          forward.transferProgressed(event);
      }

      @Override
      public void transferInitiated(TransferEvent event) throws TransferCancelledException
      {
        if (forward != null)
          forward.transferInitiated(event);
      }

      @Override
      public void transferFailed(TransferEvent event)
      {
        if (forward != null)
          forward.transferFailed(event);
      }

      @Override
      public void transferCorrupted(TransferEvent event) throws TransferCancelledException
      {
        if (forward != null)
          forward.transferCorrupted(event);
      }

      private void writeIpaHtmlFile(final File archiveFolder, final String url, final String html)
      {
        Writer w = null;

        try {

          final File f = new File(new File(new File(archiveFolder, "ota"), project.getGroupId()),
                project.getArtifactId());

          if (!f.exists() && !f.mkdirs())
            throw new IOException("Cannot create folder '" + f + "'.");

          w = new FileWriter(new File(f, getIpaPointerFileName(url)));

          IOUtils.write(html, w);
        }
        catch (IOException ex) {
          throw new RuntimeException(ex);
        }
        finally {
          IOUtils.closeQuietly(w);
        }
      }

      private String getIpaPointerFileName(final String url)
      {
        String[] parts = url.substring(url.lastIndexOf("/")).split("-");
        return parts[parts.length - 3] + "-" + parts[parts.length - 2] + "-" + parts[parts.length - 1];
      }
    }

    try {
      archiveFolder = archiveFolder.getCanonicalFile();
    }
    catch (IOException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }

    sanityChecks(archiveFolder);

    getLog().info("Using archive directory: '" + archiveFolder + "'.");

    try {

      //
      // [Q] Why do we use reflection here? What about a simple downcast to DefaultRepositorySystemSession?
      // [A] We cannot downcast since the downcast fails. The DefaultRepositorySystemSession that is available
      //     here is loaded by another class loader than the DefaultRepositorySystemSession used by the actual class.
      //     Hence we are in different runtime packages and the downcast is not possible.

      final TransferListener transferListener = (TransferListener) this.repoSession.getClass()
        .getMethod("getTransferListener", new Class[0]).invoke(this.repoSession, new Object[0]);

      TransferListener prepareIpaPointerFileTransferListener = new PrepareIpaPointerFileTransferListener(
            transferListener);

      this.repoSession.getClass().getMethod("setTransferListener", new Class[] { TransferListener.class })
        .invoke(this.repoSession, prepareIpaPointerFileTransferListener);

      getLog().info(
            "TransferListener '" + prepareIpaPointerFileTransferListener.getClass().getName() + "' has been set.");
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new MojoExecutionException(
            "Cannot set transfer listener for creating ipa pointer files: " + e.getMessage(), e);
    }
  }

  private static void sanityChecks(File archiveFolder) throws MojoExecutionException
  {
    if (archiveFolder.exists() && archiveFolder.isFile())
      throw new MojoExecutionException("Archive folder '" + archiveFolder + "' is a file rather than a directory.");

    if (!archiveFolder.exists() && !archiveFolder.mkdirs())
      throw new MojoExecutionException("Cannot create archive folder '" + archiveFolder + "'.");
  }
}
