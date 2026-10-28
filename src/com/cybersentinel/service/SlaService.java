package com.cybersentinel.service;

import com.cybersentinel.dao.SettingDAO;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.Severity;
import com.cybersentinel.model.SlaStatus;

public class SlaService {
    private final SettingDAO settingDAO = new SettingDAO();

    public static class SlaInfo {
        private final SlaStatus status;
        private final int targetMinutes;
        private final long elapsedMinutes;
        private final long remainingMinutes;

        public SlaInfo(SlaStatus status, int targetMinutes, long elapsedMinutes, long remainingMinutes) {
            this.status = status;
            this.targetMinutes = targetMinutes;
            this.elapsedMinutes = elapsedMinutes;
            this.remainingMinutes = remainingMinutes;
        }

        public SlaStatus getStatus() { return status; }
        public int getTargetMinutes() { return targetMinutes; }
        public long getElapsedMinutes() { return elapsedMinutes; }
        public long getRemainingMinutes() { return remainingMinutes; }

        public String getFormattedRemaining() {
            if (status == SlaStatus.BREACHED_SLA) {
                return "Breached by " + Math.abs(remainingMinutes) + "m";
            }
            if (remainingMinutes >= 60) {
                return (remainingMinutes / 60) + "h " + (remainingMinutes % 60) + "m left";
            }
            return remainingMinutes + "m left";
        }
    }

    public int getTargetMinutes(Severity severity) {
        if (severity == null) severity = Severity.MEDIUM;
        String key = "sla_" + severity.name().toLowerCase() + "_minutes";
        String val = settingDAO.getSetting(key, String.valueOf(severity.getDefaultSlaMinutes()));
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return severity.getDefaultSlaMinutes();
        }
    }

    public SlaInfo calculateSla(Incident incident) {
        if (incident == null) {
            return new SlaInfo(SlaStatus.WITHIN_SLA, 120, 0, 120);
        }

        int target = getTargetMinutes(incident.getSeverity());
        long elapsed = incident.getElapsedMinutes();
        long remaining = target - elapsed;

        SlaStatus status;
        if (elapsed > target) {
            status = SlaStatus.BREACHED_SLA;
        } else if (elapsed >= (target * 0.70)) {
            status = SlaStatus.APPROACHING_SLA;
        } else {
            status = SlaStatus.WITHIN_SLA;
        }

        return new SlaInfo(status, target, elapsed, remaining);
    }
}
