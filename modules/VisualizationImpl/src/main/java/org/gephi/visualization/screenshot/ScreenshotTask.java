package org.gephi.visualization.screenshot;

import java.awt.Cursor;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class ScreenshotTask implements LongTask, Runnable{

    private static final String DATE_FORMAT_NOW = "HHmmss";

    private ProgressTicket progressTicket;
    private ScreenshotModelImpl model;
    private boolean cancel = false;
    private File file;
//    private int tileWidth = width / 16;
//    private int tileHeight = height / 12;

    public ScreenshotTask(ScreenshotModelImpl model) {
        this.model = model;
    }

    @Override
    public void run() {
        beforeTaking();
        afterTaking();
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
                            NbBundle.getMessage(ScreenshotControllerImpl.class, "ScreenshotMaker.finishedMessage.title"),
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
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
