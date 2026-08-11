package com.wvw.mmw.domain.terms.repository;

import com.wvw.mmw.domain.terms.entity.TermsAgreement;
import com.wvw.mmw.domain.terms.entity.TermsType;
import com.wvw.mmw.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {

    Optional<TermsAgreement> findByUserAndTermsType(User user, TermsType termsType);
}
