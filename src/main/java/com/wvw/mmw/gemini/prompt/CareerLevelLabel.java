package com.wvw.mmw.gemini.prompt;

import com.wvw.mmw.domain.profile.entity.CareerLevel;

/**
 * CareerLevel을 프롬프트에 넣을 한글 표현으로 바꾼다.
 * TODO: 기획 확정 문구로 교체 필요.
 */
public class CareerLevelLabel {

    private CareerLevelLabel() {
    }

    public static String of(CareerLevel careerLevel) {
        return switch (careerLevel) {
            case NEWCOMER -> "신입";
            case RELATED_EXPERIENCE -> "동일 직무 경력이 있는";
            case SIMILAR_EXPERIENCE -> "유사 직무 경력이 있는";
            case SHALLOW_EXPERIENCE -> "관련 경험이 짧은";
            case FULL_TIME -> "정규직 경력이 있는";
            case CONTRACT_FREELANCE -> "계약직·프리랜서 경력이 있는";
            case INTERNSHIP -> "인턴 경험이 있는";
        };
    }
}