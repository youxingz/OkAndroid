package com.cardioflex.bioreactor.opc;


import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

public class BioreactorNodeId {
    public static final NodeId BA_System = NodeId.parse("ns=4;i=1");

    // Control_DO
    public static final NodeId Control_DO = NodeId.parse("ns=4;i=2");
    public static final NodeId DO_SV = NodeId.parse("ns=4;i=3");
    public static final NodeId DO_PV = NodeId.parse("ns=4;i=4");
    public static final NodeId DO_Kp = NodeId.parse("ns=4;i=5");
    public static final NodeId DO_Ti = NodeId.parse("ns=4;i=6");
    public static final NodeId DO_Td = NodeId.parse("ns=4;i=7");
    public static final NodeId DO_Deadbaud = NodeId.parse("ns=4;i=8");
    public static final NodeId DO_PIDMin = NodeId.parse("ns=4;i=9");
    public static final NodeId DO_PIDMax = NodeId.parse("ns=4;i=10");
    public static final NodeId DO_Cycle = NodeId.parse("ns=4;i=11");
    public static final NodeId DO_PID = NodeId.parse("ns=4;i=12");
    public static final NodeId DO_AlarmMin = NodeId.parse("ns=4;i=13");
    public static final NodeId DO_AlarmMax = NodeId.parse("ns=4;i=14");
    public static final NodeId DO_ShowMin = NodeId.parse("ns=4;i=15");
    public static final NodeId DO_ShowMax = NodeId.parse("ns=4;i=16");
    public static final NodeId DO_DOControl = NodeId.parse("ns=4;i=17");
    public static final NodeId DO_Main_DO_Sensor = NodeId.parse("ns=4;i=18");
    public static final NodeId DO_Auto_PIDParam = NodeId.parse("ns=4;i=19");
    public static final NodeId DO_ResetPID = NodeId.parse("ns=4;i=20");

    // Control_PH
    public static final NodeId Control_PH = NodeId.parse("ns=4;i=21");
    public static final NodeId PH_SV = NodeId.parse("ns=4;i=22");
    public static final NodeId PH_PV = NodeId.parse("ns=4;i=23");
    public static final NodeId PH_Kp = NodeId.parse("ns=4;i=24");
    public static final NodeId PH_Ti = NodeId.parse("ns=4;i=25");
    public static final NodeId PH_Td = NodeId.parse("ns=4;i=26");
    public static final NodeId PH_Deadbaud = NodeId.parse("ns=4;i=27");
    public static final NodeId PH_PIDMin = NodeId.parse("ns=4;i=28");
    public static final NodeId PH_PIDMax = NodeId.parse("ns=4;i=29");
    public static final NodeId PH_Cycle = NodeId.parse("ns=4;i=30");
    public static final NodeId PH_PID = NodeId.parse("ns=4;i=31");
    public static final NodeId PH_AlarmMin = NodeId.parse("ns=4;i=32");
    public static final NodeId PH_AlarmMax = NodeId.parse("ns=4;i=33");
    public static final NodeId PH_ShowMin = NodeId.parse("ns=4;i=34");
    public static final NodeId PH_ShowMax = NodeId.parse("ns=4;i=35");
    public static final NodeId PH_PHControl = NodeId.parse("ns=4;i=36");
    public static final NodeId PH_Main_PH_Sensor = NodeId.parse("ns=4;i=37");
    public static final NodeId PH_Auto_PIDParam = NodeId.parse("ns=4;i=38");
    public static final NodeId PH_ResetPID = NodeId.parse("ns=4;i=39");

