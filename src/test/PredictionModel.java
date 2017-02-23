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
import org.dmg.pmml.support_vector_machine.SupportVectorMachineModel;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.TargetField;
import org.jpmml.evaluator.regression.RegressionModelEvaluator;
import org.jpmml.evaluator.support_vector_machine.SupportVectorMachineModelEvaluator;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;

public class PredictionModel {
	// The model object
	private static PMML pmml = null;
	// The model evaluator for regression prediction
	private static Evaluator evaluator = null;
	
	/**
	 * Initializes the model object given by the model filename (path) and a model choice
	 * (linear regression or SVM regression). Also initializes the evaluator object.
	 * @param modelFilename Filename (path) of the XML model
	 * @param modelChoice Choice of model: 0 - LM, else - SVM
	 */
	public PredictionModel(String modelFilename, int modelChoice) {
		
		loadModel(modelFilename);
		
		if(modelChoice == 0) { // linear regression
			ModelEvaluator<RegressionModel> modelEvaluator = new RegressionModelEvaluator(pmml);
			evaluator = (Evaluator) modelEvaluator;
		} else { // SVM regression
			ModelEvaluator<SupportVectorMachineModel> modelEvaluator = new SupportVectorMachineModelEvaluator(pmml);
			evaluator = (Evaluator) modelEvaluator;
		}
	}
	
	/**
	 * Loads and initializes the model from an input XML file.
	 * @param modelFilename Filename (path) of the XML model
	 * @return The model
	 */
	private static void loadModel(final String modelFilename) {
		
		File inputFilePath = new File(modelFilename);
		try( InputStream in = new FileInputStream(inputFilePath) ) {
			Source source = ImportFilter.apply(new InputSource(in));
			pmml = JAXBUtil.unmarshalPMML(source);
		} catch( Exception ex ) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Performs the regression prediction.
	 * @param inputVal Array of features
	 * @return Predicted value
	 */
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
	
	/**
	 * Performs the regression prediction.
	 * @param inputVal Array of length 6 consisting of the following elements (in strict order):
	 * 0. traffic_travel_time: time-dependent sector-sector mean traffic travel time (minutes) from Google.
	 * 1. mean_resp_time: time-dependent sector-sector mean response time (minutes).
	 * 2. is_urgent: is the incident urgent? (0/1 variable).
	 * 3. num_cars: the total number of FRCs dispatched.
	 * 4. cross_npc: is the responding FRC from a different NPC? (0/1 variable).
	 * 5. hours: the (integer) hours of the incident (0 to 23).
	 * @return Predicted response time
	 */
	public double getPrediction(double[] inputVal) {
		if(inputVal.length != 6) {
			System.out.println("ERROR: Invalid input array!");
			System.exit(0);
		}
		
		double prediction = evaluateRegression(inputVal);
		if(prediction < 1.0) {
			prediction = 1.0;
		}
		
		return prediction;
	}
}
