/*
 * 
 */
package org.geoimage.viewer.core.layers.image;

import java.io.File;
import java.net.MalformedURLException;

import org.geoimage.viewer.core.SumoPlatform;
import org.slf4j.LoggerFactory;

public class Cache {
		private File path;
		private String id;    
		private static org.slf4j.Logger logger=LoggerFactory.getLogger(Cache.class);

		
	    public Cache(String id) {
	    	this.path = new File(new StringBuilder(SumoPlatform.getApplication().getCachePath()).append("/").append(id).toString());
	    	this.id=id;
	    	if(!path.exists())
	        	 path.mkdirs();
	    }
		
	    public Cache() {
	    	this.path = new File(SumoPlatform.getApplication().getCachePath());
	        if(!path.exists())
	        	 path.mkdirs();
	    }
	    
	    public long getAge(String fileName) {
	        if (fileName == null) {
	            return 0;
	        }
	        fileName = clean(fileName);
	        File file;
	        if (fileName.startsWith(getPath().getAbsolutePath())) {
	            file = new File(fileName);
	        } else {
	            file = new File(getPath().getAbsolutePath() + "/" + fileName);
	        }
	        if (file.exists()) {
	            return System.currentTimeMillis() - file.lastModified();
	        }
	        return 0;
	    }

	    public long getAge(String fileName, int level, int x, int y) {
	        if (fileName == null) {
	            return 0;
	        }
	        fileName = clean(fileName);
	        fileName =new StringBuilder(fileName).append("/").append(level).append("/").append(x).append("_").append(y).append(".png").toString();
	        java.io.File file;
	        if (fileName.startsWith(getPath().getAbsolutePath())) {
	            file = new java.io.File(fileName);
	        } else {
	            file = new File(getPath().getAbsolutePath() + "/" + fileName);
	        }

	        if (file.exists()) {
	            return System.currentTimeMillis() - file.lastModified();
	        }


	        return 0;
	    }

	    public boolean contains(String fileName) {
	        if (fileName == null) {
	            return false;
	        }

	        fileName = clean(fileName);
	        java.io.File file;
	        if (fileName.startsWith(getPath().getAbsolutePath())) {
	            file = new java.io.File(fileName);
	        } else {
	            file = new File(new StringBuilder(getPath().getAbsolutePath()).append("/").append(fileName).toString());
	        }

	        if (file.exists()) {
	            return true;
	        }


	        return false;
	    }

	    public boolean containsTile(String fileName, int level, int x, int y) {
	        if (fileName == null) {
	            return false;
	        }
	        fileName = clean(fileName);
	        fileName =new StringBuilder(fileName).append("/").append(level).append("/").append(x).append("_").append(y).append(".png").toString();

	        java.io.File file;
	        if (fileName.startsWith(getPath().getAbsolutePath())) {
	            file = new java.io.File(fileName);
	        } else {
	            file = new File(getPath().getAbsolutePath() + "/" + fileName);
	        }

	        if (file.exists()) {
	            return true;
	        }


	        return false;
	    }

	    /**
	     * Create the new file in the cache and ensure that the path exists
	     * @param fileName: name of the file, including directories path
	     * @return the newly created File
	     */
	    public java.io.File newFile(String fileName) {
	        if (fileName == null) {
	            return null;
	        }
	        fileName = clean(fileName);
	        if (this.getPath() != null) {
	            String fullPath = new StringBuilder(this.getPath().getAbsolutePath()).append("/").append( fileName).toString();
	            java.io.File file = new java.io.File(fullPath);
	           // System.out.println("file:"+file.getAbsolutePath()+"  "+file.exists());
	            if (file.getParentFile().exists()) {
	                return file;
	            } else if (file.getParentFile().mkdirs()) {
	                return file;
	            }
	        }
	        return null;
	    }
/*
	    public java.io.File newTile(String fileName, int level, int x, int y) {
	        if (fileName == null) {
	            return null;
	        }
	        fileName = clean(fileName);
	        fileName += "/" + level + "/" + x + "_" + y + ".png";
	        if (this.getPath() != null) {
	            String fullPath = this.getPath().getAbsolutePath() + "/" + fileName;
	            java.io.File file = new java.io.File(fullPath);
	            if (file.getParentFile().exists()) {
	                return file;
	            } else if (file.getParentFile().mkdirs()) {
	                return file;
	            }
	        }
	        return null;
	    }*/

	    public java.net.URL findFile(String fileName, boolean checkClassPath) {
	        if (fileName == null) {
	            return null;
	        }
	        fileName = clean(fileName);
	        if (checkClassPath) {
	            java.net.URL url = this.getClass().getClassLoader().getResource(fileName);
	            if (url != null) {
	                return url;
	            }
	        }

	        for (File dir : this.getPath().listFiles()) {
	            if (!dir.exists()) {
	                continue;
	            }

	            File file = new File(dir.getAbsolutePath() + "/" + fileName);
	            if (file.exists()) {
	                try {
	                    return file.toURI().toURL();
	                } catch (MalformedURLException e) {
	                	logger.error(e.getMessage(),e);
	                }
	            }
	        }

	        return null;
	    }
/*
	    public java.net.URL findTile(String fileName, int level, int x, int y, boolean checkClassPath) {
	        if (fileName == null) {
	            return null;
	        }
	        fileName += "/" + level + "/" + x + "_" + y + ".png";
	        if (checkClassPath) {
	            java.net.URL url = this.getClass().getClassLoader().getResource(fileName);
	            if (url != null) {
	                return url;
	            }
	        }

	        for (File dir : this.getPath().listFiles()) {
	            if (!dir.exists()) {
	                continue;
	            }

	            File file = new File(dir.getAbsolutePath() + "/" + fileName);
	            if (file.exists()) {
	                try {
	                    return file.toURI().toURL();
	                } catch (MalformedURLException e) {
	                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
	                }
	            }
	        }

	        return null;
	    }*/
/*
	    public String getOverviewURL(String fileName){
	       String f=new File( getPath(), clean(fileName + "/overview.png")).getAbsolutePath();
	       System.out.println("Overview png:"+f);
	       return f;
	    }
	    
	    public String getOverviewURLForMultipleImages(String fileName,String suffix){
	        return new File( getPath(), clean(fileName + "/overview"+suffix+".png")).getAbsolutePath();
	    }*/
	    public File getOverviewFile(){
	    	   String fileNameOverview=new StringBuilder(id).append(".png").toString();	
		       File f=new File(getPath(), fileNameOverview);
		       System.out.println("Overview file:"+f.getAbsolutePath());
		       return f;
		}
	  
	  

	    /**
	     * @param url the "file:" URL of the file to remove from the cache
	     * @throws IllegalArgumentException if <code>url</code> is null
	     */
	    public void removeFile(java.net.URL url) {
	        if (url == null) {
	            return;
	        }

	        try {
	            File file = new File(url.toURI());

	            if (file.exists()) {
	                file.delete();
	            }
	        } catch (java.net.URISyntaxException e) {
	        	logger.error(e.getMessage(),e);
	        }
	    }

	    private String clean(String fileName) {
	        fileName = fileName.replace("\\", "/");
	        if (fileName.substring(1, 3).equals(":/")) {
	            fileName = fileName.substring(3);
	        }
	        return fileName;
	    }

	    /**
	     * @return the path
	     */
	    public File getPath() {
	        return path;
	    }
		 

}
