/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import java.awt.Desktop;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.geoimage.def.GeoImageReader;
import org.geoimage.opengl.OpenGLContext;
import org.geoimage.viewer.core.PluginsManager;
import org.geoimage.viewer.core.SumoPlatform;
import org.geoimage.viewer.core.api.iactions.IAction;
import org.geoimage.viewer.core.api.iactions.IConsoleAction;
import org.geoimage.viewer.core.api.ilayer.ILayer;
import org.geoimage.viewer.core.layers.image.ImageLayer;
import org.geoimage.viewer.util.IProgress;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class ConsoleLayer extends GenericLayer {

    private String message = "";
    private String oldMessage = "";
    
   
    //private String[] commands;
    private IProgress currentAction = null;
    private PluginsManager pl;
    
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ConsoleLayer.class);
    
    public ConsoleLayer(ILayer parent) {
    	super(parent,"Console",null,null);
        super.init(parent);
        pl=SumoPlatform.getApplication().getPluginsManager();
        //commands=pl.getCommands();
    }

    public void execute(String[] arguments) {
        for (String c : pl.getCommands()) {
            if (c.startsWith(arguments[0])) {
                String[] args = new String[arguments.length - 1];
                for (int i = 1; i < arguments.length; i++) {
                    args[i - 1] = arguments[i];
                }
                try {
                    if (!pl.getPlugins().get(c).isActive()) {
                        return;
                    }
                    Object a = pl.getActions().get(c);//Class.forName(actions.get(c).getClassName()).newInstance();
                    if (!(a instanceof IAction)) {
                        return;
                    }
                    IAction ica = (IAction) a;
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
                for (ILayer l : SumoPlatform.getApplication().getLayerManager().getLayers().keySet()) {
                    if (l instanceof ImageLayer) {
                        GeoImageReader gir = ((ImageLayer) l).getImageReader();
                        double[] x0 = gir.getGeoTransform().getGeoFromPixel(0, 0);
                        double[] x1 = gir.getGeoTransform().getGeoFromPixel(gir.getWidth(), gir.getHeight());
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
                for (ILayer l : SumoPlatform.getApplication().getLayerManager().getLayers().keySet()) {
                    if (l instanceof ImageLayer) {
                        ((ImageLayer) l).level(Integer.parseInt(message.split(" ")[1]));
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
	            for (String c : pl.getCommands()) {
	                if (c.startsWith(command[0])) {
	                    String[] args = new String[command.length - 1];
	                    for (int i = 1; i < command.length; i++) {
	                        args[i - 1] = command[i];
	                    }
	                    try {
	                        Object a = pl.getActions().get(c);//Class.forName(actions.get(c).getClassName()).newInstance();
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
            String[] command = parseCommandLineAction(actionsscript[index]);
            if ((command.length != 0) && !command[0].isEmpty()) {
                for (String c : pl.getCommands()) {
                    if (c.startsWith(command[0])) {
                        String[] args = new String[command.length - 1];
                        for (int i = 1; i < command.length; i++) {
                            args[i - 1] = command[i];
                        }
                        try {

                            Object a = Class.forName(pl.getPlugins().get(c).getClassName(), true, new URLClassLoader(new URL[]{new URL(pl.getPlugins().get(c).getJarUrl())}, ClassLoader.getSystemClassLoader()));
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

    

    public String getName() {
        return message;
    }

    public void setName(String name) {
        //do nothing
    }

    public void render(OpenGLContext context) {
        if (currentAction != null) {
            if (currentAction.isDone()) {
                SumoPlatform.setInfo(((IAction) currentAction).getName() + " done", 10000);
                currentAction = null;
            } else {
                if (currentAction.isIndeterminate()) {
                    SumoPlatform.getApplication().setInfo(currentAction.getMessage());
                } else {
                    SumoPlatform.getApplication().setInfo(currentAction.getCurrent() + "/" + currentAction.getMaximum() + " " + currentAction.getMessage());
                }

            }
        }
    }

 
    public boolean isRadio() {
        return false;
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
            SumoPlatform.refresh();
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }
    }

    public void execute(String message) {
        execute(parseCommandLineAction(message));
    }

    

    public void dispose() {
    }

    private String[] parseCommandLineAction(String message) {
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
        for (final IAction act : pl.getActions().values()) {
            if (!pl.getPlugins().get(act.getName()).isActive()) {
                continue;
            }
            //final IConsoleAction action = instanciate(p);
            if (act == null) {
                continue;
            }
            if (act.getPath().startsWith("$")) {
                String classname = act.getPath().replace("$", "");
                try {
                    JPanel panel = (JPanel) Class.forName(classname).newInstance();
                    jTabbedPane1.addTab(act.getName(), panel);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(),ex);
                }
            }
        }
    }

    

   
    
}
