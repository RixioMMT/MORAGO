package com.habsida.morago.resolver;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.habsida.morago.exceptions.ExceptionGraphql;
import com.habsida.morago.model.entity.User;
import com.habsida.morago.model.inputs.LoginUserInput;
import com.habsida.morago.model.inputs.RegisterTranslatorConsultantInput;
import com.habsida.morago.model.inputs.RegisterUserInput;
import com.habsida.morago.serviceImpl.AuthenticationService;
import com.habsida.morago.serviceImpl.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationMutationResolver implements GraphQLMutationResolver {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public String signUpAsUser(RegisterUserInput registerUserInput) throws ExceptionGraphql {
        User user = authenticationService.signUpAsUser(registerUserInput);
        LoginUserInput loginUserInput = new LoginUserInput();
        loginUserInput.setPhone(user.getPhone());
        loginUserInput.setPassword(registerUserInput.getPassword());
        User authenticatedUser = authenticationService.logIn(loginUserInput);
        return jwtService.generateToken(authenticatedUser);
    }

    public String signUpAsTranslator(RegisterTranslatorConsultantInput registerTranslatorConsultantInput) throws ExceptionGraphql {
        User user = authenticationService.signUpAsTranslator(registerTranslatorConsultantInput);
        LoginUserInput loginUserInput = new LoginUserInput();
        loginUserInput.setPhone(user.getPhone());
        loginUserInput.setPassword(registerTranslatorConsultantInput.getPassword());
        User authenticatedUser = authenticationService.logIn(loginUserInput);
        return jwtService.generateToken(authenticatedUser);
    }

    public String logIn(LoginUserInput loginUserInput) {
        User authenticatedUser = authenticationService.logIn(loginUserInput);
        return jwtService.generateToken(authenticatedUser);
    }

    public String signUpAsConsultant(RegisterTranslatorConsultantInput registerTranslatorConsultantInput) throws ExceptionGraphql {
        User user = authenticationService.signUpAsConsultant(registerTranslatorConsultantInput);
        LoginUserInput loginUserInput = new LoginUserInput();
        loginUserInput.setPhone(user.getPhone());
        loginUserInput.setPassword(registerTranslatorConsultantInput.getPassword());
        User authenticatedUser = authenticationService.logIn(loginUserInput);
        return jwtService.generateToken(authenticatedUser);
    }
}
