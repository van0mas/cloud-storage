package org.example.cloudstorage.resolver;

import org.example.cloudstorage.annotation.CurrentUser;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.security.UserDetailsImpl;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) return null;

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) return null;

        Class<?> targetType = parameter.getParameterType();

        if (targetType.equals(Long.class)) {
            return userDetails.getUser().getId();
        }

        if (targetType.equals(User.class)) {
            return userDetails.getUser();
        }

        return userDetails;
    }
}
