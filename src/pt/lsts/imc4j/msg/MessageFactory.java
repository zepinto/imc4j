package pt.lsts.imc4j.msg;

import java.lang.Class;
import java.lang.Integer;
import java.lang.String;

public class MessageFactory {
	public static final int ID_EntityState = 1;

	public static final int ID_QueryEntityState = 2;

	public static final int ID_EntityInfo = 3;

	public static final int ID_QueryEntityInfo = 4;

	public static final int ID_EntityList = 5;

	public static final int ID_CpuUsage = 7;

	public static final int ID_TransportBindings = 8;

	public static final int ID_RestartSystem = 9;

	public static final int ID_DevCalibrationControl = 12;

	public static final int ID_DevCalibrationState = 13;

	public static final int ID_EntityActivationState = 14;

	public static final int ID_QueryEntityActivationState = 15;

	public static final int ID_VehicleOperationalLimits = 16;

	public static final int ID_MsgList = 20;

	public static final int ID_SimulatedState = 50;

	public static final int ID_LeakSimulation = 51;

	public static final int ID_UASimulation = 52;

	public static final int ID_DynamicsSimParam = 53;

	public static final int ID_StorageUsage = 100;

	public static final int ID_CacheControl = 101;

	public static final int ID_LoggingControl = 102;

	public static final int ID_LogBookEntry = 103;

	public static final int ID_LogBookControl = 104;

	public static final int ID_ReplayControl = 105;

	public static final int ID_ClockControl = 106;

	public static final int ID_HistoricCTD = 107;

	public static final int ID_HistoricTelemetry = 108;

	public static final int ID_HistoricSonarData = 109;

	public static final int ID_HistoricEvent = 110;

	public static final int ID_Heartbeat = 150;

	public static final int ID_Announce = 151;

	public static final int ID_AnnounceService = 152;

	public static final int ID_RSSI = 153;

	public static final int ID_VSWR = 154;

	public static final int ID_LinkLevel = 155;

	public static final int ID_Sms = 156;

	public static final int ID_SmsTx = 157;

	public static final int ID_SmsRx = 158;

	public static final int ID_SmsState = 159;

	public static final int ID_TextMessage = 160;

	public static final int ID_IridiumMsgRx = 170;

	public static final int ID_IridiumMsgTx = 171;

	public static final int ID_IridiumTxStatus = 172;

	public static final int ID_GroupMembershipState = 180;

	public static final int ID_SystemGroup = 181;

	public static final int ID_LinkLatency = 182;

	public static final int ID_ExtendedRSSI = 183;

	public static final int ID_HistoricData = 184;

	public static final int ID_CompressedHistory = 185;

	public static final int ID_HistoricSample = 186;

	public static final int ID_HistoricDataQuery = 187;

	public static final int ID_RemoteCommand = 188;

	public static final int ID_LblRange = 200;

	public static final int ID_LblBeacon = 202;

	public static final int ID_LblConfig = 203;

	public static final int ID_AcousticMessage = 206;

	public static final int ID_AcousticOperation = 211;

	public static final int ID_AcousticSystemsQuery = 212;

	public static final int ID_AcousticSystems = 213;

	public static final int ID_AcousticLink = 214;

	public static final int ID_Rpm = 250;

	public static final int ID_Voltage = 251;

	public static final int ID_Current = 252;

	public static final int ID_GpsFix = 253;

	public static final int ID_EulerAngles = 254;

	public static final int ID_EulerAnglesDelta = 255;

	public static final int ID_AngularVelocity = 256;

	public static final int ID_Acceleration = 257;

	public static final int ID_MagneticField = 258;

	public static final int ID_GroundVelocity = 259;

	public static final int ID_WaterVelocity = 260;

	public static final int ID_VelocityDelta = 261;

	public static final int ID_Distance = 262;

	public static final int ID_Temperature = 263;

	public static final int ID_Pressure = 264;

	public static final int ID_Depth = 265;

	public static final int ID_DepthOffset = 266;

	public static final int ID_SoundSpeed = 267;

	public static final int ID_WaterDensity = 268;

	public static final int ID_Conductivity = 269;

	public static final int ID_Salinity = 270;

	public static final int ID_WindSpeed = 271;

	public static final int ID_RelativeHumidity = 272;

	public static final int ID_DevDataText = 273;

	public static final int ID_DevDataBinary = 274;

	public static final int ID_SonarData = 276;

	public static final int ID_Pulse = 277;

	public static final int ID_PulseDetectionControl = 278;

	public static final int ID_FuelLevel = 279;

	public static final int ID_GpsNavData = 280;

	public static final int ID_ServoPosition = 281;

	public static final int ID_DeviceState = 282;

	public static final int ID_BeamConfig = 283;

	public static final int ID_DataSanity = 284;

	public static final int ID_RhodamineDye = 285;

	public static final int ID_CrudeOil = 286;

	public static final int ID_FineOil = 287;

	public static final int ID_Turbidity = 288;

	public static final int ID_Chlorophyll = 289;

	public static final int ID_Fluorescein = 290;

	public static final int ID_Phycocyanin = 291;

	public static final int ID_Phycoerythrin = 292;

	public static final int ID_GpsFixRtk = 293;

	public static final int ID_ExternalNavData = 294;

	public static final int ID_DissolvedOxygen = 295;

	public static final int ID_AirSaturation = 296;

	public static final int ID_Throttle = 297;

	public static final int ID_PH = 298;

	public static final int ID_Redox = 299;

	public static final int ID_CameraZoom = 300;

	public static final int ID_SetThrusterActuation = 301;

	public static final int ID_SetServoPosition = 302;

	public static final int ID_SetControlSurfaceDeflection = 303;

	public static final int ID_RemoteActionsRequest = 304;

	public static final int ID_RemoteActions = 305;

	public static final int ID_ButtonEvent = 306;

	public static final int ID_LcdControl = 307;

	public static final int ID_PowerOperation = 308;

	public static final int ID_PowerChannelControl = 309;

	public static final int ID_QueryPowerChannelState = 310;

	public static final int ID_PowerChannelState = 311;

	public static final int ID_LedBrightness = 312;

	public static final int ID_QueryLedBrightness = 313;

	public static final int ID_SetLedBrightness = 314;

	public static final int ID_SetPWM = 315;

	public static final int ID_PWM = 316;

	public static final int ID_EstimatedState = 350;

	public static final int ID_EstimatedStreamVelocity = 351;

	public static final int ID_IndicatedSpeed = 352;

	public static final int ID_TrueSpeed = 353;

	public static final int ID_NavigationUncertainty = 354;

	public static final int ID_NavigationData = 355;

	public static final int ID_GpsFixRejection = 356;

	public static final int ID_LblRangeAcceptance = 357;

	public static final int ID_DvlRejection = 358;

	public static final int ID_LblEstimate = 360;

	public static final int ID_AlignmentState = 361;

	public static final int ID_GroupStreamVelocity = 362;

	public static final int ID_Airflow = 363;

	public static final int ID_DesiredHeading = 400;

	public static final int ID_DesiredZ = 401;

	public static final int ID_DesiredSpeed = 402;

	public static final int ID_DesiredRoll = 403;

	public static final int ID_DesiredPitch = 404;

	public static final int ID_DesiredVerticalRate = 405;

	public static final int ID_DesiredPath = 406;

	public static final int ID_DesiredControl = 407;

	public static final int ID_DesiredHeadingRate = 408;

	public static final int ID_DesiredVelocity = 409;

	public static final int ID_PathControlState = 410;

	public static final int ID_AllocatedControlTorques = 411;

	public static final int ID_ControlParcel = 412;

	public static final int ID_Brake = 413;

	public static final int ID_DesiredLinearState = 414;

	public static final int ID_DesiredThrottle = 415;

	public static final int ID_Goto = 450;

	public static final int ID_PopUp = 451;

	public static final int ID_Teleoperation = 452;

	public static final int ID_Loiter = 453;

	public static final int ID_IdleManeuver = 454;

	public static final int ID_LowLevelControl = 455;

	public static final int ID_Rows = 456;

	public static final int ID_FollowPath = 457;

	public static final int ID_PathPoint = 458;

	public static final int ID_YoYo = 459;

	public static final int ID_TeleoperationDone = 460;

	public static final int ID_StationKeeping = 461;

	public static final int ID_Elevator = 462;

	public static final int ID_FollowTrajectory = 463;

	public static final int ID_TrajectoryPoint = 464;

	public static final int ID_CustomManeuver = 465;

	public static final int ID_VehicleFormation = 466;

	public static final int ID_VehicleFormationParticipant = 467;

	public static final int ID_StopManeuver = 468;

	public static final int ID_RegisterManeuver = 469;

	public static final int ID_ManeuverControlState = 470;

	public static final int ID_FollowSystem = 471;

	public static final int ID_CommsRelay = 472;

	public static final int ID_CoverArea = 473;

	public static final int ID_PolygonVertex = 474;

	public static final int ID_CompassCalibration = 475;

	public static final int ID_FormationParameters = 476;

	public static final int ID_FormationPlanExecution = 477;

	public static final int ID_FollowReference = 478;

	public static final int ID_Reference = 479;

	public static final int ID_FollowRefState = 480;

	public static final int ID_FormationMonitor = 481;

	public static final int ID_RelativeState = 482;

	public static final int ID_Dislodge = 483;

	public static final int ID_Formation = 484;

	public static final int ID_Launch = 485;

	public static final int ID_Drop = 486;

	public static final int ID_ScheduledGoto = 487;

	public static final int ID_RowsCoverage = 488;

	public static final int ID_Sample = 489;

