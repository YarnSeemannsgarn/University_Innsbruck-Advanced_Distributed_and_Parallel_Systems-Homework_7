package piEstimater.PiEstimater;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class PiEstimaterActivitiesImpl implements PiEstimaterActivities {
	private static final int LAST_DENOMINATOR = 10001;
	
	@Override
	public Double calculatePart(int startDenominator, int endDenominator) throws Exception {
		String host = InetAddress.getLocalHost().getHostName();
		System.out.println("Calculate part 4/" + startDenominator + " - 4/" + endDenominator + " on " + host);
		
		// Get sign for denominator
		boolean signPlus;
		if((startDenominator % 4) == 1) {
			signPlus = true;
		}
		else if ((startDenominator % 4) == 3) {
			signPlus = false;
		}
		else{
			throw new Exception("Wrong startDenominator (" + startDenominator + ")! StartDenominator must be unequeal.");
		}
		
		double part = 0.0;
		for(int i = startDenominator; i <= endDenominator; i+=2){
			if(signPlus) {
				part += 4.0/i;
			}
			else{
				part -= 4.0/i;
			}
			
			signPlus = !signPlus;
		}
		
		return part;
	}

	@Override
	public Double sumUp(List<Double> calculatedParts) {
		double sum = 0.0;
		for (Double part : calculatedParts) {
			sum += part;
		}
		
		return sum;
	}

	@Override
	public List<ArrayList<Integer>> calculateDenominatorDistribution(int worker) {
		// Store startDenominator and endDenominator for each instance
		List<ArrayList<Integer>> distribution = new ArrayList<ArrayList<Integer>>(); //[worker][2];
		
		//Use adapted distribution formular of homework 1
		int fractionsTotal = (LAST_DENOMINATOR - 1) / 2;
		int startDenominator = 1;
		int endDenominator = -1;
		int fractionsPerInstance = (int) Math.floor(( fractionsTotal / worker ));
		int modulo = fractionsTotal % worker;
		for (int i = 0; i < worker; i++) {
			endDenominator = startDenominator + fractionsPerInstance * 2 - 2;
			if(i <= modulo) {
				endDenominator += 2;
			}
			
			ArrayList<Integer> distPart = new ArrayList<Integer>();
			distPart.add(startDenominator);
			distPart.add(endDenominator);
			distribution.add(distPart);
			
			startDenominator = endDenominator + 2;
		}
		
		return distribution;
	}

	@Override
	public void validateResult(Double result) {
		System.out.println("\nEstimated Result: " + result);
		if(Math.abs(Math.PI - result) < 0.01) {
			System.out.println("Validation with maximum variance of 0,01: Correct result! ");
		}
		else {
			System.out.println("Validation with maximum variance of 0,01: Wrong result!");
		}
	}	
}
