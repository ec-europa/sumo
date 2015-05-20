/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.geoimage.analysis.BlackBorderAnalysis;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

/** 
 * this is the main class dealing with planning image acquisition. FTP download, Analysis, Local storage are dealt within the class
 * @author leforth, thoorfr
 */
public class ImagePlanning {

	private static org.slf4j.Logger logger=LoggerFactory.getLogger(ImagePlanning.class);

	
    private URL remoteLocation;
    private Timestamp acquisitionTime;
    private String localPath;
    private String action;
    private Geometry area;
    private String name;
    private Timestamp acquisitionstopTime;
    private boolean ftpenabled = true;
    private boolean imagedownloaded = false;
    private Thread ftpthread = null;
    private long downloadedsize = 0;
    private String processingmessage;

    public Geometry getArea() {
        return this.area;
    }

    public long getDownloadedSize() {
        return downloadedsize;
    }

    public URL getRemoteLocation() {
        return remoteLocation;
    }

    public boolean isDownloaded() {
        return imagedownloaded;
    }

    public void setRemoteLocation(URL remoteLocation) {
        this.remoteLocation = remoteLocation;
    }

    public Timestamp getAcquisitionTime() {
        return acquisitionTime;
    }

    public Timestamp getAcquisitionStopTime() {
        return acquisitionstopTime;
    }

