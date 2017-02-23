package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
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

public class PredictionModel {
	
	private static PMML pmml = null;
	private static Evaluator evaluator = null;
	
	public PredictionModel(String modelFilename, int modelChoice) {
		loadModel(modelFilename);
		if(modelChoice == 0) {
			ModelEvaluator<RegressionModel> modelEvaluator = new RegressionModelEvaluator(pmml);
			evaluator = (Evaluator) modelEvaluator;
		} else {
			System.out.println("ERROR: Not yet implemented!");
			System.exit(0);
		}
	}
	
	private static void loadModel(final String modelFilename) {
		
		File inputFilePath = new File(modelFilename);
		try( InputStream in = new FileInputStream(inputFilePath) ) {
			Source source = ImportFilter.apply(new InputSource(in));
			pmml = JAXBUtil.unmarshalPMML(source);
		} catch( Exception ex ) {
			ex.printStackTrace();
		}
	}
	
	private static double evaluateRegression(double[] inputVal) {
		
		Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
		List<InputField> inputFields = evaluator.getActiveFields();
		for(int i = 0; i < inputFields.size(); i++) {
			InputField inputField = inputFields.get(i);
			FieldName inputFieldName = inputField.getName();
			Double featureVal = inputVal[i];
			
			FieldValue inputFieldValue = inputField.prepare(featureVal);
			arguments.put(inputFieldName, inputFieldValue);
		}
		
		Map<FieldName, ?> results = evaluator.evaluate(arguments);
		List<TargetField> targetFields = evaluator.getTargetFields();
		Double prediction = 0.0;
		for(TargetField targetField : targetFields) {
			FieldName targetFieldName = targetField.getName();
			Double targetFieldValue = (Double) results.get(targetFieldName);
			if(targetFieldValue < 1.0) {
				targetFieldValue = 1.0;
			}
			prediction = targetFieldValue;
		}
		
		return prediction;
	}
	
	public double getPrediction(double[] inputVal) {
		double prediction = evaluateRegression(inputVal);
		if(prediction < 1.0) {
			prediction = 1.0;
		}
		
		return prediction;
	}
}