	public static final int ID_ImageTracking = 490;

	public static final int ID_Takeoff = 491;

	public static final int ID_Land = 492;

	public static final int ID_AutonomousSection = 493;

	public static final int ID_FollowPoint = 494;

	public static final int ID_VehicleState = 500;

	public static final int ID_VehicleCommand = 501;

	public static final int ID_MonitorEntityState = 502;

	public static final int ID_EntityMonitoringState = 503;

	public static final int ID_OperationalLimits = 504;

	public static final int ID_GetOperationalLimits = 505;

	public static final int ID_Calibration = 506;

	public static final int ID_ControlLoops = 507;

	public static final int ID_VehicleMedium = 508;

	public static final int ID_Collision = 509;

	public static final int ID_FormState = 510;

	public static final int ID_AutopilotMode = 511;

	public static final int ID_FormationState = 512;

	public static final int ID_ReportControl = 513;

	public static final int ID_Abort = 550;

	public static final int ID_PlanSpecification = 551;

	public static final int ID_PlanManeuver = 552;

	public static final int ID_PlanTransition = 553;

	public static final int ID_EmergencyControl = 554;

	public static final int ID_EmergencyControlState = 555;

	public static final int ID_PlanDB = 556;

	public static final int ID_PlanDBState = 557;

	public static final int ID_PlanDBInformation = 558;

	public static final int ID_PlanControl = 559;

	public static final int ID_PlanControlState = 560;

	public static final int ID_PlanVariable = 561;

	public static final int ID_PlanGeneration = 562;

	public static final int ID_LeaderState = 563;

	public static final int ID_PlanStatistics = 564;

	public static final int ID_ReportedState = 600;

	public static final int ID_RemoteSensorInfo = 601;

	public static final int ID_Map = 602;

	public static final int ID_MapFeature = 603;

	public static final int ID_MapPoint = 604;

	public static final int ID_CcuEvent = 606;

	public static final int ID_VehicleLinks = 650;

	public static final int ID_TrexObservation = 651;

	public static final int ID_TrexCommand = 652;

	public static final int ID_TrexOperation = 655;

	public static final int ID_TrexAttribute = 656;

	public static final int ID_TrexToken = 657;

	public static final int ID_TrexPlan = 658;

	public static final int ID_Event = 660;

	public static final int ID_CompressedImage = 702;

	public static final int ID_ImageTxSettings = 703;

	public static final int ID_RemoteState = 750;

	public static final int ID_Target = 800;

	public static final int ID_EntityParameter = 801;

	public static final int ID_EntityParameters = 802;

	public static final int ID_QueryEntityParameters = 803;

	public static final int ID_SetEntityParameters = 804;

	public static final int ID_SaveEntityParameters = 805;

	public static final int ID_CreateSession = 806;

	public static final int ID_CloseSession = 807;

	public static final int ID_SessionSubscription = 808;

	public static final int ID_SessionKeepAlive = 809;

	public static final int ID_SessionStatus = 810;

	public static final int ID_PushEntityParameters = 811;

	public static final int ID_PopEntityParameters = 812;

	public static final int ID_IoEvent = 813;

	public static final int ID_UamTxFrame = 814;

	public static final int ID_UamRxFrame = 815;

	public static final int ID_UamTxStatus = 816;

	public static final int ID_UamRxRange = 817;

	public static final int ID_FormCtrlParam = 820;

	public static final int ID_FormationEval = 821;

	public static final int ID_FormationControlParams = 822;

	public static final int ID_FormationEvaluation = 823;

	public static final int ID_MessagePart = 877;

	public static final int ID_NeptusBlob = 888;

	public static final int ID_Aborted = 889;

	public static final int ID_UsblAngles = 890;

	public static final int ID_UsblPosition = 891;

	public static final int ID_UsblFix = 892;

	public static final int ID_ParametersXml = 893;

	public static final int ID_GetParametersXml = 894;

	public static final int ID_SetImageCoords = 895;

	public static final int ID_GetImageCoords = 896;

	public static final int ID_GetWorldCoordinates = 897;

	public static final int ID_UsblAnglesExtended = 898;

	public static final int ID_UsblPositionExtended = 899;

	public static final int ID_UsblFixExtended = 900;

	public static final int ID_UsblModem = 901;

	public static final int ID_UsblConfig = 902;

	public static final int ID_DissolvedOrganicMatter = 903;

	public static final int ID_OpticalBackscatter = 904;

	public static final int ID_Tachograph = 905;

	public static final int ID_ApmStatus = 906;

