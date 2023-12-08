package testcase;

import federate.FedAmb;
import federate.Federate;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.DeletePrivilegeNotHeld;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

public class TestcaseObjectUpdate extends Testcase {
	private ObjectInstanceHandle localObjectHandle;
	private AttributeHandleValueMap attributes;
	private HLAinteger32BE objectAttribute;
	int counter = 0;
	
	public TestcaseObjectUpdate(String federateName, int iterations, Federate fed, FedAmb amb) {
		super(federateName, iterations, fed, amb);
	}
	
	public void init() {
		System.out.println("Executando TestcaseObjectUpdate");
		
		try {
			attributes = fed.rtiamb.getAttributeHandleValueMapFactory().create(1);
			localObjectHandle = fed.registerObject("HLAobjectRoot.TestcaseObject");
		} catch (FederateNotExecutionMember |
				NotConnected |
				RTIinternalError e) {
			System.out.println(e);
		}
		
	}
	
	public void step(int i) throws RTIinternalError {
		counter++;
		objectAttribute = fed.encoderFactory.createHLAinteger32BE(counter);
		attributes.put(fed.testcaseAttributeHandle, objectAttribute.toByteArray());
		
		try {
			fed.rtiamb.updateAttributeValues(localObjectHandle, attributes, fed.generateTag());
		} catch (AttributeNotOwned | AttributeNotDefined | ObjectInstanceNotKnown | SaveInProgress | RestoreInProgress
				| FederateNotExecutionMember | NotConnected | RTIinternalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fed.advanceTime();
	}
	
	public void finish() {
		System.out.println("Finalizada a execução de TestcaseObjectAttribute");
		
		try {
			fed.rtiamb.deleteObjectInstance(localObjectHandle, fed.generateTag());
		} catch (DeletePrivilegeNotHeld | ObjectInstanceNotKnown | SaveInProgress | RestoreInProgress
				| FederateNotExecutionMember | NotConnected | RTIinternalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

}
