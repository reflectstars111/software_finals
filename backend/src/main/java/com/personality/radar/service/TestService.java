package com.personality.radar.service;

import com.personality.radar.common.BusinessException;
import com.personality.radar.domain.Question;
import com.personality.radar.domain.QuestionOption;
import com.personality.radar.domain.TestResult;
import com.personality.radar.domain.TestType;
import com.personality.radar.domain.UserAccount;
import com.personality.radar.dto.ApiDtos;
import com.personality.radar.repository.QuestionOptionRepository;
import com.personality.radar.repository.QuestionRepository;
import com.personality.radar.repository.TestResultRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestService {
    private final QuestionRepository questions;
    private final QuestionOptionRepository options;
    private final TestResultRepository results;

    public TestService(QuestionRepository questions, QuestionOptionRepository options, TestResultRepository results) {
        this.questions = questions;
        this.options = options;
        this.results = results;
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.QuestionResponse> listQuestions(String type) {
        TestType testType = EnumParser.testType(type);
        return questions.findByTypeAndActiveTrueOrderBySortOrderAsc(testType).stream()
                .map(DtoMapper::question)
                .toList();
    }

    @Transactional
    public ApiDtos.TestResultResponse submit(UserAccount user, ApiDtos.TestSubmitRequest request) {
        TestType type = EnumParser.testType(request.type());
        Map<String, List<Integer>> raw = new HashMap<>();
        for (ApiDtos.AnswerInput answer : request.answers()) {
            Question question = questions.findById(answer.questionId())
                    .orElseThrow(() -> new BusinessException(404, "题目不存在"));
            if (question.getType() != type) {
                throw new BusinessException(400, "提交的题目类型不一致");
            }
            for (Long optionId : answer.optionIds()) {
                QuestionOption option = options.findById(optionId)
                        .orElseThrow(() -> new BusinessException(404, "选项不存在"));
                if (!option.getQuestion().getId().equals(question.getId())) {
                    throw new BusinessException(400, "选项不属于当前题目");
                }
                option.getWeights().forEach((dimension, weight) ->
                        raw.computeIfAbsent(dimension, ignored -> new ArrayList<>()).add(weight));
            }
        }
        TestResult result = new TestResult();
        result.setUser(user);
        result.setType(type);
        result.setScores(ScoringEngine.normalizeScores(raw));
        results.save(result);
        return DtoMapper.testResult(result);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.TestResultResponse> history(UserAccount user) {
        return results.findByUserOrderByCreatedAtDesc(user).stream().map(DtoMapper::testResult).toList();
    }

    @Transactional(readOnly = true)
    public TestResult latestOrThrow(UserAccount user, TestType type) {
        return results.findFirstByUserAndTypeOrderByCreatedAtDesc(user, type)
                .orElseThrow(() -> new BusinessException(400, "请先完成对应测试"));
    }
}
