package org.geoimage.viewer.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geoimage.viewer.core.api.Argument;
import org.geoimage.viewer.core.layers.image.CacheManager;
import org.geoimage.viewer.util.IProgress;

public class ClearCacheAction extends AbstractAction implements IProgress{
	private Logger logger = LogManager.getLogger(this);
	boolean done=false;
	
	
	@Override
	public String getName() {
		return "Clear cache";
	}

	@Override
	public String getDescription() {
		return "Clear image cache";
	}

	@Override
	public String getPath() {
		return "Tools/ClearCache";
	}

	@Override
	public boolean execute(String[] args) {
		File folder=CacheManager.getRootCacheInstance().getPath();
		//System.gc();
		try {
			Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
				   @Override
				   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					   Files.delete(file);
					   return FileVisitResult.CONTINUE;
				   }

				   @Override
				   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					   Files.delete(dir);
					   return FileVisitResult.CONTINUE;
				   }

			   });
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		}
		setDone(true);
		return true;
	}

	@Override
	public List<Argument> getArgumentTypes() {
		List <Argument> args=new  ArrayList<Argument>();
		return args;
	}

	@Override
	public boolean isIndeterminate() {
		return true;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public int getMaximum() {
		return 0;
	}

	@Override
	public int getCurrent() {
		return 0;
	}

	@Override
	public String getMessage() {
		return "Clear the sumo cache?";
	}

	@Override
	public void setCurrent(int i) {
	}

	@Override
	public void setMaximum(int size) {
	}

	@Override
	public void setMessage(String string) {
	}

	@Override
	public void setIndeterminate(boolean value) {
	}

	@Override
	public void setDone(boolean value) {
		done=value;
		
	}

}
