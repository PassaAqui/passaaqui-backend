package com.passaaqui.backend.modules.poi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.passaaqui.backend.modules.user.model.UserModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"poi_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PoiRatingModel {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private PoiModel poi;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private UserModel user;

    @Column(nullable = false)
    private Integer rating;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
