package federate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import java.util.ArrayList;
import java.util.Scanner;

import hla.rti.ReceivedInteraction;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateAmbassador.SupplementalReceiveInfo;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.AsynchronousDeliveryAlreadyEnabled;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNameAlreadyInUse;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidLookahead;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced;
import hla.rti1516e.exceptions.TimeConstrainedAlreadyEnabled;
import hla.rti1516e.exceptions.TimeRegulationAlreadyEnabled;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import testcase.Testcase;
import testcase.TestcaseEnum;
import testcase.TestcaseInteraction;
import testcase.TestcaseObjectUpdate;
import testcase.TestcaseTimeOnly;

public class Federate {

	public RTIambassador rtiamb;
	public FedAmb fedamb;

	protected String federateName;
	protected String federateNameStr;
	protected TestcaseEnum experiment;
	protected int noIterations;
	protected double evokeMin;
	protected double evokeMax;
	protected int interactionParamSize;
	protected int interactionTestCaseArray[];

	public static final String READY_TO_RUN = "ReadyToRun";
	public static final String READY_TO_SYNC = "ReadyToSync";
	public static final double DEFAULT_STEPLENGTH = 1;
	public static final int NUM_FEDERATES = 2;
	public static final String FEDERATION_NAME = "MinhaFederacao1516e";
	public static final String FOM_PATH = "/home/joaorodolforosa/eclipse-workspace/hlaPerformance/PerformanceEvaluation.xml";
	public static final String MIM_FOM_PATH = "/home/joaorodolforosa/eclipse-workspace/hlaPerformance/HLAstandardMIM.xml";

	public ObjectClassHandle testcaseObjectClassHandle;
	public AttributeHandle testcaseAttributeHandle;
	public InteractionClassHandle testcaseInteractionHandle;
	public ParameterHandle testcaseInteractionParamHandle;

	public EncoderFactory encoderFactory;

	AttributeHandleSet rttAttributes;
	AttributeHandleSet attributes;
	AttributeHandleSet attributes2;

	long start;
	long stop;

	public Federate(String fname, TestcaseEnum experiment, int noIterations, double min, double max,
			int interactionParamSize) {
		this.experiment = experiment;
		this.federateName = fname;
		this.noIterations = noIterations;
		this.evokeMin = min;
		this.evokeMax = max;
		this.interactionParamSize = interactionParamSize;
		this.interactionTestCaseArray = new int[interactionParamSize];
		for (int i = 0; i < interactionTestCaseArray.length; i++) {
			interactionTestCaseArray[i] = i;
		}
	}

	public void run() throws RTIinternalError {
		System.out.println("Executando com testcase = " + experiment + ", iter = " + noIterations + ", evokeMin = "
				+ evokeMin + ", evokeMax = " + evokeMax + ", intSize = " + interactionParamSize);

		// Set up
		RtiFactory factory = RtiFactoryFactory.getRtiFactory();
		rtiamb = factory.getRtiAmbassador();
		fedamb = new FedAmb();

		encoderFactory = factory.getEncoderFactory();

		// Connect
		try {
			rtiamb.connect(fedamb, CallbackModel.HLA_IMMEDIATE, "10.12.28.10");
			//rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);
		} catch (ConnectionFailed | InvalidLocalSettingsDesignator | UnsupportedCallbackModel | AlreadyConnected
				| CallNotAllowedFromWithinCallback | RTIinternalError e) {
			throw new RTIinternalError("Erro na conexão com o RTI ", e);
		}

		// Create and join
		File fom = new File(FOM_PATH);

		try {
			rtiamb.createFederationExecution(FEDERATION_NAME, fom.toURI().toURL());
			System.out.println("Criada a federação");
		} catch (InconsistentFDD | ErrorReadingFDD | CouldNotOpenFDD | NotConnected | RTIinternalError
				| MalformedURLException e) {
			throw new RTIinternalError("Erro na criação da federação ", e);
		} catch (FederationExecutionAlreadyExists ignored) {

		}

		try {
			rtiamb.joinFederationExecution(federateName, federateName, FEDERATION_NAME);
			System.out.println("Conectado à federação");
		} catch (CouldNotCreateLogicalTimeFactory | FederationExecutionDoesNotExist | SaveInProgress | RestoreInProgress
				| FederateAlreadyExecutionMember | NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError
				| FederateNameAlreadyInUse e) {
			throw new RTIinternalError("Erro na conexão com a Federação ", e);
		}

		initializeHandles();
		publishAndSubscribe();

		try {
			rtiamb.enableAsynchronousDelivery();
		} catch (AsynchronousDeliveryAlreadyEnabled | SaveInProgress | RestoreInProgress | FederateNotExecutionMember
				| NotConnected | RTIinternalError e) {
			throw new RTIinternalError("Erro ao habilitar entrega assíncrona (enableAsynchronousDelivery) ", e);
		}

		// Sync
		try {
			rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, generateTag());
			System.out.println("Registrado ponto de sincronização: " + READY_TO_RUN);
		} catch (SaveInProgress | RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
			throw new RTIinternalError("Erro ao registrar ponto de sincronização", e);
		}

