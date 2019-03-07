package xdean.mini.boardgame.server.security.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import xdean.jex.log.Logable;
import xdean.mini.boardgame.server.handler.DispatchLoginHandler;
import xdean.mini.boardgame.server.security.OpenIdAuthProvider;
import xdean.mini.boardgame.server.security.model.LoginResponse;
import xdean.mini.boardgame.server.security.model.SignUpResponse;

@RestController
@Api(tags = "User/Auth")
public class UserAuthEndpoint implements Logable {

  private @Inject DispatchLoginHandler loginHandler;
  private @Inject UserDetailsManager userDetailsManager;
  private @Inject AuthenticationManager authenticationManager;
  private @Inject PasswordEncoder passwordEncoder;
  private @Autowired(required = false) List<OpenIdAuthProvider> providers = Collections.emptyList();

  @ApiOperation("Sign up a new user")
  @PostMapping(path = "/sign-up")
  public SignUpResponse signUp(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(name = "username", required = false) String username,
      @RequestParam(name = "password", required = false) String password) {
    if (username == null || password == null) {
      return SignUpResponse.builder()
          .success(false)
          .message("Please input username and password")
          .errorCode(SignUpResponse.INPUT_USERNAME_PASSWORD)
          .build();
    }
    if (!username.matches("^(?!_)(?!.*?_$)[a-zA-Z0-9_]+$")) {
      return SignUpResponse.builder()
          .success(false)
          .message("Username should be letter and/or number")
          .errorCode(SignUpResponse.ILLEGAL_USERNAME)
          .build();
    }
    if (!password.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$")) {
      return SignUpResponse.builder()
          .success(false)
          .message("Password should be letter and number")
          .errorCode(SignUpResponse.ILLEGAL_PASSWORD)
          .build();
    }
    boolean exist = userDetailsManager.userExists(username);
    if (exist) {
      return SignUpResponse.builder()
          .success(false)
          .message("User name exist")
          .errorCode(SignUpResponse.USERNAME_EXIST)
          .build();
    }
    UserDetails u = User.builder()
        .username(username)
        .password(password)
        .passwordEncoder(passwordEncoder::encode)
        .authorities("USER")
        .build();
    userDetailsManager.createUser(u);
    authenticateUserAndSetSession(username, password, request, response);
    return SignUpResponse.builder()
        .success(true)
        .message("Sign up success")
        .build();
  }

  // @ApiOperation("Login with username and password")
  // @PostMapping(path = "/login")
  public LoginResponse login(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(name = "username", required = false) String username,
      @RequestParam(name = "password", required = false) String password) {
    if (username == null || password == null) {
      return LoginResponse.builder()
          .errorCode(LoginResponse.BAD_INPUT)
          .message("Please provide both username and password.")
          .build();
    }
    try {
      authenticateUserAndSetSession(username, password, request, response);
      return LoginResponse.builder()
          .message("Login Success")
          .build();
    } catch (AuthenticationException e) {
      return LoginResponse.builder()
          .errorCode(LoginResponse.BAD_CREDENTIALS)
          .message("Bad Credentials")
          .build();
    }
  }

  @ApiOperation("Login with openid")
  @PostMapping(path = "/login-openid")
  public LoginResponse loginOpenId(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(name = "token", required = false) String token,
      @RequestParam(name = "provider", required = false) String provider) {
    if (token == null || provider == null) {
      return LoginResponse.builder()
          .errorCode(LoginResponse.BAD_INPUT)
          .message("Please provide both provider and token.")
          .build();
    }
    List<OpenIdAuthProvider> findProviders = providers.stream().filter(p -> p.name().equals(provider))
        .collect(Collectors.toList());
    if (findProviders.isEmpty()) {
      return LoginResponse.builder()
          .errorCode(LoginResponse.PROVIDER_NOT_FOUND)
          .message("There is no provider support: " + provider)
          .build();
    }
    List<AuthenticationException> errors = new ArrayList<>();
    for (int i = 0; i < findProviders.size(); i++) {
      OpenIdAuthProvider p = findProviders.get(i);
      try {
        String result = p.attemptAuthentication(token);
        if (result != null) {
          UserDetails u = User.builder()
              .username(result + "@" + provider)
              .password(result)
              .passwordEncoder(passwordEncoder::encode)
              .authorities("USER")
              .build();
          if (!userDetailsManager.userExists(u.getUsername())) {
            userDetailsManager.createUser(u);
          }
          authenticateUserAndSetSession(u.getUsername(), result, request, response);
          return LoginResponse.builder()
              .message("Login Success")
              .build();
        }
      } catch (AuthenticationException e) {
        trace("Fail to authenticate: " + token, e);
        errors.add(e);
      }
    }
    SecurityContextHolder.clearContext();
    return LoginResponse.builder()
        .errorCode(LoginResponse.BAD_CREDENTIALS)
        .message("Bad Credentials:\n" + errors.stream().map(e -> "- " + e.getMessage()).collect(Collectors.joining("\n")))
        .build();
  }

  private void authenticateUserAndSetSession(String username, String rawPassword, HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, rawPassword);

    request.getSession();

    token.setDetails(new WebAuthenticationDetails(request));
    Authentication authenticatedUser = authenticationManager.authenticate(token);

    SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
    loginHandler.afterSuccess(request, response, username);
  }
}