	public static Message create(int mgid) {
		switch(mgid) {
			case ID_EntityState: {
				return new EntityState();
			}
			case ID_QueryEntityState: {
				return new QueryEntityState();
			}
			case ID_EntityInfo: {
				return new EntityInfo();
			}
			case ID_QueryEntityInfo: {
				return new QueryEntityInfo();
			}
			case ID_EntityList: {
				return new EntityList();
			}
			case ID_CpuUsage: {
				return new CpuUsage();
			}
			case ID_TransportBindings: {
				return new TransportBindings();
			}
			case ID_RestartSystem: {
				return new RestartSystem();
			}
			case ID_DevCalibrationControl: {
				return new DevCalibrationControl();
			}
			case ID_DevCalibrationState: {
				return new DevCalibrationState();
			}
			case ID_EntityActivationState: {
				return new EntityActivationState();
			}
			case ID_QueryEntityActivationState: {
				return new QueryEntityActivationState();
			}
			case ID_VehicleOperationalLimits: {
				return new VehicleOperationalLimits();
			}
			case ID_MsgList: {
				return new MsgList();
			}
			case ID_SimulatedState: {
				return new SimulatedState();
			}
			case ID_LeakSimulation: {
				return new LeakSimulation();
			}
			case ID_UASimulation: {
				return new UASimulation();
			}
			case ID_DynamicsSimParam: {
				return new DynamicsSimParam();
			}
			case ID_StorageUsage: {
				return new StorageUsage();
			}
			case ID_CacheControl: {
				return new CacheControl();
			}
			case ID_LoggingControl: {
				return new LoggingControl();
			}
			case ID_LogBookEntry: {
				return new LogBookEntry();
			}
			case ID_LogBookControl: {
				return new LogBookControl();
			}
			case ID_ReplayControl: {
				return new ReplayControl();
			}
			case ID_ClockControl: {
				return new ClockControl();
			}
			case ID_HistoricCTD: {
				return new HistoricCTD();
			}
			case ID_HistoricTelemetry: {
				return new HistoricTelemetry();
			}
			case ID_HistoricSonarData: {
				return new HistoricSonarData();
			}
			case ID_HistoricEvent: {
				return new HistoricEvent();
			}
			case ID_Heartbeat: {
				return new Heartbeat();
			}
			case ID_Announce: {
				return new Announce();
			}
			case ID_AnnounceService: {
				return new AnnounceService();
			}
			case ID_RSSI: {
				return new RSSI();
			}
			case ID_VSWR: {
				return new VSWR();
			}
			case ID_LinkLevel: {
				return new LinkLevel();
			}
			case ID_Sms: {
				return new Sms();
			}
			case ID_SmsTx: {
				return new SmsTx();
			}
			case ID_SmsRx: {
				return new SmsRx();
			}
			case ID_SmsState: {
				return new SmsState();
			}
			case ID_TextMessage: {
				return new TextMessage();
			}
			case ID_IridiumMsgRx: {
				return new IridiumMsgRx();
			}
			case ID_IridiumMsgTx: {
				return new IridiumMsgTx();
			}
			case ID_IridiumTxStatus: {
				return new IridiumTxStatus();
			}
			case ID_GroupMembershipState: {
				return new GroupMembershipState();
			}
			case ID_SystemGroup: {
				return new SystemGroup();
			}
			case ID_LinkLatency: {
				return new LinkLatency();
			}
			case ID_ExtendedRSSI: {
				return new ExtendedRSSI();
			}
			case ID_HistoricData: {
				return new HistoricData();
			}
			case ID_CompressedHistory: {
				return new CompressedHistory();
			}
			case ID_HistoricSample: {
				return new HistoricSample();
			}
			case ID_HistoricDataQuery: {
				return new HistoricDataQuery();
			}
			case ID_RemoteCommand: {
				return new RemoteCommand();
			}
			case ID_LblRange: {
				return new LblRange();
			}
			case ID_LblBeacon: {
				return new LblBeacon();
			}
			case ID_LblConfig: {
				return new LblConfig();
			}
			case ID_AcousticMessage: {
				return new AcousticMessage();
			}
			case ID_AcousticOperation: {
				return new AcousticOperation();
			}
			case ID_AcousticSystemsQuery: {
				return new AcousticSystemsQuery();
			}
			case ID_AcousticSystems: {
				return new AcousticSystems();
			}
			case ID_AcousticLink: {
				return new AcousticLink();
			}
			case ID_Rpm: {
				return new Rpm();
			}
			case ID_Voltage: {
				return new Voltage();
			}
			case ID_Current: {
				return new Current();
			}
			case ID_GpsFix: {
				return new GpsFix();
			}
			case ID_EulerAngles: {
				return new EulerAngles();
			}
			case ID_EulerAnglesDelta: {
				return new EulerAnglesDelta();
			}
			case ID_AngularVelocity: {
				return new AngularVelocity();
			}
			case ID_Acceleration: {
				return new Acceleration();
			}
			case ID_MagneticField: {
				return new MagneticField();
			}
			case ID_GroundVelocity: {
				return new GroundVelocity();
			}
			case ID_WaterVelocity: {
				return new WaterVelocity();
			}
			case ID_VelocityDelta: {
				return new VelocityDelta();
			}
			case ID_Distance: {
				return new Distance();
			}
			case ID_Temperature: {
				return new Temperature();
			}
			case ID_Pressure: {
				return new Pressure();
			}
			case ID_Depth: {
				return new Depth();
			}
			case ID_DepthOffset: {
				return new DepthOffset();
			}
			case ID_SoundSpeed: {
				return new SoundSpeed();
			}
			case ID_WaterDensity: {
				return new WaterDensity();
			}
			case ID_Conductivity: {
				return new Conductivity();
			}
			case ID_Salinity: {
				return new Salinity();
			}
			case ID_WindSpeed: {
				return new WindSpeed();
			}
			case ID_RelativeHumidity: {
				return new RelativeHumidity();
			}
			case ID_DevDataText: {
				return new DevDataText();
			}
			case ID_DevDataBinary: {
				return new DevDataBinary();
			}
			case ID_SonarData: {
				return new SonarData();
			}
			case ID_Pulse: {
				return new Pulse();
			}
			case ID_PulseDetectionControl: {
				return new PulseDetectionControl();
			}
			case ID_FuelLevel: {
				return new FuelLevel();
			}
			case ID_GpsNavData: {
				return new GpsNavData();
			}
			case ID_ServoPosition: {
				return new ServoPosition();
			}
			case ID_DeviceState: {
				return new DeviceState();
			}
			case ID_BeamConfig: {
				return new BeamConfig();
			}
			case ID_DataSanity: {
				return new DataSanity();
			}
			case ID_RhodamineDye: {
				return new RhodamineDye();
			}
			case ID_CrudeOil: {
				return new CrudeOil();
			}
			case ID_FineOil: {
				return new FineOil();
			}
			case ID_Turbidity: {
				return new Turbidity();
			}
			case ID_Chlorophyll: {
				return new Chlorophyll();
			}
			case ID_Fluorescein: {
				return new Fluorescein();
			}
			case ID_Phycocyanin: {
				return new Phycocyanin();
			}
			case ID_Phycoerythrin: {
				return new Phycoerythrin();
			}
			case ID_GpsFixRtk: {
				return new GpsFixRtk();
			}
			case ID_ExternalNavData: {
				return new ExternalNavData();
			}
			case ID_DissolvedOxygen: {
				return new DissolvedOxygen();
			}
			case ID_AirSaturation: {
				return new AirSaturation();
			}
			case ID_Throttle: {
				return new Throttle();
			}
			case ID_PH: {
				return new PH();
			}
			case ID_Redox: {
				return new Redox();
			}
			case ID_CameraZoom: {
				return new CameraZoom();
			}
			case ID_SetThrusterActuation: {
				return new SetThrusterActuation();
			}
			case ID_SetServoPosition: {
				return new SetServoPosition();
			}
			case ID_SetControlSurfaceDeflection: {
				return new SetControlSurfaceDeflection();
			}
			case ID_RemoteActionsRequest: {
				return new RemoteActionsRequest();
			}
			case ID_RemoteActions: {
				return new RemoteActions();
			}
			case ID_ButtonEvent: {
				return new ButtonEvent();
			}
			case ID_LcdControl: {
				return new LcdControl();
			}
			case ID_PowerOperation: {
				return new PowerOperation();
			}
			case ID_PowerChannelControl: {
				return new PowerChannelControl();
			}
			case ID_QueryPowerChannelState: {
				return new QueryPowerChannelState();
			}
			case ID_PowerChannelState: {
				return new PowerChannelState();
			}
			case ID_LedBrightness: {
				return new LedBrightness();
			}
			case ID_QueryLedBrightness: {
				return new QueryLedBrightness();
			}
			case ID_SetLedBrightness: {
				return new SetLedBrightness();
			}
			case ID_SetPWM: {
				return new SetPWM();
			}
			case ID_PWM: {
				return new PWM();
			}
			case ID_EstimatedState: {
				return new EstimatedState();
			}
			case ID_EstimatedStreamVelocity: {
				return new EstimatedStreamVelocity();
			}
			case ID_IndicatedSpeed: {
				return new IndicatedSpeed();
			}
			case ID_TrueSpeed: {
				return new TrueSpeed();
			}
			case ID_NavigationUncertainty: {
				return new NavigationUncertainty();
			}
			case ID_NavigationData: {
				return new NavigationData();
			}
			case ID_GpsFixRejection: {
				return new GpsFixRejection();
			}
			case ID_LblRangeAcceptance: {
				return new LblRangeAcceptance();
			}
			case ID_DvlRejection: {
				return new DvlRejection();
			}
			case ID_LblEstimate: {
				return new LblEstimate();
			}
			case ID_AlignmentState: {
				return new AlignmentState();
			}
			case ID_GroupStreamVelocity: {
				return new GroupStreamVelocity();
			}
			case ID_Airflow: {
				return new Airflow();
			}
			case ID_DesiredHeading: {
				return new DesiredHeading();
			}
			case ID_DesiredZ: {
				return new DesiredZ();
			}
			case ID_DesiredSpeed: {
				return new DesiredSpeed();
			}
			case ID_DesiredRoll: {
				return new DesiredRoll();
			}
			case ID_DesiredPitch: {
				return new DesiredPitch();
			}
			case ID_DesiredVerticalRate: {
				return new DesiredVerticalRate();
			}
			case ID_DesiredPath: {
				return new DesiredPath();
			}
			case ID_DesiredControl: {
				return new DesiredControl();
			}
			case ID_DesiredHeadingRate: {
				return new DesiredHeadingRate();
			}
			case ID_DesiredVelocity: {
				return new DesiredVelocity();
			}
			case ID_PathControlState: {
				return new PathControlState();
			}
			case ID_AllocatedControlTorques: {
				return new AllocatedControlTorques();
			}
			case ID_ControlParcel: {
				return new ControlParcel();
			}
			case ID_Brake: {
				return new Brake();
			}
			case ID_DesiredLinearState: {
				return new DesiredLinearState();
			}
			case ID_DesiredThrottle: {
				return new DesiredThrottle();
			}
			case ID_Goto: {
				return new Goto();
			}
			case ID_PopUp: {
				return new PopUp();
			}
			case ID_Teleoperation: {
				return new Teleoperation();
			}
			case ID_Loiter: {
				return new Loiter();
			}
			case ID_IdleManeuver: {
				return new IdleManeuver();
			}
			case ID_LowLevelControl: {
				return new LowLevelControl();
			}
			case ID_Rows: {
				return new Rows();
			}
			case ID_FollowPath: {
				return new FollowPath();
			}
			case ID_PathPoint: {
				return new PathPoint();
			}
			case ID_YoYo: {
				return new YoYo();
			}
			case ID_TeleoperationDone: {
				return new TeleoperationDone();
			}
			case ID_StationKeeping: {
				return new StationKeeping();
			}
			case ID_Elevator: {
				return new Elevator();
			}
			case ID_FollowTrajectory: {
				return new FollowTrajectory();
			}
			case ID_TrajectoryPoint: {
				return new TrajectoryPoint();
			}
			case ID_CustomManeuver: {
				return new CustomManeuver();
			}
			case ID_VehicleFormation: {
				return new VehicleFormation();
			}
			case ID_VehicleFormationParticipant: {
				return new VehicleFormationParticipant();
			}
			case ID_StopManeuver: {
				return new StopManeuver();
			}
			case ID_RegisterManeuver: {
				return new RegisterManeuver();
			}
			case ID_ManeuverControlState: {
				return new ManeuverControlState();
			}
			case ID_FollowSystem: {
				return new FollowSystem();
			}
			case ID_CommsRelay: {
				return new CommsRelay();
			}
			case ID_CoverArea: {
				return new CoverArea();
			}
			case ID_PolygonVertex: {
				return new PolygonVertex();
			}
			case ID_CompassCalibration: {
				return new CompassCalibration();
			}
			case ID_FormationParameters: {
				return new FormationParameters();
			}
			case ID_FormationPlanExecution: {
				return new FormationPlanExecution();
			}
			case ID_FollowReference: {
				return new FollowReference();
			}
			case ID_Reference: {
				return new Reference();
			}
			case ID_FollowRefState: {
				return new FollowRefState();
			}
			case ID_FormationMonitor: {
				return new FormationMonitor();
			}
			case ID_RelativeState: {
				return new RelativeState();
			}
			case ID_Dislodge: {
				return new Dislodge();
			}
			case ID_Formation: {
				return new Formation();
			}
			case ID_Launch: {
				return new Launch();
			}
			case ID_Drop: {
				return new Drop();
			}
			case ID_ScheduledGoto: {
				return new ScheduledGoto();
			}
			case ID_RowsCoverage: {
				return new RowsCoverage();
			}
			case ID_Sample: {
				return new Sample();
			}
			case ID_ImageTracking: {
				return new ImageTracking();
			}
			case ID_Takeoff: {
				return new Takeoff();
			}
			case ID_Land: {
				return new Land();
			}
			case ID_AutonomousSection: {
				return new AutonomousSection();
			}
			case ID_FollowPoint: {
				return new FollowPoint();
			}
			case ID_VehicleState: {
				return new VehicleState();
			}
			case ID_VehicleCommand: {
				return new VehicleCommand();
			}
			case ID_MonitorEntityState: {
				return new MonitorEntityState();
			}
			case ID_EntityMonitoringState: {
				return new EntityMonitoringState();
			}
			case ID_OperationalLimits: {
				return new OperationalLimits();
			}
			case ID_GetOperationalLimits: {
				return new GetOperationalLimits();
			}
			case ID_Calibration: {
				return new Calibration();
			}
			case ID_ControlLoops: {
				return new ControlLoops();
			}
			case ID_VehicleMedium: {
				return new VehicleMedium();
			}
			case ID_Collision: {
				return new Collision();
			}
			case ID_FormState: {
				return new FormState();
			}
			case ID_AutopilotMode: {
				return new AutopilotMode();
			}
			case ID_FormationState: {
				return new FormationState();
			}
			case ID_ReportControl: {
				return new ReportControl();
			}
			case ID_Abort: {
				return new Abort();
			}
			case ID_PlanSpecification: {
				return new PlanSpecification();
			}
			case ID_PlanManeuver: {
				return new PlanManeuver();
			}
			case ID_PlanTransition: {
				return new PlanTransition();
			}
			case ID_EmergencyControl: {
				return new EmergencyControl();
			}
			case ID_EmergencyControlState: {
				return new EmergencyControlState();
			}
			case ID_PlanDB: {
				return new PlanDB();
			}
			case ID_PlanDBState: {
				return new PlanDBState();
			}
			case ID_PlanDBInformation: {
				return new PlanDBInformation();
			}
			case ID_PlanControl: {
				return new PlanControl();
			}
			case ID_PlanControlState: {
				return new PlanControlState();
			}
			case ID_PlanVariable: {
				return new PlanVariable();
			}
			case ID_PlanGeneration: {
				return new PlanGeneration();
			}
			case ID_LeaderState: {
				return new LeaderState();
			}
			case ID_PlanStatistics: {
				return new PlanStatistics();
			}
			case ID_ReportedState: {
				return new ReportedState();
			}
			case ID_RemoteSensorInfo: {
				return new RemoteSensorInfo();
			}
			case ID_Map: {
				return new Map();
			}
			case ID_MapFeature: {
				return new MapFeature();
			}
			case ID_MapPoint: {
				return new MapPoint();
			}
			case ID_CcuEvent: {
				return new CcuEvent();
			}
			case ID_VehicleLinks: {
				return new VehicleLinks();
			}
			case ID_TrexObservation: {
				return new TrexObservation();
			}
			case ID_TrexCommand: {
				return new TrexCommand();
			}
			case ID_TrexOperation: {
				return new TrexOperation();
			}
			case ID_TrexAttribute: {
				return new TrexAttribute();
			}
			case ID_TrexToken: {
				return new TrexToken();
			}
			case ID_TrexPlan: {
				return new TrexPlan();
			}
			case ID_Event: {
				return new Event();
			}
			case ID_CompressedImage: {
				return new CompressedImage();
			}
			case ID_ImageTxSettings: {
				return new ImageTxSettings();
			}
			case ID_RemoteState: {
				return new RemoteState();
			}
			case ID_Target: {
				return new Target();
			}
			case ID_EntityParameter: {
				return new EntityParameter();
			}
			case ID_EntityParameters: {
				return new EntityParameters();
			}
			case ID_QueryEntityParameters: {
				return new QueryEntityParameters();
			}
			case ID_SetEntityParameters: {
				return new SetEntityParameters();
			}
			case ID_SaveEntityParameters: {
				return new SaveEntityParameters();
			}
			case ID_CreateSession: {
				return new CreateSession();
			}
			case ID_CloseSession: {
				return new CloseSession();
			}
			case ID_SessionSubscription: {
				return new SessionSubscription();
			}
			case ID_SessionKeepAlive: {
				return new SessionKeepAlive();
			}
			case ID_SessionStatus: {
				return new SessionStatus();
			}
			case ID_PushEntityParameters: {
				return new PushEntityParameters();
			}
			case ID_PopEntityParameters: {
				return new PopEntityParameters();
			}
			case ID_IoEvent: {
				return new IoEvent();
			}
			case ID_UamTxFrame: {
				return new UamTxFrame();
			}
			case ID_UamRxFrame: {
				return new UamRxFrame();
			}
			case ID_UamTxStatus: {
				return new UamTxStatus();
			}
			case ID_UamRxRange: {
				return new UamRxRange();
			}
			case ID_FormCtrlParam: {
				return new FormCtrlParam();
			}
			case ID_FormationEval: {
				return new FormationEval();
			}
			case ID_FormationControlParams: {
				return new FormationControlParams();
			}
			case ID_FormationEvaluation: {
				return new FormationEvaluation();
			}
			case ID_MessagePart: {
				return new MessagePart();
			}
			case ID_NeptusBlob: {
				return new NeptusBlob();
			}
			case ID_Aborted: {
				return new Aborted();
			}
			case ID_UsblAngles: {
				return new UsblAngles();
			}
			case ID_UsblPosition: {
				return new UsblPosition();
			}
			case ID_UsblFix: {
				return new UsblFix();
			}
			case ID_ParametersXml: {
				return new ParametersXml();
			}
			case ID_GetParametersXml: {
				return new GetParametersXml();
			}
			case ID_SetImageCoords: {
				return new SetImageCoords();
			}
			case ID_GetImageCoords: {
				return new GetImageCoords();
			}
			case ID_GetWorldCoordinates: {
				return new GetWorldCoordinates();
			}
			case ID_UsblAnglesExtended: {
				return new UsblAnglesExtended();
			}
			case ID_UsblPositionExtended: {
				return new UsblPositionExtended();
			}
			case ID_UsblFixExtended: {
				return new UsblFixExtended();
			}
			case ID_UsblModem: {
				return new UsblModem();
			}
			case ID_UsblConfig: {
				return new UsblConfig();
			}
			case ID_DissolvedOrganicMatter: {
				return new DissolvedOrganicMatter();
			}
			case ID_OpticalBackscatter: {
				return new OpticalBackscatter();
			}
			case ID_Tachograph: {
				return new Tachograph();
			}
			case ID_ApmStatus: {
				return new ApmStatus();
			}
			default: {
				return null;
			}
		}
	}

