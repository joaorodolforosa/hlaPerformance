package testcase;

import federate.FedAmb;
import federate.Federate;
import federate.HLAopaqueDataCoder;
import hla.rti.ReceivedInteraction;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

public class TestcaseInteraction extends Testcase {
	
	private EncoderFactory encoderFactory;
	private RtiFactory rtiFactory;
	private int[] intArray;
	
	public TestcaseInteraction(String federateName, int iterations, Federate fed, FedAmb amb, int[] testCaseArray) throws RTIinternalError {
		super(federateName, iterations, fed, amb);
		
		rtiFactory = RtiFactoryFactory.getRtiFactory();
		encoderFactory = rtiFactory.getEncoderFactory();
		intArray = testCaseArray;
	}
	
	
		
	@Override
	public void init() {
		System.out.println("Executando TestcaseInteraction");
	}
	
	@Override
	public void step(int i) throws RTIinternalError {
		// int[] intArray = {1, 3, 5, 7};
		try {
			ParameterHandleValueMap	parameter = fed.rtiamb.getParameterHandleValueMapFactory().create(1);
			HLAopaqueDataCoder opaqueDataCoder = new HLAopaqueDataCoder(encoderFactory);
			parameter.put(amb.testcaseInteractionParamHandle, opaqueDataCoder.encode(intArray));
			
			fed.rtiamb.sendInteraction(amb.testcaseInteractionHandle, parameter, fed.generateTag());
		} catch (FederateNotExecutionMember 
				| NotConnected 
				| InteractionClassNotPublished 
				| InteractionParameterNotDefined 
				| InteractionClassNotDefined 
				| SaveInProgress 
				| RestoreInProgress e) {
			throw new RTIinternalError("Erro em testCaseInteraction", null);
		}
		
		fed.advanceTime();
	}
	
	@Override
	public void finish() {
		System.out.println("Finalizada a execução de TestcaseInteraction");
	}
}
