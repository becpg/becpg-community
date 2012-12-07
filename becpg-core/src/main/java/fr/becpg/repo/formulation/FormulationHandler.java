package fr.becpg.repo.formulation;

/**
 * A handler in the chain of responsibility scheme.
 * @param <T> Any type passed as context information.
 */
public interface FormulationHandler<T> {
 
    /**
     * Implements processing element logic in a chain.
     * @param context Any type passed as context information.
     * @return <code>true</code>, if the request was handled and the chain of
     * execution should stop,<br>
     * <code>false</code> if the request should be passed down the chain.
     */
    boolean process(T context) throws FormulateException;
 
    /**
     * Sets next handler for the current one; called in post-processing
     */
    void setNextHandler(FormulationHandler<T> next);
 
    /**
     * Entry point to the handler chain
     * @throws FormulateException 
     */
    void start(T context) throws FormulateException;
}