    // Control_Temp
    public static final NodeId Control_Temp = NodeId.parse("ns=4;i=40");
    public static final NodeId Temp_SV = NodeId.parse("ns=4;i=41");
    public static final NodeId Temp_PV = NodeId.parse("ns=4;i=42");
    public static final NodeId Temp_Kp = NodeId.parse("ns=4;i=43");
    public static final NodeId Temp_Ti = NodeId.parse("ns=4;i=44");
    public static final NodeId Temp_Td = NodeId.parse("ns=4;i=45");
    public static final NodeId Temp_Deadbaud = NodeId.parse("ns=4;i=46");
    public static final NodeId Temp_PIDMin = NodeId.parse("ns=4;i=47");
    public static final NodeId Temp_PIDMax = NodeId.parse("ns=4;i=48");
    public static final NodeId Temp_Cycle = NodeId.parse("ns=4;i=49");
    public static final NodeId Temp_PID = NodeId.parse("ns=4;i=50");
    public static final NodeId Temp_AlarmMin = NodeId.parse("ns=4;i=51");
    public static final NodeId Temp_AlarmMax = NodeId.parse("ns=4;i=52");
    public static final NodeId Temp_ShowMin = NodeId.parse("ns=4;i=53");
    public static final NodeId Temp_ShowMax = NodeId.parse("ns=4;i=54");
    public static final NodeId Temp_TempControl = NodeId.parse("ns=4;i=55");
    public static final NodeId Temp_JacketTemp_PV = NodeId.parse("ns=4;i=56");
    public static final NodeId Temp_JacketTemp_Protect = NodeId.parse("ns=4;i=57");
    public static final NodeId Temp_JacketTemp_AlarmMax = NodeId.parse("ns=4;i=58");
    public static final NodeId Temp_JacketTemp_AlarmMin = NodeId.parse("ns=4;i=59");
    public static final NodeId Temp_Auto_PIDParam = NodeId.parse("ns=4;i=60");
    public static final NodeId Temp_ResetPID = NodeId.parse("ns=4;i=61");

    // Control_Level
    public static final NodeId Control_Level = NodeId.parse("ns=4;i=62");
    public static final NodeId Level_SV = NodeId.parse("ns=4;i=63");
    public static final NodeId Level_PV = NodeId.parse("ns=4;i=64");
    public static final NodeId Level_PID = NodeId.parse("ns=4;i=65");
    public static final NodeId Level_PIDMin = NodeId.parse("ns=4;i=66");
    public static final NodeId Level_PIDMax = NodeId.parse("ns=4;i=67");
    public static final NodeId Level_AlarmMin = NodeId.parse("ns=4;i=68");
    public static final NodeId Level_AlarmMax = NodeId.parse("ns=4;i=69");
    public static final NodeId Level_ShowMin = NodeId.parse("ns=4;i=70");
    public static final NodeId Level_ShowMax = NodeId.parse("ns=4;i=71");
    public static final NodeId Level_LevelControl = NodeId.parse("ns=4;i=72");

    // Equit_Pump1
    public static final NodeId Equit_Pump1 = NodeId.parse("ns=4;i=73");
    public static final NodeId Equit_Pump1_Static = NodeId.parse("ns=4;i=74");
    public static final NodeId Equit_Pump1_Equit_Min = NodeId.parse("ns=4;i=75");
    public static final NodeId Equit_Pump1_Equit_Max = NodeId.parse("ns=4;i=76");
    public static final NodeId Equit_Pump1_Alarm_Min = NodeId.parse("ns=4;i=77");
    public static final NodeId Equit_Pump1_Alarm_Max = NodeId.parse("ns=4;i=78");
    public static final NodeId Equit_Pump1_Show_Min = NodeId.parse("ns=4;i=79");
    public static final NodeId Equit_Pump1_Show_Max = NodeId.parse("ns=4;i=80");
    public static final NodeId Equit_Pump1_Dir = NodeId.parse("ns=4;i=81");
    public static final NodeId Equit_Pump1_ControlMode = NodeId.parse("ns=4;i=82");
    public static final NodeId Equit_Pump1_Mode = NodeId.parse("ns=4;i=83");
    public static final NodeId Equit_Pump1_Cycle = NodeId.parse("ns=4;i=84");
    public static final NodeId Equit_Pump1_Calibration_Time = NodeId.parse("ns=4;i=85");
    public static final NodeId Equit_Pump1_Calibration_CountDown = NodeId.parse("ns=4;i=86");
    public static final NodeId Equit_Pump1_Calibration_Volume = NodeId.parse("ns=4;i=87");
    public static final NodeId Equit_Pump1_Calibration_RPM = NodeId.parse("ns=4;i=88");
    public static final NodeId Equit_Pump1_Calibration_Started = NodeId.parse("ns=4;i=89");
    public static final NodeId Equit_Pump1_Calibration_Write_Triggle = NodeId.parse("ns=4;i=90");
    public static final NodeId Equit_Pump1_PulPerRound = NodeId.parse("ns=4;i=91");
    public static final NodeId Equit_Pump1_VolumePerRound = NodeId.parse("ns=4;i=92");
    public static final NodeId Equit_Pump1_EquitMax_RPM = NodeId.parse("ns=4;i=93");
    public static final NodeId Equit_Pump1_MeanRPM = NodeId.parse("ns=4;i=94");
    public static final NodeId Equit_Pump1_MeanVPM = NodeId.parse("ns=4;i=95");
    public static final NodeId Equit_Pump1_Dosage = NodeId.parse("ns=4;i=96");
    public static final NodeId Equit_Pump1_DosageReset = NodeId.parse("ns=4;i=97");
    public static final NodeId Equit_Pump1_Equit_Type = NodeId.parse("ns=4;i=98");
    public static final NodeId Equit_Pump1_UserScript_SV = NodeId.parse("ns=4;i=99");
    public static final NodeId Equit_Pump1_Name = NodeId.parse("ns=4;i=100");

