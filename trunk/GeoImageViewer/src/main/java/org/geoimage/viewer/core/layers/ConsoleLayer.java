/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import javax.persistence.EntityManagerFactory;
import javax.swing.JTabbedPane;

import org.geoimage.viewer.core.api.GeoContext;

import java.util.List;

import org.geoimage.viewer.core.*;
import org.geoimage.viewer.core.api.ILayerManager;
import org.geoimage.viewer.core.api.ILayer;
import org.geoimage.viewer.core.api.IConsoleAction;
import org.geoimage.viewer.core.api.IImageLayer;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.geoimage.def.GeoImageReader;
import org.geoimage.utils.IProgress;
import org.geoimage.viewer.util.ClassPathHacker;
import org.geoimage.viewer.widget.ActionDialog;

/**
 *
 * @author thoorfr
 */
public class ConsoleLayer implements ILayer {

    private String message = "";
    private String oldMessage = "";
    private Map<String, Plugins> actions;
    private String[] commands;
    private IProgress currentAction = null;
    private final EntityManagerFactory emf;

    public ConsoleLayer() {
        emf = Persistence.createEntityManagerFactory("GeoImageViewerPU");
        actions = new HashMap<String, Plugins>();
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery("select p from Plugins p");
        List<Plugins> plugins = q.getResultList();
        em.getTransaction().commit();

        if (plugins.size() == 0) {
            populateDatabase();
        }
        em.getTransaction().begin();
        plugins = q.getResultList();
        em.getTransaction().commit();

        em.close();
        parseActions(plugins);

    }

