package fr.becpg.repo.formulation;

public abstract class FormulationBaseHandler<T> implements FormulationHandler<T> {
 
    private FormulationHandler<T> nextHandler;
 
    public void setNextHandler(FormulationHandler<T> next) {
        nextHandler = next;
    }
 
    public void start(T context) throws FormulateException {
        // Calls "this" handler logic
        boolean processed = process(context);
        if (! processed && nextHandler != null)
            // Note that next handler's method is called through "start", not "process"
            nextHandler.start(context);
    }
}