    // Equit_Pump2
    public static final NodeId Equit_Pump2 = NodeId.parse("ns=4;i=101");
    public static final NodeId Equit_Pump2_Static = NodeId.parse("ns=4;i=102");
    public static final NodeId Equit_Pump2_Equit_Min = NodeId.parse("ns=4;i=103");
    public static final NodeId Equit_Pump2_Equit_Max = NodeId.parse("ns=4;i=104");
    public static final NodeId Equit_Pump2_Alarm_Min = NodeId.parse("ns=4;i=105");
    public static final NodeId Equit_Pump2_Alarm_Max = NodeId.parse("ns=4;i=106");
    public static final NodeId Equit_Pump2_Show_Min = NodeId.parse("ns=4;i=107");
    public static final NodeId Equit_Pump2_Show_Max = NodeId.parse("ns=4;i=108");
    public static final NodeId Equit_Pump2_Dir = NodeId.parse("ns=4;i=109");
    public static final NodeId Equit_Pump2_ControlMode = NodeId.parse("ns=4;i=110");
    public static final NodeId Equit_Pump2_Mode = NodeId.parse("ns=4;i=111");
    public static final NodeId Equit_Pump2_Cycle = NodeId.parse("ns=4;i=112");
    public static final NodeId Equit_Pump2_Calibration_Time = NodeId.parse("ns=4;i=113");
    public static final NodeId Equit_Pump2_Calibration_CountDown = NodeId.parse("ns=4;i=114");
    public static final NodeId Equit_Pump2_Calibration_Volume = NodeId.parse("ns=4;i=115");
    public static final NodeId Equit_Pump2_Calibration_RPM = NodeId.parse("ns=4;i=116");
    public static final NodeId Equit_Pump2_Calibration_Started = NodeId.parse("ns=4;i=117");
    public static final NodeId Equit_Pump2_Calibration_Write_Triggle = NodeId.parse("ns=4;i=118");
    public static final NodeId Equit_Pump2_PulPerRound = NodeId.parse("ns=4;i=119");
    public static final NodeId Equit_Pump2_VolumePerRound = NodeId.parse("ns=4;i=120");
    public static final NodeId Equit_Pump2_EquitMax_RPM = NodeId.parse("ns=4;i=121");
    public static final NodeId Equit_Pump2_MeanRPM = NodeId.parse("ns=4;i=122");
    public static final NodeId Equit_Pump2_MeanVPM = NodeId.parse("ns=4;i=123");
    public static final NodeId Equit_Pump2_Dosage = NodeId.parse("ns=4;i=124");
    public static final NodeId Equit_Pump2_DosageReset = NodeId.parse("ns=4;i=125");
    public static final NodeId Equit_Pump2_Equit_Type = NodeId.parse("ns=4;i=126");
    public static final NodeId Equit_Pump2_UserScript_SV = NodeId.parse("ns=4;i=127");
    public static final NodeId Equit_Pump2_Name = NodeId.parse("ns=4;i=128");

