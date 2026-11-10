package rw.parkshare.backend.common.dto;

import jakarta.validation.constraints.*;

public class ReviewRequestDto {

    @Min(1)
    @Max(5)
    private Integer rating;
    @Size(max = 2000)
    private String comment;

    public ReviewRequestDto() {
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}