		while (fedamb.isAnnounced == false) {
			try {
				rtiamb.evokeMultipleCallbacks(evokeMin, evokeMax);
			} catch (CallNotAllowedFromWithinCallback | RTIinternalError e) {
				throw new RTIinternalError("Erro ao evocar múltiplos callbacks", e);
			}
		}

		waitForFederates();
		// waitForUser();

		try {
			rtiamb.synchronizationPointAchieved(READY_TO_RUN);
			System.out.println("Atingido o ponto de sincronização: " + READY_TO_RUN);
			while (fedamb.isReadyToRun == false) {
				rtiamb.evokeMultipleCallbacks(evokeMin, evokeMax);
			}
		} catch (SynchronizationPointLabelNotAnnounced | SaveInProgress | RestoreInProgress | FederateNotExecutionMember
				| NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
			throw new RTIinternalError("Erro ao atingir ponto de sincronização", e);
		}

		// almost ready
		enableTimePolicy();

		// start test
		switch (experiment) {
		case Testcase_TimeOnly:
			TestcaseTimeOnly t = new TestcaseTimeOnly(federateName, noIterations, this, fedamb);
			for (int i = 0; i < 10; i++) {
				runTestcase(t);
			}
			break;
		case Testcase_Interaction:
			TestcaseInteraction t1 = new TestcaseInteraction(federateName, noIterations, this, fedamb, interactionTestCaseArray);
			for (int i = 0; i < 10; i++) {
				runTestcase(t1);				
			}
			break;
		case Testcase_ObjectUpdate:
			TestcaseObjectUpdate t3 = new TestcaseObjectUpdate(federateName, noIterations, this, fedamb);
			for (int i = 0; i < 10; i++) {
				runTestcase(t3);				
			}
			break;
		}

		// clean up

		try {
			rtiamb.resignFederationExecution(ResignAction.CANCEL_THEN_DELETE_THEN_DIVEST);
		} catch (InvalidResignAction | OwnershipAcquisitionPending | FederateOwnsAttributes | FederateNotExecutionMember
				| NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
			throw new RTIinternalError("Erro ao abandonar a federação", e);
		}

		try {
			rtiamb.destroyFederationExecution(FEDERATION_NAME);
		} catch (NotConnected | RTIinternalError e) {
			throw new RTIinternalError("Erro ao destruir a federação", e);
		} catch (FederatesCurrentlyJoined ignores) {

		} catch (FederationExecutionDoesNotExist ignores) {

		}