    // Equit_Pump3
    public static final NodeId Equit_Pump3 = NodeId.parse("ns=4;i=129");
    public static final NodeId Equit_Pump3_Static = NodeId.parse("ns=4;i=130");
    public static final NodeId Equit_Pump3_Equit_Min = NodeId.parse("ns=4;i=131");
    public static final NodeId Equit_Pump3_Equit_Max = NodeId.parse("ns=4;i=132");
    public static final NodeId Equit_Pump3_Alarm_Min = NodeId.parse("ns=4;i=133");
    public static final NodeId Equit_Pump3_Alarm_Max = NodeId.parse("ns=4;i=134");
    public static final NodeId Equit_Pump3_Show_Min = NodeId.parse("ns=4;i=135");
    public static final NodeId Equit_Pump3_Show_Max = NodeId.parse("ns=4;i=136");
    public static final NodeId Equit_Pump3_Dir = NodeId.parse("ns=4;i=137");
    public static final NodeId Equit_Pump3_ControlMode = NodeId.parse("ns=4;i=138");
    public static final NodeId Equit_Pump3_Mode = NodeId.parse("ns=4;i=139");
    public static final NodeId Equit_Pump3_Cycle = NodeId.parse("ns=4;i=140");
    public static final NodeId Equit_Pump3_Calibration_Time = NodeId.parse("ns=4;i=141");
    public static final NodeId Equit_Pump3_Calibration_CountDown = NodeId.parse("ns=4;i=142");
    public static final NodeId Equit_Pump3_Calibration_Volume = NodeId.parse("ns=4;i=143");
    public static final NodeId Equit_Pump3_Calibration_RPM = NodeId.parse("ns=4;i=144");
    public static final NodeId Equit_Pump3_Calibration_Started = NodeId.parse("ns=4;i=145");
    public static final NodeId Equit_Pump3_Calibration_Write_Triggle = NodeId.parse("ns=4;i=146");
    public static final NodeId Equit_Pump3_PulPerRound = NodeId.parse("ns=4;i=147");
    public static final NodeId Equit_Pump3_VolumePerRound = NodeId.parse("ns=4;i=148");
    public static final NodeId Equit_Pump3_EquitMax_RPM = NodeId.parse("ns=4;i=149");
    public static final NodeId Equit_Pump3_MeanRPM = NodeId.parse("ns=4;i=150");
    public static final NodeId Equit_Pump3_MeanVPM = NodeId.parse("ns=4;i=151");
    public static final NodeId Equit_Pump3_Dosage = NodeId.parse("ns=4;i=152");
    public static final NodeId Equit_Pump3_DosageReset = NodeId.parse("ns=4;i=153");
    public static final NodeId Equit_Pump3_Equit_Type = NodeId.parse("ns=4;i=154");
    public static final NodeId Equit_Pump3_UserScript_SV = NodeId.parse("ns=4;i=155");
    public static final NodeId Equit_Pump3_Name = NodeId.parse("ns=4;i=156");

    // Equit_Pump4
    public static final NodeId Equit_Pump4 = NodeId.parse("ns=4;i=157");
    public static final NodeId Equit_Pump4_Static = NodeId.parse("ns=4;i=158");
    public static final NodeId Equit_Pump4_Equit_Min = NodeId.parse("ns=4;i=159");
    public static final NodeId Equit_Pump4_Equit_Max = NodeId.parse("ns=4;i=160");
    public static final NodeId Equit_Pump4_Alarm_Min = NodeId.parse("ns=4;i=161");
    public static final NodeId Equit_Pump4_Alarm_Max = NodeId.parse("ns=4;i=162");
    public static final NodeId Equit_Pump4_Show_Min = NodeId.parse("ns=4;i=163");
    public static final NodeId Equit_Pump4_Show_Max = NodeId.parse("ns=4;i=164");
    public static final NodeId Equit_Pump4_Dir = NodeId.parse("ns=4;i=165");
    public static final NodeId Equit_Pump4_ControlMode = NodeId.parse("ns=4;i=166");
    public static final NodeId Equit_Pump4_Mode = NodeId.parse("ns=4;i=167");
    public static final NodeId Equit_Pump4_Cycle = NodeId.parse("ns=4;i=168");
    public static final NodeId Equit_Pump4_Calibration_Time = NodeId.parse("ns=4;i=169");
    public static final NodeId Equit_Pump4_Calibration_CountDown = NodeId.parse("ns=4;i=170");
    public static final NodeId Equit_Pump4_Calibration_Volume = NodeId.parse("ns=4;i=171");
    public static final NodeId Equit_Pump4_Calibration_RPM = NodeId.parse("ns=4;i=172");
    public static final NodeId Equit_Pump4_Calibration_Started = NodeId.parse("ns=4;i=173");
    public static final NodeId Equit_Pump4_Calibration_Write_Triggle = NodeId.parse("ns=4;i=174");
    public static final NodeId Equit_Pump4_PulPerRound = NodeId.parse("ns=4;i=175");
    public static final NodeId Equit_Pump4_VolumePerRound = NodeId.parse("ns=4;i=176");
    public static final NodeId Equit_Pump4_EquitMax_RPM = NodeId.parse("ns=4;i=177");
    public static final NodeId Equit_Pump4_MeanRPM = NodeId.parse("ns=4;i=178");
    public static final NodeId Equit_Pump4_MeanVPM = NodeId.parse("ns=4;i=179");
    public static final NodeId Equit_Pump4_Dosage = NodeId.parse("ns=4;i=180");
    public static final NodeId Equit_Pump4_DosageReset = NodeId.parse("ns=4;i=181");
    public static final NodeId Equit_Pump4_Equit_Type = NodeId.parse("ns=4;i=182");
    public static final NodeId Equit_Pump4_UserScript_SV = NodeId.parse("ns=4;i=183");
    public static final NodeId Equit_Pump4_Name = NodeId.parse("ns=4;i=184");

