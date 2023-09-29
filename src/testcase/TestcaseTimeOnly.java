package testcase;

import federate.FedAmb;
import federate.Federate;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.RTIinternalError;

public class TestcaseTimeOnly extends Testcase {

	public TestcaseTimeOnly(String federateName, int iterations, Federate fed, FedAmb amb) {
		super(federateName, iterations, fed, amb);
	}
	
	@Override
	public void init() {
		System.out.println("Executando TestcaseTimeOnly");
	}
	
	@Override
	public void step(int i) throws RTIinternalError {
		fed.advanceTime(1);
	}
	
	@Override
	public void finish() {
		System.out.println("Finalizada a execução de TestcaseTimeOnly");
	}

}
