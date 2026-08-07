package com.wvw.mmw.domain.profile.entity;

import com.wvw.mmw.domain.user.entity.User;
import com.wvw.mmw.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "application_profiles",
        indexes = @Index(name = "idx_application_profiles_user_id", columnList = "user_id")
)
public class ApplicationProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "job_position", nullable = false, length = 100)
    private String jobPosition;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "career_level", nullable = false, length = 30)
    private CareerLevel careerLevel;

    @Builder
    private ApplicationProfile(User user, String companyName, String jobPosition,
                               CareerLevel careerLevel) {
        this.user = user;
        this.companyName = companyName;
        this.jobPosition = jobPosition;
        this.careerLevel = careerLevel;
    }

    public void update(String companyName, String jobPosition, CareerLevel careerLevel) {
        this.companyName = companyName;
        this.jobPosition = jobPosition;
        this.careerLevel = careerLevel;
    }
}
