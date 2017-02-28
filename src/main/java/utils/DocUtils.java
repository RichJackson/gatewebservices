package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import gate.Annotation;
import gate.Factory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * Created by rich on 17/02/17.
 */
@Service
public class DocUtils {

    @Value("${annotationSet:#{null}}")
    private String annotationSet;


    @Value("${annotationTypes:#{null}}")
    private String annotationTypes;

    private Set<String> annotTypesSet;

    @PostConstruct
    private void init(){
        if(annotTypesSet != null) annotTypesSet = new HashSet<>(Arrays.asList(annotationTypes.split(",")));
        if (annotationSet == null) annotationSet = "";
    }

    public String convertDocToJSON(gate.Document doc) throws IOException {
        Map<String, Collection<Annotation>> gateMap = new HashMap<>();


        if(annotTypesSet ==null){
            doc.getAnnotations(annotationSet).forEach((k)->{
                gateMap.put(k.getType(),doc.getAnnotations(annotationSet).get(k.getType()));
            });

        }else{
            annotTypesSet.forEach((k) ->{
                Object annot = doc.getAnnotations(annotationSet).get(k);
                        doc.getAnnotations(annotationSet).get(k).forEach((y)->{
                            gateMap.put(y.getType(),doc.getAnnotations(annotationSet).get(y.getType()));
                        });
            });
        }
        String result = gate.corpora.DocumentJsonUtils.toJson(doc, gateMap);


        return result;
    }

}
