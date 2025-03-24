/*
 * Copyright 2025 Your Organization
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.example.manus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlanStatus {
    private String planId;
    private String title;
    private List<String> stepStatuses;
    private List<String> stepNotes;
    private Map<String, Integer> statusCounts;
    private int completedSteps;
    private int totalSteps;
    private double progress;
    private String state;
    private double progressPercentage;
    private List<Step> steps = new ArrayList<>();
    private List<LogEntry> logs = new ArrayList<>();

    // Getters and Setters
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<String> getStepStatuses() { return stepStatuses; }
    public void setStepStatuses(List<String> stepStatuses) { this.stepStatuses = stepStatuses; }
    public List<String> getStepNotes() { return stepNotes; }
    public void setStepNotes(List<String> stepNotes) { this.stepNotes = stepNotes; }
    public Map<String, Integer> getStatusCounts() { return statusCounts; }
    public void setStatusCounts(Map<String, Integer> statusCounts) { this.statusCounts = statusCounts; }
    public int getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(int completedSteps) { this.completedSteps = completedSteps; }
    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }
    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { 
        this.steps = steps;
        // 同步更新 stepStatuses
        if (this.stepStatuses == null) {
            this.stepStatuses = new ArrayList<>();
        }
        this.stepStatuses.clear();
        for (Step step : steps) {
            this.stepStatuses.add(step.getStatus());
        }
    }
    public List<LogEntry> getLogs() { return logs; }
    public void setLogs(List<LogEntry> logs) { this.logs = logs; }

    // Inner class definitions
    public static class Step {
        private String description;
        private String status; // "completed", "in progress", "blocked", "not started"
        private String notes;

        // getters and setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class LogEntry {
        private String message;
        private String level; // "info", "error", etc.

        // getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }
}
