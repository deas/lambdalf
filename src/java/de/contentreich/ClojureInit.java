package de.contentreich;

import java.util.List;
import clojure.lang.IFn;
import clojure.java.api.Clojure;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Quick hack to get rid of the AOT pita
 */
public class ClojureInit {
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected String namespace;
    protected String function;
    protected List arguments;

    public void setArguments(List arguments) {
        this.arguments = arguments;
    }

    public List getArguments() {
        return this.arguments;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    public String getNamespace() {
        return this.namespace;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getFunction() {
        return this.function;
    }

    // Meta-Meta !
    public void init() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read(this.namespace));
        logger.info("Invoking " + this.namespace + "/" + this.function + " ...");
        IFn fn = Clojure.var(this.namespace, this.function);
        int cnt = arguments != null ? arguments.size() : 0;
        Class[] paramTypes = new Class[cnt];
        for (int i=0; i<cnt; i++) {
            paramTypes[i] = Object.class;
        }
        try {
            Method method = fn.getClass().getMethod("invoke", paramTypes);
            Object[] args = this.arguments != null ? this.arguments.toArray() : new Object[0];
            method.invoke(fn, args);// We do not want the ref to the retval!
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
