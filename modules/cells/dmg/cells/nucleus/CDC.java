package dmg.cells.nucleus;

import java.io.Serializable;

import org.slf4j.MDC;
import org.dcache.commons.util.NDC;

import dmg.util.TimebasedCounter;

/**
 * The Cell Diagnostic Context, a utility class for working with the
 * Log4j NDC and MDC.
 *
 * Notice that the MDC is automatically inherited by child threads
 * upon creation. Thus the domain, cell name, and session identifier
 * is inherited. The same is not true for the NDC, which needs to be
 * explicitly copied. Special care must be taken when using shared
 * thread pools, as this can span several cells. For those the MDC
 * should be initialised per task, not per thread.
 *
 * The class serves two purposes:
 *
 * - It contains a number of static methods for manipulating the MDC
 *   and NDC.
 *
 * - CDC instances capture the Cells related MDC values and the NDC.
 *   These can be apply to other threads. This is useful when using
 *   worker threads that should inherit the context of the task
 *   creation point.
 *
 */
public class CDC
{
    public final static String MDC_DOMAIN = "cells.domain";
    public final static String MDC_CELL = "cells.cell";
    public final static String MDC_SESSION = "cells.session";

    private final static TimebasedCounter _sessionCounter =
        new TimebasedCounter();

    private final NDC _ndc;
    private final String _session;
    private final String _cell;
    private final String _domain;

    /**
     * Captures the cells diagnostic context of the calling thread.
     */
    public CDC()
    {
        _session = MDC.get(MDC_SESSION);
        _cell = MDC.get(MDC_CELL);
        _domain = MDC.get(MDC_DOMAIN);
        _ndc = NDC.cloneNdc();
    }

    /**
     * Wrapper around <code>MDC.put</code> and
     * <code>MDC.remove</code>. <code>value</code> is allowed to e
     * null.
     */
    static private void setMdc(String key, String value)
    {
        if (value != null) {
            MDC.put(key, value);
        } else {
            MDC.remove(key);
        }
    }

    /**
     * Applies the cells diagnostic context to the calling thread.  If
     * <code>clone</code> is false, then the <code>apply</code> can
     * only be called once for this CDC. If <code>clone</code> is
     * true, then the CDC may be applied several times, however the
     * operation is more expensive.
     */
    public void apply()
    {
        setMdc(MDC_DOMAIN, _domain);
        setMdc(MDC_CELL, _cell);
        setMdc(MDC_SESSION, _session);
        if (_ndc == null) {
            NDC.clear();
        } else {
            NDC.set(_ndc);
        }
    }

    /**
     * Returns the session identifier stored in the MDC of the calling
     * thread.
     */
    static public String getSession()
    {
        return MDC.get(MDC_SESSION);
    }

    /**
     * Sets the session in the MDC for the calling thread.
     *
     * @param session Session identifier.
     */
    static public void setSession(String session)
    {
        setMdc(MDC_SESSION, session);
    }

    /**
     * Creates and sets a new session identifier. The session
     * identifier is based on static TimedbasedCounter and is thus
     * with a high probability unique in this JVM.
     *
     * As long as each JVM uses its own prefix, the identifier will be
     * unique over multible JVMs.
     *
     * @see dmg.util.TimedbasedCounter
     */
    static public void createSession(String prefix)
    {
        setSession(prefix + "-" + _sessionCounter.next());
    }

    /**
     * Creates and sets a new session identifier. Uses the domain name
     * stored in he MDC of the calling thread as a prefix. This
     * guarantees uniqueness among a cells system (but see
     * documentation of TimebasedCounter for the limits).
     *
     * @throws IllegalStateException if domain name is not set in MDC
     * @see dmg.util.TimedbasedCounter
     */
    static public void createSession()
    {
        Object domain = MDC.get(MDC_DOMAIN);
        if (domain == null)
            throw new IllegalStateException("Missing domain name in MDC");

        createSession(domain.toString());
    }

    /**
     * Setup the cell diagnostic context of the calling
     * thread. Threads created from the calling thread automatically
     * inherit this information.
     */
    static public void setCellsContext(CellNucleus cell)
    {
        setCellsContext(cell.getCellName(), cell.getCellDomainName());
    }

    /**
     * Setup the cell diagnostic context of the calling
     * thread. Threads created from the calling thread automatically
     * inherit this information.
     */
    static public void setCellsContext(String cellName, String domainName)
    {
        setMdc(MDC_CELL, cellName);
        setMdc(MDC_DOMAIN, domainName);
    }

    /**
     * Returns the message description added to the NDC as part of the
     * message related diagnostic context.
     */
    static protected String getMessageContext(CellMessage envelope)
    {
        StringBuilder s = new StringBuilder();

        Object session = envelope.getSession();
        if (session != null) {
            s.append(session).append(' ');
        }

        s.append(envelope.getSourceAddress().getCellName());

        Object msg = envelope.getMessageObject();
        if (msg instanceof HasDiagnosticContext) {
            String context =
                ((HasDiagnosticContext) msg).getDiagnosticContext();
            s.append(' ').append(context);
        }

        return s.toString();
    }

    /**
     * Setup message related diagnostic context for the calling
     * thread. Adds information about a message to the MDC and NDC.
     *
     * @see clearMessageContext
     */
    static public void setMessageContext(CellMessage envelope)
    {
        Object session = envelope.getSession();
        NDC.push(getMessageContext(envelope));
        setMdc(MDC_SESSION, (session == null) ? null : session.toString());
    }

    /**
     * Clears the diagnostic context entries added by
     * <code>setMessageContext</code>.  For this to work, the NDC has
     * to be in the same state as when <code>setMessageContext</code>
     * returned.
     *
     * @see setMessageContext
     */
    static public void clearMessageContext()
    {
        MDC.remove(MDC_SESSION);
        NDC.pop();
    }

    /**
     * Clears all cells related MDC entries and the NDC.
     */
    static public void clear()
    {
        MDC.remove(MDC_DOMAIN);
        MDC.remove(MDC_CELL);
        MDC.remove(MDC_SESSION);
        NDC.clear();
    }
}