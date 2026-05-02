package org.gephi.visualization.screenshot;

import static org.gephi.visualization.screenshot.ScreenshotModelImpl.LAST_PATH;
import static org.gephi.visualization.screenshot.ScreenshotModelImpl.LAST_PATH_DEFAULT;

import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

public class ScreenshotTask implements LongTask, Runnable {

    private static final String DATE_FORMAT_NOW = "HHmmss";

    private ProgressTicket progressTicket;
    private final ScreenshotModelImpl model;
    private final JOGLRenderingTarget renderingTarget;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final BooleanSupplier isCancelled = cancelled::get;
    private File file;

    public ScreenshotTask(VizEngine<JOGLRenderingTarget, ?> engine, ScreenshotModelImpl model) {
        this.model = model;
        this.renderingTarget = engine.getRenderingTarget();
    }

    @Override
    public void run() {
        beforeTaking();

        int scaleFactor = model.getScaleFactor();
        boolean transparentBackground = model.isTransparentBackground();
        try {
            BufferedImage image =
                renderingTarget.requestScreenshot(scaleFactor, transparentBackground, isCancelled).get();

            // Write image to file
            // Get File
            SwingUtilities.invokeAndWait(() -> {
                if (!model.isAutoSave()) {
                    //Get last directory
                    String lastPathDefault =
                        NbPreferences.forModule(ScreenshotTask.class).get(LAST_PATH_DEFAULT, null);
                    String lastPath =
                        NbPreferences.forModule(ScreenshotTask.class).get(LAST_PATH, lastPathDefault);
                    final JFileChooser chooser = new JFileChooser(lastPath);
                    chooser.setAcceptAllFileFilterUsed(false);
                    chooser.setDialogTitle(
                        NbBundle.getMessage(ScreenshotTask.class, "ScreenshotMaker.filechooser.title"));
                    DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle
                        .getMessage(ScreenshotTask.class, "ScreenshotMaker.filechooser.pngDescription"));
                    dialogFileFilter.addExtension("png");
                    chooser.addChoosableFileFilter(dialogFileFilter);
                    File selectedFile = new File(chooser.getCurrentDirectory(), getDefaultFileName() + ".png");
                    chooser.setSelectedFile(selectedFile);
                    int returnFile = chooser.showSaveDialog(null);
                    if (returnFile == JFileChooser.APPROVE_OPTION) {
                        this.file = chooser.getSelectedFile();

                        if (!ScreenshotTask.this.file.getPath().endsWith(".png")) {
                            this.file = new File(this.file.getPath() + ".png");
                        }

                        //Save last path
                        NbPreferences.forModule(ScreenshotTask.class)
                            .put(LAST_PATH, this.file.getParentFile().getAbsolutePath());
                    } else {
                        this.file = null;
                    }
                } else {
                    this.file = new File(model.getDefaultDirectory(), getDefaultFileName() + ".png");
                }
            });

            // Write file
            if (file != null) {
                javax.imageio.ImageIO.write(image, "png", file);
            }

        } catch (CancellationException e) {
            // Task cancelled, do nothing
            final String msg = NbBundle
                .getMessage(ScreenshotControllerImpl.class, "ScreenshotMaker.progress.cancelled");
            StatusDisplayer.getDefault().setStatusText(msg);
        } catch (InterruptedException | ExecutionException | IOException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            afterTaking();
        }
    }

    private void beforeTaking() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
    }

    private void afterTaking() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                WindowManager.getDefault().getMainWindow().setCursor(Cursor.getDefaultCursor());
                if (file != null) {
                    if (model.isAutoSave()) {
                        final String msg = NbBundle
                            .getMessage(ScreenshotControllerImpl.class, "ScreenshotMaker.finishedMessage.message",
                                file.getAbsolutePath());
                        StatusDisplayer.getDefault().setStatusText(msg);
                    } else {
                        final String msg = NbBundle
                            .getMessage(ScreenshotControllerImpl.class, "ScreenshotMaker.finishedMessage.message",
                                file.getName());
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg,
                            NbBundle.getMessage(ScreenshotControllerImpl.class,
                                "ScreenshotMaker.finishedMessage.title"),
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }

    private static String getDefaultFileName() {
        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_NOW);
        String datetime = dateFormat.format(cal.getTime());

        return "screenshot_" + datetime;
    }

    @Override
    public boolean cancel() {
        cancelled.set(true);
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