    public void execute(String[] arguments) {
        for (String c : commands) {
            if (c.startsWith(arguments[0])) {
                String[] args = new String[arguments.length - 1];
                for (int i = 1; i < arguments.length; i++) {
                    args[i - 1] = arguments[i];
                }
                try {
                    if (!actions.get(c).isActive()) {
                        return;
                    }
                    Object a = Class.forName(actions.get(c).getClassName()).newInstance();
                    if (!(a instanceof IConsoleAction)) {
                        return;
                    }
                    IConsoleAction ica = (IConsoleAction) a;
                    if (ica instanceof IProgress) {
                        currentAction = (IProgress) ica;
                    }
                    ica.execute(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        try {
            if (message.startsWith("google")) {
                for (ILayer l : Platform.getLayerManager().getLayers()) {
                    if (l instanceof IImageLayer) {
                        GeoImageReader gir = ((IImageLayer) l).getImageReader();
                        double[] x0 = gir.getGeoTransform().getGeoFromPixel(0, 0, "EPSG:4326");
                        double[] x1 = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), gir.getHeight(), "EPSG:4326");
                        Desktop.getDesktop().browse(new URI("http://maps.google.com/?ie=UTF8&ll=" + (x1[1] + x0[1]) / 2 + "," + (x0[0] + x1[0]) / 2 + "&spn=0.009676,0.020084&t=h&z=9"));
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        try {
            if (message.startsWith("level")) {
                for (ILayer l : Platform.getLayerManager().getLayers()) {
                    if (l instanceof IImageLayer) {
                        ((IImageLayer) l).level(Integer.parseInt(message.split(" ")[1]));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void runScript(String file) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        String line = null;
        try{
	        while ((line = raf.readLine()) != null) {
	            String[] command = line.split(" ");
	            for (String c : commands) {
	                if (c.startsWith(command[0])) {
	                    String[] args = new String[command.length - 1];
	                    for (int i = 1; i < command.length; i++) {
	                        args[i - 1] = command[i];
	                    }
	                    try {
	                        Object a = Class.forName(actions.get(c).getClassName()).newInstance();
	                        if (!(a instanceof IConsoleAction)) {
	                            return;
	                        }
	                        IConsoleAction ica = (IConsoleAction) a;
	                        if (ica instanceof IProgress) {
	                            currentAction = (IProgress) ica;
	                            if (ica.execute(args)) {
	                                return;
	                            }
	                            while (currentAction != null && !currentAction.isDone()) {
	                                Thread.sleep(1000);
	                            }
	                        } else {
	                            if (!ica.execute(args)) {
	                                return;
	                            }
	                        }
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                        return;
	                    }
	                }
	            }
	            Thread.sleep(1000);
	        }
        }finally{    
        	raf.close();
        }	
    }

    public void runScriptString(String script) throws Exception {
        String[] actionsscript = script.split("\n");
        for (int index = 0; index < actionsscript.length; index++) {
            String[] command = parseActions(actionsscript[index]);
            if ((command.length != 0) && !command[0].isEmpty()) {
                for (String c : commands) {
                    if (c.startsWith(command[0])) {
                        String[] args = new String[command.length - 1];
                        for (int i = 1; i < command.length; i++) {
                            args[i - 1] = command[i];
                        }
                        try {

                            Object a = Class.forName(actions.get(c).getClassName(), true, new URLClassLoader(new URL[]{new URL(actions.get(c).getJarUrl())}, ClassLoader.getSystemClassLoader()));
                            if (!(a instanceof IConsoleAction)) {
                                return;
                            }
                            IConsoleAction ica = (IConsoleAction) a;
                            if (ica instanceof IProgress) {
                                currentAction = (IProgress) ica;
                                if (!ica.execute(args)) {
                                    return;
                                }
                                while (currentAction != null && !currentAction.isDone()) {
                                    Thread.sleep(1000);
                                }
                            } else {
                                if (!ica.execute(args)) {
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            }
            Thread.sleep(1000);
        }
    }

    private void parseActions(List<Plugins> plugins) {
        for (Plugins p : plugins) {
            try {
                Object temp = instanciate(p);
                if (temp instanceof IConsoleAction) {
                    IConsoleAction action = (IConsoleAction) temp;
                    getActions().put(action.getName(), p);
                    System.out.println(temp.toString() + " added");
                }
            } catch (Exception ex) {
                //Logger.getLogger(ConsoleLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        commands = getActions().keySet().toArray(new String[getActions().size()]);
    }

    public String getName() {
        return message;
    }

    public void setName(String name) {
        //do nothing
    }

    public void render(GeoContext context) {
        if (currentAction != null) {
            if (currentAction.isDone()) {
                Platform.setInfo(((IConsoleAction) currentAction).getName() + " done", 10000);
                currentAction = null;
            } else {
                if (currentAction.isIndeterminate()) {
                    Platform.setInfo(currentAction.getMessage());
                } else {
                    Platform.setInfo(currentAction.getCurrent() + "/" + currentAction.getMaximum() + " " + currentAction.getMessage());
                }

            }
        }
    }

    private IConsoleAction instanciate(Plugins p) {
        try {
            ClassPathHacker.addFile(new File(new URI(p.getJarUrl())));
            return (IConsoleAction) Class.forName(p.getClassName()).newInstance();
        } catch (Exception ex) {
            Logger.getLogger(ConsoleLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean isActive() {
        return true;
    }

    public void setActive(boolean active) {
    }

    public boolean isRadio() {
        return false;
    }

    public ILayerManager getParent() {
        return null;
    }

    public String getDescription() {
        return "Inline Console";
    }

    public void addChar(char c) {
        if (c == '\n') {
            if (message.equals("")) {
                execute(oldMessage);
            } else {
                execute(this.message);
                oldMessage = message;
                this.message = "";
            }

        } else if (c == '\b' & this.message.length() > 0) {
            this.message = this.message.substring(0, this.message.length() - 1);
        } else if (c != '\b') {
            this.message += c;
        }
        try {
            Platform.refresh();
        } catch (Exception ex) {
            Logger.getLogger(ConsoleLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void execute(String message) {
        execute(parseActions(message));
    }

    public JMenuBar getMenuBar() {
        JMenuBar bar = new JMenuBar();
        Map<String, JMenuItem> menus = new Hashtable<String, JMenuItem>();
        JMenuItem temp = null;
        for (Plugins p : getActions().values()) {
            final IConsoleAction action = instanciate(p);
            String[] path = action.getPath().split("/");
            JMenuItem mitem = null;
            String mediumpath = "";
            for (int i = 0; i < path.length; i++) {
                mediumpath =new StringBuilder(mediumpath).append(path[i]).append("/").toString();
                if (menus.containsKey(mediumpath)) {
                    temp = menus.get(mediumpath);
                } else {

                    if (i == path.length - 1) {
                        temp = new JMenuItem(new AbstractAction(path[i]) {

                            public void actionPerformed(ActionEvent e) {
                                if (action.getArgumentTypes() != null) {
                                    new ActionDialog(JFrame.getFrames()[0], true, action).setVisible(true);
                                } else {
                                    action.execute(null);
                                }
                            }
                        });

                    } else {
                        temp = new JMenu(path[i]);
                    }

                    if (i < path.length - 1) {
                        menus.put(mediumpath, temp);
                    }
                }

                if (mitem == null) {
                    mitem = temp;
                    bar.add((JMenu) temp);
                } else {
                    mitem.add(temp);
                    mitem = temp;
                }

            }
        }

        return bar;
    }

    public List<JMenu> getMenus() {
        List<JMenu> out = new Vector<JMenu>();
        Map<String, JMenuItem> menus = new Hashtable<String, JMenuItem>();
        JMenuItem temp = null;
        for (Plugins p : getActions().values()) {
            final IConsoleAction action = instanciate(p);
            String[] path = action.getPath().split("/");
            JMenuItem mitem = null;
            String mediumpath = "";
            for (int i = 0; i < path.length; i++) {
                mediumpath =new StringBuilder(mediumpath).append(path[i]).append("/").toString();
                if (menus.containsKey(mediumpath)) {
                    temp = menus.get(mediumpath);
                } else {

                    if (i == path.length - 1) {
                        temp = new JMenuItem(new AbstractAction(path[i]) {

                            public void actionPerformed(ActionEvent e) {
                                if (action.getArgumentTypes() != null) {
                                    new ActionDialog(JFrame.getFrames()[0], true, action).setVisible(true);
                                } else {
                                    action.execute(null);
                                }
                            }
                        });
                        temp.setToolTipText(action.getDescription());
                    } else {
                        temp = new JMenu(path[i]);
                    }
                    menus.put(mediumpath, temp);
                }

                if (mitem == null) {
                    mitem = temp;
                    out.add((JMenu) temp);
                } else {
                    mitem.add(temp);
                    mitem = temp;
                }
            }
        }

        return out;
    }

    public void setMenus(JMenuBar menubar) {
        Map<String, JMenuItem> menus = new Hashtable<String, JMenuItem>();
        // fill with existing menu items
        for (int i = 0; i < menubar.getMenuCount(); i++) {
            JMenu menu = menubar.getMenu(i);
            String menutext = menu.getText() + "/";
            menus.put(menutext, menu);
            for (int j = 0; j < menu.getMenuComponentCount(); j++) {
                if (menu.getMenuComponent(j) instanceof JMenu) {
                    JMenu submenu = (JMenu) menu.getMenuComponent(j);
                    menus.put(menutext + submenu.getText() + "/", submenu);
                }
            }
        }
        JMenuItem temp = null;
        for (Plugins p : getActions().values()) {
            if (!p.isActive()) {
                continue;
            }
            final IConsoleAction action = instanciate(p);
            if (action == null) {
                continue;
            }
            if (action.getPath().startsWith("$")) {
                continue;
            }
            String[] path = action.getPath().split("/");
            JMenuItem mitem = null;
            String mediumpath = "";
            for (int i = 0; i < path.length; i++) {
                mediumpath = new StringBuilder(mediumpath).append(path[i]).append("/").toString();
                if (menus.containsKey(mediumpath)) {
                    temp = menus.get(mediumpath);
                } else {

                    if (i == path.length - 1) {
                        temp = new JMenuItem(new AbstractAction(path[i]) {

                            public void actionPerformed(ActionEvent e) {
                                if (action.getArgumentTypes() != null) {
                                    new ActionDialog(JFrame.getFrames()[0], true, action).setVisible(true);
                                } else {
                                    action.execute(null);
                                }
                            }
                        });
                    } else {
                        temp = new JMenu(path[i]);
                    }
                    menus.put(mediumpath, temp);
                }

                if (mitem == null) {
                    mitem = temp;
                    menubar.add((JMenu) temp);
                } else {
                    mitem.add(temp);
                    mitem = temp;
                }
            }
        }

    }

    public void dispose() {
        getActions().clear();
        actions = null;
    }

    private String[] parseActions(String message) {
        String[] stringarray = {};
        ArrayList<String> commandList = new ArrayList<String>();
        String[] commandfirst = message.split(" ");
        for (int index = 0; index < commandfirst.length; index++) {
            String commandtext = commandfirst[index];
            if (commandtext.startsWith("\"")) {
                String string = "";
                // look for the remaining string delimination
                while ((index < commandfirst.length) && (!commandfirst[index].endsWith("\""))) {
                    string = new StringBuilder(string).append(commandfirst[index++]).append(" ").toString();
                }
                string += commandfirst[index];
                commandList.add(string.substring(1, string.length() - 1));
            } else {
                for (String string : commandtext.split(" ")) {
                    commandList.add(string);
                }
            }
        }
        return commandList.toArray(stringarray);
    }

    public void updateTab(JTabbedPane jTabbedPane1) {
        for (Plugins p : getActions().values()) {
            if (!p.isActive()) {
                continue;
            }
            final IConsoleAction action = instanciate(p);
            if (action == null) {
                continue;
            }
            if (action.getPath().startsWith("$")) {
                String classname = action.getPath().replace("$", "");
                try {
                    JPanel panel = (JPanel) Class.forName(classname).newInstance();
                    jTabbedPane1.addTab(action.getName(), panel);
                } catch (Exception ex) {
                    Logger.getLogger(ConsoleLayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * @return the actions
     */
    public Map<String, Plugins> getActions() {
        return actions;
    }

    private void populateDatabase() {
        String[] classes = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("actions").split(",");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (int i = 0; i < classes.length; i++) {
            try {
                Object temp = Class.forName(classes[i]).newInstance();
                if (temp instanceof IConsoleAction) {
                    Plugins p = new Plugins(classes[i]);
                    p.setActive(Boolean.TRUE);
                    p.setJarUrl(temp.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
                    em.persist(p);
                    IConsoleAction action = (IConsoleAction) temp;
                    getActions().put(action.getName(), p);
                    System.out.println(temp.toString() + " added");
                }
            } catch (Exception ex) {
                //Logger.getLogger(ConsoleLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        em.getTransaction().commit();

        em.close();
        commands = getActions().keySet().toArray(new String[getActions().size()]);
    }
}
