package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class IssueReportResponseDto {

    private Long id;
    private Long userId;
    private Long parkingLotId;
    private Long reservationId;
    private String issueType;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long resolvedByUserId;
    private OffsetDateTime resolvedAt;
    private String resolutionNotes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public IssueReportResponseDto() {
    }

    public IssueReportResponseDto(
            Long id,
            Long userId,
            Long parkingLotId,
            Long reservationId,
            String issueType,
            String title,
            String description,
            String status,
            String priority,
            Long resolvedByUserId,
            OffsetDateTime resolvedAt,
            String resolutionNotes,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.parkingLotId = parkingLotId;
        this.reservationId = reservationId;
        this.issueType = issueType;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.resolvedByUserId = resolvedByUserId;
        this.resolvedAt = resolvedAt;
        this.resolutionNotes = resolutionNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getResolvedByUserId() {
        return resolvedByUserId;
    }

    public void setResolvedByUserId(Long resolvedByUserId) {
        this.resolvedByUserId = resolvedByUserId;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
