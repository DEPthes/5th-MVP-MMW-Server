package com.wvw.mmw.domain.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wvw.mmw.domain.interview.dto.CreateInterviewSessionRequest;
import com.wvw.mmw.domain.interview.dto.InterviewSessionStartResponse;
import com.wvw.mmw.domain.interview.entity.InterviewQuestion;
import com.wvw.mmw.domain.interview.entity.InterviewSession;
import com.wvw.mmw.domain.interview.entity.SessionStatus;
import com.wvw.mmw.domain.interview.repository.InterviewQuestionRepository;
import com.wvw.mmw.domain.interview.repository.InterviewSessionRepository;
import com.wvw.mmw.domain.profile.entity.ApplicationProfile;
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

/**
 * 면접 세션 생성을 담당.
 * Gemini로 질문 세트를 만들어 세션과 함께 저장.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionService {

    // 질문 하나에 배정하는 시간(분). TTS 재생 + 답변 시간 기준.
    private static final int MINUTES_PER_QUESTION = 1;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final EntityManager entityManager;

    /**
     * 면접 세션을 만들고 질문 세트를 발급.
     *
     * @param userId  요청한 사용자 ID
     * @param request 면접 조건
     */
    @Transactional
    public InterviewSessionStartResponse create(Long userId, CreateInterviewSessionRequest request) {
        List<String> contents = generateQuestions(request); // 1.Gemini

        InterviewSession session = sessionRepository.save(buildSession(userId, request)); // 2.저장
        List<InterviewQuestion> questions = saveQuestions(session, contents);

        return InterviewSessionStartResponse.of(session, questions);
    }

    // Gemini에 질문 세트를 요청하고 문자열 목록으로 변환.
    private List<String> generateQuestions(CreateInterviewSessionRequest request) {
        String prompt = QuestionPrompt.build(
                request.companyName(),
                request.jobPosition(),
                CareerLevelLabel.of(request.careerLevel()),
                calculateQuestionCount(request.durationMinutes())
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
            log.error("질문 생성 실패. company={}, position={}",
                    request.companyName(), request.jobPosition(), e);
            throw new BusinessException(ErrorCode.QUESTION_GENERATION_FAILED);
        }
    }

    // 면접 시간에서 생성할 질문 개수를 구한다.
    private int calculateQuestionCount(int durationMinutes) {
        return durationMinutes / MINUTES_PER_QUESTION;
    }

    // 요청값으로 세션 엔티티를 만든다.
    private InterviewSession buildSession(Long userId, CreateInterviewSessionRequest request) {
        return InterviewSession.builder()
                .user(entityManager.getReference(User.class, userId))
                .applicationProfile(referenceProfile(request.applicationProfileId()))
                .companyName(request.companyName())
                .jobPosition(request.jobPosition())
                .careerLevel(request.careerLevel())
                .interviewType(request.interviewType())
                .durationMinutes(request.durationMinutes())
                .status(SessionStatus.READY)
                .build();
    }

    // 프로필 ID가 있으면 참조를 만든다. 없으면 null.
    private ApplicationProfile referenceProfile(Long profileId) {
        if (profileId == null) {
            return null;
        }
        return entityManager.getReference(ApplicationProfile.class, profileId);
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