package webservice;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;

import gate.*;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import utils.DocUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

@RestController
public class GateController {
    private DocUtils docUtils;
    private LinkedBlockingQueue<CorpusController> gateQueue;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GateController.class);

    @Value("${gateApp}")
    private String gateApp;

    @Value("${poolSize}")
    private int poolSize;

    @Value("${gateHome}")
    private String gateHome;

    @PostConstruct
    public void init() throws GateException, IOException, URISyntaxException {

        //required for bioyodie
        System.setProperty("at.ofai.gate.modularpipelines.configFile","");
        //File gateHome = new File("GATE_Developer_8.1");
        //in case called by other contexts

        Gate.setGateHome(new File(gateHome));
        LOG.info("GATE home is " +gateHome);
        Gate.init();


        gateQueue = new LinkedBlockingQueue<>(poolSize);

        LOG.info("Populating CorpusController queue with " +gateApp);
        for (int i=0;i<poolSize;i++){

            CorpusController pipeline = (CorpusController) PersistenceManager
                    .loadObjectFromFile(new File(gateApp));
            Corpus corpus = Factory.newCorpus("restCorpus");
            pipeline.setCorpus(corpus);
            gateQueue.add(pipeline);
        }
        LOG.info("CorpusController queue is populated. Size is " +gateQueue.size());
        docUtils = new DocUtils();



    }

    //refactor this at some point to use the pool strategy
    @RequestMapping(value = "/bioyodie", method = RequestMethod.POST, headers = "Accept=*")
    @ResponseBody
    public  String process(HttpServletResponse response, @RequestBody String text)
            throws ResourceInstantiationException, ExecutionException, IOException {
        CorpusController pipeline = null;
        LOG.debug("About to process following text: " +text);
        try {
            pipeline = gateQueue.take();
            pipeline.getCorpus().clear();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            Document doc = Factory.newDocument(text);
            pipeline.getCorpus().add(doc);
            pipeline.execute();
            String processedJsonString = docUtils.convertDocToJSON(pipeline.getCorpus().get(0)).toString();
            Factory.deleteResource(pipeline.getCorpus().get(0));
            gateQueue.add(pipeline);
            LOG.debug("processing complete");
            return processedJsonString;
        } catch (InterruptedException e) {
            LOG.info("processing interrupted. Most likely application is being shut down.");
        }
        return null;
    }



}