	public static Message create(String abbrev) {
		return create(idOf(abbrev));
	}

	public static int idOf(String abbrev) {
		switch(abbrev) {
			case "EntityState": {
				return ID_EntityState;
			}
			case "QueryEntityState": {
				return ID_QueryEntityState;
			}
			case "EntityInfo": {
				return ID_EntityInfo;
			}
			case "QueryEntityInfo": {
				return ID_QueryEntityInfo;
			}
			case "EntityList": {
				return ID_EntityList;
			}
			case "CpuUsage": {
				return ID_CpuUsage;
			}
			case "TransportBindings": {
				return ID_TransportBindings;
			}
			case "RestartSystem": {
				return ID_RestartSystem;
			}
			case "DevCalibrationControl": {
				return ID_DevCalibrationControl;
			}
			case "DevCalibrationState": {
				return ID_DevCalibrationState;
			}
			case "EntityActivationState": {
				return ID_EntityActivationState;
			}
			case "QueryEntityActivationState": {
				return ID_QueryEntityActivationState;
			}
			case "VehicleOperationalLimits": {
				return ID_VehicleOperationalLimits;
			}
			case "MsgList": {
				return ID_MsgList;
			}
			case "SimulatedState": {
				return ID_SimulatedState;
			}
			case "LeakSimulation": {
				return ID_LeakSimulation;
			}
			case "UASimulation": {
				return ID_UASimulation;
			}
			case "DynamicsSimParam": {
				return ID_DynamicsSimParam;
			}
			case "StorageUsage": {
				return ID_StorageUsage;
			}
			case "CacheControl": {
				return ID_CacheControl;
			}
			case "LoggingControl": {
				return ID_LoggingControl;
			}
			case "LogBookEntry": {
				return ID_LogBookEntry;
			}
			case "LogBookControl": {
				return ID_LogBookControl;
			}
			case "ReplayControl": {
				return ID_ReplayControl;
			}
			case "ClockControl": {
				return ID_ClockControl;
			}
			case "HistoricCTD": {
				return ID_HistoricCTD;
			}
			case "HistoricTelemetry": {
				return ID_HistoricTelemetry;
			}
			case "HistoricSonarData": {
				return ID_HistoricSonarData;
			}
			case "HistoricEvent": {
				return ID_HistoricEvent;
			}
			case "Heartbeat": {
				return ID_Heartbeat;
			}
			case "Announce": {
				return ID_Announce;
			}
			case "AnnounceService": {
				return ID_AnnounceService;
			}
			case "RSSI": {
				return ID_RSSI;
			}
			case "VSWR": {
				return ID_VSWR;
			}
			case "LinkLevel": {
				return ID_LinkLevel;
			}
			case "Sms": {
				return ID_Sms;
			}
			case "SmsTx": {
				return ID_SmsTx;
			}
			case "SmsRx": {
				return ID_SmsRx;
			}
			case "SmsState": {
				return ID_SmsState;
			}
			case "TextMessage": {
				return ID_TextMessage;
			}
			case "IridiumMsgRx": {
				return ID_IridiumMsgRx;
			}
			case "IridiumMsgTx": {
				return ID_IridiumMsgTx;
			}
			case "IridiumTxStatus": {
				return ID_IridiumTxStatus;
			}
			case "GroupMembershipState": {
				return ID_GroupMembershipState;
			}
			case "SystemGroup": {
				return ID_SystemGroup;
			}
			case "LinkLatency": {
				return ID_LinkLatency;
			}
			case "ExtendedRSSI": {
				return ID_ExtendedRSSI;
			}
			case "HistoricData": {
				return ID_HistoricData;
			}
			case "CompressedHistory": {
				return ID_CompressedHistory;
			}
			case "HistoricSample": {
				return ID_HistoricSample;
			}
			case "HistoricDataQuery": {
				return ID_HistoricDataQuery;
			}
			case "RemoteCommand": {
				return ID_RemoteCommand;
			}
			case "LblRange": {
				return ID_LblRange;
			}
			case "LblBeacon": {
				return ID_LblBeacon;
			}
			case "LblConfig": {
				return ID_LblConfig;
			}
			case "AcousticMessage": {
				return ID_AcousticMessage;
			}
			case "AcousticOperation": {
				return ID_AcousticOperation;
			}
			case "AcousticSystemsQuery": {
				return ID_AcousticSystemsQuery;
			}
			case "AcousticSystems": {
				return ID_AcousticSystems;
			}
			case "AcousticLink": {
				return ID_AcousticLink;
			}
			case "Rpm": {
				return ID_Rpm;
			}
			case "Voltage": {
				return ID_Voltage;
			}
			case "Current": {
				return ID_Current;
			}
			case "GpsFix": {
				return ID_GpsFix;
			}
			case "EulerAngles": {
				return ID_EulerAngles;
			}
			case "EulerAnglesDelta": {
				return ID_EulerAnglesDelta;
			}
			case "AngularVelocity": {
				return ID_AngularVelocity;
			}
			case "Acceleration": {
				return ID_Acceleration;
			}
			case "MagneticField": {
				return ID_MagneticField;
			}
			case "GroundVelocity": {
				return ID_GroundVelocity;
			}
			case "WaterVelocity": {
				return ID_WaterVelocity;
			}
			case "VelocityDelta": {
				return ID_VelocityDelta;
			}
			case "Distance": {
				return ID_Distance;
			}
			case "Temperature": {
				return ID_Temperature;
			}
			case "Pressure": {
				return ID_Pressure;
			}
			case "Depth": {
				return ID_Depth;
			}
			case "DepthOffset": {
				return ID_DepthOffset;
			}
			case "SoundSpeed": {
				return ID_SoundSpeed;
			}
			case "WaterDensity": {
				return ID_WaterDensity;
			}
			case "Conductivity": {
				return ID_Conductivity;
			}
			case "Salinity": {
				return ID_Salinity;
			}
			case "WindSpeed": {
				return ID_WindSpeed;
			}
			case "RelativeHumidity": {
				return ID_RelativeHumidity;
			}
			case "DevDataText": {
				return ID_DevDataText;
			}
			case "DevDataBinary": {
				return ID_DevDataBinary;
			}
			case "SonarData": {
				return ID_SonarData;
			}
			case "Pulse": {
				return ID_Pulse;
			}
			case "PulseDetectionControl": {
				return ID_PulseDetectionControl;
			}
			case "FuelLevel": {
				return ID_FuelLevel;
			}
			case "GpsNavData": {
				return ID_GpsNavData;
			}
			case "ServoPosition": {
				return ID_ServoPosition;
			}
			case "DeviceState": {
				return ID_DeviceState;
			}
			case "BeamConfig": {
				return ID_BeamConfig;
			}
			case "DataSanity": {
				return ID_DataSanity;
			}
			case "RhodamineDye": {
				return ID_RhodamineDye;
			}
			case "CrudeOil": {
				return ID_CrudeOil;
			}
			case "FineOil": {
				return ID_FineOil;
			}
			case "Turbidity": {
				return ID_Turbidity;
			}
			case "Chlorophyll": {
				return ID_Chlorophyll;
			}
			case "Fluorescein": {
				return ID_Fluorescein;
			}
			case "Phycocyanin": {
				return ID_Phycocyanin;
			}
			case "Phycoerythrin": {
				return ID_Phycoerythrin;
			}
			case "GpsFixRtk": {
				return ID_GpsFixRtk;
			}
			case "ExternalNavData": {
				return ID_ExternalNavData;
			}
			case "DissolvedOxygen": {
				return ID_DissolvedOxygen;
			}
			case "AirSaturation": {
				return ID_AirSaturation;
			}
			case "Throttle": {
				return ID_Throttle;
			}
			case "PH": {
				return ID_PH;
			}
			case "Redox": {
				return ID_Redox;
			}
			case "CameraZoom": {
				return ID_CameraZoom;
			}
			case "SetThrusterActuation": {
				return ID_SetThrusterActuation;
			}
			case "SetServoPosition": {
				return ID_SetServoPosition;
			}
			case "SetControlSurfaceDeflection": {
				return ID_SetControlSurfaceDeflection;
			}
			case "RemoteActionsRequest": {
				return ID_RemoteActionsRequest;
			}
			case "RemoteActions": {
				return ID_RemoteActions;
			}
			case "ButtonEvent": {
				return ID_ButtonEvent;
			}
			case "LcdControl": {
				return ID_LcdControl;
			}
			case "PowerOperation": {
				return ID_PowerOperation;
			}
			case "PowerChannelControl": {
				return ID_PowerChannelControl;
			}
			case "QueryPowerChannelState": {
				return ID_QueryPowerChannelState;
			}
			case "PowerChannelState": {
				return ID_PowerChannelState;
			}
			case "LedBrightness": {
				return ID_LedBrightness;
			}
			case "QueryLedBrightness": {
				return ID_QueryLedBrightness;
			}
			case "SetLedBrightness": {
				return ID_SetLedBrightness;
			}
			case "SetPWM": {
				return ID_SetPWM;
			}
			case "PWM": {
				return ID_PWM;
			}
			case "EstimatedState": {
				return ID_EstimatedState;
			}
			case "EstimatedStreamVelocity": {
				return ID_EstimatedStreamVelocity;
			}
			case "IndicatedSpeed": {
				return ID_IndicatedSpeed;
			}
			case "TrueSpeed": {
				return ID_TrueSpeed;
			}
			case "NavigationUncertainty": {
				return ID_NavigationUncertainty;
			}
			case "NavigationData": {
				return ID_NavigationData;
			}
			case "GpsFixRejection": {
				return ID_GpsFixRejection;
			}
			case "LblRangeAcceptance": {
				return ID_LblRangeAcceptance;
			}
			case "DvlRejection": {
				return ID_DvlRejection;
			}
			case "LblEstimate": {
				return ID_LblEstimate;
			}
			case "AlignmentState": {
				return ID_AlignmentState;
			}
			case "GroupStreamVelocity": {
				return ID_GroupStreamVelocity;
			}
			case "Airflow": {
				return ID_Airflow;
			}
			case "DesiredHeading": {
				return ID_DesiredHeading;
			}
			case "DesiredZ": {
				return ID_DesiredZ;
			}
			case "DesiredSpeed": {
				return ID_DesiredSpeed;
			}
			case "DesiredRoll": {
				return ID_DesiredRoll;
			}
			case "DesiredPitch": {
				return ID_DesiredPitch;
			}
			case "DesiredVerticalRate": {
				return ID_DesiredVerticalRate;
			}
			case "DesiredPath": {
				return ID_DesiredPath;
			}
			case "DesiredControl": {
				return ID_DesiredControl;
			}
			case "DesiredHeadingRate": {
				return ID_DesiredHeadingRate;
			}
			case "DesiredVelocity": {
				return ID_DesiredVelocity;
			}
			case "PathControlState": {
				return ID_PathControlState;
			}
			case "AllocatedControlTorques": {
				return ID_AllocatedControlTorques;
			}
			case "ControlParcel": {
				return ID_ControlParcel;
			}
			case "Brake": {
				return ID_Brake;
			}
			case "DesiredLinearState": {
				return ID_DesiredLinearState;
			}
			case "DesiredThrottle": {
				return ID_DesiredThrottle;
			}
			case "Goto": {
				return ID_Goto;
			}
			case "PopUp": {
				return ID_PopUp;
			}
			case "Teleoperation": {
				return ID_Teleoperation;
			}
			case "Loiter": {
				return ID_Loiter;
			}
			case "IdleManeuver": {
				return ID_IdleManeuver;
			}
			case "LowLevelControl": {
				return ID_LowLevelControl;
			}
			case "Rows": {
				return ID_Rows;
			}
			case "FollowPath": {
				return ID_FollowPath;
			}
			case "PathPoint": {
				return ID_PathPoint;
			}
			case "YoYo": {
				return ID_YoYo;
			}
			case "TeleoperationDone": {
				return ID_TeleoperationDone;
			}
			case "StationKeeping": {
				return ID_StationKeeping;
			}
			case "Elevator": {
				return ID_Elevator;
			}
			case "FollowTrajectory": {
				return ID_FollowTrajectory;
			}
			case "TrajectoryPoint": {
				return ID_TrajectoryPoint;
			}
			case "CustomManeuver": {
				return ID_CustomManeuver;
			}
			case "VehicleFormation": {
				return ID_VehicleFormation;
			}
			case "VehicleFormationParticipant": {
				return ID_VehicleFormationParticipant;
			}
			case "StopManeuver": {
				return ID_StopManeuver;
			}
			case "RegisterManeuver": {
				return ID_RegisterManeuver;
			}
			case "ManeuverControlState": {
				return ID_ManeuverControlState;
			}
			case "FollowSystem": {
				return ID_FollowSystem;
			}
			case "CommsRelay": {
				return ID_CommsRelay;
			}
			case "CoverArea": {
				return ID_CoverArea;
			}
			case "PolygonVertex": {
				return ID_PolygonVertex;
			}
			case "CompassCalibration": {
				return ID_CompassCalibration;
			}
			case "FormationParameters": {
				return ID_FormationParameters;
			}
			case "FormationPlanExecution": {
				return ID_FormationPlanExecution;
			}
			case "FollowReference": {
				return ID_FollowReference;
			}
			case "Reference": {
				return ID_Reference;
			}
			case "FollowRefState": {
				return ID_FollowRefState;
			}
			case "FormationMonitor": {
				return ID_FormationMonitor;
			}
			case "RelativeState": {
				return ID_RelativeState;
			}
			case "Dislodge": {
				return ID_Dislodge;
			}
			case "Formation": {
				return ID_Formation;
			}
			case "Launch": {
				return ID_Launch;
			}
			case "Drop": {
				return ID_Drop;
			}
			case "ScheduledGoto": {
				return ID_ScheduledGoto;
			}
			case "RowsCoverage": {
				return ID_RowsCoverage;
			}
			case "Sample": {
				return ID_Sample;
			}
			case "ImageTracking": {
				return ID_ImageTracking;
			}
			case "Takeoff": {
				return ID_Takeoff;
			}
			case "Land": {
				return ID_Land;
			}
			case "AutonomousSection": {
				return ID_AutonomousSection;
			}
			case "FollowPoint": {
				return ID_FollowPoint;
			}
			case "VehicleState": {
				return ID_VehicleState;
			}
			case "VehicleCommand": {
				return ID_VehicleCommand;
			}
			case "MonitorEntityState": {
				return ID_MonitorEntityState;
			}
			case "EntityMonitoringState": {
				return ID_EntityMonitoringState;
			}
			case "OperationalLimits": {
				return ID_OperationalLimits;
			}
			case "GetOperationalLimits": {
				return ID_GetOperationalLimits;
			}
			case "Calibration": {
				return ID_Calibration;
			}
			case "ControlLoops": {
				return ID_ControlLoops;
			}
			case "VehicleMedium": {
				return ID_VehicleMedium;
			}
			case "Collision": {
				return ID_Collision;
			}
			case "FormState": {
				return ID_FormState;
			}
			case "AutopilotMode": {
				return ID_AutopilotMode;
			}
			case "FormationState": {
				return ID_FormationState;
			}
			case "ReportControl": {
				return ID_ReportControl;
			}
			case "Abort": {
				return ID_Abort;
			}
			case "PlanSpecification": {
				return ID_PlanSpecification;
			}
			case "PlanManeuver": {
				return ID_PlanManeuver;
			}
			case "PlanTransition": {
				return ID_PlanTransition;
			}
			case "EmergencyControl": {
				return ID_EmergencyControl;
			}
			case "EmergencyControlState": {
				return ID_EmergencyControlState;
			}
			case "PlanDB": {
				return ID_PlanDB;
			}
			case "PlanDBState": {
				return ID_PlanDBState;
			}
			case "PlanDBInformation": {
				return ID_PlanDBInformation;
			}
			case "PlanControl": {
				return ID_PlanControl;
			}
			case "PlanControlState": {
				return ID_PlanControlState;
			}
			case "PlanVariable": {
				return ID_PlanVariable;
			}
			case "PlanGeneration": {
				return ID_PlanGeneration;
			}
			case "LeaderState": {
				return ID_LeaderState;
			}
			case "PlanStatistics": {
				return ID_PlanStatistics;
			}
			case "ReportedState": {
				return ID_ReportedState;
			}
			case "RemoteSensorInfo": {
				return ID_RemoteSensorInfo;
			}
			case "Map": {
				return ID_Map;
			}
			case "MapFeature": {
				return ID_MapFeature;
			}
			case "MapPoint": {
				return ID_MapPoint;
			}
			case "CcuEvent": {
				return ID_CcuEvent;
			}
			case "VehicleLinks": {
				return ID_VehicleLinks;
			}
			case "TrexObservation": {
				return ID_TrexObservation;
			}
			case "TrexCommand": {
				return ID_TrexCommand;
			}
			case "TrexOperation": {
				return ID_TrexOperation;
			}
			case "TrexAttribute": {
				return ID_TrexAttribute;
			}
			case "TrexToken": {
				return ID_TrexToken;
			}
			case "TrexPlan": {
				return ID_TrexPlan;
			}
			case "Event": {
				return ID_Event;
			}
			case "CompressedImage": {
				return ID_CompressedImage;
			}
			case "ImageTxSettings": {
				return ID_ImageTxSettings;
			}
			case "RemoteState": {
				return ID_RemoteState;
			}
			case "Target": {
				return ID_Target;
			}
			case "EntityParameter": {
				return ID_EntityParameter;
			}
			case "EntityParameters": {
				return ID_EntityParameters;
			}
			case "QueryEntityParameters": {
				return ID_QueryEntityParameters;
			}
			case "SetEntityParameters": {
				return ID_SetEntityParameters;
			}
			case "SaveEntityParameters": {
				return ID_SaveEntityParameters;
			}
			case "CreateSession": {
				return ID_CreateSession;
			}
			case "CloseSession": {
				return ID_CloseSession;
			}
			case "SessionSubscription": {
				return ID_SessionSubscription;
			}
			case "SessionKeepAlive": {
				return ID_SessionKeepAlive;
			}
			case "SessionStatus": {
				return ID_SessionStatus;
			}
			case "PushEntityParameters": {
				return ID_PushEntityParameters;
			}
			case "PopEntityParameters": {
				return ID_PopEntityParameters;
			}
			case "IoEvent": {
				return ID_IoEvent;
			}
			case "UamTxFrame": {
				return ID_UamTxFrame;
			}
			case "UamRxFrame": {
				return ID_UamRxFrame;
			}
			case "UamTxStatus": {
				return ID_UamTxStatus;
			}
			case "UamRxRange": {
				return ID_UamRxRange;
			}
			case "FormCtrlParam": {
				return ID_FormCtrlParam;
			}
			case "FormationEval": {
				return ID_FormationEval;
			}
			case "FormationControlParams": {
				return ID_FormationControlParams;
			}
			case "FormationEvaluation": {
				return ID_FormationEvaluation;
			}
			case "MessagePart": {
				return ID_MessagePart;
			}
			case "NeptusBlob": {
				return ID_NeptusBlob;
			}
			case "Aborted": {
				return ID_Aborted;
			}
			case "UsblAngles": {
				return ID_UsblAngles;
			}
			case "UsblPosition": {
				return ID_UsblPosition;
			}
			case "UsblFix": {
				return ID_UsblFix;
			}
			case "ParametersXml": {
				return ID_ParametersXml;
			}
			case "GetParametersXml": {
				return ID_GetParametersXml;
			}
			case "SetImageCoords": {
				return ID_SetImageCoords;
			}
			case "GetImageCoords": {
				return ID_GetImageCoords;
			}
			case "GetWorldCoordinates": {
				return ID_GetWorldCoordinates;
			}
			case "UsblAnglesExtended": {
				return ID_UsblAnglesExtended;
			}
			case "UsblPositionExtended": {
				return ID_UsblPositionExtended;
			}
			case "UsblFixExtended": {
				return ID_UsblFixExtended;
			}
			case "UsblModem": {
				return ID_UsblModem;
			}
			case "UsblConfig": {
				return ID_UsblConfig;
			}
			case "DissolvedOrganicMatter": {
				return ID_DissolvedOrganicMatter;
			}
			case "OpticalBackscatter": {
				return ID_OpticalBackscatter;
			}
			case "Tachograph": {
				return ID_Tachograph;
			}
			case "ApmStatus": {
				return ID_ApmStatus;
			}
			default: {
				return -1;
			}
		}
	}

