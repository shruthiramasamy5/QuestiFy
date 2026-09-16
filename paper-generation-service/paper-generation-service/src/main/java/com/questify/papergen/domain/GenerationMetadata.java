package com.questify.papergen.domain;

import java.util.HashMap;
import java.util.Map;

/** Records how the paper was generated so results are auditable. */
public class GenerationMetadata {

    private Map<String, Integer> requestedCoDistribution = new HashMap<>();
    private Map<String, Integer> requestedBloomDistribution = new HashMap<>();
    private Map<String, Integer> requestedDifficultyMix = new HashMap<>();
    private Map<String, Integer> achievedCoDistribution = new HashMap<>();
    private Map<String, Integer> achievedBloomDistribution = new HashMap<>();
    private Map<String, Integer> achievedDifficultyMix = new HashMap<>();
    private int candidatePoolSize;
    private int repetitionExclusions;
    private String strategy = "RULE_BASED";

    public Map<String, Integer> getRequestedCoDistribution() { return requestedCoDistribution; }
    public void setRequestedCoDistribution(Map<String, Integer> v) { this.requestedCoDistribution = v; }
    public Map<String, Integer> getRequestedBloomDistribution() { return requestedBloomDistribution; }
    public void setRequestedBloomDistribution(Map<String, Integer> v) { this.requestedBloomDistribution = v; }
    public Map<String, Integer> getRequestedDifficultyMix() { return requestedDifficultyMix; }
    public void setRequestedDifficultyMix(Map<String, Integer> v) { this.requestedDifficultyMix = v; }
    public Map<String, Integer> getAchievedCoDistribution() { return achievedCoDistribution; }
    public void setAchievedCoDistribution(Map<String, Integer> v) { this.achievedCoDistribution = v; }
    public Map<String, Integer> getAchievedBloomDistribution() { return achievedBloomDistribution; }
    public void setAchievedBloomDistribution(Map<String, Integer> v) { this.achievedBloomDistribution = v; }
    public Map<String, Integer> getAchievedDifficultyMix() { return achievedDifficultyMix; }
    public void setAchievedDifficultyMix(Map<String, Integer> v) { this.achievedDifficultyMix = v; }
    public int getCandidatePoolSize() { return candidatePoolSize; }
    public void setCandidatePoolSize(int candidatePoolSize) { this.candidatePoolSize = candidatePoolSize; }
    public int getRepetitionExclusions() { return repetitionExclusions; }
    public void setRepetitionExclusions(int repetitionExclusions) { this.repetitionExclusions = repetitionExclusions; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
}
