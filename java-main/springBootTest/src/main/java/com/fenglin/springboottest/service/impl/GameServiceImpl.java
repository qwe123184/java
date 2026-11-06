package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.*;
import com.fenglin.springboottest.entity.*;
import com.fenglin.springboottest.mapper.*;
import com.fenglin.springboottest.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 神域对决中心业务实现：对决擂台 / 禁墟答题 / 反应试炼
 */
@Service
public class GameServiceImpl implements GameService {

    private static final String[] DUEL_MOVES = {"ATTACK", "DEFEND", "SKILL"};
    private static final int ROOM_CODE_LEN = 6;

    @Autowired private DuelRoomMapper duelRoomMapper;
    @Autowired private DuelRoundMapper duelRoundMapper;
    @Autowired private QuizMapper quizMapper;
    @Autowired private QuizRoomMapper quizRoomMapper;
    @Autowired private QuizAnswerMapper quizAnswerMapper;
    @Autowired private UserGameStatMapper userGameStatMapper;
    @Autowired private UserStageMapper userStageMapper;
    @Autowired private UserMapper userMapper;

    // ===================== 工具方法 =====================

    private User findUser(String username) {
        if (!StringUtils.hasText(username)) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username.trim()));
    }

    private static String displayName(User u) {
        if (u == null) return "未知守夜人";
        return StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername();
    }

    private UserGameStat ensureStat(Long userId) {
        UserGameStat s = userGameStatMapper.selectById(userId);
        if (s == null) {
            s = new UserGameStat(userId);
            userGameStatMapper.insert(s);
        }
        return s;
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ROOM_CODE_LEN; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
            String code = sb.toString();
            Long cnt = duelRoomMapper.selectCount(new LambdaQueryWrapper<DuelRoom>().eq(DuelRoom::getRoomCode, code));
            if (cnt != null && cnt == 0) return code;
        }
        return "RM" + System.currentTimeMillis() % 1000000;
    }

    private static String normalizeTheme(String theme) {
        return "tomjerry".equalsIgnoreCase(theme) ? "tomjerry" : "shenyu";
    }

    // ===================== 对决擂台 =====================

    @Override
    public Result<DuelRoomVO> createDuel(Long userId, DuelCreateRequest req) {
        User opp = findUser(req.getOpponentUsername());
        if (opp == null) return Result.fail("对手代号未觉醒，请确认后重试。");
        if (opp.getId().equals(userId)) return Result.fail("不可向自己发起对决。");

        int target = req.getTargetWins() == null ? 3 : req.getTargetWins();
        target = Math.min(5, Math.max(2, target));

        DuelRoom room = new DuelRoom();
        room.setRoomCode(generateRoomCode());
        room.setChallengerId(userId);
        room.setOpponentId(opp.getId());
        room.setTheme(normalizeTheme(req.getTheme()));
        room.setStatus("WAITING");
        room.setCurrentRound(1);
        room.setP1Wins(0);
        room.setP2Wins(0);
        room.setTargetWins(target);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        duelRoomMapper.insert(room);
        return Result.success("对决邀约已发往「" + displayName(opp) + "」，等待应战。", buildDuelRoomVO(room, userId));
    }

    @Override
    public Result<DuelRoomVO> acceptDuel(Long userId, Long roomId) {
        DuelRoom room = duelRoomMapper.selectById(roomId);
        if (room == null) return Result.fail("对决房间已消散。");
        if (!"WAITING".equals(room.getStatus())) return Result.fail("该对决已开局或已结束。");
        if (!room.getOpponentId().equals(userId)) return Result.fail("仅被挑战者可应战。");

        room.setStatus("PLAYING");
        room.setUpdatedAt(LocalDateTime.now());
        duelRoomMapper.updateById(room);

        DuelRound round = new DuelRound(room.getId(), 1);
        duelRoundMapper.insert(round);
        return Result.success("应战成功，禁墟对决开始！", buildDuelRoomVO(room, userId));
    }

    @Override
    public Result<DuelRoomVO> duelMove(Long userId, Long roomId, DuelMoveRequest req) {
        DuelRoom room = duelRoomMapper.selectById(roomId);
        if (room == null) return Result.fail("对决房间已消散。");
        if (!"PLAYING".equals(room.getStatus())) return Result.fail("对决尚未开始或已结束。");

        String slot = room.getChallengerId().equals(userId) ? "P1" : (room.getOpponentId().equals(userId) ? "P2" : null);
        if (slot == null) return Result.fail("你并非本场对决的参与者。");

        String choice = req.getChoice() == null ? null : req.getChoice().toUpperCase();
        if (!Arrays.asList(DUEL_MOVES).contains(choice)) return Result.fail("出手无效，请选择 攻/防/术。");

        // 当前未结算回合
        DuelRound round = duelRoundMapper.selectOne(
                new LambdaQueryWrapper<DuelRound>()
                        .eq(DuelRound::getRoomId, roomId)
                        .isNull(DuelRound::getWinner)
                        .orderByDesc(DuelRound::getRoundNo)
                        .last("LIMIT 1"));
        if (round == null) {
            round = new DuelRound(room.getId(), room.getCurrentRound());
            duelRoundMapper.insert(round);
        }
        if ("P1".equals(slot)) {
            if (round.getP1Choice() != null) return Result.fail("本回合你已出手，静候对手。");
            round.setP1Choice(choice);
        } else {
            if (round.getP2Choice() != null) return Result.fail("本回合你已出手，静候对手。");
            round.setP2Choice(choice);
        }

        boolean resolved = false;
        if (round.getP1Choice() != null && round.getP2Choice() != null && round.getWinner() == null) {
            String winner = resolveDuel(round.getP1Choice(), round.getP2Choice());
            round.setWinner(winner);
            if ("P1".equals(winner)) room.setP1Wins(room.getP1Wins() + 1);
            else if ("P2".equals(winner)) room.setP2Wins(room.getP2Wins() + 1);
            resolved = true;
        }
        duelRoundMapper.updateById(round);

        if (resolved) {
            if (room.getP1Wins() >= room.getTargetWins() || room.getP2Wins() >= room.getTargetWins()) {
                Long winnerId = room.getP1Wins() >= room.getTargetWins() ? room.getChallengerId() : room.getOpponentId();
                Long loserId = winnerId.equals(room.getChallengerId()) ? room.getOpponentId() : room.getChallengerId();
                room.setStatus("FINISHED");
                room.setWinnerId(winnerId);
                room.setFinishedAt(LocalDateTime.now());
                recordDuel(winnerId, loserId);
                duelRoomMapper.updateById(room);
                return Result.success("对决结束！胜者：「" + displayName(userMapper.selectById(winnerId)) + "」", buildDuelRoomVO(room, userId));
            } else {
                room.setCurrentRound(room.getCurrentRound() + 1);
                duelRoomMapper.updateById(room);
                DuelRound next = new DuelRound(room.getId(), room.getCurrentRound());
                duelRoundMapper.insert(next);
            }
        }
        return Result.success(buildDuelRoomVO(room, userId));
    }

    /** 攻(ATTACK)克术(SKILL)，术( SKILL)克防(DEFEND)，防(DEFEND)克攻(ATTACK)；相同为平局 */
    private static String resolveDuel(String p1, String p2) {
        if (p1.equals(p2)) return "DRAW";
        if (("ATTACK".equals(p1) && "SKILL".equals(p2)) ||
            ("SKILL".equals(p1) && "DEFEND".equals(p2)) ||
            ("DEFEND".equals(p1) && "ATTACK".equals(p2))) {
            return "P1";
        }
        return "P2";
    }

    @Override
    public Result<DuelRoomVO> getDuelRoom(Long userId, Long roomId) {
        DuelRoom room = duelRoomMapper.selectById(roomId);
        if (room == null) return Result.fail("对决房间已消散。");
        return Result.success(buildDuelRoomVO(room, userId));
    }

    @Override
    public Result<List<DuelRoomVO>> myDuels(Long userId) {
        List<DuelRoom> rooms = duelRoomMapper.selectList(
                new LambdaQueryWrapper<DuelRoom>()
                        .and(w -> w.eq(DuelRoom::getChallengerId, userId).or().eq(DuelRoom::getOpponentId, userId))
                        .in(DuelRoom::getStatus, "WAITING", "PLAYING")
                        .orderByDesc(DuelRoom::getUpdatedAt));
        List<DuelRoomVO> vos = rooms.stream().map(r -> buildDuelRoomVO(r, userId)).collect(Collectors.toList());
        return Result.success(vos);
    }

    @Override
    public Result<List<LeaderboardVO>> duelLeaderboard() {
        List<UserGameStat> all = userGameStatMapper.selectList(null);
        List<UserGameStat> list = all.stream()
                .filter(s -> (s.getDuelWins() + s.getDuelLosses() + s.getDuelDraws()) > 0)
                .sorted((a, b) -> {
                    int c = Integer.compare(b.getDuelWins(), a.getDuelWins());
                    if (c != 0) return c;
                    return Integer.compare(b.getDuelWins() - b.getDuelLosses(), a.getDuelWins() - a.getDuelLosses());
                })
                .limit(20)
                .collect(Collectors.toList());
        return Result.success(decorate(list, s -> {
            LeaderboardVO vo = new LeaderboardVO();
            vo.setScore(s.getDuelWins());
            vo.setSub(s.getDuelWins() + "胜 " + s.getDuelLosses() + "负 " + s.getDuelDraws() + "平");
            return vo;
        }));
    }

    private DuelRoomVO buildDuelRoomVO(DuelRoom room, Long userId) {
        DuelRoomVO vo = new DuelRoomVO();
        vo.setId(room.getId());
        vo.setRoomCode(room.getRoomCode());
        vo.setTheme(room.getTheme());
        vo.setStatus(room.getStatus());
        vo.setCurrentRound(room.getCurrentRound());
        vo.setP1Wins(room.getP1Wins());
        vo.setP2Wins(room.getP2Wins());
        vo.setTargetWins(room.getTargetWins());
        vo.setWinnerId(room.getWinnerId());
        if (room.getWinnerId() != null) {
            vo.setWinnerRole(room.getWinnerId().equals(room.getChallengerId()) ? "P1" : "P2");
        }
        vo.setChallengerName(displayName(userMapper.selectById(room.getChallengerId())));
        vo.setOpponentName(displayName(userMapper.selectById(room.getOpponentId())));
        vo.setMyRole(room.getChallengerId().equals(userId) ? "P1" : "P2");

        List<DuelRound> rounds = duelRoundMapper.selectList(
                new LambdaQueryWrapper<DuelRound>().eq(DuelRound::getRoomId, room.getId()).orderByAsc(DuelRound::getRoundNo));
        vo.setRounds(rounds);
        if (!rounds.isEmpty()) {
            DuelRound cur = rounds.get(rounds.size() - 1);
            vo.setMyChoice("P1".equals(vo.getMyRole()) ? cur.getP1Choice() : cur.getP2Choice());
            if (cur.getP1Choice() != null && cur.getP2Choice() != null) {
                vo.setOppChoice("P1".equals(vo.getMyRole()) ? cur.getP2Choice() : cur.getP1Choice());
            }
        }
        return vo;
    }

    private void recordDuel(Long winnerId, Long loserId) {
        UserGameStat w = ensureStat(winnerId);
        w.setDuelWins(w.getDuelWins() + 1);
        userGameStatMapper.updateById(w);
        UserGameStat l = ensureStat(loserId);
        l.setDuelLosses(l.getDuelLosses() + 1);
        userGameStatMapper.updateById(l);
    }

    // ===================== 禁墟答题 =====================

    @Override
    public Result<QuizRoomVO> createQuiz(Long userId, QuizCreateRequest req) {
        User opp = findUser(req.getOpponentUsername());
        if (opp == null) return Result.fail("对手代号未觉醒，请确认后重试。");
        if (opp.getId().equals(userId)) return Result.fail("不可向自己发起答题对战。");

        List<Quiz> pool = quizMapper.selectList(new LambdaQueryWrapper<Quiz>().eq(Quiz::getStatus, 1));
        if (pool.isEmpty()) return Result.fail("神域题库暂为空，无法开局。");
        Collections.shuffle(pool);
        int n = Math.min(req.getCount() == null ? 5 : Math.min(10, Math.max(3, req.getCount())), pool.size());
        List<Long> ids = pool.subList(0, n).stream().map(Quiz::getId).collect(Collectors.toList());
        String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));

        QuizRoom room = new QuizRoom();
        room.setRoomCode(generateRoomCode());
        room.setChallengerId(userId);
        room.setOpponentId(opp.getId());
        room.setTheme(normalizeTheme(req.getTheme()));
        room.setStatus("WAITING");
        room.setQCount(ids.size());
        room.setQuestionIds(idsStr);
        room.setP1Score(0);
        room.setP2Score(0);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        quizRoomMapper.insert(room);
        return Result.success("答题邀约已发往「" + displayName(opp) + "」，等待应战。", buildQuizRoomVO(room, userId));
    }

    @Override
    public Result<QuizRoomVO> acceptQuiz(Long userId, Long roomId) {
        QuizRoom room = quizRoomMapper.selectById(roomId);
        if (room == null) return Result.fail("答题房间已消散。");
        if (!"WAITING".equals(room.getStatus())) return Result.fail("该对战已开局或已结束。");
        if (!room.getOpponentId().equals(userId)) return Result.fail("仅被挑战者可应战。");
        room.setStatus("PLAYING");
        room.setUpdatedAt(LocalDateTime.now());
        quizRoomMapper.updateById(room);
        return Result.success("应战成功，禁墟问答开始！", buildQuizRoomVO(room, userId));
    }

    @Override
    public Result<QuizRoomVO> quizAnswer(Long userId, Long roomId, QuizAnswerRequest req) {
        QuizRoom room = quizRoomMapper.selectById(roomId);
        if (room == null) return Result.fail("答题房间已消散。");
        if (!"PLAYING".equals(room.getStatus())) return Result.fail("对战尚未开始或已结束。");
        if (!room.getChallengerId().equals(userId) && !room.getOpponentId().equals(userId))
            return Result.fail("你并非本场对战的参与者。");

        List<Long> ids = parseIds(room.getQuestionIds());
        if (ids.isEmpty()) return Result.fail("题目数据异常。");

        List<QuizAnswer> mine = quizAnswerMapper.selectList(
                new LambdaQueryWrapper<QuizAnswer>().eq(QuizAnswer::getRoomId, roomId).eq(QuizAnswer::getUserId, userId));
        if (mine.size() >= ids.size()) return Result.fail("你已答完全部题目。");

        Long qid;
        if (req.getQuestionId() != null && ids.contains(req.getQuestionId())) {
            boolean already = mine.stream().anyMatch(a -> a.getQuestionId().equals(req.getQuestionId()));
            qid = already ? ids.get(mine.size()) : req.getQuestionId();
        } else {
            qid = ids.get(mine.size());
        }
        Quiz q = quizMapper.selectById(qid);
        if (q == null) return Result.fail("题目缺失。");

        String choice = (req.getChoice() == null ? "" : req.getChoice().toUpperCase());
        int correct = choice.equals(q.getAnswer()) ? 1 : 0;
        quizAnswerMapper.insert(new QuizAnswer(roomId, userId, qid, choice, correct));

        long myCount = quizAnswerMapper.selectCount(new LambdaQueryWrapper<QuizAnswer>().eq(QuizAnswer::getRoomId, roomId).eq(QuizAnswer::getUserId, userId));
        long oppCount = quizAnswerMapper.selectCount(new LambdaQueryWrapper<QuizAnswer>().eq(QuizAnswer::getRoomId, roomId).eq(QuizAnswer::getUserId, otherId(room, userId)));

        if (myCount == ids.size() && oppCount == ids.size()) {
            int p1 = correctCount(roomId, room.getChallengerId());
            int p2 = correctCount(roomId, room.getOpponentId());
            room.setP1Score(p1);
            room.setP2Score(p2);
            if (p1 > p2) room.setWinnerId(room.getChallengerId());
            else if (p2 > p1) room.setWinnerId(room.getOpponentId());
            else room.setWinnerId(null);
            room.setStatus("FINISHED");
            room.setFinishedAt(LocalDateTime.now());
            quizRoomMapper.updateById(room);
            int p1s = p1, p2s = p2;
            if (room.getWinnerId() != null) {
                recordQuiz(room.getWinnerId(), otherId(room, room.getWinnerId()),
                        room.getWinnerId().equals(room.getChallengerId()) ? p1s : p2s,
                        room.getWinnerId().equals(room.getChallengerId()) ? p2s : p1s);
            } else {
                recordQuizDraw(room.getChallengerId(), p1s, room.getOpponentId(), p2s);
            }
            return Result.success("答题对战结束！", buildQuizRoomVO(room, userId));
        }
        return Result.success(buildQuizRoomVO(room, userId));
    }

    @Override
    public Result<QuizRoomVO> getQuizRoom(Long userId, Long roomId) {
        QuizRoom room = quizRoomMapper.selectById(roomId);
        if (room == null) return Result.fail("答题房间已消散。");
        return Result.success(buildQuizRoomVO(room, userId));
    }

    @Override
    public Result<List<QuizRoomVO>> myQuizzes(Long userId) {
        List<QuizRoom> rooms = quizRoomMapper.selectList(
                new LambdaQueryWrapper<QuizRoom>()
                        .and(w -> w.eq(QuizRoom::getChallengerId, userId).or().eq(QuizRoom::getOpponentId, userId))
                        .in(QuizRoom::getStatus, "WAITING", "PLAYING")
                        .orderByDesc(QuizRoom::getUpdatedAt));
        List<QuizRoomVO> vos = rooms.stream().map(r -> buildQuizRoomVO(r, userId)).collect(Collectors.toList());
        return Result.success(vos);
    }

    @Override
    public Result<List<LeaderboardVO>> quizLeaderboard() {
        List<UserGameStat> all = userGameStatMapper.selectList(null);
        List<UserGameStat> list = all.stream()
                .filter(s -> (s.getQuizWins() + s.getQuizLosses()) > 0)
                .sorted((a, b) -> {
                    int c = Integer.compare(b.getQuizWins(), a.getQuizWins());
                    if (c != 0) return c;
                    return Integer.compare(b.getQuizBestScore(), a.getQuizBestScore());
                })
                .limit(20)
                .collect(Collectors.toList());
        return Result.success(decorate(list, s -> {
            LeaderboardVO vo = new LeaderboardVO();
            vo.setScore(s.getQuizWins());
            vo.setSub(s.getQuizWins() + "胜 " + s.getQuizLosses() + "负 · 最佳 " + s.getQuizBestScore() + "分");
            return vo;
        }));
    }

    private QuizRoomVO buildQuizRoomVO(QuizRoom room, Long userId) {
        QuizRoomVO vo = new QuizRoomVO();
        vo.setId(room.getId());
        vo.setRoomCode(room.getRoomCode());
        vo.setTheme(room.getTheme());
        vo.setStatus(room.getStatus());
        vo.setQCount(room.getQCount());
        vo.setWinnerId(room.getWinnerId());
        if (room.getWinnerId() != null) {
            vo.setWinnerRole(room.getWinnerId().equals(room.getChallengerId()) ? "P1" : "P2");
        }
        vo.setChallengerName(displayName(userMapper.selectById(room.getChallengerId())));
        vo.setOpponentName(displayName(userMapper.selectById(room.getOpponentId())));
        vo.setMyRole(room.getChallengerId().equals(userId) ? "P1" : "P2");

        List<Long> ids = parseIds(room.getQuestionIds());
        Map<Long, Quiz> quizMap = quizMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(Quiz::getId, q -> q));
        List<QuizAnswer> mine = quizAnswerMapper.selectList(
                new LambdaQueryWrapper<QuizAnswer>().eq(QuizAnswer::getRoomId, room.getId()).eq(QuizAnswer::getUserId, userId));
        Map<Long, String> myChoiceMap = mine.stream().collect(Collectors.toMap(QuizAnswer::getQuestionId, QuizAnswer::getChoice));
        long oppCount = quizAnswerMapper.selectCount(
                new LambdaQueryWrapper<QuizAnswer>().eq(QuizAnswer::getRoomId, room.getId()).eq(QuizAnswer::getUserId, otherId(room, userId)));

        boolean finished = "FINISHED".equals(room.getStatus());
        List<QuizQuestionVO> qs = new ArrayList<>();
        for (Long id : ids) {
            Quiz q = quizMap.get(id);
            if (q == null) continue;
            QuizQuestionVO qv = new QuizQuestionVO();
            qv.setId(q.getId());
            qv.setQuestion(q.getQuestion());
            qv.setOptionA(q.getOptionA());
            qv.setOptionB(q.getOptionB());
            qv.setOptionC(q.getOptionC());
            qv.setOptionD(q.getOptionD());
            qv.setMyChoice(myChoiceMap.get(id));
            qv.setAnswered(myChoiceMap.containsKey(id));
            if (finished) qv.setCorrectAnswer(q.getAnswer());
            qs.add(qv);
        }
        vo.setQuestions(qs);
        vo.setMyProgress(mine.size());
        vo.setOppProgress((int) oppCount);
        vo.setMyScore(correctCount(room.getId(), userId));
        vo.setOppScore(correctCount(room.getId(), otherId(room, userId)));
        return vo;
    }

    private List<Long> parseIds(String csv) {
        if (!StringUtils.hasText(csv)) return new ArrayList<>();
        return Arrays.stream(csv.split(","))
                .map(s -> s.trim())
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    private Long otherId(QuizRoom room, Long userId) {
        return room.getChallengerId().equals(userId) ? room.getOpponentId() : room.getChallengerId();
    }

    private int correctCount(Long roomId, Long userId) {
        return Math.toIntExact(quizAnswerMapper.selectCount(
                new LambdaQueryWrapper<QuizAnswer>().eq(QuizAnswer::getRoomId, roomId).eq(QuizAnswer::getUserId, userId).eq(QuizAnswer::getCorrect, 1)));
    }

    private void recordQuiz(Long winnerId, Long loserId, int winnerScore, int loserScore) {
        UserGameStat w = ensureStat(winnerId);
        w.setQuizWins(w.getQuizWins() + 1);
        w.setQuizBestScore(Math.max(w.getQuizBestScore(), winnerScore));
        userGameStatMapper.updateById(w);
        UserGameStat l = ensureStat(loserId);
        l.setQuizLosses(l.getQuizLosses() + 1);
        l.setQuizBestScore(Math.max(l.getQuizBestScore(), loserScore));
        userGameStatMapper.updateById(l);
    }

    private void recordQuizDraw(Long id1, int score1, Long id2, int score2) {
        UserGameStat s1 = ensureStat(id1);
        s1.setQuizBestScore(Math.max(s1.getQuizBestScore(), score1));
        userGameStatMapper.updateById(s1);
        UserGameStat s2 = ensureStat(id2);
        s2.setQuizBestScore(Math.max(s2.getQuizBestScore(), score2));
        userGameStatMapper.updateById(s2);
    }

    // ===================== 反应试炼 =====================

    @Override
    public Result<Map<String, Object>> submitReaction(Long userId, Integer ms) {
        if (ms == null || ms < 80 || ms > 10000) return Result.fail("反应耗时异常，请在 80~10000ms 之间提交。");
        UserGameStat s = ensureStat(userId);
        if (s.getReactionBestMs() == 0 || ms < s.getReactionBestMs()) s.setReactionBestMs(ms);
        s.setReactionPlays(s.getReactionPlays() + 1);
        userGameStatMapper.updateById(s);

        long better = userGameStatMapper.selectCount(
                new LambdaQueryWrapper<UserGameStat>().gt(UserGameStat::getReactionBestMs, 0).lt(UserGameStat::getReactionBestMs, ms));
        int rank = (int) better + 1;

        Map<String, Object> data = new HashMap<>();
        data.put("bestMs", s.getReactionBestMs());
        data.put("plays", s.getReactionPlays());
        data.put("rank", rank);
        return Result.success("反应试炼已记录，最佳 " + s.getReactionBestMs() + "ms，当前排名第 " + rank + "。", data);
    }

    @Override
    public Result<List<LeaderboardVO>> reactionLeaderboard() {
        List<UserGameStat> all = userGameStatMapper.selectList(null);
        List<UserGameStat> list = all.stream()
                .filter(s -> s.getReactionPlays() > 0)
                .sorted(Comparator.comparingInt(UserGameStat::getReactionBestMs))
                .limit(20)
                .collect(Collectors.toList());
        return Result.success(decorate(list, s -> {
            LeaderboardVO vo = new LeaderboardVO();
            vo.setScore(s.getReactionBestMs());
            vo.setSub(s.getReactionPlays() + "次试炼");
                return vo;
            }));
    }

    // ===================== 禁墟试炼 · 境界突破（单人 PvE 闯关） =====================

    private UserStage ensureStage(Long userId) {
        UserStage s = userStageMapper.selectById(userId);
        if (s == null) {
            s = new UserStage(userId);
            userStageMapper.insert(s);
        }
        return s;
    }

    @Override
    public Result<StageProgressVO> stageProgress(Long userId) {
        UserStage st = ensureStage(userId);
        RealmStageConfig.Realm cur = RealmStageConfig.byKey(st.getCurrentRealm());
        if (cur == null) cur = RealmStageConfig.REALMS.get(0);
        int maxIdx = st.getMaxRealm() == null ? -1 : RealmStageConfig.indexOf(st.getMaxRealm());

        StageProgressVO vo = new StageProgressVO();
        vo.setCurrentRealmKey(cur.key);
        vo.setCurrentRealmName(cur.name);
        vo.setCurrentRealmIndex(RealmStageConfig.indexOf(cur.key));
        vo.setTotalRealms(RealmStageConfig.REALMS.size());
        vo.setCurrentQuestions(cur.questions);
        vo.setPassThreshold(RealmStageConfig.passThreshold(cur.questions));
        vo.setRealmDesc(cur.desc);
        vo.setMaxRealmKey(st.getMaxRealm());
        vo.setMaxRealmName(RealmStageConfig.displayName(st.getMaxRealm()));
        vo.setAllCleared(st.getMaxRealm() != null && "shenming".equalsIgnoreCase(st.getMaxRealm()));
        vo.setClears(st.getClears());
        vo.setExp(st.getExp());
        vo.setBestStreak(st.getBestStreak());
        vo.setProgressPercent(maxIdx < 0 ? 0 : (int) Math.round((maxIdx + 1.0) / RealmStageConfig.REALMS.size() * 100));
        return Result.success(vo);
    }

    @Override
    public Result<StageStartVO> stageStart(Long userId) {
        UserStage st = ensureStage(userId);
        RealmStageConfig.Realm cur = RealmStageConfig.byKey(st.getCurrentRealm());
        if (cur == null) return Result.fail("已登临神明之境，试炼圆满。");

        List<Quiz> pool = quizMapper.selectList(new LambdaQueryWrapper<Quiz>().eq(Quiz::getStatus, 1));
        if (pool.isEmpty()) return Result.fail("题库尚未录入，暂无法开启试炼。");

        Collections.shuffle(pool, new Random());
        int n = Math.min(cur.questions, pool.size());
        List<QuizQuestionVO> items = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Quiz q = pool.get(i);
            QuizQuestionVO v = new QuizQuestionVO();
            v.setId(q.getId());
            v.setQuestion(q.getQuestion());
            v.setOptionA(q.getOptionA());
            v.setOptionB(q.getOptionB());
            v.setOptionC(q.getOptionC());
            v.setOptionD(q.getOptionD());
            items.add(v);
        }

        boolean already = st.getMaxRealm() != null
                && RealmStageConfig.indexOf(st.getMaxRealm()) >= RealmStageConfig.indexOf(cur.key);
        StageStartVO vo = new StageStartVO();
        vo.setRealmKey(cur.key);
        vo.setRealmName(cur.name);
        vo.setQuestions(n);
        vo.setPassThreshold(RealmStageConfig.passThreshold(n));
        vo.setDesc(cur.desc);
        vo.setItems(items);
        vo.setAlreadyCleared(already);
        return Result.success("「" + cur.name + "」试炼开启，答对 " + vo.getPassThreshold() + "/" + n + " 题即可突破。", vo);
    }

    @Override
    public Result<StageSubmitVO> stageSubmit(Long userId, StageSubmitRequest req) {
        if (req == null || req.getAnswers() == null || req.getAnswers().isEmpty())
            return Result.fail("未收到作答内容。");

        UserStage st = ensureStage(userId);
        RealmStageConfig.Realm cur = RealmStageConfig.byKey(st.getCurrentRealm());
        if (cur == null) return Result.fail("当前无可挑战的境界。");
        if (req.getRealmKey() != null && !req.getRealmKey().equalsIgnoreCase(cur.key))
            return Result.fail("试炼境界不一致，请重新开启当前试炼。");

        Map<Long, String> answers = new HashMap<>();
        for (StageSubmitRequest.StageAnswer a : req.getAnswers()) {
            if (a.getQuestionId() == null) continue;
            Quiz q = quizMapper.selectById(a.getQuestionId());
            if (q != null && q.getAnswer() != null) answers.put(q.getId(), q.getAnswer().toUpperCase());
        }

        int correct = 0, streak = 0, best = 0;
        for (StageSubmitRequest.StageAnswer a : req.getAnswers()) {
            if (a.getQuestionId() == null || a.getChoice() == null) continue;
            String correctAnswer = answers.get(a.getQuestionId());
            if (correctAnswer != null && correctAnswer.equals(a.getChoice().toUpperCase())) {
                correct++;
                streak++;
                if (streak > best) best = streak;
            } else {
                streak = 0;
            }
        }

        int total = req.getAnswers().size();
        int threshold = RealmStageConfig.passThreshold(Math.min(cur.questions, total));
        boolean passed = correct >= threshold;

        StageSubmitVO vo = new StageSubmitVO();
        vo.setRealmKey(cur.key);
        vo.setRealmName(cur.name);
        vo.setCorrectCount(correct);
        vo.setTotal(total);
        vo.setPassThreshold(threshold);
        vo.setBreakthroughValue(correct * 20);

        st.setExp(st.getExp() + correct * 10);
        if (best > st.getBestStreak()) st.setBestStreak(best);

        if (passed) {
            boolean wasMax = st.getMaxRealm() != null
                    && RealmStageConfig.indexOf(st.getMaxRealm()) >= RealmStageConfig.indexOf(cur.key);
            if (!wasMax) st.setMaxRealm(cur.key);
            st.setClears(st.getClears() + 1);

            RealmStageConfig.Realm nx = RealmStageConfig.next(cur.key);
            if (nx != null) {
                st.setCurrentRealm(nx.key);
                vo.setNextRealmKey(nx.key);
                vo.setNextRealmName(nx.name);
                vo.setMessage("突破成功！「" + cur.name + "」已证，前方是「" + nx.name + "」之境。");
            } else {
                st.setCurrentRealm(cur.key);
                vo.setAllCleared(true);
                vo.setMessage("神明之境已成！你立于万万人前，试炼圆满。");
            }
            vo.setPassed(true);
        } else {
            vo.setPassed(false);
            vo.setMessage("突破未竟，需在「" + cur.name + "」试炼中答对至少 " + threshold + " 题。凝心神，重来一次。");
        }

        st.setUpdatedAt(LocalDateTime.now());
        userStageMapper.updateById(st);

        vo.setClears(st.getClears());
        vo.setExp(st.getExp());
        vo.setBestStreak(st.getBestStreak());
        return Result.success(vo.getMessage(), vo);
    }

    @Override
    public Result<List<LeaderboardVO>> stageLeaderboard() {
        List<UserStage> all = userStageMapper.selectList(null);
        Comparator<UserStage> byRealm = Comparator.comparingInt(
                (UserStage s) -> RealmStageConfig.indexOf(s.getMaxRealm())).reversed();
        Comparator<UserStage> byClears = Comparator.comparingInt(UserStage::getClears).reversed();
        List<UserStage> list = all.stream()
                .sorted(byRealm.thenComparing(byClears))
                .limit(20)
                .collect(Collectors.toList());

        List<LeaderboardVO> out = new ArrayList<>();
        int rank = 1;
        for (UserStage s : list) {
            LeaderboardVO vo = new LeaderboardVO();
            vo.setScore(RealmStageConfig.indexOf(s.getMaxRealm()) + 1);
            vo.setSub("突破 " + s.getClears() + " 次 · " + s.getExp() + " 经验");
            User u = userMapper.selectById(s.getUserId());
            vo.setRank(rank++);
            vo.setUserId(s.getUserId());
            vo.setUsername(u == null ? "" : u.getUsername());
            vo.setNickname(u == null ? "" : (StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername()));
            vo.setAvatar(u == null ? null : u.getAvatar());
            out.add(vo);
        }
        return Result.success(out);
    }

    // ===================== 排行榜通用装饰 =====================

    private List<LeaderboardVO> decorate(List<UserGameStat> stats, java.util.function.Function<UserGameStat, LeaderboardVO> mapper) {
        List<LeaderboardVO> out = new ArrayList<>();
        int rank = 1;
        for (UserGameStat s : stats) {
            LeaderboardVO vo = mapper.apply(s);
            User u = userMapper.selectById(s.getUserId());
            vo.setRank(rank++);
            vo.setUserId(s.getUserId());
            vo.setUsername(u == null ? "" : u.getUsername());
            vo.setNickname(u == null ? "" : (StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername()));
            vo.setAvatar(u == null ? null : u.getAvatar());
            out.add(vo);
        }
        return out;
    }
}
