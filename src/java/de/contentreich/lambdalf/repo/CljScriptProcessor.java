/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package de.contentreich.lambdalf.repo;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.processor.ProcessorExtension;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.processor.BaseProcessor;
import org.alfresco.scripts.ScriptException;
import org.alfresco.scripts.ScriptResourceLoader;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;
import spring.surf.webscript.Script;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CljScriptProcessor extends BaseProcessor implements ScriptProcessor, ScriptResourceLoader/* , InitializingBean*/ {
    private static final Log logger = LogFactory.getLog(CljScriptProcessor.class);
    private static final Log callLogger = LogFactory.getLog(CljScriptProcessor.class.getName() + ".calls");

    private static final String PATH_CLASSPATH = "classpath:";
    private BaseProcessor rhinoProcessor;
    private StoreRef storeRef;
    private String storePath;
    private boolean compile = true;
    private final Map<String, Script> scriptCache = new ConcurrentHashMap<String, Script>(256);

    public void setStoreUrl(String storeRef) {
        this.storeRef = new StoreRef(storeRef);
    }
    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }
    public void setCompile(boolean compile) {
        this.compile = compile;
    }

    public void setRhinoProcessor(BaseProcessor rhinoProcessor) {
        this.rhinoProcessor = rhinoProcessor;
    }
    public void reset() {
        this.scriptCache.clear();
    }

    protected Script compileClojureScript(InputStream is) {
        // this.addProcessorModelExtensions(model);

        try {
            return (Script) clojure.lang.Compiler.load(new InputStreamReader(is));
        } catch (Exception exception) {
            throw new org.springframework.extensions.surf.core.scripts.ScriptException("Error executing Clojure script", exception);
        }
    }

    @Override
    public Object execute(ScriptLocation location, Map<String, Object> model) {
        try {
            // test the cache for a pre-compiled script matching our path
            Script script = null;
            String path = location.getPath();
            if (this.compile && location.isCachable()) {
                script = this.scriptCache.get(path);
            }
            if (script == null) {
                if (logger.isDebugEnabled())
                    logger.debug("Resolving and compiling script path: " + path);
                    script = compileClojureScript(location.getInputStream());
                    if (this.compile && location.isCachable()) {
                        this.scriptCache.put(path, script);
                    }
            }

            String debugScriptName = null;
            if (callLogger.isDebugEnabled()) {
                int i = path.lastIndexOf('/');
                debugScriptName = (i != -1) ? path.substring(i + 1) : path;
            }
            return executeScriptImpl(script, model/*, location.isSecure()*/, debugScriptName);
        } catch (Throwable err) {
            throw new ScriptException("Failed to execute script '" + location.toString() + "': " + err.getMessage(), err);
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#execute(java.lang.String, java.util.Map)
     */
    public Object execute(String location, Map<String, Object> model) {
        return execute(new ClasspathScriptLocation(location), model);
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#execute(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public Object execute(NodeRef nodeRef, QName contentProp, Map<String, Object> model) {
        try {
            if (this.services.getNodeService().exists(nodeRef) == false) {
                throw new AlfrescoRuntimeException("Script Node does not exist: " + nodeRef);
            }

            if (contentProp == null) {
                contentProp = ContentModel.PROP_CONTENT;
            }
            ContentReader cr = this.services.getContentService().getReader(nodeRef, contentProp);
            if (cr == null || cr.exists() == false) {
                throw new AlfrescoRuntimeException("Script Node content not found: " + nodeRef);
            }

            // compile the script based on the node content
            Script script  = compileClojureScript(cr.getContentInputStream());// cx.compileString(resolveScriptImports(cr.getContentString()), nodeRef.toString(), 1, null);

            return executeScriptImpl(script, model/*, false*/, nodeRef.toString());
        } catch (Throwable err) {
            throw new ScriptException("Failed to execute script '" + nodeRef.toString() + "': " + err.getMessage(), err);
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptProcessor#executeString(java.lang.String, java.util.Map)
     */
    public Object executeString(String source, Map<String, Object> model) {
        try {
            // compile the script based on the node content
            Script script = compileClojureScript(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
            return executeScriptImpl(script, model/*, true*/, "string script");
        } catch (Throwable err) {
            throw new ScriptException("Failed to execute supplied script: " + err.getMessage(), err);
        }
    }

    /**
     * Load a script content from the specific resource path.
     *
     * @param resource Resources can be of the form:
     *                 <pre>
     *                 classpath:alfresco/includeme.js
     *                 workspace://SpacesStore/6f73de1b-d3b4-11db-80cb-112e6c2ea048
     *                 /Company Home/Data Dictionary/Scripts/includeme.js
     *                 </pre>
     * @return the content from the resource, null if not recognised format
     * @throws AlfrescoRuntimeException on any IO or ContentIO error
     */
    public String loadScriptResource(String resource) {
        String result = null;

        if (resource.startsWith(PATH_CLASSPATH)) {
            try {
                // Load from classpath
                String scriptClasspath = resource.substring(PATH_CLASSPATH.length());
                URL scriptResource = getClass().getClassLoader().getResource(scriptClasspath);
                if (scriptResource == null && scriptClasspath.startsWith("/")) {
                    // The Eclipse classloader prefers alfresco/foo to /alfresco/foo, try that
                    scriptResource = getClass().getClassLoader().getResource(scriptClasspath.substring(1));
                }
                if (scriptResource == null) {
                    throw new AlfrescoRuntimeException("Unable to locate included script classpath resource: " + resource);
                }
                InputStream stream = scriptResource.openStream();
                if (stream == null) {
                    throw new AlfrescoRuntimeException("Unable to load included script classpath resource: " + resource);
                }
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                FileCopyUtils.copy(stream, os);  // both streams are closed
                byte[] bytes = os.toByteArray();
                // create the string from the byte[] using encoding if necessary
                result = new String(bytes, "UTF-8");
            } catch (IOException err) {
                throw new AlfrescoRuntimeException("Unable to load included script classpath resource: " + resource);
            }
        } else {
            NodeRef scriptRef;
            if (resource.startsWith("/")) {
                // resolve from default SpacesStore as cm:name based path
                // TODO: remove this once FFS correctly allows name path resolving from store root!
                NodeRef rootNodeRef = this.services.getNodeService().getRootNode(this.storeRef);
                List<NodeRef> nodes = this.services.getSearchService().selectNodes(
                        rootNodeRef, this.storePath, null, this.services.getNamespaceService(), false);
                if (nodes.size() == 0) {
                    throw new AlfrescoRuntimeException("Unable to find store path: " + this.storePath);
                }
                StringTokenizer tokenizer = new StringTokenizer(resource, "/");
                List<String> elements = new ArrayList<String>(6);
                if (tokenizer.hasMoreTokens()) {
                    tokenizer.nextToken();
                }
                while (tokenizer.hasMoreTokens()) {
                    elements.add(tokenizer.nextToken());
                }
                try {
                    FileInfo fileInfo = this.services.getFileFolderService().resolveNamePath(nodes.get(0), elements);
                    scriptRef = fileInfo.getNodeRef();
                } catch (FileNotFoundException err) {
                    throw new AlfrescoRuntimeException("Unable to load included script repository resource: " + resource);
                }
            } else {
                scriptRef = new NodeRef(resource);
            }

            // load from NodeRef default content property
            try {
                ContentReader cr = this.services.getContentService().getReader(scriptRef, ContentModel.PROP_CONTENT);
                if (cr == null || cr.exists() == false) {
                    throw new AlfrescoRuntimeException("Included Script Node content not found: " + resource);
                }
                result = cr.getContentString();
            } catch (ContentIOException err) {
                throw new AlfrescoRuntimeException("Unable to load included script repository resource: " + resource);
            }
        }

        return result;
    }

    /**
     * Execute the supplied script content. Adds the default data model and custom configured root
     * objects into the root scope for access by the script.
     *
     * @param script          The script to execute.
     * @param model           Data model containing objects to be added to the root scope.
     *                        param secure        True if the script is considered secure and may access java.* libs directly
     * @param debugScriptName To identify the script in debug messages.
     * @return result of the script execution, can be null.
     * @throws AlfrescoRuntimeException
     */
    private Object executeScriptImpl(Script script, Map<String, Object> model/*, boolean secure*/, String debugScriptName)
            throws AlfrescoRuntimeException {
        long startTime = 0;
        if (callLogger.isDebugEnabled()) {
            callLogger.debug(debugScriptName + " Start");
            startTime = System.nanoTime();
        }
        try {
            for (ProcessorExtension ex : this.processorExtensions.values()) {
                model.put(ex.getExtensionName(), ex);
            }
            /* Can't embed object in code, maybe print-dup not defined:
            for (ProcessorExtension ex : this.rhinoProcessor.getProcessorExtensions()) // Ugh! Hijack from JS.
            {
                model.put(ex.getExtensionName(), ex);
            }
            */
            return script.run(model,  this.processorExtensions);
        } catch (Throwable err) {
            if (callLogger.isDebugEnabled()) {
                callLogger.debug(debugScriptName + " Exception", err);
            }
            throw new AlfrescoRuntimeException(err.getMessage(), err);
        } finally {
            if (callLogger.isDebugEnabled()) {
                long endTime = System.nanoTime();
                callLogger.debug(debugScriptName + " End " + (endTime - startTime) / 1000000 + " ms");
            }
        }
    }
}
