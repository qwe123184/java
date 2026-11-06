package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.*;
import com.fenglin.springboottest.service.GameService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 神域对决中心接口（对决擂台 / 禁墟答题 / 反应试炼）
 * 位于 /api/game/**，需携带有效访问令牌（由 AuthInterceptor 校验并注入 userId）。
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService gameService;

    // ===== 对决擂台 =====
    @PostMapping("/duel/create")
    public Result<DuelRoomVO> createDuel(@Valid @RequestBody DuelCreateRequest req, @RequestAttribute Long userId) {
        return gameService.createDuel(userId, req);
    }

    @PostMapping("/duel/{roomId}/accept")
    public Result<DuelRoomVO> acceptDuel(@PathVariable Long roomId, @RequestAttribute Long userId) {
        return gameService.acceptDuel(userId, roomId);
    }

    @PostMapping("/duel/{roomId}/move")
    public Result<DuelRoomVO> duelMove(@PathVariable Long roomId, @Valid @RequestBody DuelMoveRequest req, @RequestAttribute Long userId) {
        return gameService.duelMove(userId, roomId, req);
    }

    @GetMapping("/duel/{roomId}")
    public Result<DuelRoomVO> getDuel(@PathVariable Long roomId, @RequestAttribute Long userId) {
        return gameService.getDuelRoom(userId, roomId);
    }

    @GetMapping("/duel/mine")
    public Result<List<DuelRoomVO>> myDuels(@RequestAttribute Long userId) {
        return gameService.myDuels(userId);
    }

    @GetMapping("/duel/leaderboard")
    public Result<List<LeaderboardVO>> duelLeaderboard() {
        return gameService.duelLeaderboard();
    }

    // ===== 禁墟答题 =====
    @PostMapping("/quiz/create")
    public Result<QuizRoomVO> createQuiz(@Valid @RequestBody QuizCreateRequest req, @RequestAttribute Long userId) {
        return gameService.createQuiz(userId, req);
    }

    @PostMapping("/quiz/{roomId}/accept")
    public Result<QuizRoomVO> acceptQuiz(@PathVariable Long roomId, @RequestAttribute Long userId) {
        return gameService.acceptQuiz(userId, roomId);
    }

    @PostMapping("/quiz/{roomId}/answer")
    public Result<QuizRoomVO> quizAnswer(@PathVariable Long roomId, @Valid @RequestBody QuizAnswerRequest req, @RequestAttribute Long userId) {
        return gameService.quizAnswer(userId, roomId, req);
    }

    @GetMapping("/quiz/{roomId}")
    public Result<QuizRoomVO> getQuiz(@PathVariable Long roomId, @RequestAttribute Long userId) {
        return gameService.getQuizRoom(userId, roomId);
    }

    @GetMapping("/quiz/mine")
    public Result<List<QuizRoomVO>> myQuizzes(@RequestAttribute Long userId) {
        return gameService.myQuizzes(userId);
    }

    @GetMapping("/quiz/leaderboard")
    public Result<List<LeaderboardVO>> quizLeaderboard() {
        return gameService.quizLeaderboard();
    }

    // ===== 反应试炼 =====
    @PostMapping("/reaction/submit")
    public Result<Map<String, Object>> submitReaction(@Valid @RequestBody ReactionSubmitRequest req, @RequestAttribute Long userId) {
        return gameService.submitReaction(userId, req.getMs());
    }

    @GetMapping("/reaction/leaderboard")
    public Result<List<LeaderboardVO>> reactionLeaderboard() {
        return gameService.reactionLeaderboard();
    }

    // ===== 禁墟试炼 · 境界突破（单人 PvE 闯关） =====
    @GetMapping("/stage/progress")
    public Result<StageProgressVO> stageProgress(@RequestAttribute Long userId) {
        return gameService.stageProgress(userId);
    }

    @PostMapping("/stage/start")
    public Result<StageStartVO> stageStart(@RequestAttribute Long userId) {
        return gameService.stageStart(userId);
    }

    @PostMapping("/stage/submit")
    public Result<StageSubmitVO> stageSubmit(@RequestBody StageSubmitRequest req, @RequestAttribute Long userId) {
        return gameService.stageSubmit(userId, req);
    }

    @GetMapping("/stage/leaderboard")
    public Result<List<LeaderboardVO>> stageLeaderboard() {
        return gameService.stageLeaderboard();
    }
}
