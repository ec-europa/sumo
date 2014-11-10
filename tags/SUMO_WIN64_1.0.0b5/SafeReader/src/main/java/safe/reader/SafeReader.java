package safe.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import esa.safe.Safe;
import esa.xfdu.object.data.ByteStream;
import esa.xfdu.object.data.DataObject;
import esa.xfdu.object.metadata.MetadataObject;
import esa.xfdu.object.metadata.MetadataWrap;
import esa.xfdu.object.metadata.Reference;
import fr.gael.drb.impl.DrbNodeImpl;

public class SafeReader {
	private Safe safe;
	private Collection <MetadataObject> metaDataObjs=null;
	private Collection <DataObject> dataObjects=null;
	
	public SafeReader(String safePath) throws IllegalArgumentException, IOException{
		safe=new Safe(safePath);
		metaDataObjs=this.safe.getMetadataObjects();
	}
	
	/**
	 * return the MetadataObjects collection
	 * @return
	 */
	public Collection <MetadataObject> getMetadataObjects(){
		return metaDataObjs;
	}
	
	/**
	 * return the MetadataObjects collection
	 * @return
	 */
	public Collection <DataObject> getDataObjects(){
		return dataObjects;
	}
	
	
	/**
	 * Return the mesaurements
	 * @return
	 */
	public Collection<String> getMesasurementFiles(){
			Collection <String>mesaurementFiles=new ArrayList<String>();
		
	
			//safe filePath
			String fp=safe.getFilePath();
			
			Collection<DataObject> dataObj=safe.getDataObjects();
			
			Iterator<DataObject> iii=dataObj.iterator();
			while(iii.hasNext()){
				
				DataObject dob=iii.next();
				// find node with measurement information
				if(dob.getRepID().equalsIgnoreCase("s1Level1MeasurementSchema")){
					try{
						System.out.println(dob.getRepID());
						List<ByteStream> bytesStream=dob.getByteStreams();
						for(int i=0;i<bytesStream.size();i++){
							Reference ref=bytesStream.get(i).getFileLocations2().iterator().next().getReference();
							String filePath=ref.getHref();
							if(filePath.startsWith("."))
								filePath=filePath.substring(2);
							StringBuilder build=new StringBuilder(fp).append(filePath);
							mesaurementFiles.add(build.toString());
							System.out.println(build);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				
			}
			return mesaurementFiles;
		}
		
		
	public void readGeneralProductInformation(){
		MetadataObject general=safe.getMetadataObject("generalProductInformation");
		Collection<MetadataWrap>wrapper=general.getMetadataWrap();
	}
	
	public void getPolarizations(){
		
		
	}
	
	
}
