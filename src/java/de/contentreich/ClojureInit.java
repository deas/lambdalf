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
    protected String initNs;
    protected String initFn;
    protected List arguments;
    protected Object initVal;

    public void setArguments(List arguments) {
        this.arguments = arguments;
    }

    public List getArguments() {
        return this.arguments;
    }

    public void setInitNs(String initNs) {
        this.initNs = initNs;
    }
    public String getInitNs() {
        return this.initNs;
    }

    public void setInitFn(String initFn) {
        this.initFn = initFn;
    }

    public String getInitFn() {
        return this.initFn;
    }

    // Meta-Meta !
    public void init() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read(initNs));
        logger.info("Invoking " + this.initNs + "/" + this.initFn + " ...");
        IFn fn = Clojure.var(this.initNs, this.initFn);
        int cnt = arguments != null ? arguments.size() : 0;
        Class[] paramTypes = new Class[cnt];
        for (int i=0; i<cnt; i++) {
            paramTypes[i] = Object.class;
        }
        try {
            Method method = fn.getClass().getMethod("invoke", paramTypes);
            Object[] args = this.arguments != null ? this.arguments.toArray() : new Object[0];
            this.initVal = method.invoke(fn, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getInitVal() {
        return this.initVal;
    }
}
