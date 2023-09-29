package federate;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import hla.rti1516.SynchronizationPointFailureReason;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.FederateAmbassador.SupplementalReceiveInfo;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

public class FedAmb extends NullFederateAmbassador {
	double federateTime = 0.0;
	public double federateLookahead = 1.0;
	boolean isRegulating = false;
	boolean isConstrained = false;
	boolean isAdvancing = false;
	boolean isAnnounced = false;
	boolean isReadyToRun = false;
	volatile boolean owner = false;
	volatile boolean divestRequest = false;
	int lastValue = 0;
	int joinedFederates = 0;
	boolean isSynced = false;
	boolean isSyncedAnnounced = false;
	
	ObjectClassHandle testcaseObjectClassHandle;
	AttributeHandle testcaseAttributeHandle;
	
	ObjectInstanceHandle testcaseRTTObjectInstanceHandle; //for rtt
	volatile boolean receivedTestcaseRTTObjectHandle = false;
	
	public InteractionClassHandle testcaseInteractionHandle; // for interaction
	public ParameterHandle testcaseInteractionParamHandle;
	
	ObjectInstanceHandle target;
	AttributeHandleSet targetAtt;
	ObjectClassHandle hlaFederate;
	AttributeHandle hlaFederateHandle;
	
	public double convertTime(LogicalTime theTime) {
		HLAfloat64Time castTime = (HLAfloat64Time)theTime;
		return castTime.getValue();
	}
	
	@Override
	public void synchronizationPointRegistrationSucceeded(final String label) {
		System.out.println("Ponto de sincronização registrado com sucesso");
	}
	
	public void synchronizationPointRegistrationFailed(String synchronizationPointLabel,
            SynchronizationPointFailureReason reason) {
		System.out.println("Erro ao registrar ponto de sincronização: " + synchronizationPointLabel);
	}
	
	@Override
	public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] userSuppliedTag) {
		System.out.println("Anunciado ponto de sincronização: " + synchronizationPointLabel);
		if(synchronizationPointLabel.equals("ReadyToRun")) {
			isAnnounced = true;
		} else if (synchronizationPointLabel.equals("ReadyToSync")) {
			isSyncedAnnounced = true;
		}
		
	}
	
	@Override
	public void federationSynchronized(String synchronizationPointLabel, FederateHandleSet failedToSyncSet) {
		System.out.println("Federação sincronizada: " + synchronizationPointLabel);
		if(synchronizationPointLabel.equals("ReadyToRun")) {
			isReadyToRun = true;
		} else if(synchronizationPointLabel.equals("ReadyToSync")) {
			isSynced = true;
		}
	}
	
	@Override
	public void timeRegulationEnabled(LogicalTime time) {
		isRegulating = true;
		federateTime = convertTime(time);
	}
	
	@Override
	public void timeConstrainedEnabled(LogicalTime time) {
		isConstrained = true;
		federateTime = convertTime(time);
	}
	
	@Override
	public void timeAdvanceGrant(LogicalTime theTime) {
		isAdvancing = false;
		federateTime = convertTime(theTime);
	}
	
	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject,
            ObjectClassHandle theObjectClass,
            String objectName) {
		System.out.print("Discovered Object: handle = " + theObject);
		System.out.print(", classHandle = " + theObjectClass);
		System.out.print(", name = " + theObject);
		
		if(theObjectClass.equals(this.hlaFederate)) {
			System.out.println("Descoberto novo federado");
			joinedFederates++;
		} else {
			System.out.println("Object desconhecido do tipo: " + theObjectClass.toString());
		}
		
	}
	
	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject,
            ObjectClassHandle theObjectClass,
            String objectName,
            FederateHandle producingFederate) {
		System.out.print("Discovered Object: handle = " + theObject);
		System.out.print(", classHandle = " + theObjectClass);
		System.out.print(", name = " + theObject);
		System.out.println(", criado por = " + producingFederate);

		if(theObjectClass.equals(this.hlaFederate)) {
			System.out.println("Descoberto novo federado");
			joinedFederates++;
			System.out.println(joinedFederates);
		} else {
			System.out.println("Object desconhecido do tipo: " + theObjectClass.toString());
		}
	}
		
	@Override
	public final void receiveInteraction(InteractionClassHandle interactionClass,
			ParameterHandleValueMap theParameters,
			byte[] userSuppliedTag,
			OrderType sentOrdering,
			TransportationTypeHandle theTransport,
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
		if(interactionClass.equals(testcaseInteractionHandle)) { 
			
		}
	}

}
