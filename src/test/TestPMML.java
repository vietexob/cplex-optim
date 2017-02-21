package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import org.jpmml.evaluator.TargetField;
import org.jpmml.evaluator.regression.RegressionModelEvaluator;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;

public class TestPMML {
	// Test reading a trained model saved as PMML into a Java program and do prediction.
	
	public final static PMML loadModel(final String filename) throws Exception {
		PMML pmml = null;
		File inputFilePath = new File(filename);
		
		try( InputStream in = new FileInputStream(inputFilePath) ) {
			Source source = ImportFilter.apply(new InputSource(in));
			pmml = JAXBUtil.unmarshalPMML(source);
		} catch( Exception ex ) {
			ex.printStackTrace();
		}
		
		return pmml;
	}
	
	public static void main(String[] args) throws Exception {
		String modelFilename = "./data/test/resp_lm.xml";
		PMML pmml = loadModel(modelFilename);
		
		ModelEvaluator<RegressionModel> modelEvaluator = new RegressionModelEvaluator(pmml);
		Evaluator evaluator = (Evaluator) modelEvaluator;
		
		String testFilename = "./data/test/test_data.csv";
		BufferedReader br = null;
		String line = "";
		List<Double> testVal = new LinkedList<Double>();
		List<Double> predVal = new LinkedList<Double>();
		
		try {
			br = new BufferedReader(new FileReader(testFilename));
			String firstLine = br.readLine();
			System.out.println(firstLine);
			
			while((line = br.readLine()) != null) {
				String[] lineStr = line.split(",");
				Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
				
				List<InputField> inputFields = evaluator.getInputFields();
				for(int i = 0; i < inputFields.size()+1; i++) {
					if(i < inputFields.size()) {
						InputField inputField = inputFields.get(i);
						FieldName inputFieldName = inputField.getName();
						Double featureVal = Double.valueOf(lineStr[i]);
						
						FieldValue inputFieldValue = inputField.prepare(featureVal);
						arguments.put(inputFieldName, inputFieldValue);
					} else {
						Double aTestVal = Double.valueOf(lineStr[i]);
						testVal.add(aTestVal);
					}
				}
				
				Map<FieldName, ?> results = evaluator.evaluate(arguments);
				List<TargetField> targetFields = evaluator.getTargetFields();
				for(TargetField targetField : targetFields) {
					FieldName targetFieldName = targetField.getName();
					Double targetFieldValue = (Double) results.get(targetFieldName);
					if(targetFieldValue < 1.0) {
						targetFieldValue = 1.0;
					}
					predVal.add(targetFieldValue);
				}
			}
		} catch(FileNotFoundException ex) {
			ex.printStackTrace();
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			br.close();
		}
		
		double rmse = getPredError(predVal, testVal, true);
		double mae = getPredError(predVal, testVal, false);
		System.out.println("RMSE = " + rmse + ", MAE = " + mae);
	}
	
	public static double getPredError(List<Double> predVal, List<Double> testVal, boolean isRMSE) {
		if(isRMSE) {
			double rmse = 0;
			double diff;
			for(int i = 0; i < predVal.size(); i++) {
				diff = testVal.get(i) - predVal.get(i);
				rmse += diff * diff;
			}
			
			rmse = Math.sqrt(rmse / predVal.size());
			return rmse;
		} else {
			double mae = 0;
			double diff;
			for(int i = 0; i < predVal.size(); i++) {
				diff = testVal.get(i) - predVal.get(i);
				mae += Math.abs(diff);
			}
			
			mae = mae / predVal.size();
			return mae;
		}
	}
}
