package com.wvw.mmw.domain.terms.entity;

import com.wvw.mmw.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
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
        name = "terms_agreements",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_terms_agreements_user_id_terms_type",
                columnNames = {"user_id", "terms_type"})
)
public class TermsAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "terms_type", nullable = false, length = 30)
    private TermsType termsType;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    @Builder
    private TermsAgreement(User user, TermsType termsType, LocalDateTime agreedAt) {
        this.user = user;
        this.termsType = termsType;
        this.agreedAt = agreedAt;
    }

    public static TermsAgreement create(User user, TermsType termsType, LocalDateTime agreedAt) {
        return new TermsAgreement(user, termsType, agreedAt);
    }
}
