package com.plociennik.vestal.git;

import com.plociennik.vestal.common.VestalException;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class BrowserLauncher {

    public static void openUrl(String url) {
        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("nux") || os.contains("nix")) {
            if (tryXdgOpen(url)) return;
            if (tryAwtDesktop(url)) return;
        } else {
            if (tryAwtDesktop(url)) return;
            if (tryXdgOpen(url)) return;
        }
        throw new VestalException("1326_270726", "Could not open a browser automatically.");
    }

    private static boolean tryAwtDesktop(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean tryXdgOpen(String url) {
        try {
            Process process = new ProcessBuilder("xdg-open", url)
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            if (!finished) {
                return true;
            }
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return false;
        }
    }
}
