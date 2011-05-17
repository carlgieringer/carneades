/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.fokus.carneades;

import org.fokus.carneades.clojureutil.NS;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateful;
import org.fokus.carneades.Fn.AskException;
import org.fokus.carneades.Fn.AskHandler;
import org.fokus.carneades.Fn.Askable;
import org.fokus.carneades.api.CarneadesMessage;
import org.fokus.carneades.api.MessageType;
import org.fokus.carneades.api.Statement;
import org.fokus.carneades.clojureutil.ClojureUtil;
import org.fokus.carneades.lkif.LKIFHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stb
 */

@Stateful
public class CarneadesServiceManager implements CarneadesService{

    private static final Logger log = LoggerFactory.getLogger(CarneadesServiceManager.class);

    private Map state = null;
    private List<Statement> answers = new ArrayList<Statement>();
    private List goal = null;
    private Statement query = null;
    private List lastQuestion = null;
    private IFn toEngine = null;
    private IFn fromEngine = null;

    public CarneadesServiceManager() {
        log.info("constructing stateful session bean");
        try {
            // loading scripts
            log.info("loading lkif.clj");
            RT.loadResourceScript("carneades/engine/lkif.clj");
            log.info("loading ask.clj");
            RT.loadResourceScript("carneades/engine/ask.clj");
            // RT.loadResourceScript("carneades/engine/argument-search.clj");
            log.info("loading shell.clj");
            RT.loadResourceScript("carneades/engine/shell.clj");
            //log.info("loading viewer.clj");
            //RT.loadResourceScript("carneades/ui/diagram/viewer.clj");
            log.info("loading viewer2.clj");
            RT.loadResourceScript("carneades/ui/diagram/graphvizviewer.clj");
            log.info("loading json.clj");
            RT.loadResourceScript("clojure/contrib/json.clj");
            log.info("loading clojure files finished");
        } catch(Exception e) {
            log.error(e.toString());
        }
    }

    public CarneadesMessage getSVGFromGraph(String argGraph) {        
        
        CarneadesMessage cm = new CarneadesMessage();
        
        try {
            
            log.info("get svg from lkif") ;
            
            // importing lkif
            log.info("loading lkif");
            Map lkif = (Map) RT.var(NS.LKIF, "lkif-import").invoke(argGraph);
            log.info("get arg graphs");
            List argGraphs = (List) lkif.get(Keyword.intern("ags"));
            log.info("get first graph");
            Map ag = (Map)argGraphs.get(0);
            
            int c = 0;
            String prepath = "/tmp/";
            String svgPath = prepath + "graph0.svg";
            File f = new File(svgPath);
            while(f.exists()) {
                c++;
                svgPath = prepath + "graph" + Integer.toString(c) + ".svg";            
                f = new File(svgPath);
            } 
            
            log.info("svg path : "+svgPath);
            
            Object stmtStr = RT.var(NS.STATEMENT, "statement-formatted");
            
            RT.var(NS.VIEWER, "gen-image").invoke(ag, stmtStr, svgPath);
            log.info("image saved");
            
            cm.setAG(svgPath);
            cm.setType(MessageType.SVG);
            
        } catch (Exception e) {
            handleStandardError(e);
        } finally {
            return cm;
        }
    }
    
    

