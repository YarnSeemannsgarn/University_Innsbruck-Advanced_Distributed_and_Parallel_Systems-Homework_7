package piEstimater.PiEstimater;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.Promises;

public class PiEstimaterWorkflowImpl implements PiEstimaterWorkflow {
	private PiEstimaterActivitiesClient client = new PiEstimaterActivitiesClientImpl();
	
	@Override
	public void estimatePi(int worker) {
		// Get distribution (one worker needed)
		Promise<List<ArrayList<Integer>>> distribution = client.calculateDenominatorDistribution(worker);
		
		// Calculate as many parts as workers are given
		Promise<List<Double>> calculatedParts = getCalculatedParts(distribution);
			
		// sumUp
		Promise<Double> sum = client.sumUp(calculatedParts);
		
		// Validate
		client.validateResult(sum);
	}
	
	@Asynchronous
	private Promise<List<Double>> getCalculatedParts(Promise<List<ArrayList<Integer>>> distribution){
		List<Promise<Double>> calculatedParts = new ArrayList<Promise<Double>>();
		for (List<Integer> distrubitonForWorker : distribution.get()) {
			int startDenominator = distrubitonForWorker.get(0); // startDenomination
			int endDenominator = distrubitonForWorker.get(1); // endDenomination
			
			try {
				Promise<Double> calculatedPart = client.calculatePart(startDenominator, endDenominator);
				calculatedParts.add(calculatedPart);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Promises.listOfPromisesToPromise(calculatedParts);
	}
}