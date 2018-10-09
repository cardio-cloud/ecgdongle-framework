package ru.nordavind.ecgdonglelib.scan;

/**
 * Leads of a ECG Dongle
 */
public enum Lead {
    I,
    II,
    III,
    aVR,
    aVL,
    aVF,
    V1,
    V2,
    V3,
    V4,
    V5,
    V6,
    CH0,
    CH1,
    CH2,;


    public static final Lead[] LEADS3 = new Lead[]{CH0, CH1, CH2};
    public static final Lead[] LEADS6 = new Lead[]{I, II, III, aVR, aVL, aVF};
    public static final Lead[] LEADS8 = new Lead[]{I, II, III, aVR, aVL, aVF, V1, V2};
    public static final Lead[] LEADS12 = new Lead[]{I, II, III, aVR, aVL, aVF, V1, V2, V3, V4, V5, V6};

    public static final Lead[] LEADS_HARDWARE2 = new Lead[]{II, III};
    public static final Lead[] LEADS_HARDWARE4 = new Lead[]{II, III, V1, V2};
    public static final Lead[] LEADS_HARDWARE8 = new Lead[]{II, III, V1, V2, V3, V4, V5, V6};

    Lead() {
    }

    /*
    I = II-III = ch0 - ch1
    II = ch0
    III = ch1
    aVR = -(II+I)/2 = -(ch0 + ch0 - ch1) / 2 =  ch1/2 - ch0
    aVL = I-II/2 = (ch0 - ch1) - ch0 / 2 = ch0/2 - ch1
    aVF = II-I/2 = ch0 - (ch0 - ch1)/ 2 = (ch0 + ch1)/2
    */
}