    // Equit_Air
    public static final NodeId Equit_Air = NodeId.parse("ns=4;i=185");
    public static final NodeId Equit_Air_Static = NodeId.parse("ns=4;i=186");
    public static final NodeId Equit_Air_EquitMin = NodeId.parse("ns=4;i=187");
    public static final NodeId Equit_Air_EquitMax = NodeId.parse("ns=4;i=188");
    public static final NodeId Equit_Air_AlarmMin = NodeId.parse("ns=4;i=189");
    public static final NodeId Equit_Air_AlarmMax = NodeId.parse("ns=4;i=190");
    public static final NodeId Equit_Air_ShowMin = NodeId.parse("ns=4;i=191");
    public static final NodeId Equit_Air_ShowMax = NodeId.parse("ns=4;i=192");
    public static final NodeId Equit_Air_PV = NodeId.parse("ns=4;i=193");
    public static final NodeId Equit_Air_CV = NodeId.parse("ns=4;i=194");
    public static final NodeId Equit_Air_SV = NodeId.parse("ns=4;i=195");
    public static final NodeId Equit_Air_Cycle = NodeId.parse("ns=4;i=196");
    public static final NodeId Equit_Air_ControlMode = NodeId.parse("ns=4;i=197");
    public static final NodeId Equit_Air_Dir = NodeId.parse("ns=4;i=198");
    public static final NodeId Equit_Air_Dosage = NodeId.parse("ns=4;i=199");
    public static final NodeId Equit_Air_DosageReset = NodeId.parse("ns=4;i=200");
    public static final NodeId Equit_Air_Equit_Type = NodeId.parse("ns=4;i=201");
    public static final NodeId Equit_Air_UserScript_SV = NodeId.parse("ns=4;i=202");
    public static final NodeId Equit_Air_Sequence_SV = NodeId.parse("ns=4;i=203");
    public static final NodeId Equit_Air_Condition_SV = NodeId.parse("ns=4;i=204");

    // Equit_CO2
    public static final NodeId Equit_CO2 = NodeId.parse("ns=4;i=205");
    public static final NodeId Equit_CO2_Static = NodeId.parse("ns=4;i=206");
    public static final NodeId Equit_CO2_EquitMin = NodeId.parse("ns=4;i=207");
    public static final NodeId Equit_CO2_EquitMax = NodeId.parse("ns=4;i=208");
    public static final NodeId Equit_CO2_AlarmMin = NodeId.parse("ns=4;i=209");
    public static final NodeId Equit_CO2_AlarmMax = NodeId.parse("ns=4;i=210");
    public static final NodeId Equit_CO2_ShowMin = NodeId.parse("ns=4;i=211");
    public static final NodeId Equit_CO2_ShowMax = NodeId.parse("ns=4;i=212");
    public static final NodeId Equit_CO2_PV = NodeId.parse("ns=4;i=213");
    public static final NodeId Equit_CO2_CV = NodeId.parse("ns=4;i=214");
    public static final NodeId Equit_CO2_SV = NodeId.parse("ns=4;i=215");
    public static final NodeId Equit_CO2_Cycle = NodeId.parse("ns=4;i=216");
    public static final NodeId Equit_CO2_ControlMode = NodeId.parse("ns=4;i=217");
    public static final NodeId Equit_CO2_Dir = NodeId.parse("ns=4;i=218");
    public static final NodeId Equit_CO2_Dosage = NodeId.parse("ns=4;i=219");
    public static final NodeId Equit_CO2_DosageReset = NodeId.parse("ns=4;i=220");
    public static final NodeId Equit_CO2_Equit_Type = NodeId.parse("ns=4;i=221");
    public static final NodeId Equit_CO2_UserScript_SV = NodeId.parse("ns=4;i=222");
    public static final NodeId Equit_CO2_Sequence_SV = NodeId.parse("ns=4;i=223");
    public static final NodeId Equit_CO2_Condition_SV = NodeId.parse("ns=4;i=224");

