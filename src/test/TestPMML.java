package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.regression.RegressionModel;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.regression.RegressionModelEvaluator;
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
		String modelFilename = "./data/test/resp_lm.xml";
		PMML pmml = loadModel(modelFilename);
		
		ModelEvaluator<RegressionModel> modelEvaluator = new RegressionModelEvaluator(pmml);
		Evaluator evaluator = modelEvaluator;
		List<InputField> features = evaluator.getActiveFields();
		
		String testFilename = "./data/test/test_data.csv";
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(testFilename));
			String firstLine = br.readLine();
			while((line = br.readLine()) != null) {
				String[] lineStr = line.split(",");
				HashMap<FieldName, Double> featureRow = new HashMap<FieldName, Double>();
				
				for(int i = 0; i < lineStr.length-1; i++) {
					double featureVal = Double.valueOf(lineStr[0]);
					FieldName featureName = features.get(i).getName();
					featureRow.put(featureName, featureVal);
				}
				
				Map results = modelEvaluator.evaluate(featureRow);
				FieldName key = evaluator.getTargetFields().get(0).getName();
				System.out.println(results.get(key));
			}
		} catch(FileNotFoundException ex) {
			ex.printStackTrace();
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			br.close();
		}
	}
}
