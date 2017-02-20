package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.transform.Source;

import org.dmg.pmml.PMML;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;

public class TestPMML {
	// Test reading a trained model saved as PMML into a Java program
	
	public final static PMML loadModel(final String filename) throws Exception {
		PMML pmml = null;
		File inputFilePath = new File(filename);
		
		try( InputStream in = new FileInputStream(inputFilePath) ) {
			Source source = ImportFilter.apply(new InputSource(in));
			pmml = JAXBUtil.unmarshalPMML(source);
		} catch( Exception ex ) {
//			logger.error(ex.toString());
			System.out.println(ex.toString());
			throw ex;
		}
		
		return pmml;
	}
	
	public static void main(String[] args) throws Exception {
//		System.out.println(System.getProperty("user.dir"));
		String filename = "./data/test/resp_lm.xml";
		PMML pmml = loadModel(filename);
		System.out.println(pmml);
		
	}
}