    // Equit_O2
    public static final NodeId Equit_O2 = NodeId.parse("ns=4;i=225");
    public static final NodeId Equit_O2_Static = NodeId.parse("ns=4;i=226");
    public static final NodeId Equit_O2_EquitMin = NodeId.parse("ns=4;i=227");
    public static final NodeId Equit_O2_EquitMax = NodeId.parse("ns=4;i=228");
    public static final NodeId Equit_O2_AlarmMin = NodeId.parse("ns=4;i=229");
    public static final NodeId Equit_O2_AlarmMax = NodeId.parse("ns=4;i=230");
    public static final NodeId Equit_O2_ShowMin = NodeId.parse("ns=4;i=231");
    public static final NodeId Equit_O2_ShowMax = NodeId.parse("ns=4;i=232");
    public static final NodeId Equit_O2_PV = NodeId.parse("ns=4;i=233");
    public static final NodeId Equit_O2_CV = NodeId.parse("ns=4;i=234");
    public static final NodeId Equit_O2_SV = NodeId.parse("ns=4;i=235");
    public static final NodeId Equit_O2_Cycle = NodeId.parse("ns=4;i=236");
    public static final NodeId Equit_O2_ControlMode = NodeId.parse("ns=4;i=237");
    public static final NodeId Equit_O2_Dir = NodeId.parse("ns=4;i=238");
    public static final NodeId Equit_O2_Dosage = NodeId.parse("ns=4;i=239");
    public static final NodeId Equit_O2_DosageReset = NodeId.parse("ns=4;i=240");
    public static final NodeId Equit_O2_Equit_Type = NodeId.parse("ns=4;i=241");
    public static final NodeId Equit_O2_UserScript_SV = NodeId.parse("ns=4;i=242");
    public static final NodeId Equit_O2_Sequence_SV = NodeId.parse("ns=4;i=243");
    public static final NodeId Equit_O2_Condition_SV = NodeId.parse("ns=4;i=244");

    // Equit_Overlay
    public static final NodeId Equit_Overlay = NodeId.parse("ns=4;i=245");
    public static final NodeId Equit_Overlay_Static = NodeId.parse("ns=4;i=246");
    public static final NodeId Equit_Overlay_EquitMin = NodeId.parse("ns=4;i=247");
    public static final NodeId Equit_Overlay_EquitMax = NodeId.parse("ns=4;i=248");
    public static final NodeId Equit_Overlay_AlarmMin = NodeId.parse("ns=4;i=249");
    public static final NodeId Equit_Overlay_AlarmMax = NodeId.parse("ns=4;i=250");
    public static final NodeId Equit_Overlay_ShowMin = NodeId.parse("ns=4;i=251");
    public static final NodeId Equit_Overlay_ShowMax = NodeId.parse("ns=4;i=252");
    public static final NodeId Equit_Overlay_PV = NodeId.parse("ns=4;i=253");
    public static final NodeId Equit_Overlay_CV = NodeId.parse("ns=4;i=254");
    public static final NodeId Equit_Overlay_SV = NodeId.parse("ns=4;i=255");
    public static final NodeId Equit_Overlay_Cycle = NodeId.parse("ns=4;i=256");
    public static final NodeId Equit_Overlay_ControlMode = NodeId.parse("ns=4;i=257");
    public static final NodeId Equit_Overlay_Dir = NodeId.parse("ns=4;i=258");
    public static final NodeId Equit_Overlay_Dosage = NodeId.parse("ns=4;i=259");
    public static final NodeId Equit_Overlay_DosageReset = NodeId.parse("ns=4;i=260");
    public static final NodeId Equit_Overlay_Equit_Type = NodeId.parse("ns=4;i=261");
    public static final NodeId Equit_Overlay_UserScript_SV = NodeId.parse("ns=4;i=262");
    public static final NodeId Equit_Overlay_Sequence_SV = NodeId.parse("ns=4;i=263");
    public static final NodeId Equit_Overlay_Condition_SV = NodeId.parse("ns=4;i=264");

