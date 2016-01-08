/*
 * PlanningPanel.java
 *
 * Created on October 27, 2008, 11:31 AM
 */

package org.geoimage.viewer.widget;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.geoimage.viewer.core.ImagePlanning;
import org.geoimage.viewer.core.Utilities;
import org.geoimage.viewer.core.wwj.H2Fetcher;
import org.geoimage.viewer.widget.dialog.PlanningDialog;
import org.jdesktop.application.Action;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.LoggerFactory;


/**
 *
 * @author  leforth
 */
public class PlanningPanel extends javax.swing.JPanel {
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(PlanningPanel.class);

	
    private WWJPanel wwjPanel;
    private List<ImagePlanning> listImageplanning = null;
    private static String START_LABEL = "Start Scanning";
    private static String STOP_LABEL = "Stop Scanning";

    /** Creates new form PlanningPanel */
    public PlanningPanel(final WWJPanel wwjPanel) {
        initComponents();
        
        this.wwjPanel = wwjPanel;
        
        // start the timer label display
        new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        jLabel1.setText(new Date().toString());
                    }
                } catch (InterruptedException ex) {
                	logger.error(ex.getMessage(),ex);
                }
            }
        }).start();
        updateAcquisitionTree();
        jButton1.setText(START_LABEL);
        jButton2.setEnabled(true);
        
    }

      private void displayAcquisitions(DefaultMutableTreeNode selectednode, boolean mode) {
        // clean layer first
        wwjPanel.removeAllAreas();
        // scan through tree
        DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode)jTree1.getModel().getRoot();
        if(parentnode != null)
        {
            for(int index = 0; index < jTree1.getModel().getChildCount(parentnode); index++)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)parentnode.getChildAt(index);
                if(node != null)
                {
                    if(node.getUserObject() instanceof ImagePlanning)
                    {
                        ImagePlanning imagePlanning = (ImagePlanning)node.getUserObject();
                        if(imagePlanning != null)
                        {
                            // if display all
                            if(mode)
                            {
                                // display the image frame depending on node
                                wwjPanel.toggleAcquisitionArea(imagePlanning.getArea(), imagePlanning.getAcquisitionTime(), node.equals(selectednode));
                            } else {
                                // display only selected node image frame
                                if(node.equals(selectednode))
                                    wwjPanel.toggleAcquisitionArea(imagePlanning.getArea(), imagePlanning.getAcquisitionTime(), true);
                            }
                        }
                    }
                }
            }
        }
        wwjPanel.refresh();
    }

    private void updateAcquisitionTree() {
        jTree1.setModel(null);
        try {
            DefaultMutableTreeNode imageplanningNode = new DefaultMutableTreeNode("Image Acquisition Tree");
            // initialise tree structure from H2 database
            this.listImageplanning = H2Fetcher.getImagePlanning("SUMODB");
            for(ImagePlanning imagePlanning : listImageplanning)
            {
                // create image node
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(imagePlanning);
                DefaultMutableTreeNode urlnode = new DefaultMutableTreeNode("URL");
                DefaultMutableTreeNode urlvaluenode = new DefaultMutableTreeNode(imagePlanning.getRemoteLocation().toString());
                urlnode.add(urlvaluenode);
                node.add(urlnode);
                DefaultMutableTreeNode datenode = new DefaultMutableTreeNode("Start and Stop Date");
                DefaultMutableTreeNode datestartnode = new DefaultMutableTreeNode(imagePlanning.getAcquisitionTime().toString());
                DefaultMutableTreeNode datestopnode = new DefaultMutableTreeNode(imagePlanning.getAcquisitionStopTime().toString());
                datenode.add(datestartnode);
                datenode.add(datestopnode);
                node.add(datenode);
                DefaultMutableTreeNode statusnode = new DefaultMutableTreeNode("Status                                     ");
                node.add(statusnode);
                DefaultMutableTreeNode actionnode = new DefaultMutableTreeNode("Action");
                node.add(actionnode);
                imageplanningNode.add(node);
            }
            jTree1.setModel(new DefaultTreeModel(imageplanningNode));
            jTree1.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                       jTree1.getLastSelectedPathComponent();

                    /* if nothing is selected */ 
                    if (node == null) return;

                    /* retrieve the node that was selected */ 
                    Object nodeInfo = node.getUserObject();
                    /* React to the node selection. */
                    if(nodeInfo instanceof ImagePlanning)
                    {
                        displayAcquisitions(node, jCheckBox1.isSelected());
                    }

                }

            });
            
            // start the scanning
            //toggleScanning();
            
        } catch (Exception ex) {
            //Logger.getLogger(PlanningPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private ImagePlanning findImagePlanning(String string) {
        for(ImagePlanning imagePlanning : listImageplanning)
            if(imagePlanning.getName().equals(string))
                return imagePlanning;
        
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.geoimage.viewer.core.SumoPlatform.class).getContext().getResourceMap(PlanningPanel.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTree1.setName("jTree1"); // NOI18N
        jScrollPane1.setViewportView(jTree1);

        jScrollPane2.setViewportView(jScrollPane1);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.geoimage.viewer.core.SumoPlatform.class).getContext().getActionMap(PlanningPanel.class, this);
        jButton1.setAction(actionMap.get("toggleScanning")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setAction(actionMap.get("importPlanning")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        jCheckBox1.setAction(actionMap.get("displayAcquisitions")); // NOI18N
        jCheckBox1.setText(resourceMap.getString("jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jCheckBox1)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2))
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(184, 184, 184))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void importPlanning() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Planning File Filter", "xml"));
        fileChooser.showDialog(this, "Import Planning File");
        File imageplanningFile = fileChooser.getSelectedFile();
        // check file format
        try {
            // create xml reader
            SAXBuilder builder = new SAXBuilder();
            Document doc;
            doc = builder.build(imageplanningFile);
            Element atts = doc.getRootElement();
            if(atts.getName().equalsIgnoreCase("XMLExport"))
            {
                imageplanningFile = convertEOLISAImagePlanning(imageplanningFile);
            }
            H2Fetcher.addImagePlanning("SUMODB", imageplanningFile);
        } catch (Exception ex) {
            Utilities.errorWindow("Problem importing planner");
        }
        updateAcquisitionTree();
    }

    public File convertEOLISAImagePlanning(File planningFile) throws Exception {
        // open new file
        File outputfile = new File(planningFile.getAbsolutePath() + ".xml");
        FileOutputStream fileoutput = new FileOutputStream(outputfile);
        String outputstring = "\n<imageplanning>";
        // create dialog for image parameters
        PlanningDialog planningdialog = new PlanningDialog(null, true);
        // create xml reader
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        doc = builder.build(planningFile);
        Element atts = doc.getRootElement();
        for(Object rowobject : atts.getChildren("Row"))
        {
            Element row = (Element)rowobject;
            // string for area
            String polygonstring = "";
            if(row != null)
            {
                for(Object columnobject : row.getChildren("ColumnEntry"))
                {
                    Element column = (Element)columnobject;
                    if(column != null)
                    {
                        if(column.getChild("Name") != null)
                        {
                            if(column.getChild("Name").getText().equalsIgnoreCase("Stop"))
                            {
                                if(column.getChild("Value").getText().length() != 0)
                                {
                                    planningdialog.setImagename("ENVISAT" + column.getChild("Value").getText());
                                    planningdialog.setStartdate(column.getChild("Value").getText());
                                    planningdialog.setEnddate((new Timestamp(Timestamp.valueOf(column.getChild("Value").getText()).getTime() + 24 * 60 * 60 * 1000)).toString());
                                    planningdialog.setOutputdirectory("C:\\temp\\" + planningdialog.getImagename());
                                    planningdialog.setAction("image image file=" + planningdialog.getOutputdirectory() + "/*.N1\n" +
                                                                "vds k-dist 1.5 gshhs\n" +
                                                                "thumbnails currentimage \"VDS analysis H/H\" id \"" + planningdialog.getImagename() + ".zip\" true\n" +
                                                                "sendmail ipsc-mail.jrc.it leforth JayneRose \"thomas.lefort@jrc.it\" \"automatic detection and sending of report\" \"This file was generated after automatic downloading of image, vds detection and thumbnail generation. It should be visualised using the viewer whenever it is available from Fix!!!\" \"" + planningdialog.getImagename() + ".zip\"\n" +
                                                                "home\n");
                                }
                            }
                            if(column.getChild("Name").getText().equalsIgnoreCase("SCENE_FOOTPRINT"))
                            {
                                String[] footprintarray = column.getChild("Value").getText().split(" ");
                                String footprint = "";
                                if(footprintarray.length > 2)
                                {
                                    for(int index = 0; index < footprintarray.length - 2; index += 2)
                                    {
                                        footprint += footprintarray[index + 1] + " " + footprintarray[index] + ", ";
                                    }
                                    footprint += footprintarray[footprintarray.length - 1] + " " + footprintarray[footprintarray.length - 2];
                                }
                                
                                polygonstring += "\n\t\t<area>POLYGON ((" + footprint + "))</area>";
                            }
                        }
                    }
                }
            }
            planningdialog.setVisible(true);
            if(planningdialog.isValidated())
            {
                // add new image
                outputstring += "\n\t<Image>";
                outputstring += "\n\t\t<name>" + planningdialog.getImagename() + "</name>";
                outputstring += "\n\t\t<url>" + planningdialog.getUrl() + "</url>";
                outputstring += "\n\t\t<startDate>" + planningdialog.getStartdate() + "</startDate>";
                outputstring += "\n\t\t<endDate>" + planningdialog.getEnddate() + "</endDate>";
                outputstring += polygonstring;
                outputstring += "\n\t\t<action>" + planningdialog.getAction() + "</action>";
                outputstring += "\n\t</Image>";
            }
        }
        outputstring += "\n\t</imageplanning>";

        // write to file
        fileoutput.write(outputstring.getBytes());
        fileoutput.close();
        
        return outputfile;
    }

    @Action
    public void displayAcquisitions() {
        boolean mode = jCheckBox1.isSelected();
        displayAcquisitions(null, mode);
    }

    @Action
    public void toggleScanning() {
        if(jButton1.getText().equals(START_LABEL))
        {
            jButton1.setText(STOP_LABEL);
            // disable import button
            jButton2.setEnabled(false);
            Thread scanningThread = new Thread(new Runnable()
            {
                public void run()
                {
                    while(jButton1.getText().equalsIgnoreCase(STOP_LABEL))
                    {
                        // check tree every 10 seconds
                        long time = System.currentTimeMillis();
                        DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode)jTree1.getModel().getRoot();
                        if(parentnode != null)
                        {
                            for(int index = 0; index < jTree1.getModel().getChildCount(parentnode); index++)
                            {
                                DefaultMutableTreeNode node = (DefaultMutableTreeNode)parentnode.getChildAt(index);
                                if(node != null)
                                {
                                    if(node.getUserObject() instanceof ImagePlanning)
                                    {
                                        ImagePlanning imagePlanning = (ImagePlanning)node.getUserObject();
                                        if(imagePlanning != null)
                                        {
                                            // check maximum time left before acquisition
                                            if(imagePlanning.getAcquisitionStopTime().getTime() - time < 0)
                                            {
                                                imagePlanning.stopListener();
                                                // change diosplay of node in the tree
                                                node.removeAllChildren();
                                                node.setUserObject(imagePlanning.getName() + " - downloading time expired");
                                                ((DefaultTreeModel)jTree1.getModel()).nodeChanged(node);
                                            } else {
                                                DefaultMutableTreeNode foundnode = null;
                                                // check maximum time left before acquisition
                                                if(imagePlanning.isDownloaded())
                                                {
                                                    if(node.getChildCount() != 0)
                                                    {
                                                        // change display of node in the tree
                                                        node.removeAllChildren();
                                                        imagePlanning.setName(imagePlanning.getName() + " - image is downloaded");
                                                        ((DefaultTreeModel)jTree1.getModel()).nodeChanged(node);
                                                    }
                                                } else {
                                                    // display time left
                                                    for(int indexnode = 0; indexnode < node.getChildCount(); indexnode++)
                                                    {
                                                        Object stringnode = ((DefaultMutableTreeNode)node.getChildAt(indexnode)).getUserObject();
                                                        if(stringnode instanceof String)
                                                            if(((String)stringnode).startsWith("Status"))
                                                            {
                                                                foundnode = (DefaultMutableTreeNode)node.getChildAt(indexnode);
                                                            }
                                                    }
                                                    if(foundnode != null)
                                                        node.remove(foundnode);
                                                    // check time left before acquisition
                                                    if(imagePlanning.getAcquisitionTime().getTime() - time < 0)
                                                    {
                                                        try {
                                                            foundnode = new DefaultMutableTreeNode("Status - " + imagePlanning.startDownloadImage());
                                                            node.add(foundnode);
                                                        } catch (MalformedURLException ex) {
                                                        	logger.error(ex.getMessage(),ex);
                                                            foundnode = new DefaultMutableTreeNode("Problem with URL");
                                                            node.add(foundnode);
                                                        }
                                                    } else {
                                                        foundnode = new DefaultMutableTreeNode("Status - Time left: " + ((imagePlanning.getAcquisitionTime().getTime() - time) / 1000) + " s");
                                                        node.add(foundnode);
                                                    }
                                                }
                                        }
                                    }
                                }
                            }
                        }
                    }
                        
                    jTree1.updateUI();

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                    	logger.error(ex.getMessage(),ex);
                    }
               }
            }});
            scanningThread.start();
        } else {
            jButton1.setText(START_LABEL);
            jButton2.setEnabled(true);
            DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode)jTree1.getModel().getRoot();
            if(parentnode != null)
            {
                for(int index = 0; index < jTree1.getModel().getChildCount(parentnode); index++)
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)parentnode.getChildAt(index);
                    if(node != null)
                    {
                        if(node.getUserObject() instanceof ImagePlanning)
                        {
                            ImagePlanning imagePlanning = (ImagePlanning)node.getUserObject();
                            if(imagePlanning != null)
                            {
                                imagePlanning.startListener();
                            }
                        }
                    }
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

}