	public static Class<? extends Message> classOf(Integer mgid) {
		switch(mgid) {
			case ID_EntityState: {
				return EntityState.class;
			}
			case ID_QueryEntityState: {
				return QueryEntityState.class;
			}
			case ID_EntityInfo: {
				return EntityInfo.class;
			}
			case ID_QueryEntityInfo: {
				return QueryEntityInfo.class;
			}
			case ID_EntityList: {
				return EntityList.class;
			}
			case ID_CpuUsage: {
				return CpuUsage.class;
			}
			case ID_TransportBindings: {
				return TransportBindings.class;
			}
			case ID_RestartSystem: {
				return RestartSystem.class;
			}
			case ID_DevCalibrationControl: {
				return DevCalibrationControl.class;
			}
			case ID_DevCalibrationState: {
				return DevCalibrationState.class;
			}
			case ID_EntityActivationState: {
				return EntityActivationState.class;
			}
			case ID_QueryEntityActivationState: {
				return QueryEntityActivationState.class;
			}
			case ID_VehicleOperationalLimits: {
				return VehicleOperationalLimits.class;
			}
			case ID_MsgList: {
				return MsgList.class;
			}
			case ID_SimulatedState: {
				return SimulatedState.class;
			}
			case ID_LeakSimulation: {
				return LeakSimulation.class;
			}
			case ID_UASimulation: {
				return UASimulation.class;
			}
			case ID_DynamicsSimParam: {
				return DynamicsSimParam.class;
			}
			case ID_StorageUsage: {
				return StorageUsage.class;
			}
			case ID_CacheControl: {
				return CacheControl.class;
			}
			case ID_LoggingControl: {
				return LoggingControl.class;
			}
			case ID_LogBookEntry: {
				return LogBookEntry.class;
			}
			case ID_LogBookControl: {
				return LogBookControl.class;
			}
			case ID_ReplayControl: {
				return ReplayControl.class;
			}
			case ID_ClockControl: {
				return ClockControl.class;
			}
			case ID_HistoricCTD: {
				return HistoricCTD.class;
			}
			case ID_HistoricTelemetry: {
				return HistoricTelemetry.class;
			}
			case ID_HistoricSonarData: {
				return HistoricSonarData.class;
			}
			case ID_HistoricEvent: {
				return HistoricEvent.class;
			}
			case ID_Heartbeat: {
				return Heartbeat.class;
			}
			case ID_Announce: {
				return Announce.class;
			}
			case ID_AnnounceService: {
				return AnnounceService.class;
			}
			case ID_RSSI: {
				return RSSI.class;
			}
			case ID_VSWR: {
				return VSWR.class;
			}
			case ID_LinkLevel: {
				return LinkLevel.class;
			}
			case ID_Sms: {
				return Sms.class;
			}
			case ID_SmsTx: {
				return SmsTx.class;
			}
			case ID_SmsRx: {
				return SmsRx.class;
			}
			case ID_SmsState: {
				return SmsState.class;
			}
			case ID_TextMessage: {
				return TextMessage.class;
			}
			case ID_IridiumMsgRx: {
				return IridiumMsgRx.class;
			}
			case ID_IridiumMsgTx: {
				return IridiumMsgTx.class;
			}
			case ID_IridiumTxStatus: {
				return IridiumTxStatus.class;
			}
			case ID_GroupMembershipState: {
				return GroupMembershipState.class;
			}
			case ID_SystemGroup: {
				return SystemGroup.class;
			}
			case ID_LinkLatency: {
				return LinkLatency.class;
			}
			case ID_ExtendedRSSI: {
				return ExtendedRSSI.class;
			}
			case ID_HistoricData: {
				return HistoricData.class;
			}
			case ID_CompressedHistory: {
				return CompressedHistory.class;
			}
			case ID_HistoricSample: {
				return HistoricSample.class;
			}
			case ID_HistoricDataQuery: {
				return HistoricDataQuery.class;
			}
			case ID_RemoteCommand: {
				return RemoteCommand.class;
			}
			case ID_LblRange: {
				return LblRange.class;
			}
			case ID_LblBeacon: {
				return LblBeacon.class;
			}
			case ID_LblConfig: {
				return LblConfig.class;
			}
			case ID_AcousticMessage: {
				return AcousticMessage.class;
			}
			case ID_AcousticOperation: {
				return AcousticOperation.class;
			}
			case ID_AcousticSystemsQuery: {
				return AcousticSystemsQuery.class;
			}
			case ID_AcousticSystems: {
				return AcousticSystems.class;
			}
			case ID_AcousticLink: {
				return AcousticLink.class;
			}
			case ID_Rpm: {
				return Rpm.class;
			}
			case ID_Voltage: {
				return Voltage.class;
			}
			case ID_Current: {
				return Current.class;
			}
			case ID_GpsFix: {
				return GpsFix.class;
			}
			case ID_EulerAngles: {
				return EulerAngles.class;
			}
			case ID_EulerAnglesDelta: {
				return EulerAnglesDelta.class;
			}
			case ID_AngularVelocity: {
				return AngularVelocity.class;
			}
			case ID_Acceleration: {
				return Acceleration.class;
			}
			case ID_MagneticField: {
				return MagneticField.class;
			}
			case ID_GroundVelocity: {
				return GroundVelocity.class;
			}
			case ID_WaterVelocity: {
				return WaterVelocity.class;
			}
			case ID_VelocityDelta: {
				return VelocityDelta.class;
			}
			case ID_Distance: {
				return Distance.class;
			}
			case ID_Temperature: {
				return Temperature.class;
			}
			case ID_Pressure: {
				return Pressure.class;
			}
			case ID_Depth: {
				return Depth.class;
			}
			case ID_DepthOffset: {
				return DepthOffset.class;
			}
			case ID_SoundSpeed: {
				return SoundSpeed.class;
			}
			case ID_WaterDensity: {
				return WaterDensity.class;
			}
			case ID_Conductivity: {
				return Conductivity.class;
			}
			case ID_Salinity: {
				return Salinity.class;
			}
			case ID_WindSpeed: {
				return WindSpeed.class;
			}
			case ID_RelativeHumidity: {
				return RelativeHumidity.class;
			}
			case ID_DevDataText: {
				return DevDataText.class;
			}
			case ID_DevDataBinary: {
				return DevDataBinary.class;
			}
			case ID_SonarData: {
				return SonarData.class;
			}
			case ID_Pulse: {
				return Pulse.class;
			}
			case ID_PulseDetectionControl: {
				return PulseDetectionControl.class;
			}
			case ID_FuelLevel: {
				return FuelLevel.class;
			}
			case ID_GpsNavData: {
				return GpsNavData.class;
			}
			case ID_ServoPosition: {
				return ServoPosition.class;
			}
			case ID_DeviceState: {
				return DeviceState.class;
			}
			case ID_BeamConfig: {
				return BeamConfig.class;
			}
			case ID_DataSanity: {
				return DataSanity.class;
			}
			case ID_RhodamineDye: {
				return RhodamineDye.class;
			}
			case ID_CrudeOil: {
				return CrudeOil.class;
			}
			case ID_FineOil: {
				return FineOil.class;
			}
			case ID_Turbidity: {
				return Turbidity.class;
			}
			case ID_Chlorophyll: {
				return Chlorophyll.class;
			}
			case ID_Fluorescein: {
				return Fluorescein.class;
			}
			case ID_Phycocyanin: {
				return Phycocyanin.class;
			}
			case ID_Phycoerythrin: {
				return Phycoerythrin.class;
			}
			case ID_GpsFixRtk: {
				return GpsFixRtk.class;
			}
			case ID_ExternalNavData: {
				return ExternalNavData.class;
			}
			case ID_DissolvedOxygen: {
				return DissolvedOxygen.class;
			}
			case ID_AirSaturation: {
				return AirSaturation.class;
			}
			case ID_Throttle: {
				return Throttle.class;
			}
			case ID_PH: {
				return PH.class;
			}
			case ID_Redox: {
				return Redox.class;
			}
			case ID_CameraZoom: {
				return CameraZoom.class;
			}
			case ID_SetThrusterActuation: {
				return SetThrusterActuation.class;
			}
			case ID_SetServoPosition: {
				return SetServoPosition.class;
			}
			case ID_SetControlSurfaceDeflection: {
				return SetControlSurfaceDeflection.class;
			}
			case ID_RemoteActionsRequest: {
				return RemoteActionsRequest.class;
			}
			case ID_RemoteActions: {
				return RemoteActions.class;
			}
			case ID_ButtonEvent: {
				return ButtonEvent.class;
			}
			case ID_LcdControl: {
				return LcdControl.class;
			}
			case ID_PowerOperation: {
				return PowerOperation.class;
			}
			case ID_PowerChannelControl: {
				return PowerChannelControl.class;
			}
			case ID_QueryPowerChannelState: {
				return QueryPowerChannelState.class;
			}
			case ID_PowerChannelState: {
				return PowerChannelState.class;
			}
			case ID_LedBrightness: {
				return LedBrightness.class;
			}
			case ID_QueryLedBrightness: {
				return QueryLedBrightness.class;
			}
			case ID_SetLedBrightness: {
				return SetLedBrightness.class;
			}
			case ID_SetPWM: {
				return SetPWM.class;
			}
			case ID_PWM: {
				return PWM.class;
			}
			case ID_EstimatedState: {
				return EstimatedState.class;
			}
			case ID_EstimatedStreamVelocity: {
				return EstimatedStreamVelocity.class;
			}
			case ID_IndicatedSpeed: {
				return IndicatedSpeed.class;
			}
			case ID_TrueSpeed: {
				return TrueSpeed.class;
			}
			case ID_NavigationUncertainty: {
				return NavigationUncertainty.class;
			}
			case ID_NavigationData: {
				return NavigationData.class;
			}
			case ID_GpsFixRejection: {
				return GpsFixRejection.class;
			}
			case ID_LblRangeAcceptance: {
				return LblRangeAcceptance.class;
			}
			case ID_DvlRejection: {
				return DvlRejection.class;
			}
			case ID_LblEstimate: {
				return LblEstimate.class;
			}
			case ID_AlignmentState: {
				return AlignmentState.class;
			}
			case ID_GroupStreamVelocity: {
				return GroupStreamVelocity.class;
			}
			case ID_Airflow: {
				return Airflow.class;
			}
			case ID_DesiredHeading: {
				return DesiredHeading.class;
			}
			case ID_DesiredZ: {
				return DesiredZ.class;
			}
			case ID_DesiredSpeed: {
				return DesiredSpeed.class;
			}
			case ID_DesiredRoll: {
				return DesiredRoll.class;
			}
			case ID_DesiredPitch: {
				return DesiredPitch.class;
			}
			case ID_DesiredVerticalRate: {
				return DesiredVerticalRate.class;
			}
			case ID_DesiredPath: {
				return DesiredPath.class;
			}
			case ID_DesiredControl: {
				return DesiredControl.class;
			}
			case ID_DesiredHeadingRate: {
				return DesiredHeadingRate.class;
			}
			case ID_DesiredVelocity: {
				return DesiredVelocity.class;
			}
			case ID_PathControlState: {
				return PathControlState.class;
			}
			case ID_AllocatedControlTorques: {
				return AllocatedControlTorques.class;
			}
			case ID_ControlParcel: {
				return ControlParcel.class;
			}
			case ID_Brake: {
				return Brake.class;
			}
			case ID_DesiredLinearState: {
				return DesiredLinearState.class;
			}
			case ID_DesiredThrottle: {
				return DesiredThrottle.class;
			}
			case ID_Goto: {
				return Goto.class;
			}
			case ID_PopUp: {
				return PopUp.class;
			}
			case ID_Teleoperation: {
				return Teleoperation.class;
			}
			case ID_Loiter: {
				return Loiter.class;
			}
			case ID_IdleManeuver: {
				return IdleManeuver.class;
			}
			case ID_LowLevelControl: {
				return LowLevelControl.class;
			}
			case ID_Rows: {
				return Rows.class;
			}
			case ID_FollowPath: {
				return FollowPath.class;
			}
			case ID_PathPoint: {
				return PathPoint.class;
			}
			case ID_YoYo: {
				return YoYo.class;
			}
			case ID_TeleoperationDone: {
				return TeleoperationDone.class;
			}
			case ID_StationKeeping: {
				return StationKeeping.class;
			}
			case ID_Elevator: {
				return Elevator.class;
			}
			case ID_FollowTrajectory: {
				return FollowTrajectory.class;
			}
			case ID_TrajectoryPoint: {
				return TrajectoryPoint.class;
			}
			case ID_CustomManeuver: {
				return CustomManeuver.class;
			}
			case ID_VehicleFormation: {
				return VehicleFormation.class;
			}
			case ID_VehicleFormationParticipant: {
				return VehicleFormationParticipant.class;
			}
			case ID_StopManeuver: {
				return StopManeuver.class;
			}
			case ID_RegisterManeuver: {
				return RegisterManeuver.class;
			}
			case ID_ManeuverControlState: {
				return ManeuverControlState.class;
			}
			case ID_FollowSystem: {
				return FollowSystem.class;
			}
			case ID_CommsRelay: {
				return CommsRelay.class;
			}
			case ID_CoverArea: {
				return CoverArea.class;
			}
			case ID_PolygonVertex: {
				return PolygonVertex.class;
			}
			case ID_CompassCalibration: {
				return CompassCalibration.class;
			}
			case ID_FormationParameters: {
				return FormationParameters.class;
			}
			case ID_FormationPlanExecution: {
				return FormationPlanExecution.class;
			}
			case ID_FollowReference: {
				return FollowReference.class;
			}
			case ID_Reference: {
				return Reference.class;
			}
			case ID_FollowRefState: {
				return FollowRefState.class;
			}
			case ID_FormationMonitor: {
				return FormationMonitor.class;
			}
			case ID_RelativeState: {
				return RelativeState.class;
			}
			case ID_Dislodge: {
				return Dislodge.class;
			}
			case ID_Formation: {
				return Formation.class;
			}
			case ID_Launch: {
				return Launch.class;
			}
			case ID_Drop: {
				return Drop.class;
			}
			case ID_ScheduledGoto: {
				return ScheduledGoto.class;
			}
			case ID_RowsCoverage: {
				return RowsCoverage.class;
			}
			case ID_Sample: {
				return Sample.class;
			}
			case ID_ImageTracking: {
				return ImageTracking.class;
			}
			case ID_Takeoff: {
				return Takeoff.class;
			}
			case ID_Land: {
				return Land.class;
			}
			case ID_AutonomousSection: {
				return AutonomousSection.class;
			}
			case ID_FollowPoint: {
				return FollowPoint.class;
			}
			case ID_VehicleState: {
				return VehicleState.class;
			}
			case ID_VehicleCommand: {
				return VehicleCommand.class;
			}
			case ID_MonitorEntityState: {
				return MonitorEntityState.class;
			}
			case ID_EntityMonitoringState: {
				return EntityMonitoringState.class;
			}
			case ID_OperationalLimits: {
				return OperationalLimits.class;
			}
			case ID_GetOperationalLimits: {
				return GetOperationalLimits.class;
			}
			case ID_Calibration: {
				return Calibration.class;
			}
			case ID_ControlLoops: {
				return ControlLoops.class;
			}
			case ID_VehicleMedium: {
				return VehicleMedium.class;
			}
			case ID_Collision: {
				return Collision.class;
			}
			case ID_FormState: {
				return FormState.class;
			}
			case ID_AutopilotMode: {
				return AutopilotMode.class;
			}
			case ID_FormationState: {
				return FormationState.class;
			}
			case ID_ReportControl: {
				return ReportControl.class;
			}
			case ID_Abort: {
				return Abort.class;
			}
			case ID_PlanSpecification: {
				return PlanSpecification.class;
			}
			case ID_PlanManeuver: {
				return PlanManeuver.class;
			}
			case ID_PlanTransition: {
				return PlanTransition.class;
			}
			case ID_EmergencyControl: {
				return EmergencyControl.class;
			}
			case ID_EmergencyControlState: {
				return EmergencyControlState.class;
			}
			case ID_PlanDB: {
				return PlanDB.class;
			}
			case ID_PlanDBState: {
				return PlanDBState.class;
			}
			case ID_PlanDBInformation: {
				return PlanDBInformation.class;
			}
			case ID_PlanControl: {
				return PlanControl.class;
			}
			case ID_PlanControlState: {
				return PlanControlState.class;
			}
			case ID_PlanVariable: {
				return PlanVariable.class;
			}
			case ID_PlanGeneration: {
				return PlanGeneration.class;
			}
			case ID_LeaderState: {
				return LeaderState.class;
			}
			case ID_PlanStatistics: {
				return PlanStatistics.class;
			}
			case ID_ReportedState: {
				return ReportedState.class;
			}
			case ID_RemoteSensorInfo: {
				return RemoteSensorInfo.class;
			}
			case ID_Map: {
				return Map.class;
			}
			case ID_MapFeature: {
				return MapFeature.class;
			}
			case ID_MapPoint: {
				return MapPoint.class;
			}
			case ID_CcuEvent: {
				return CcuEvent.class;
			}
			case ID_VehicleLinks: {
				return VehicleLinks.class;
			}
			case ID_TrexObservation: {
				return TrexObservation.class;
			}
			case ID_TrexCommand: {
				return TrexCommand.class;
			}
			case ID_TrexOperation: {
				return TrexOperation.class;
			}
			case ID_TrexAttribute: {
				return TrexAttribute.class;
			}
			case ID_TrexToken: {
				return TrexToken.class;
			}
			case ID_TrexPlan: {
				return TrexPlan.class;
			}
			case ID_Event: {
				return Event.class;
			}
			case ID_CompressedImage: {
				return CompressedImage.class;
			}
			case ID_ImageTxSettings: {
				return ImageTxSettings.class;
			}
			case ID_RemoteState: {
				return RemoteState.class;
			}
			case ID_Target: {
				return Target.class;
			}
			case ID_EntityParameter: {
				return EntityParameter.class;
			}
			case ID_EntityParameters: {
				return EntityParameters.class;
			}
			case ID_QueryEntityParameters: {
				return QueryEntityParameters.class;
			}
			case ID_SetEntityParameters: {
				return SetEntityParameters.class;
			}
			case ID_SaveEntityParameters: {
				return SaveEntityParameters.class;
			}
			case ID_CreateSession: {
				return CreateSession.class;
			}
			case ID_CloseSession: {
				return CloseSession.class;
			}
			case ID_SessionSubscription: {
				return SessionSubscription.class;
			}
			case ID_SessionKeepAlive: {
				return SessionKeepAlive.class;
			}
			case ID_SessionStatus: {
				return SessionStatus.class;
			}
			case ID_PushEntityParameters: {
				return PushEntityParameters.class;
			}
			case ID_PopEntityParameters: {
				return PopEntityParameters.class;
			}
			case ID_IoEvent: {
				return IoEvent.class;
			}
			case ID_UamTxFrame: {
				return UamTxFrame.class;
			}
			case ID_UamRxFrame: {
				return UamRxFrame.class;
			}
			case ID_UamTxStatus: {
				return UamTxStatus.class;
			}
			case ID_UamRxRange: {
				return UamRxRange.class;
			}
			case ID_FormCtrlParam: {
				return FormCtrlParam.class;
			}
			case ID_FormationEval: {
				return FormationEval.class;
			}
			case ID_FormationControlParams: {
				return FormationControlParams.class;
			}
			case ID_FormationEvaluation: {
				return FormationEvaluation.class;
			}
			case ID_MessagePart: {
				return MessagePart.class;
			}
			case ID_NeptusBlob: {
				return NeptusBlob.class;
			}
			case ID_Aborted: {
				return Aborted.class;
			}
			case ID_UsblAngles: {
				return UsblAngles.class;
			}
			case ID_UsblPosition: {
				return UsblPosition.class;
			}
			case ID_UsblFix: {
				return UsblFix.class;
			}
			case ID_ParametersXml: {
				return ParametersXml.class;
			}
			case ID_GetParametersXml: {
				return GetParametersXml.class;
			}
			case ID_SetImageCoords: {
				return SetImageCoords.class;
			}
			case ID_GetImageCoords: {
				return GetImageCoords.class;
			}
			case ID_GetWorldCoordinates: {
				return GetWorldCoordinates.class;
			}
			case ID_UsblAnglesExtended: {
				return UsblAnglesExtended.class;
			}
			case ID_UsblPositionExtended: {
				return UsblPositionExtended.class;
			}
			case ID_UsblFixExtended: {
				return UsblFixExtended.class;
			}
			case ID_UsblModem: {
				return UsblModem.class;
			}
			case ID_UsblConfig: {
				return UsblConfig.class;
			}
			case ID_DissolvedOrganicMatter: {
				return DissolvedOrganicMatter.class;
			}
			case ID_OpticalBackscatter: {
				return OpticalBackscatter.class;
			}
			case ID_Tachograph: {
				return Tachograph.class;
			}
			case ID_ApmStatus: {
				return ApmStatus.class;
			}
			default: {
				return Message.class;
			}
		}
	}

	public static Class<? extends Message> classOf(String abbrev) {
		return classOf(idOf(abbrev));
	}
}
