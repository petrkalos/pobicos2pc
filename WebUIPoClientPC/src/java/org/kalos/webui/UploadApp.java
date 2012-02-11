/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kalos.webui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.*;

public class UploadApp extends CustomComponent
                        implements Upload.SucceededListener,
                                   Upload.FailedListener,
                                   Upload.Receiver {

    Panel root;         // Root element for contained components.
    File  file;         // File to write to.

    public UploadApp() {
        root = new Panel("My Upload Component");
        //setCompositionRoot(root);

        // Create the Upload component.
        final Upload upload =
                new Upload("Upload the application here", this);

        // Use a custom button caption instead of plain "Upload".
        upload.setButtonCaption("Upload Now");

        // Listen for events regarding the success of upload.
        upload.addListener((Upload.SucceededListener) this);
        upload.addListener((Upload.FailedListener) this);

        root.addComponent(upload);

    }

    // Callback method to begin receiving the upload.
    public OutputStream receiveUpload(String filename,
                                      String MIMEType) {
        FileOutputStream fos = null; // Output stream to write to
        file = new File("app.hex");
        try {
            // Open the file for writing.
            fos = new FileOutputStream(file);
        } catch (final java.io.FileNotFoundException e) {
            // Error while opening the file. Not reported here.
            e.printStackTrace();
            return null;
        }

        return fos; // Return the output stream to write to
    }

    // This is called if the upload is finished.
    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        // Log the upload on screen.
        root.addComponent(new Label("File " + event.getFilename()
                + " uploaded."));
    }

    // This is called if the upload fails.
    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        // Log the failure on screen.
        root.addComponent(new Label("Uploading "
                + event.getFilename() + " of type failed."));
    }
}
