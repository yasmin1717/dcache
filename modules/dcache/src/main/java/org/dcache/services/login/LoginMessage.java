package org.dcache.services.login;

import javax.security.auth.Subject;

import java.util.Collections;
import java.util.Set;

import diskCacheV111.vehicles.Message;

import org.dcache.auth.attributes.LoginAttribute;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Requests a login by a login cell.
 */
public class LoginMessage extends Message
{
    private static final long serialVersionUID = -162269433527077293L;

    private Set<LoginAttribute> _loginAttributes =
        Collections.emptySet();
    private Set<Object> _publicCredentials;
    private Set<Object> _privateCredentials;

    public LoginMessage(Subject subject)
    {
        setSubject(subject);
    }

    @Override
    public void setSubject(Subject subject)
    {
        /* Subject does not serialize its credentials, so we need to
         * take care of those ourselves.
         */
        super.setSubject(subject);
        _publicCredentials = subject.getPublicCredentials();
        _privateCredentials = subject.getPrivateCredentials();
    }

    @Override
    public Subject getSubject()
    {
        return new Subject(false,
                           super.getSubject().getPrincipals(),
                           _publicCredentials,
                           _privateCredentials);
    }

    public void setLoginAttributes(Set<LoginAttribute> loginAttributes)
    {
        _loginAttributes = checkNotNull(loginAttributes);
    }

    public Set<LoginAttribute> getLoginAttributes()
    {
        return _loginAttributes;
    }
}
