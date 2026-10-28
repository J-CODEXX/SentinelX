package com.cybersentinel.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardStats {
    // Summary KPI metrics
    private int totalIncidents;
    private int openIncidents;
    private int investigatingIncidents;
    private int criticalIncidents;
    private int highIncidents;
    private int resolvedIncidents;
    private int unassignedIncidents;
    private int escalatedIncidents;
    private int myAssignedIncidents;
    private int activeAnalysts;
    private int simulatedIncidents;
    private int realIncidents;

    // SLA metrics
    private int withinSlaCount;
    private int approachingSlaCount;
    private int breachedSlaCount;

    // Chart distribution maps
    private Map<String, Integer> severityDistribution = new LinkedHashMap<>();
    private Map<String, Integer> statusDistribution = new LinkedHashMap<>();
    private Map<String, Integer> categoryDistribution = new LinkedHashMap<>();
    private Map<String, Integer> analystWorkload = new LinkedHashMap<>();
    private Map<String, Integer> trendDaily = new LinkedHashMap<>();
    private Map<String, Integer> threatIntelDistribution = new LinkedHashMap<>();

    public DashboardStats() {}

    public int getTotalIncidents() { return totalIncidents; }
    public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }

    public int getOpenIncidents() { return openIncidents; }
    public void setOpenIncidents(int openIncidents) { this.openIncidents = openIncidents; }

    public int getInvestigatingIncidents() { return investigatingIncidents; }
    public void setInvestigatingIncidents(int investigatingIncidents) { this.investigatingIncidents = investigatingIncidents; }

    public int getCriticalIncidents() { return criticalIncidents; }
    public void setCriticalIncidents(int criticalIncidents) { this.criticalIncidents = criticalIncidents; }

    public int getHighIncidents() { return highIncidents; }
    public void setHighIncidents(int highIncidents) { this.highIncidents = highIncidents; }

    public int getResolvedIncidents() { return resolvedIncidents; }
    public void setResolvedIncidents(int resolvedIncidents) { this.resolvedIncidents = resolvedIncidents; }

    public int getUnassignedIncidents() { return unassignedIncidents; }
    public void setUnassignedIncidents(int unassignedIncidents) { this.unassignedIncidents = unassignedIncidents; }

    public int getEscalatedIncidents() { return escalatedIncidents; }
    public void setEscalatedIncidents(int escalatedIncidents) { this.escalatedIncidents = escalatedIncidents; }

    public int getMyAssignedIncidents() { return myAssignedIncidents; }
    public void setMyAssignedIncidents(int myAssignedIncidents) { this.myAssignedIncidents = myAssignedIncidents; }

    public int getActiveAnalysts() { return activeAnalysts; }
    public void setActiveAnalysts(int activeAnalysts) { this.activeAnalysts = activeAnalysts; }

    public int getSimulatedIncidents() { return simulatedIncidents; }
    public void setSimulatedIncidents(int simulatedIncidents) { this.simulatedIncidents = simulatedIncidents; }

    public int getRealIncidents() { return realIncidents; }
    public void setRealIncidents(int realIncidents) { this.realIncidents = realIncidents; }

    public int getWithinSlaCount() { return withinSlaCount; }
    public void setWithinSlaCount(int withinSlaCount) { this.withinSlaCount = withinSlaCount; }

    public int getApproachingSlaCount() { return approachingSlaCount; }
    public void setApproachingSlaCount(int approachingSlaCount) { this.approachingSlaCount = approachingSlaCount; }

    public int getBreachedSlaCount() { return breachedSlaCount; }
    public void setBreachedSlaCount(int breachedSlaCount) { this.breachedSlaCount = breachedSlaCount; }

    public Map<String, Integer> getSeverityDistribution() { return severityDistribution; }
    public void setSeverityDistribution(Map<String, Integer> severityDistribution) { this.severityDistribution = severityDistribution; }

    public Map<String, Integer> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Integer> statusDistribution) { this.statusDistribution = statusDistribution; }

    public Map<String, Integer> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(Map<String, Integer> categoryDistribution) { this.categoryDistribution = categoryDistribution; }

    public Map<String, Integer> getAnalystWorkload() { return analystWorkload; }
    public void setAnalystWorkload(Map<String, Integer> analystWorkload) { this.analystWorkload = analystWorkload; }

    public Map<String, Integer> getTrendDaily() { return trendDaily; }
    public void setTrendDaily(Map<String, Integer> trendDaily) { this.trendDaily = trendDaily; }

    public Map<String, Integer> getThreatIntelDistribution() { return threatIntelDistribution; }
    public void setThreatIntelDistribution(Map<String, Integer> threatIntelDistribution) { this.threatIntelDistribution = threatIntelDistribution; }
}
