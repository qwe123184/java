package com.fenglin.springboottest.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 工具：签发与校验守夜人令牌（无状态，重启仍有效）。
 * 双令牌模型：
 *  - access token：短期（默认 15 分钟），用于受保护接口（如 /api/user/me）；
 *  - refresh token：长期（默认 7 天），仅用于 /api/user/refresh 换发新令牌。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /** refresh token 有效期（秒），默认 7 天 */
    @Value("${jwt.expiration:604800}")
    private long refreshExpirationSeconds;

    /** access token 有效期（秒），默认 15 分钟 */
    @Value("${jwt.access-expiration:900}")
    private long accessExpirationSeconds;

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret);
    }

    private JWTVerifier verifier() {
        return JWT.require(algorithm()).build();
    }

    private String generate(Long userId, String username, String type, long expSec) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expSec * 1000L);
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("username", username)
                .withClaim(CLAIM_TYPE, type)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(algorithm());
    }

    public String generateAccessToken(Long userId, String username) {
        return generate(userId, username, TYPE_ACCESS, accessExpirationSeconds);
    }

    public String generateRefreshToken(Long userId, String username) {
        return generate(userId, username, TYPE_REFRESH, refreshExpirationSeconds);
    }

    /** 校验任意有效令牌并提取 userId；签名错误 / 已过期 / 格式非法返回 null */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(verifier().verify(token).getSubject());
        } catch (JWTVerificationException | NumberFormatException e) {
            return null;
        }
    }

    private String getClaim(String token, String claim) {
        try {
            return verifier().verify(token).getClaim(claim).asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(getClaim(token, CLAIM_TYPE));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(getClaim(token, CLAIM_TYPE));
    }
}
