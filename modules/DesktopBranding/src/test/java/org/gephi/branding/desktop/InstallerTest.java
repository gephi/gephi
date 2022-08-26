package org.gephi.branding.desktop;

import org.junit.Assert;
import org.junit.Test;

public class InstallerTest {

    @Test
    public void testNewVersion() {
        Assert.assertFalse(Installer.isNewVersion("0.9.2", "0.9.2"));
        Assert.assertTrue(Installer.isNewVersion("0.9.3", "0.9.2"));
        Assert.assertTrue(Installer.isNewVersion("0.10.0", "0.9.2"));
        Assert.assertTrue(Installer.isNewVersion("0.10.1", "0.10.0"));
        Assert.assertTrue(Installer.isNewVersion("1.0.0", "0.10.0"));
    }
}
