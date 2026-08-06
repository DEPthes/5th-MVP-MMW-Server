package com.wvw.mmw.domain.interview.service;

import com.wvw.mmw.domain.interview.dto.CreateInterviewSessionRequest;
import com.wvw.mmw.domain.interview.dto.InterviewSessionStartResponse;
import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.domain.interview.entity.InterviewSession;
import com.wvw.mmw.domain.interview.entity.SessionStatus;
import com.wvw.mmw.domain.interview.repository.InterviewQuestionRepository;
import com.wvw.mmw.domain.interview.repository.InterviewSessionRepository;
import com.wvw.mmw.domain.profile.entity.ApplicationProfile;
import com.wvw.mmw.domain.profile.repository.ApplicationProfileRepository;
import com.wvw.mmw.domain.user.entity.User;
import com.wvw.mmw.gemini.GeminiClient;
import com.wvw.mmw.gemini.prompt.CareerLevelLabel;
import com.wvw.mmw.gemini.prompt.QuestionPrompt;
import com.wvw.mmw.global.exception.BusinessException;
import com.wvw.mmw.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * 면접 세션 생성을 담당한다.
 * 지원 프로필을 조회해 Gemini로 질문 세트를 만들고, 세션과 함께 저장한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final ApplicationProfileRepository profileRepository;
    private final EntityManager entityManager;

    /**
     * 면접 세션을 만들고 질문 세트를 발급한다.
     *
     * @param userId  요청한 사용자 ID
     * @param request 면접 조건
     */
    @Transactional
    public InterviewSessionStartResponse create(Long userId, CreateInterviewSessionRequest request) {
        ApplicationProfile profile = findProfile(userId, request.applicationProfileId());
        List<String> contents = generateQuestions(profile, request.durationMinutes());

        InterviewSession session = sessionRepository.save(buildSession(userId, profile, request));
        session.start();

        List<InterviewQuestion> questions = saveQuestions(session, contents);

        return InterviewSessionStartResponse.of(session, questions);
    }

    // 본인 소유의 지원 프로필을 조회.
    private ApplicationProfile findProfile(Long userId, Long profileId) {
        return profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    // Gemini에 질문 세트를 요청하고 문자열 목록으로 변환.
    private List<String> generateQuestions(ApplicationProfile profile, int durationMinutes) {
        String prompt = QuestionPrompt.build(
                profile.getCompanyName(),
                profile.getJobPosition(),
                CareerLevelLabel.of(profile.getCareerLevel()),
                QuestionCountPolicy.maxCount(durationMinutes)
        );

        try {
            String json = geminiClient.generateJson(prompt, QuestionPrompt.schema());
            List<String> questions = objectMapper.readValue(json, GeneratedQuestions.class).questions();

            if (questions == null || questions.isEmpty()) {
                throw new BusinessException(ErrorCode.QUESTION_GENERATION_FAILED);
            }
            return questions;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("질문 생성 실패. profileId={}", profile.getId(), e);
            throw new BusinessException(ErrorCode.QUESTION_GENERATION_FAILED);
        }
    }

    // 프로필 정보를 스냅샷으로 복사해 세션 엔티티를 만든다.
    private InterviewSession buildSession(Long userId, ApplicationProfile profile,
                                          CreateInterviewSessionRequest request) {
        return InterviewSession.builder()
                .user(entityManager.getReference(User.class, userId))
                .applicationProfile(profile)
                .companyName(profile.getCompanyName())
                .jobPosition(profile.getJobPosition())
                .careerLevel(profile.getCareerLevel())
                .interviewType(request.interviewType())
                .durationMinutes(request.durationMinutes())
                .status(SessionStatus.READY)
                .build();
    }

    // 질문 목록을 순서대로 저장.
    private List<InterviewQuestion> saveQuestions(InterviewSession session, List<String> contents) {
        List<InterviewQuestion> questions = new ArrayList<>();

        for (int i = 0; i < contents.size(); i++) {
            questions.add(InterviewQuestion.builder()
                    .interviewSession(session)
                    .content(contents.get(i))
                    .sequence(i + 1)
                    .build());
        }
        return questionRepository.saveAll(questions);
    }

    // Gemini 응답 JSON을 받는 형태. {"questions": ["...", "..."]}
    record GeneratedQuestions(List<String> questions) {
    }
}