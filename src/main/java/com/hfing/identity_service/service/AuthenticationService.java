package com.hfing.identity_service.service;
import com.hfing.identity_service.dto.request.IntrospectRequest;
import com.hfing.identity_service.dto.request.LogoutRequest;
import com.hfing.identity_service.dto.request.RefreshRequest;
import com.hfing.identity_service.dto.response.AuthenticationResponse;
import com.hfing.identity_service.dto.response.IntrospectResponse;
import com.hfing.identity_service.entity.InvalidatedToken;
import com.hfing.identity_service.entity.User;
import com.hfing.identity_service.exception.ErrorCode;
import com.hfing.identity_service.dto.request.AuthenticationRequest;
import com.hfing.identity_service.exception.AppException;
import com.hfing.identity_service.repository.InvalidtedTokenRepository;
import com.hfing.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.text.ParseException;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.util.CollectionUtils;


@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Service
@Slf4j
public class AuthenticationService {
    UserRepository userRepository;
    InvalidtedTokenRepository invalidtedTokenRepository;

    @NonFinal
    @Value("${jwt.signingKey}")
    protected String SIGNING_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refresh-duration}")
    protected long REFRESH_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean authenticated = passwordEncoder.matches(request.getPassword(),
                user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .isAuthenticated(true)
                .build();
    }

public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid= true;
        try{
            verifyToken(token, false);
        }
        catch (Exception ex){
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }


    public void logout(LogoutRequest token) throws ParseException, JOSEException {
        try{
            var signedJWT = verifyToken(token.getToken(), true);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jti)
                    .expiryTime(expirationTime)
                    .build();

            invalidtedTokenRepository.save(invalidatedToken);
        }catch (AppException ex){
            log.error("Token already expired");
        }



    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expirationTime)
                .build();

        invalidtedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .isAuthenticated(true)
                .token(token)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNING_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = (isRefresh)
                ? new Date (signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli())
        :signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if (!(verified && expirationTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidtedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);


        return signedJWT;
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hfing.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNING_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role ->{stringJoiner.add("ROLE_" + role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions())){
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
                }
            });

        }
        return stringJoiner.toString();
    }
}