		try {
			rtiamb.disconnect();
		} catch (FederateIsExecutionMember | CallNotAllowedFromWithinCallback | RTIinternalError e) {
			throw new RTIinternalError("Erro ao desconectar do RTI", e);
		}

	}

	public void initializeHandles() throws RTIinternalError {
		try {
			testcaseObjectClassHandle = rtiamb.getObjectClassHandle("TestcaseObject");
			testcaseAttributeHandle = rtiamb.getAttributeHandle(testcaseObjectClassHandle, "TestcaseObjectAttribute");
			fedamb.hlaFederate = rtiamb.getObjectClassHandle("HLAmanager.HLAfederate");
			fedamb.hlaFederateHandle = rtiamb.getAttributeHandle(fedamb.hlaFederate, "HLAfederateHandle");
			System.out.println("Handles inicializados com sucesso");
		} catch (NameNotFound | FederateNotExecutionMember | NotConnected | RTIinternalError
				| InvalidObjectClassHandle e) {
			throw new RTIinternalError(
					"Não foi possível instanciar testcaseObjectClassHandle ou" + " testCaseAttributeHandle handles", e);
		}

		try {
			fedamb.testcaseInteractionHandle = rtiamb
					.getInteractionClassHandle("HLAinteractionRoot.TestcaseInteraction");
			fedamb.testcaseInteractionParamHandle = rtiamb.getParameterHandle(fedamb.testcaseInteractionHandle,
					"Payload");
			System.out.println(testcaseInteractionParamHandle);
		} catch (NameNotFound | FederateNotExecutionMember | NotConnected | RTIinternalError
				| InvalidInteractionClassHandle e) {
			throw new RTIinternalError(
					"Não foi possível instanciar testcaseInteractionHandle ou " + "testcaseInteractionParamHandle", e);
		}
	}

	public void publishAndSubscribe() throws RTIinternalError {
		try {
			rttAttributes = rtiamb.getAttributeHandleSetFactory().create();
			rttAttributes.add(testcaseAttributeHandle);
			rtiamb.publishObjectClassAttributes(testcaseObjectClassHandle, rttAttributes);
			rtiamb.subscribeObjectClassAttributes(testcaseObjectClassHandle, rttAttributes);

			rtiamb.publishInteractionClass(fedamb.testcaseInteractionHandle);
			rtiamb.subscribeInteractionClass(fedamb.testcaseInteractionHandle);

			attributes = rtiamb.getAttributeHandleSetFactory().create();
			attributes.add(fedamb.hlaFederateHandle);
			rtiamb.publishObjectClassAttributes(fedamb.hlaFederate, attributes);
			rtiamb.subscribeObjectClassAttributes(fedamb.hlaFederate, attributes);

		} catch (FederateNotExecutionMember | NotConnected | AttributeNotDefined | ObjectClassNotDefined
				| SaveInProgress | RestoreInProgress | InteractionClassNotDefined
				| FederateServiceInvocationsAreBeingReportedViaMOM | RTIinternalError e) {
			throw new RTIinternalError("Erro ao publicar e subscrever: ", e);
		}
	}

	public void waitForUser() {
		System.out.println("Pressione [ENTER] para continuar");
		try (Scanner line = new Scanner(System.in)) {
			line.nextLine();
		}
	}

	public void waitForFederates() throws RTIinternalError {
		System.out.println("Aguardando a descoberta de novos federados");
		while (fedamb.joinedFederates < NUM_FEDERATES) {
			try {
				evoke();
				attributes2 = rtiamb.getAttributeHandleSetFactory().create();
				attributes2.add(fedamb.hlaFederateHandle);
				rtiamb.requestAttributeValueUpdate(fedamb.hlaFederate, attributes2, generateTag());
				System.out.println("Conectados: " + fedamb.joinedFederates);

			} catch (RTIinternalError | FederateNotExecutionMember | NotConnected | AttributeNotDefined
					| ObjectClassNotDefined | SaveInProgress | RestoreInProgress e) {
				throw new RTIinternalError("Erro em waitForFederates: ", e);
			}
		}
	}

	public void enableTimePolicy() throws RTIinternalError {
		try {
			double lookahead = fedamb.federateLookahead;
			HLAfloat64TimeFactory timeFactory = (HLAfloat64TimeFactory) LogicalTimeFactoryFactory
					.getLogicalTimeFactory("HLAfloat64Time");
			HLAfloat64Interval interval = timeFactory.makeInterval(lookahead);
			rtiamb.enableTimeRegulation(interval);
		} catch (InvalidLookahead | InTimeAdvancingState | RequestForTimeRegulationPending
				| TimeRegulationAlreadyEnabled | SaveInProgress | RestoreInProgress | FederateNotExecutionMember
				| NotConnected | RTIinternalError e) {
			throw new RTIinternalError("Erro ao habilitar política de tempo", e);
		}

		while (fedamb.isRegulating == false) {
			try {

				rtiamb.evokeMultipleCallbacks(evokeMin, evokeMax);
			} catch (CallNotAllowedFromWithinCallback | RTIinternalError e) {
				throw new RTIinternalError("Erro ao evocar múltiplos callbacks", e);
			}
		}

		try {
			rtiamb.enableTimeConstrained();
		} catch (InTimeAdvancingState | RequestForTimeConstrainedPending | TimeConstrainedAlreadyEnabled
				| SaveInProgress | RestoreInProgress | FederateNotExecutionMember | NotConnected | RTIinternalError e) {
			throw new RTIinternalError("Erro ao habilitar limitação do tempo", e);
		}

		while (fedamb.isConstrained == false) {
			try {
				rtiamb.evokeMultipleCallbacks(evokeMin, evokeMax);
			} catch (CallNotAllowedFromWithinCallback | RTIinternalError e) {
				throw new RTIinternalError("Erro ao evocar múltiplos callbacks", e);
			}
		}
	}

	public void evoke() throws RTIinternalError {
		try {
			rtiamb.evokeMultipleCallbacks(evokeMin, evokeMax);
		} catch (CallNotAllowedFromWithinCallback | RTIinternalError e) {
			throw new RTIinternalError("Erro ao evocar múltiplos callbacks", e);
		}
	}

	public void advanceTime(double timeStep) throws RTIinternalError {
		fedamb.isAdvancing = true;
		HLAfloat64TimeFactory timeFactory = (HLAfloat64TimeFactory) LogicalTimeFactoryFactory
				.getLogicalTimeFactory("HLAfloat64Time");
		HLAfloat64Time newTime = timeFactory.makeTime(fedamb.federateTime + timeStep);
		try {
			rtiamb.timeAdvanceRequest(newTime);
			while (fedamb.isAdvancing) {
				rtiamb.evokeMultipleCallbacks(evokeMin, evokeMax);
			}
		} catch (LogicalTimeAlreadyPassed | InvalidLogicalTime | InTimeAdvancingState | RequestForTimeRegulationPending
				| RequestForTimeConstrainedPending | SaveInProgress | RestoreInProgress | FederateNotExecutionMember
				| NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
			throw new RTIinternalError("Erro em advanceTime(double timestep): ", e);
		}

	}

	public void advanceTime() throws RTIinternalError {
		try {
			advanceTime(DEFAULT_STEPLENGTH);
		} catch (RTIinternalError e) {
			throw new RTIinternalError("Erro em advanceTime(): ", e);
		}
	}

	void deleteObject(ObjectInstanceHandle objectHandle) {

	}

	public void timerStart() {
		start = System.nanoTime();
	}

	public void timerStop() {
		stop = System.nanoTime();
	}

	public ObjectInstanceHandle registerObject(String identifier) throws RTIinternalError {
		try {
			ObjectClassHandle oh = rtiamb.getObjectClassHandle(identifier);
			ObjectInstanceHandle ih = rtiamb.registerObjectInstance(oh);
			System.out.println(ih.toString());
			return ih;
		} catch (NameNotFound | FederateNotExecutionMember | NotConnected | ObjectClassNotPublished
				| ObjectClassNotDefined | SaveInProgress | RestoreInProgress | RTIinternalError e) {
			throw new RTIinternalError("Erro ao registrar objeto: ", e);
		}
	}

	void responseToOwnershipReleaseRequest(ObjectInstanceHandle theOobject, AttributeHandleSet candidateAttributes) {

	}

	public byte[] generateTag() {
		return ("(timestamp) " + System.currentTimeMillis()).getBytes();
	}

	public void runTestcase(Testcase testcase) throws RTIinternalError {
		System.out.println("Executando testcase.init()\nFederate: " + federateName);
		testcase.init();
		timerStart();
		for (int i = 0; i < noIterations; i++) {
			try {
				testcase.step(i);
			} catch (RTIinternalError e) {
				throw new RTIinternalError("Erro ao executar testcase", e);
			}
		}
		timerStop();
		System.out.println(stop - start);
	}

	public void ownershipReleaseRequest() {

	}

	public static void main(String[] args) throws RTIinternalError {
		if (args.length < 6) {
			System.out.println("bin testcase iterations evokemin evokemax [paramsize]");
		} else {
			int interactionParamSize = Integer.parseInt(args[0]);
			String federateName = args[1];
			TestcaseEnum experiment = TestcaseEnum.valueOf(args[2]);
			int noIterations = Integer.parseInt(args[3]);
			float evokeMin = Float.parseFloat(args[4]);
			float evokeMax = Float.parseFloat(args[5]);

			Federate federate = new Federate(federateName, experiment, noIterations, evokeMin, evokeMax,
					interactionParamSize);

			federate.run();
		}

	}

}
