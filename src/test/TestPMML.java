package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TestPMML {
	// Test reading a trained model saved as PMML into a Java program and do prediction.
	
	public static void main(String[] args) throws Exception {
		String modelFilename = "./data/test/resp_gbr.xml";
		int modelChoice = 2;
		PredictionModel myModel = new PredictionModel(modelFilename, modelChoice);
		
		String testFilename = "./data/test/test_response.csv";
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
				double[] inputVal = new double[lineStr.length-1];
				for(int i = 0; i < lineStr.length; i++) {
					if(i < lineStr.length-1) {
						inputVal[i] = Double.valueOf(lineStr[i]);
					} else {
						Double aTestVal = Double.valueOf(lineStr[i]);
						testVal.add(aTestVal);
					}
				}
				
				double aPredVal = myModel.getPrediction(inputVal);
				predVal.add(aPredVal);
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