    public void setAcquisitionTime(Timestamp acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public void setAcquisitionStopTime(Timestamp acquisitionTime) {
        this.acquisitionstopTime = acquisitionTime;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void startListener() {
        ftpenabled = true;
    }

    public void stopListener() {
        ftpenabled = false;
        ftpthread = null;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setArea(Geometry imageGeom) {
        this.area = imageGeom;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String startDownloadImage() throws MalformedURLException {

        try {
            if (ftpthread == null) {
                ftpthread = new Thread(new Runnable() {

                    public void run() {
                        logger.info("\nFTP " + getRemoteLocation());
                    	
                        processingmessage = "Starting Download";
                        try {
                            URLConnection con = null;
                            String timer = "/";
                            InputStream stream = null;
                            final String imagename = remoteLocation.getFile().substring(remoteLocation.getFile().lastIndexOf("/") + 1);
                            FTPClient client = null;
                            // look for files in folder
                            URL remotelocationfolder = new URL(remoteLocation.toString().substring(0, remoteLocation.toString().lastIndexOf("/") + 1));
                            // check for wild card in url
                                while ((con = remotelocationfolder.openConnection()) == null) {
                                    timer = timer.equalsIgnoreCase("/") ? "\\" : "/";
                                    processingmessage = "attempting to connect " + timer;
                                    Thread.sleep(1000);
                                }
                                processingmessage = "Connected to server";
                                // different protocol handling
                                String protocol = remoteLocation.getProtocol();
                                // check for protocol
                                if (protocol != null) {
                                    if (protocol.compareToIgnoreCase("FTP") == 0) {
                                        client = new FTPClient();
                                        client.addProtocolCommandListener(
                                                new PrintCommandListener(
                                                new PrintWriter(System.out)));

                                        client.connect(remotelocationfolder.getHost());
                                        String urlstring = remotelocationfolder.toString();
                                        String user = urlstring.substring(urlstring.indexOf("ftp://") + 6, urlstring.length());
                                        user = user.substring(0, user.indexOf(":"));
                                        String password = urlstring.substring(urlstring.indexOf(user + ":") + user.length() + 1, urlstring.indexOf("@"));
                                        if (!client.login(user, password)) {
                                            throw new Exception();
                                        }

                                        FTPFile[] files = null;
                                        boolean accept = false;
                                        while (!accept) {
                                            // look for file with matching name
                                            files = client.listFiles();
                                            for (FTPFile ftpfile : files) {
                                                String name = ftpfile.getName();
                                                if (name.contains(imagename)) {
                                                    remoteLocation = new URL(remotelocationfolder.toString() + name);
                                                    downloadFile();
                                                }
                                            }
                                            if (!ftpenabled) {
                                                client.logout();
                                                client.disconnect();
                                                return;
                                            }
                                            Thread.sleep(1000);
                                        }
                                        client.logout();
                                        client.disconnect();
                                    } else {

                                        while (stream == null) {
                                            try {
                                                stream = con.getInputStream();
                                            } catch (FileNotFoundException e) {
                                                timer = timer.equalsIgnoreCase("/") ? "\\" : "/";
                                                processingmessage = "looking for file " + timer;
                                                if (!ftpenabled) {
                                                    return;
                                                }
                                                Thread.sleep(1000);
                                            }
                                        }
                                        File[] files = null;
                                        while (files == null) {
                                            BufferedInputStream in = new BufferedInputStream(stream);
                                            File outputfile = new File(remotelocationfolder.toString());
                                            // look for file with matching name
                                            files = outputfile.listFiles(new FilenameFilter() {

                                                public boolean accept(File dir, String name) {
                                                    boolean accept = true;
                                                    for (String string : imagename.split("*")) {
                                                        accept = accept && name.contains(string);
                                                    }
                                                    return accept;
                                                }
                                            });
                                            if (!ftpenabled) {
                                                return;
                                            }
                                            Thread.sleep(1000);
                                        }
                                        if (files.length != 0) {
                                            remoteLocation = new URL(remotelocationfolder.toString() + files[0]);
                                        }
                                    }
                                }
                            


                        } catch (Exception e) {
                        	logger.error(e.getMessage(),e);
                            processingmessage = "Error during download";
                        }

                    }

                    private void downloadImage(InputStream stream, String outputname) throws FileNotFoundException, IOException {

                        BufferedInputStream in = new BufferedInputStream(stream);
                        File outputfile = new File(outputname);
                        FileOutputStream out = new FileOutputStream(outputfile);
                        downloadedsize = 0;

                        byte[] bytesIn = new byte[1024];
                        int ioresult = 0;
                        while (ioresult != -1) {
                            if (!ftpenabled) {
                                out.close();
                                in.close();
                                imagedownloaded = false;
                                return;
                            }
                            try {
                                ioresult = in.read(bytesIn);
                            } catch (IOException e) {
                            	logger.error(e.getMessage(),"IO problem while downloading image");
                            }
                            if (ioresult != -1) {
                                out.write(bytesIn, 0, ioresult);
                                downloadedsize += bytesIn.length;
                            }
                            processingmessage = "Downloading: " + ((int) getDownloadedSize() / 1024) + "KBytes";
                        }
                        out.close();
                        in.close();
                    }

                    private void downloadFile() throws Exception {
                            String protocol = remoteLocation.getProtocol();
                            String downloadedfile = "C:\\temp\\" + remoteLocation.getPath().substring(remoteLocation.getPath().lastIndexOf("/") + 1, remoteLocation.getPath().length());
                            FTPClient client=null;
                            InputStream stream = null;
                            URLConnection con = null;
                            if (protocol != null) {
                                if (protocol.compareToIgnoreCase("FTP") == 0) {
                                    client = new FTPClient();
                                    client.addProtocolCommandListener(
                                            new PrintCommandListener(
                                            new PrintWriter(System.out)));

                                    client.connect(remoteLocation.getHost());
                                    String urlstring = remoteLocation.toString();
                                    String user = urlstring.substring(urlstring.indexOf("ftp://") + 6, urlstring.length());
                                    user = user.substring(0, user.indexOf(":"));
                                    String password = urlstring.substring(urlstring.indexOf(user + ":") + user.length() + 1, urlstring.indexOf("@"));
                                    if (!client.login(user, password)) {
                                        throw new Exception();
                                    }

                                    FTPFile[] files = client.listFiles(remoteLocation.getFile());
                                    for (FTPFile file : files) {
                                        while (stream == null) {
                                            try {
                                                stream = client.retrieveFileStream(remoteLocation.getPath());
                                            } catch (FileNotFoundException e) {
                                                if (!ftpenabled) {
                                                    return;
                                                }
                                                Thread.sleep(1000);
                                            }
                                        }
                                        downloadImage(stream, "c:/temp/" + file.getName());
                                    }
                                    client.logout();
                                    client.disconnect();
                                    if (!ftpenabled) {
                                        return;
                                    }
                                } else {

                                    // start polling the site for the file
                                    while ((con = remoteLocation.openConnection()) == null) {
                                        processingmessage = "attempting to connect";
                                        if (!ftpenabled) {
                                            return;
                                        }
                                        Thread.sleep(1000);
                                    }
                                    processingmessage = "Connected to server";
                                    while (stream == null) {
                                        try {
                                            stream = con.getInputStream();
                                        } catch (FileNotFoundException e) {
                                            if (!ftpenabled) {
                                                return;
                                            }
                                            Thread.sleep(1000);
                                        }
                                    }
                                    downloadImage(stream, downloadedfile);
                                }
                            }

                            if (!ftpenabled) {
                                return;
                            }
                            // unzip the file if zip extension
                            if (downloadedfile.endsWith(".zip")) {
                                final int BUFFER = 2048;
                                // unzip file first
                                try {
                                    BufferedOutputStream dest = null;
                                    FileInputStream fis = new FileInputStream(downloadedfile);
                                    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
                                    ZipEntry entry;
                                    while ((entry = zis.getNextEntry()) != null) {
                                        processingmessage = "Extracting: " + entry;
                                        int count;
                                        byte data[] = new byte[BUFFER];
                                        // write the files to the disk
                                        File zipfile = new File((downloadedfile.substring(0, downloadedfile.lastIndexOf(File.separator)) + "\\" + entry.getName()));
                                        File parent = zipfile.getParentFile();
                                        if (!parent.exists()) {
                                            parent.mkdirs();
                                        }
                                        zipfile.createNewFile();
                                        FileOutputStream fos = new FileOutputStream(zipfile);
                                        dest = new BufferedOutputStream(fos, BUFFER);
                                        while ((count = zis.read(data, 0, BUFFER)) != -1) {
                                            dest.write(data, 0, count);
                                        }
                                        dest.flush();
                                        dest.close();
                                    }
                                    zis.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            // execute action script
                            logger.info("\naction\n" + action);
                            processingmessage = "Processing Image in SUMO";
                            Platform.getConsoleLayer().runScriptString(action);
                            processingmessage = "Processed";

                            imagedownloaded = true;
                    }
                });
                ftpthread.start();
            }

        } catch (Exception ex) {
        	logger.error(ex.getMessage(),ex);
        }

        return processingmessage;

    }
}
