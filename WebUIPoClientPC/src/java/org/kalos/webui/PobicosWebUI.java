/*
 * PobicosWebUI.java
 *
 * Created on February 6, 2012, 4:23 PM
 */
package org.kalos.webui;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.terminal.Sizeable;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kalos.clientmgr.NodeInfo;
import org.kalos.clientmgr.Reactor;
import org.kalos.clientmgr.Registry;
import org.lekkas.poclient.PoMainA;
import org.vaadin.hezamu.googlemapwidget.GoogleMap;
import org.vaadin.hezamu.googlemapwidget.overlay.BasicMarker;

/** 
 *
 * @author kalos
 * @version 
 */
public class PobicosWebUI extends Application {

    Window mainWindow;
    Panel mainPanel, leftPanel, rightPanel;
    Panel appMsg, midMsg;
    GoogleMap map;
    Thread midthread;

    public class ChatRefreshListener implements RefreshListener {

        @Override
        public void refresh(final Refresher source) {
            refreshLog();
        }
    }

    @Override
    public void init() {
        initWindow();
        initButtons();
        initUpload();
    }

    public void initWindow() {
        mainWindow = new Window("PobicosWebUI");
        mainWindow.setSizeFull();

        setMainWindow(mainWindow);

        SplitPanel splitPanel = new SplitPanel(
                SplitPanel.ORIENTATION_HORIZONTAL);
        splitPanel.setLocked(true);
        splitPanel.setSizeFull();

        VerticalLayout layout = new VerticalLayout();

        mainPanel = new Panel("PobicosWebUI");
        Label label = new Label("Welcome to POBICOS Server Web UI");
        label.setSizeUndefined();
        layout.addComponent(label);
        layout.setComponentAlignment(label, Alignment.TOP_CENTER);
        mainPanel.addComponent(layout);

        leftPanel = new Panel("Middleware Control");
        leftPanel.setSizeFull();
        leftPanel.setImmediate(true);
        leftPanel.setHeight(480, Sizeable.UNITS_PIXELS);
        rightPanel = new Panel("Application Control");
        rightPanel.setSizeFull();
        rightPanel.setHeight(480, Sizeable.UNITS_PIXELS);

        splitPanel.addComponent(leftPanel);
        splitPanel.addComponent(rightPanel);

        mainWindow.addComponent(mainPanel);
        mainPanel.addComponent(splitPanel);

        appMsg = new Panel();
        appMsg.setSizeFull();
        appMsg.setHeight(240, Sizeable.UNITS_PIXELS);
        appMsg.setImmediate(true);
        appMsg.setScrollable(true);
        appMsg.setScrollTop(100000);
        midMsg = new Panel();
        midMsg.setSizeFull();
        midMsg.setHeight(240, Sizeable.UNITS_PIXELS);
        midMsg.setImmediate(true);
        midMsg.setScrollable(true);
        midMsg.setScrollTop(100000);

        final Refresher refresher = new Refresher();
        refresher.setRefreshInterval(200);
        refresher.addListener(new ChatRefreshListener());
        layout.addComponent(refresher);

        map = new GoogleMap(this,
                new Point2D.Double(37.160317, 49.570313), 3);
        map.setWidth("640px");
        map.setHeight("480px");
        map.setImmediate(true);

        mainWindow.addComponent(map);
    }

    public void initButtons() {

        Button startMidButton = new Button("Start Middleware", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                midthread = new Thread("Middleware Thread Start") {

                    @Override
                    public void run() {
                        if (!PoMainA.isRunning()) {
                            PoMainA.startMiddleware("192.168.178.29");
                        }
                    }
                };
                midthread.start();
            }
        });


        Button startApp = new Button("Start App", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Thread t = new Thread() {

                    @Override
                    public void run() {
                        PoMainA.startApp("app.hex");
                    }
                };
                t.start();
            }
        });

        Button stopAppButton = new Button("Stop App", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Thread t = new Thread() {

                    @Override
                    public void run() {
                        //PoMainA.stopApp();
                    }
                };
                t.start();
            }
        });

        Button stopMidButton = new Button("Stop Middleware", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Thread t = new Thread("Middleware Thread Stop") {

                    @Override
                    public void run() {
                        PoMainA.stopMid();
                    }
                };
                t.start();
                midthread.interrupt();
                midthread.stop();
            }
        });

        leftPanel.addComponent(startMidButton);
        rightPanel.addComponent(startApp);
        rightPanel.addComponent(stopAppButton);
        leftPanel.addComponent(stopMidButton);

        rightPanel.addComponent(appMsg);
        leftPanel.addComponent(midMsg);

    }

    public void initUpload() {
        UploadApp uploader = new UploadApp();
        rightPanel.addComponent(uploader.root);
    }

    public void refreshLog() {
        do {
            String str = (String) org.kalos.Log.msgQueue.poll();
            if (str == null) {
                break;
            }
            if (str.contains("APP:")) {
                appMsg.addComponent(new Label(str));
            } else {
                midMsg.addComponent(new Label(str));
            }
            for (NodeInfo n : Reactor.registry.reg) {
                if (n.getLat() != -1 && n.getLon() != -1) {
                    map.addMarker(new BasicMarker(1L,
                            new Point2D.Double(n.getLat(), n.getLon()), "Pobicos node addr " + n.getPoNodeAddr()));
                }
            }
        } while (true);
    }
}