    // Equit_N2
    public static final NodeId Equit_N2 = NodeId.parse("ns=4;i=265");
    public static final NodeId Equit_N2_Static = NodeId.parse("ns=4;i=266");
    public static final NodeId Equit_N2_EquitMin = NodeId.parse("ns=4;i=267");
    public static final NodeId Equit_N2_EquitMax = NodeId.parse("ns=4;i=268");
    public static final NodeId Equit_N2_AlarmMin = NodeId.parse("ns=4;i=269");
    public static final NodeId Equit_N2_AlarmMax = NodeId.parse("ns=4;i=270");
    public static final NodeId Equit_N2_ShowMin = NodeId.parse("ns=4;i=271");
    public static final NodeId Equit_N2_ShowMax = NodeId.parse("ns=4;i=272");
    public static final NodeId Equit_N2_PV = NodeId.parse("ns=4;i=273");
    public static final NodeId Equit_N2_CV = NodeId.parse("ns=4;i=274");
    public static final NodeId Equit_N2_SV = NodeId.parse("ns=4;i=275");
    public static final NodeId Equit_N2_Cycle = NodeId.parse("ns=4;i=276");
    public static final NodeId Equit_N2_ControlMode = NodeId.parse("ns=4;i=277");
    public static final NodeId Equit_N2_Dir = NodeId.parse("ns=4;i=278");
    public static final NodeId Equit_N2_Dosage = NodeId.parse("ns=4;i=279");
    public static final NodeId Equit_N2_DosageReset = NodeId.parse("ns=4;i=280");
    public static final NodeId Equit_N2_Equit_Type = NodeId.parse("ns=4;i=281");
    public static final NodeId Equit_N2_UserScript_SV = NodeId.parse("ns=4;i=282");
    public static final NodeId Equit_N2_Sequence_SV = NodeId.parse("ns=4;i=283");
    public static final NodeId Equit_N2_Condition_SV = NodeId.parse("ns=4;i=284");

    // Equit_Stir
    public static final NodeId Equit_Stir = NodeId.parse("ns=4;i=285");
    public static final NodeId Equit_Stir_Input = NodeId.parse("ns=4;i=286");
    public static final NodeId Equit_Stir_Static = NodeId.parse("ns=4;i=287");
    public static final NodeId Equit_Stir_EquitMin = NodeId.parse("ns=4;i=288");
    public static final NodeId Equit_Stir_EquitMax = NodeId.parse("ns=4;i=289");
    public static final NodeId Equit_Stir_AlarmMin = NodeId.parse("ns=4;i=290");
    public static final NodeId Equit_Stir_AlarmMax = NodeId.parse("ns=4;i=291");
    public static final NodeId Equit_Stir_ShowMin = NodeId.parse("ns=4;i=292");
    public static final NodeId Equit_Stir_ShowMax = NodeId.parse("ns=4;i=293");
    public static final NodeId Equit_Stir_PV = NodeId.parse("ns=4;i=294");
    public static final NodeId Equit_Stir_Enable = NodeId.parse("ns=4;i=295");
    public static final NodeId Equit_Stir_PulsePerRound = NodeId.parse("ns=4;i=296");
    public static final NodeId Equit_Stir_Dir = NodeId.parse("ns=4;i=297");
    public static final NodeId Equit_Stir_Status = NodeId.parse("ns=4;i=298");
    public static final NodeId Equit_Stir_SV = NodeId.parse("ns=4;i=299");
    public static final NodeId Equit_Stir_ControlMode = NodeId.parse("ns=4;i=300");
    public static final NodeId Equit_Stir_Speed_Ratio = NodeId.parse("ns=4;i=301");
    public static final NodeId Equit_Stir_Torque_PV = NodeId.parse("ns=4;i=302");
    public static final NodeId Equit_Stir_UserScript_SV = NodeId.parse("ns=4;i=303");

