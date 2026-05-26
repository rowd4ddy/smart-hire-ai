package com.smarthireai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recruiter_id", nullable = false)
    @JsonIgnore
    private User recruiter;

    private String title;
    private String company;
    private Integer minimumExperienceYears;
    private String educationLevel;
    private String location;
    private String department;
    private String employmentType;
    private String workMode;
    private String salaryRange;
    private LocalDate applicationDeadline;
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_required_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> requiredSkills = new ArrayList<>();

    public Job() {
    }

    public Job(
            User recruiter,
            String title,
            String company,
            List<String> requiredSkills,
            Integer minimumExperienceYears,
            String educationLevel,
            String location,
            String department,
            String employmentType,
            String workMode,
            String salaryRange,
            LocalDate applicationDeadline,
            String status
    ) {
        this.recruiter = recruiter;
        this.title = title;
        this.company = company;
        this.requiredSkills = requiredSkills;
        this.minimumExperienceYears = minimumExperienceYears;
        this.educationLevel = educationLevel;
        this.location = location;
        this.department = department;
        this.employmentType = employmentType;
        this.workMode = workMode;
        this.salaryRange = salaryRange;
        this.applicationDeadline = applicationDeadline;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public User getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(User recruiter) {
        this.recruiter = recruiter;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public Integer getMinimumExperienceYears() {
        return minimumExperienceYears;
    }

    public void setMinimumExperienceYears(Integer minimumExperienceYears) {
        this.minimumExperienceYears = minimumExperienceYears;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getWorkMode() {
        return workMode;
    }

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
