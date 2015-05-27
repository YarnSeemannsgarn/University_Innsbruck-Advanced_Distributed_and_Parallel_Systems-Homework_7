package piEstimater.PiEstimater;


import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 300, defaultTaskStartToCloseTimeoutSeconds = 10)

@Activities(version="1.00")

public interface PiEstimaterActivities {
	public List<ArrayList<Integer>> calculateDenominatorDistribution(int worker);
	public Double calculatePart(int startDenominator, int endDenominator) throws Exception;
	public Double sumUp(List<Double> calculatedParts);
	public void validateResult(Double result);
}