    // Equit_Alarm
    public static final NodeId Equit_Alarm = NodeId.parse("ns=4;i=304");
    public static final NodeId Equit_Alarm_Alarm_Active = NodeId.parse("ns=4;i=305");
    public static final NodeId Equit_Alarm_Stir_LowAlarm = NodeId.parse("ns=4;i=306");
    public static final NodeId Equit_Alarm_Stir_HighAlarm = NodeId.parse("ns=4;i=307");
    public static final NodeId Equit_Alarm_DO_LowAlarm = NodeId.parse("ns=4;i=308");
    public static final NodeId Equit_Alarm_DO_HighAlarm = NodeId.parse("ns=4;i=309");
    public static final NodeId Equit_Alarm_PH_LowAlarm = NodeId.parse("ns=4;i=310");
    public static final NodeId Equit_Alarm_PH_HighAlarm = NodeId.parse("ns=4;i=311");
    public static final NodeId Equit_Alarm_TankTemp_LowAlarm = NodeId.parse("ns=4;i=312");
    public static final NodeId Equit_Alarm_TankTemp_HighAlarm = NodeId.parse("ns=4;i=313");
    public static final NodeId Equit_Alarm_JacketTemp_HighAlarm = NodeId.parse("ns=4;i=314");
    public static final NodeId Equit_Alarm_JacketTemp_LowAlarm = NodeId.parse("ns=4;i=315");
    public static final NodeId Equit_Alarm_Pressure_LowAlarm = NodeId.parse("ns=4;i=316");
    public static final NodeId Equit_Alarm_Pressure_HighAlarm = NodeId.parse("ns=4;i=317");
    public static final NodeId Equit_Alarm_Weight_LowAlarm = NodeId.parse("ns=4;i=318");
    public static final NodeId Equit_Alarm_Weight_HighAlarm = NodeId.parse("ns=4;i=319");
    public static final NodeId Equit_Alarm_Stir_LowAlarm_Active = NodeId.parse("ns=4;i=320");
    public static final NodeId Equit_Alarm_Stir_HighAlarm_Active = NodeId.parse("ns=4;i=321");
    public static final NodeId Equit_Alarm_DO_LowAlarm_Active = NodeId.parse("ns=4;i=322");
    public static final NodeId Equit_Alarm_DO_HighAlarm_Active = NodeId.parse("ns=4;i=323");
    public static final NodeId Equit_Alarm_PH_LowAlarm_Active = NodeId.parse("ns=4;i=324");
    public static final NodeId Equit_Alarm_PH_HighAlarm_Active = NodeId.parse("ns=4;i=325");
    public static final NodeId Equit_Alarm_TankTemp_LowAlarm_Active = NodeId.parse("ns=4;i=326");
    public static final NodeId Equit_Alarm_TankTemp_HighAlarm_Active = NodeId.parse("ns=4;i=327");
    public static final NodeId Equit_Alarm_JacketTemp_HighAlarm_Active = NodeId.parse("ns=4;i=328");
    public static final NodeId Equit_Alarm_JacketTemp_LowAlarm_Active = NodeId.parse("ns=4;i=329");
    public static final NodeId Equit_Alarm_Pressure_LowAlarm_Active = NodeId.parse("ns=4;i=330");
    public static final NodeId Equit_Alarm_Pressure_HighAlarm_Active = NodeId.parse("ns=4;i=331");
    public static final NodeId Equit_Alarm_Weight_LowAlarm_Active = NodeId.parse("ns=4;i=332");
    public static final NodeId Equit_Alarm_Weight_HighAlarm_Active = NodeId.parse("ns=4;i=333");
    public static final NodeId Equit_Alarm_Pressure_LowProtect = NodeId.parse("ns=4;i=334");
    public static final NodeId Equit_Alarm_Pressure_HighProtect = NodeId.parse("ns=4;i=335");
    public static final NodeId Equit_Alarm_JacketTemp_HighProtect = NodeId.parse("ns=4;i=336");
    public static final NodeId Equit_Alarm_Air_Alarm = NodeId.parse("ns=4;i=337");
    public static final NodeId Equit_Alarm_N2_Alarm = NodeId.parse("ns=4;i=338");
    public static final NodeId Equit_Alarm_O2_Alarm = NodeId.parse("ns=4;i=339");
    public static final NodeId Equit_Alarm_CO2_Alarm = NodeId.parse("ns=4;i=340");
    public static final NodeId Equit_Alarm_Overlay_Alarm = NodeId.parse("ns=4;i=341");
    public static final NodeId Equit_Alarm_Air_Alarm_Active = NodeId.parse("ns=4;i=342");
    public static final NodeId Equit_Alarm_N2_Alarm_Active = NodeId.parse("ns=4;i=343");
    public static final NodeId Equit_Alarm_O2_Alarm_Active = NodeId.parse("ns=4;i=344");
    public static final NodeId Equit_Alarm_CO2_Alarm_Active = NodeId.parse("ns=4;i=345");
    public static final NodeId Equit_Alarm_Overlay_Alarm_Active = NodeId.parse("ns=4;i=346");
}