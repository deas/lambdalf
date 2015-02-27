package de.contentreich.lambdalf.repo;

import java.util.List;
import clojure.java.api.Clojure;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by deas on 1/16/15.
 * Can't reify abstract class, but delegation is not that bad
 * http://dev.clojure.org/jira/browse/CLJ-1255
 */
public class CljAction extends ActionExecuterAbstractBase {
    private static Logger logger = LoggerFactory.getLogger(CljAction.class);
    // By name because function can be redefined
    String ns = null;
    String fn = null;
    List<ParameterDefinition> paramDefs = null;

    public String getNs() {
        return ns;
    }

    public void setNs(String ns) {
        this.ns = ns;
    }

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }

    public void setParamDefs(List<ParameterDefinition> paramDefs) {
        this.paramDefs = paramDefs;
    }

    @Override
    protected void executeImpl(Action action, NodeRef nodeRef) {
        logger.info("Executing action {}/{} against {}", ns, fn, nodeRef);
        Object sym = Clojure.var("clojure.core", "symbol").invoke(ns);
        Clojure.var("clojure.core", "require").invoke(sym);
        Clojure.var(ns, fn).invoke(action, nodeRef);

    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> list) {
        if (this.paramDefs != null) {
            list.addAll(this.paramDefs);
        }
    }

    // Abstract base registers, ActionServiceImpl does not have unregister functionality

}
