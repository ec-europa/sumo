package safe.reader;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import esa.safe.Safe;
import esa.xfdu.object.data.ByteStream;
import esa.xfdu.object.data.DataObject;
import esa.xfdu.object.metadata.MetadataObject;
import esa.xfdu.object.metadata.Reference;
import fr.gael.drb.impl.DrbNodeImpl;

public class SafeReader0 {
	private final String LAST_FOLDER="LAST_FOLDER";
	JFrame frame;
	Preferences pref;
	
	public String selectFile(){
		String filePath=null;
		String folder=(String)pref.get(LAST_FOLDER,"");
		JFileChooser chooser=new JFileChooser(folder);
		int returnVal=chooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            filePath=file.getAbsolutePath();
            pref.put(LAST_FOLDER, file.getParentFile().getAbsolutePath());	
        }
		return filePath;
	}
	
	
	public SafeReader0(){
		frame=new JFrame();
		pref=  Preferences.userRoot().node(getClass().getName());
	}
	
	
	
	
	
	
	public void read() throws IllegalArgumentException, IOException {
		String pathSafe=selectFile();
		if(pathSafe!=null){
			Safe safeReader=new Safe(pathSafe);
			Collection <MetadataObject> metas=safeReader.getMetadataObjects();
			
			Iterator<MetadataObject> ii=metas.iterator();
			while(ii.hasNext()){
				MetadataObject meta=ii.next();
				System.out.println(meta.getIdentifier());
			}
			
			String fp=safeReader.getFilePath();
			
			
			Collection<DataObject> dataObj=safeReader.getDataObjects();
			
			Iterator<DataObject> iii=dataObj.iterator();
			while(iii.hasNext()){
				
				DataObject dob=iii.next();
				if(dob.getRepID().equalsIgnoreCase("s1Level1MeasurementSchema")){
					try{
						
						System.out.println(dob.getRepID());
						List<ByteStream> bytesStream=dob.getByteStreams();
						for(int i=0;i<bytesStream.size();i++){
							System.out.println("---"+bytesStream.get(i).getFileLocations2().iterator().next().getReference());
							Reference ref=bytesStream.get(i).getFileLocations2().iterator().next().getReference();
							System.out.println("---"+ref.getHref());
							//System.out.println("---"+bytesStream.get(i).getFileLocations().iterator().next().getReference());
							//System.out.println("---"+bytesStream.get(i).getFileContent());
							
							/*System.out.println("---"+bytesStream.get(i).getIdentifier());
							System.out.println("---"+bytesStream.get(i).getMimeType());
							System.out.println("---"+bytesStream.get(i).getFileContent());*/
						}
						
						safeReader.getDataObject(dob.getIdentifier());
						
						/*if(dob.getDataNode()!=null&&dob.getDataNode().getName()!=null){
							
								System.out.println(dob.getDataNode().getName());
								
						}*/
						DrbNodeImpl impl=(DrbNodeImpl) dob.toNode();
						impl.getAttributes();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				
			}
			System.out.println(fp);
			
		}
		
		
		
	}
	
	
	public static void main(String[] args){
		SafeReader0 test=new SafeReader0();
		try {
			test.read();
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			System.exit(0);
		}
		
	}
	
}
