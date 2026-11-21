package org.gephi.branding.desktop;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class InstallerTest {

    @Test
    public void testNewVersion() {
        Assert.assertFalse(Installer.isNewVersion("0.9.2", "0.9.2"));
        Assert.assertTrue(Installer.isNewVersion("0.10.0", "0.9.2"));
        Assert.assertTrue(Installer.isNewVersion("0.12.0", "0.11.2"));
        Assert.assertTrue(Installer.isNewVersion("1.0.0", "0.10.0"));
    }

    @Test
    public void testPatchVersionIgnored() {
        Assert.assertFalse(Installer.isNewVersion("0.9.3", "0.9.2"));
        Assert.assertFalse(Installer.isNewVersion("0.10.1", "0.10.0"));
        Assert.assertFalse(Installer.isNewVersion("0.11.3", "0.11.2"));
        Assert.assertFalse(Installer.isNewVersion("0.11.3", "0.11.2 202605011234"));
    }

    @Test
    public void testVersionFromReleaseUrl() {
        Assert.assertEquals("0.11.4",
            Installer.versionFromReleaseUrl("https://github.com/gephi/gephi/releases/tag/v0.11.4"));
        Assert.assertEquals("0.12.0", Installer.versionFromReleaseUrl("/gephi/gephi/releases/tag/0.12.0"));
        Assert.assertNull(Installer.versionFromReleaseUrl("https://github.com/gephi/gephi/releases"));
        Assert.assertNull(Installer.versionFromReleaseUrl(null));
        Assert.assertNull(Installer.versionFromReleaseUrl("https://github.com/gephi/gephi/releases/tag/v0.12<b>x</b>"));
        Assert.assertNull(Installer.versionFromReleaseUrl("https://github.com/gephi/gephi/releases/tag/v0.12.0-rc1"));
    }

    @Test
    public void testFetchLatestVersionFromGitHub() {
        String latest;
        try {
            latest = Installer.fetchLatestVersion();
        } catch (IOException ex) {
            Assume.assumeNoException("GitHub isn't reachable", ex);
            return;
        }
        Assert.assertNotNull("GitHub's latest release page no longer redirects to a vX.Y.Z tag", latest);
        Assert.assertFalse("Latest release " + latest + " is older than 0.11", Installer.isNewVersion("0.11.0", latest));
    }

    @Test
    public void testOlderLatestVersion() {
        Assert.assertFalse(Installer.isNewVersion("0.10.0", "0.11.0"));
        Assert.assertFalse(Installer.isNewVersion("0.11", "0.11.2"));
    }
}
