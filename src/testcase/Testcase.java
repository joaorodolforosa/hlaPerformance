package testcase;

import federate.FedAmb;
import federate.Federate;
import hla.rti1516e.exceptions.RTIinternalError;

public class Testcase {
		
	String federateName;
	int iterations;
	Federate fed;
	FedAmb amb;
	
	public Testcase(String federateName, int iterations, Federate fed, FedAmb amb) {
		this.federateName = federateName;
		this.iterations = iterations;
		this.fed = fed;
		this.amb = amb;
	}
	
	public void init() throws RTIinternalError {
	
	}
	
	public void step(int iteration) throws RTIinternalError {
		
	}
	
	public void finish() throws RTIinternalError {
		
	}
	
	

}
