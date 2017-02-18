package webservice;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;


import com.fasterxml.jackson.core.JsonParser;
import gate.*;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import utils.DocUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

@RestController
public class GateController {
    private CorpusController biolarkPipeline;
    private Corpus corpus;
    private DocUtils docUtils;
    private JsonParser jsonParser;

    @PostConstruct
    public void init() throws GateException, IOException, URISyntaxException {

        //required for bioyodie
        System.setProperty("at.ofai.gate.modularpipelines.configFile","");
        //File gateHome = new File("GATE_Developer_8.1");
        //in case called by other contexts
        if(!Gate.isInitialised()) {
            //Gate.setGateHome(gateHome);
            Gate.runInSandbox(true);
            Gate.init();
        }

        biolarkPipeline = (CorpusController) PersistenceManager
                .loadObjectFromFile(new File("bio-yodie-D1.2.1/main-bio/main-bio.xgapp"));


        corpus = Factory.newCorpus("restCorpus");
        docUtils = new DocUtils();

    }

    //refactor this at some point to use the pool strategy
    @RequestMapping(value = "/bioyodie", method = RequestMethod.POST, headers = "Accept=*")
    @ResponseBody
    public synchronized String process(HttpServletResponse response, @RequestBody String text)
            throws ResourceInstantiationException, ExecutionException, IOException {
        biolarkPipeline.setCorpus(corpus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Document doc = Factory.newDocument(text);
        corpus.add(doc);
        biolarkPipeline.execute();
        String processedJsonString = docUtils.convertDocToJSON(corpus.get(0)).toString();
        Factory.deleteResource(corpus.get(0));
        return processedJsonString;
    }



}
