package webservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    @Value("${jwt.username}")
    private String username;

    @Value("${jwt.secret.key}")
    private String secret;

    public String getUsernameFromToken(String jwtToken) {
        return getClaimFromToken(jwtToken, Claims::getSubject);
    }

    private <T> T getClaimFromToken(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(jwtToken);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String jwtToken) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken).getBody();
    }

    private Claims getExpirationFromToken(String jwtToken) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken).getBody();
    }

    private boolean isTokenExpired(String jwtToken) {
        final Date expiration = getClaimFromToken(jwtToken, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 5*60*60 * 1000))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public boolean validateToken(String jwtToken, UserDetails userDetails) {
        final String userName = getUsernameFromToken(jwtToken);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken);
    }
}
