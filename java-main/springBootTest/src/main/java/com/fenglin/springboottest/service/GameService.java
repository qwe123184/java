package com.fenglin.springboottest.service;

import com.fenglin.springboottest.common.DuelCreateRequest;
import com.fenglin.springboottest.common.DuelMoveRequest;
import com.fenglin.springboottest.common.DuelRoomVO;
import com.fenglin.springboottest.common.LeaderboardVO;
import com.fenglin.springboottest.common.QuizAnswerRequest;
import com.fenglin.springboottest.common.QuizCreateRequest;
import com.fenglin.springboottest.common.QuizRoomVO;
import com.fenglin.springboottest.common.ReactionSubmitRequest;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.StageProgressVO;
import com.fenglin.springboottest.common.StageStartVO;
import com.fenglin.springboottest.common.StageSubmitRequest;
import com.fenglin.springboottest.common.StageSubmitVO;

import java.util.List;
import java.util.Map;

/**
 * 神域对决中心：对决擂台（PvP）/ 禁墟答题（PvP）/ 反应试炼（跨用户排行）
 * / 禁墟试炼 · 境界突破（单人 PvE 闯关）
 */
public interface GameService {

    // ===== 禁墟试炼 · 境界突破（单人 PvE 闯关） =====
    Result<StageProgressVO> stageProgress(Long userId);
    Result<StageStartVO> stageStart(Long userId);
    Result<StageSubmitVO> stageSubmit(Long userId, StageSubmitRequest req);
    Result<List<LeaderboardVO>> stageLeaderboard();

    // ===== 对决擂台 =====
    Result<DuelRoomVO> createDuel(Long userId, DuelCreateRequest req);
    Result<DuelRoomVO> acceptDuel(Long userId, Long roomId);
    Result<DuelRoomVO> duelMove(Long userId, Long roomId, DuelMoveRequest req);
    Result<DuelRoomVO> getDuelRoom(Long userId, Long roomId);
    Result<List<DuelRoomVO>> myDuels(Long userId);
    Result<List<LeaderboardVO>> duelLeaderboard();

    // ===== 禁墟答题 =====
    Result<QuizRoomVO> createQuiz(Long userId, QuizCreateRequest req);
    Result<QuizRoomVO> acceptQuiz(Long userId, Long roomId);
    Result<QuizRoomVO> quizAnswer(Long userId, Long roomId, QuizAnswerRequest req);
    Result<QuizRoomVO> getQuizRoom(Long userId, Long roomId);
    Result<List<QuizRoomVO>> myQuizzes(Long userId);
    Result<List<LeaderboardVO>> quizLeaderboard();

    // ===== 反应试炼 =====
    Result<Map<String, Object>> submitReaction(Long userId, Integer ms);
    Result<List<LeaderboardVO>> reactionLeaderboard();
}
