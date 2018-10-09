package ru.nordavind.ecgdonglelib.scan;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * Channel configuration of a dongle device
 * Describes hardware and software channels
 */

public enum DongleChannelConfiguration {
    Channels1(new Lead[]{Lead.I}, new Lead[]{Lead.I}),
    Channels2(Lead.LEADS6, Lead.LEADS_HARDWARE2),
    Channels3(Lead.LEADS3, Lead.LEADS3),
    Channels4(Lead.LEADS8, Lead.LEADS_HARDWARE4),
    Channels8(Lead.LEADS12, Lead.LEADS_HARDWARE8);

    private final int leadNumbers[] = new int[Lead.values().length];
    private final int hardwareLeadNumbers[] = new int[Lead.values().length];

    /**
     * all leads in this configuration
     */
    private final Lead[] leads;

    /**
     * hardware (source) leads for this configuration
     */
    private final Lead[] hardwareLeads;

    DongleChannelConfiguration(Lead[] leads, Lead[] hardwareLeads) {
        this.leads = leads;
        this.hardwareLeads = hardwareLeads;
        Arrays.fill(leadNumbers, -1);
        Arrays.fill(hardwareLeadNumbers, -1);

        for (int i = 0; i < leads.length; i++) {
            leadNumbers[leads[i].ordinal()] = i;
        }
        for (int i = 0; i < hardwareLeads.length; i++) {
            hardwareLeadNumbers[hardwareLeads[i].ordinal()] = i;
        }
    }

    public static DongleChannelConfiguration getForChannelsCount(int hardwareChannels) {
        switch (hardwareChannels) {
            case 1:
                return Channels1;
            case 2:
                return Channels2;
            case 3:
                return Channels3;
            case 4:
                return Channels4;
            case 8:
                return Channels8;

            default:
                throw new RuntimeException("not implemented for " + hardwareChannels + "hardware channels");
        }
    }

    /**
     * @return leads for ECG Dongle device (hardware and software)
     */
    public Lead[] getLeads() {
        return leads;
    }

    /**
     * @return hardware leads for dongle device
     */
    public Lead[] getHardwareLeads() {
        return hardwareLeads;
    }

    /**
     * @param lead
     * @return lead number within leads; can be used to draw leads in correct order
     */
    public int getLeadNum(@NonNull Lead lead) {
        return leadNumbers[lead.ordinal()];
    }

    /**
     * @param lead
     * @return ordinal number of the lead in ECG Dongle's hardware leads
     * -1 if the lead is not hardware or the dongle can't provide the lead's data
     */
    public int getHardwareLeadNum(@NonNull Lead lead) {
        return hardwareLeadNumbers[lead.ordinal()];
    }
}