    public CarneadesMessage getPolicySchemes(String argGraph) {
        
        CarneadesMessage cm = new CarneadesMessage();
        
        try {
            
            // importing lkif
            log.info("loading lkif");
            Map lkif = (Map) RT.var(NS.LKIF, "lkif-import").invoke(argGraph);
            log.info("get arg graphs");
            List argGraphs = (List) lkif.get(Keyword.intern("ags"));
            log.info("get first graph");
            Map ag = (Map)argGraphs.get(0);
            
            log.info("get argument map");
            Map argumentMap = (Map)ag.get(Keyword.intern("arguments"));
            log.info("get arguments");
            List arguments = (List)RT.var(NS.CORE, "vals").invoke(argumentMap);
            
            List<String> policySchemes = new ArrayList<String>();
            // log.info("start argument loop");
            for(Object o : arguments) {
                //log.info(o.toString());
                Map arg = (Map)o;
                String scheme = (String)arg.get(Keyword.intern("scheme"));
                // log.info("scheme : "+scheme);
                if(scheme.startsWith("policy")) {
                    policySchemes.add(scheme);
                }
            }
            
            log.info("found "+Integer.toString(policySchemes.size()) + " policy rules");
            
            cm.setSchemes(policySchemes);
            cm.setType(MessageType.SCHEMES);
            
        } catch (Exception e) {
            handleStandardError(e);
        } finally {
            return cm;
        }
    }
    
    

    public CarneadesMessage evaluateArgGraph(String argGraph, List<String> accepts, List<String> rejects) {
        
        CarneadesMessage cm = null;
        
        try {
                    
            // importing lkif
            log.info("loading lkif");
            Map lkif = (Map) RT.var(NS.LKIF, "lkif-import").invoke(argGraph);
            List argGraphs = (List) lkif.get(Keyword.intern("ags"));
            Map ag = (Map)argGraphs.get(0);
            Map<String, Statement> stmtIDs = LKIFHelper.getStmtIDs(argGraph);

            // evaluate
            List<Statement> accStmts = new ArrayList<Statement>();
            for(String a : accepts) {
                accStmts.add(stmtIDs.get(a));
            }
            List accSExpr = ClojureUtil.getSeqFromStatementList(accStmts); 
            Map accAG = (Map)RT.var(NS.ARGUMENT, "accept").invoke(ag, accSExpr);
            
            List<Statement> rejStmts = new ArrayList<Statement>();
            for(String r : rejects) {
                rejStmts.add(stmtIDs.get(r));
            }
            List rejSExpr = ClojureUtil.getSeqFromStatementList(rejStmts);
            Map evaluatedAG = (Map)RT.var(NS.ARGUMENT, "reject").invoke(accAG, rejSExpr);
                        

            // save & return evaluated graph
            String evaluatePath = storeArgGraph(evaluatedAG);
            cm.setAG(evaluatePath);            
            cm.setType(MessageType.GRAPH);
        
        } catch (Exception e) {
            handleStandardError(e);
        } finally { 
            log.info("sending Carneades Message back");
            return cm;
        }
    }
    
    

    public CarneadesMessage askEngine(Statement query, String kb, List<String> askables, List<Statement> answers2) {

        CarneadesMessage cm = null;

        try {
            
            // checking new and old answers
            if (answers2 != null) {
                log.info("number of new answers: " + Integer.toString(answers2.size()));
                this.answers.addAll(answers2);
            } else {
                log.info("no new answers");
            }
            log.info("number of total answers: " + Integer.toString(this.answers.size()));

            // creating new ask handler
            log.info("creating answers", this.answers);
            List<List> cljAnswers = ClojureUtil.getSeqFromStatementList(this.answers);
            log.info("creating answer function");
            AskHandler askHandler = new AskHandler(cljAnswers);

            // start or continue engine
            if (state == null) {
                cm = startEngine(query, kb, askables, askHandler);
            } else {
                cm = continueEngine(askHandler);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            log.info("sending Carneades Message back");
            return cm;
        }
        
    }
    
    private CarneadesMessage startEngine(Statement query, String kb, List<String> askables, AskHandler askHandler) {
        
        CarneadesMessage cm = null;

        log.info("starting engine with kb: "+kb);
        log.info("query: " + query.toString());
        this.query = query;
        
        try {
            // importing lkif
            log.info("loading lkif");
            Map lkif = (Map) RT.var(NS.LKIF, "lkif-import").invoke(kb);

            // getting goal
            List argGraphs = (List) lkif.get(Keyword.intern("ags"));
            log.info("creating goal");
            // TODO : the goal has to be general
            this.goal = ClojureUtil.getSeqFromStatement(query);//(List) RT.var("clojure.core", "list").invoke(Symbol.intern("p"), Symbol.intern("?x"));
                        
            // creating promises
            toEngine = (IFn)RT.var(NS.CORE, "promise").invoke();
            fromEngine = (IFn)RT.var(NS.CORE, "promise").invoke();
            
            // creating generators
            log.info("creating lkif generator");
            IFn lkifGen = (IFn) RT.var(NS.LKIF, "generate-arguments-from-lkif").invoke(lkif);            
            log.info("generators as list");            
            List generators = (List) RT.var(NS.CORE, "list").invoke(lkifGen);
            
            // askable function
            log.info("creating askable? function");
            Askable askableFn = new Askable(askables);
                        
            // start engine
            log.info("starting engine for the first time");
            Map ag;
            if (argGraphs == null) {
                ag = (Map) RT.var(NS.ARGUMENT, "*empty-argument-graph*").invoke();
            } else {
                ag = (Map) argGraphs.get(0);
            }
            //RT.var(NS.CORE, "doall").invoke(
            RT.var(NS.SHELL, "future-construction").invoke(toEngine, fromEngine);
            
            // sending first request
            Object msg = RT.var(NS.CORE, "list").invoke(this.goal, 50, ag, generators, askableFn);
            cm = communicateWithEngine(msg, askHandler);
            
   
        } catch (Exception e) {
            e.printStackTrace();
            handleStandardError(e);
        } finally {            
            return cm;            
        }
    }
    
    private CarneadesMessage continueEngine(AskHandler askHandler) {
        
        CarneadesMessage cm = null;
        
        try {
        
            // sending further request
            log.info("continue with engine");           
            Object msg = askHandler.getAnswer(this.lastQuestion, this.state);
            cm = communicateWithEngine(msg, askHandler);
            
             
        } catch (Exception e) {
            handleStandardError(e);
        } finally {        
            return cm;
        }
        
    }
    
    private CarneadesMessage communicateWithEngine(Object msg, AskHandler askHandler) {
        
        CarneadesMessage cm = null;
        
        try {
            
            // sending message
            log.info("sending request to engine", msg);
            RT.var(NS.CORE, "deliver").invoke(this.toEngine, msg);
            
            // read response
            // TODO : may block here !
            log.info("waiting for answer from engine");
            List o = (List)RT.var(NS.CORE, "deref").invoke(this.fromEngine);            
            log.info("answer received from engine: " + Integer.toString(o.size()));
            /*
            Map<Thread,StackTraceElement[]> traces = Thread.getAllStackTraces();
            Set<Thread> threads = traces.keySet();
            Iterator<Thread> it = threads.iterator();
            while(it.hasNext()) {
                Thread t = it.next();
                String tName = t.getName();
                if(tName.startsWith("http") || tName.startsWith("pool")) {
                    String m = "Thread " + t.toString() + " " + tName + " " + Long.toString(t.getId());
                    log.info(m);
                    StackTraceElement[] stack = traces.get(t);
                    for(int i=0; i<stack.length; i++) {
                        System.out.println(stack[i].toString());
                    }
                }
            }
            System.out.println(Thread.currentThread().getName()); */
            // log.info((String)RT.var(NS.CORE,"pr-str").invoke(o));
            // RT.var(NS.CORE, "println").invoke(o);
            
            // check for solution or ask
            boolean isSolution = (Boolean)RT.var(NS.CORE, "=").invoke(Symbol.intern("solution"), o.get(0));
            boolean isAsk = (Boolean)RT.var(NS.CORE, "=").invoke(Symbol.intern("ask"), o.get(0));
            
            log.info("is solution? : "+Boolean.toString(isSolution));
            log.info("is ask? : "+Boolean.toString(isAsk));
            
            if(isSolution) {
                
                // solution found                
                List solutions = (List)o.get(1);
                int solNr = (Integer)RT.var(NS.CORE, "count").invoke(solutions);
                log.info("solution: " + Integer.toString(solNr) + " - "+solutions.getClass().getName() );
                // TODO : handle all solutions
                // get last solution for substitution
                Map lastSol = (Map)solutions.get(solutions.size()-1);
                Map lastSubs = (Map)lastSol.get(Keyword.intern("substitutions"));
                List lastSolStmt = (List) RT.var(NS.UNIFY, "apply-substitution").invoke(lastSubs,this.goal);
                log.info("uniting solutions");
                Map solAG = (Map) RT.var(NS.CORE,"doall").invoke(RT.var(NS.SHELL, "unite-solutions").invoke(solutions));
                solAG = (Map)RT.var(NS.CORE, "assoc").invoke(solAG, Keyword.intern("main-issue"), lastSolStmt);
                log.info("serializing argument graph");                
                //PrintWriter jsonWriter = new PrintWriter(new StringWriter());
                //String jsonString = "";
                //RT.var(NS.JSON, "write-json") .invoke(solAG, jsonWriter);
                // {:ags (solAG)}
                //Map lkifMap = (Map)RT.map(Keyword.intern("ags"),RT.var(NS.CORE, "list").invoke(solAG));
                //RT.var(NS.LKIF,"lkif-export").invoke(lkifMap, lkifWriter);
                //RT.var(NS.CORE, "println").invoke(lkifMap);
                //jsonString = jsonWriter.toString();
                //log.info(lkifString);
                String agPath = storeArgGraph(solAG);
                //String ag = (String)RT.var(NS.JSON,"json-str").invoke(solAG);
                //log.info(jsonString);
                log.info(agPath);
                log.info("creating CarneadesMessage");
                cm = new CarneadesMessage();
                cm.setMessage(ClojureUtil.getStatementFromSeq(lastSolStmt));
                //cm.setAG(jsonString);
                cm.setAG(agPath);
                cm.setType(MessageType.SOLUTION);
                
            } else if (isAsk) {
                
                // question raised
                log.info("question from engine");
                this.lastQuestion = (List)o.get(1);
                this.state = (Map)o.get(2);
                this.toEngine = (IFn)o.get(3);
                this.fromEngine = (IFn)o.get(4);
                
                // checking if question has already been answered
                try {
                    // already answered
                    Object answer = askHandler.getAnswer(this.lastQuestion, this.state);
                    cm = communicateWithEngine(answer, askHandler);
                } catch(AskException e) {
                    // ask user
                    Statement subgoal = ClojureUtil.getStatementFromSeq(this.lastQuestion);                
                    cm = new CarneadesMessage();
                    cm.setMessage(subgoal);
                    cm.setAG(null);
                    cm.setType(MessageType.ASKUSER);
                }
                

            } else {
                log.info("unknown answer from engine");
            }

        
            
        } catch (Exception e) {
            e.printStackTrace();
            handleStandardError(e);
        } finally {        
            return cm;
        }
        
    }
    
    private void handleStandardError(Exception e) {
        log.error("Error during argumentation construction: " + e.getClass().getName() + " " + e.getCause().getMessage());
        e.printStackTrace();
    }
    
    private String storeArgGraph(Map argGraph) throws Exception{
        
        // TODO : replace with CMS
        
        log.info("storing argument graph");
        
        int c = 0;
        String prepath = "/tmp/";
        String path = prepath + "graph0.lkif";
        File f = new File(path);
        while(f.exists()) {
            c++;
            path = prepath + "graph" + Integer.toString(c) + ".lkif";            
            f = new File(path);
        }          
        
        storeArgGraph(argGraph, path);
        
        return path;
        
    }
    
    private void storeArgGraph(Map argGraph, String path) throws Exception{        
        log.info(path);                
        // {:ags (solAG)}
        Map lkifMap = (Map)RT.map(Keyword.intern("ags"),RT.var(NS.CORE, "list").invoke(argGraph));
        RT.var(NS.LKIF,"lkif-export").invoke(lkifMap, path);        
    }

}
